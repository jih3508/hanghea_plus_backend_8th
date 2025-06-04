#!/bin/bash

# 부하 테스트 실행 스크립트
# STEP 19 & 20 과제용 통합 테스트 실행기

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 기본 설정
BASE_URL=${BASE_URL:-"http://localhost:8080"}
RESULTS_DIR="load-test/reports/$(date +%Y%m%d_%H%M%S)"
K6_SCRIPT="load-test/k6-scripts/ecommerce-load-test.js"

# 테스트 시나리오 정의
SCENARIOS=("baseline" "peak" "spike" "stress")

# 함수 정의
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 사용법 출력
show_usage() {
    cat << EOF
사용법: $0 [옵션]

옵션:
    -u, --url URL           테스트 대상 URL (기본값: http://localhost:8080)
    -s, --scenario NAME     특정 시나리오만 실행 (baseline|peak|spike|stress)
    -a, --all              모든 시나리오 실행 (기본값)
    -d, --docker           Docker 최적화 테스트 포함
    -r, --report           테스트 후 보고서 생성
    -h, --help             이 도움말 출력

예시:
    $0 --all --report                    # 모든 테스트 실행 후 보고서 생성
    $0 --scenario baseline               # baseline 시나리오만 실행
    $0 --url http://prod.example.com     # 프로덕션 환경 테스트
    $0 --docker --report                 # Docker 최적화 테스트 포함

EOF
}

# 사전 조건 확인
check_prerequisites() {
    log_info "사전 조건 확인 중..."
    
    # K6 설치 확인
    if ! command -v k6 &> /dev/null; then
        log_error "K6가 설치되지 않았습니다. 설치 후 다시 실행해주세요."
        echo "설치 방법: https://k6.io/docs/getting-started/installation/"
        exit 1
    fi
    
    # 스크립트 파일 존재 확인
    if [[ ! -f "$K6_SCRIPT" ]]; then
        log_error "K6 스크립트 파일을 찾을 수 없습니다: $K6_SCRIPT"
        exit 1
    fi
    
    # 서버 연결 확인
    log_info "서버 연결 확인: $BASE_URL"
    if ! curl -sf "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        log_warning "서버에 연결할 수 없습니다. 계속 진행하시겠습니까? (y/N)"
        read -r response
        if [[ ! "$response" =~ ^[Yy]$ ]]; then
            log_info "테스트를 취소합니다."
            exit 0
        fi
    fi
    
    log_success "사전 조건 확인 완료"
}

# 결과 디렉토리 생성
setup_results_dir() {
    mkdir -p "$RESULTS_DIR"
    log_info "결과 저장 디렉토리: $RESULTS_DIR"
    
    # 테스트 설정 정보 저장
    cat > "$RESULTS_DIR/test-config.json" << EOF
{
    "timestamp": "$(date -Iseconds)",
    "base_url": "$BASE_URL",
    "k6_script": "$K6_SCRIPT",
    "scenarios": $(printf '%s\n' "${SCENARIOS[@]}" | jq -R . | jq -s .),
    "environment": {
        "k6_version": "$(k6 version 2>/dev/null | head -1 || echo 'unknown')",
        "os": "$(uname -s)",
        "arch": "$(uname -m)"
    }
}
EOF
}

# 시스템 정보 수집
collect_system_info() {
    log_info "시스템 정보 수집 중..."
    
    cat > "$RESULTS_DIR/system-info.txt" << EOF
=== 시스템 정보 ===
OS: $(uname -a)
Date: $(date)
CPU Info: $(nproc) cores
Memory: $(free -h | grep '^Mem:' | awk '{print $2}')
Disk: $(df -h / | tail -1 | awk '{print $4}' | sed 's/G/ GB/')

=== Docker 정보 ===
$(docker --version 2>/dev/null || echo "Docker not available")
$(docker-compose --version 2>/dev/null || echo "Docker Compose not available")

=== K6 정보 ===
$(k6 version)

=== 네트워크 정보 ===
Target URL: $BASE_URL
$(curl -w "Connect: %{time_connect}s, Total: %{time_total}s\n" -o /dev/null -s "$BASE_URL" 2>/dev/null || echo "네트워크 연결 실패")
EOF
}

# 단일 시나리오 실행
run_scenario() {
    local scenario=$1
    local output_file="$RESULTS_DIR/${scenario}_results.json"
    local summary_file="$RESULTS_DIR/${scenario}_summary.json"
    
    log_info "시나리오 실행 중: $scenario"
    
    # K6 실행
    k6 run \
        --env BASE_URL="$BASE_URL" \
        --env SCENARIO="$scenario" \
        --out json="$output_file" \
        --summary-export="$summary_file" \
        "$K6_SCRIPT"
    
    local exit_code=$?
    
    if [[ $exit_code -eq 0 ]]; then
        log_success "$scenario 테스트 완료"
        
        # 주요 메트릭 추출
        if [[ -f "$summary_file" ]]; then
            local avg_duration=$(jq -r '.metrics.http_req_duration.values.avg // "N/A"' "$summary_file")
            local p95_duration=$(jq -r '.metrics.http_req_duration.values.p95 // "N/A"' "$summary_file")
            local error_rate=$(jq -r '.metrics.http_req_failed.values.rate // "N/A"' "$summary_file")
            
            log_info "  평균 응답시간: ${avg_duration}ms"
            log_info "  P95 응답시간: ${p95_duration}ms"  
            log_info "  오류율: ${error_rate}%"
        fi
    else
        log_warning "$scenario 테스트에서 문제가 발생했습니다 (exit code: $exit_code)"
    fi
    
    return $exit_code
}

# 모든 시나리오 실행
run_all_scenarios() {
    local failed_scenarios=()
    
    for scenario in "${SCENARIOS[@]}"; do
        echo "=================================================="
        if ! run_scenario "$scenario"; then
            failed_scenarios+=("$scenario")
        fi
        
        # 시나리오 간 대기 시간 (시스템 안정화)
        if [[ "$scenario" != "${SCENARIOS[-1]}" ]]; then
            log_info "다음 시나리오 준비를 위해 30초 대기합니다..."
            sleep 30
        fi
    done
    
    # 실패한 시나리오 요약
    if [[ ${#failed_scenarios[@]} -gt 0 ]]; then
        log_warning "실패한 시나리오: ${failed_scenarios[*]}"
        return 1
    else
        log_success "모든 시나리오가 성공적으로 완료되었습니다"
        return 0
    fi
}

# Docker 최적화 테스트 실행
run_docker_optimization() {
    local docker_script="load-test/docker-optimization/docker-perf-test.sh"
    
    if [[ -f "$docker_script" ]]; then
        log_info "Docker 최적화 테스트 실행 중..."
        bash "$docker_script"
        
        # Docker 테스트 결과를 메인 결과 디렉토리로 복사
        if [[ -d "load-test/reports/docker-optimization" ]]; then
            local latest_docker_dir=$(ls -t load-test/reports/docker-optimization/ | head -1)
            if [[ -n "$latest_docker_dir" ]]; then
                cp -r "load-test/reports/docker-optimization/$latest_docker_dir" "$RESULTS_DIR/docker-optimization"
                log_success "Docker 최적화 테스트 결과가 복사되었습니다"
            fi
        fi
    else
        log_warning "Docker 최적화 스크립트를 찾을 수 없습니다: $docker_script"
    fi
}

# 결과 분석 및 보고서 생성
generate_report() {
    log_info "테스트 결과 분석 및 보고서 생성 중..."
    
    # 간단한 텍스트 요약 보고서
    cat > "$RESULTS_DIR/summary.md" << EOF
# 부하 테스트 결과 요약

## 테스트 정보
- **테스트 일시**: $(date)
- **테스트 대상**: $BASE_URL
- **실행 시나리오**: ${SCENARIOS[*]}

## 주요 결과
EOF

    # 각 시나리오별 요약 추가
    for scenario in "${SCENARIOS[@]}"; do
        local summary_file="$RESULTS_DIR/${scenario}_summary.json"
        if [[ -f "$summary_file" ]]; then
            echo "### $scenario 시나리오" >> "$RESULTS_DIR/summary.md"
            echo "- 평균 응답시간: $(jq -r '.metrics.http_req_duration.values.avg // "N/A"' "$summary_file")ms" >> "$RESULTS_DIR/summary.md"
            echo "- P95 응답시간: $(jq -r '.metrics.http_req_duration.values.p95 // "N/A"' "$summary_file")ms" >> "$RESULTS_DIR/summary.md"
            echo "- 오류율: $(jq -r '.metrics.http_req_failed.values.rate // "N/A"' "$summary_file")%" >> "$RESULTS_DIR/summary.md"
            echo "" >> "$RESULTS_DIR/summary.md"
        fi
    done
    
    log_success "보고서가 생성되었습니다: $RESULTS_DIR/"
    log_info "  - 요약 보고서: $RESULTS_DIR/summary.md"
}

# 결과 정리
cleanup_and_summary() {
    log_info "테스트 결과 정리 중..."
    
    # 압축 파일 생성
    local archive_name="load-test-results-$(date +%Y%m%d_%H%M%S).tar.gz"
    tar -czf "$archive_name" -C "$(dirname "$RESULTS_DIR")" "$(basename "$RESULTS_DIR")"
    
    log_success "=== 테스트 완료 ==="
    log_info "결과 디렉토리: $RESULTS_DIR"
    log_info "압축 파일: $archive_name"
    
    echo ""
    echo "=== 다음 단계 ==="
    echo "1. 결과 분석: $RESULTS_DIR/summary.md 참조"
    echo "2. 상세 데이터: $RESULTS_DIR/*_results.json 파일들 확인"
    echo "3. 개선 방안: STEP 20 장애 대응 보고서 참조"
    echo ""
}

# 메인 함수
main() {
    local run_scenarios=()
    local include_docker=false
    local generate_report_flag=false
    
    # 명령행 인수 처리
    while [[ $# -gt 0 ]]; do
        case $1 in
            -u|--url)
                BASE_URL="$2"
                shift 2
                ;;
            -s|--scenario)
                run_scenarios=("$2")
                shift 2
                ;;
            -a|--all)
                run_scenarios=("${SCENARIOS[@]}")
                shift
                ;;
            -d|--docker)
                include_docker=true
                shift
                ;;
            -r|--report)
                generate_report_flag=true
                shift
                ;;
            -h|--help)
                show_usage
                exit 0
                ;;
            *)
                log_error "알 수 없는 옵션: $1"
                show_usage
                exit 1
                ;;
        esac
    done
    
    # 기본값 설정
    if [[ ${#run_scenarios[@]} -eq 0 ]]; then
        run_scenarios=("${SCENARIOS[@]}")
    fi
    
    # 시작 메시지
    log_info "=== E-commerce 부하 테스트 시작 ==="
    log_info "대상 URL: $BASE_URL"
    log_info "실행 시나리오: ${run_scenarios[*]}"
    
    # 사전 조건 확인
    check_prerequisites
    
    # 결과 디렉토리 설정
    setup_results_dir
    
    # 시스템 정보 수집
    collect_system_info
    
    # 테스트 실행
    SCENARIOS=("${run_scenarios[@]}")
    local test_success=true
    
    if ! run_all_scenarios; then
        test_success=false
    fi
    
    # Docker 최적화 테스트 (옵션)
    if [[ "$include_docker" == true ]]; then
        run_docker_optimization
    fi
    
    # 보고서 생성 (옵션)
    if [[ "$generate_report_flag" == true ]]; then
        generate_report
    fi
    
    # 결과 정리
    cleanup_and_summary
    
    # 최종 종료 코드
    if [[ "$test_success" == true ]]; then
        log_success "모든 테스트가 성공적으로 완료되었습니다!"
        exit 0
    else
        log_warning "일부 테스트에서 문제가 발생했습니다. 결과를 확인해주세요."
        exit 1
    fi
}

# 스크립트가 직접 실행된 경우에만 main 함수 호출
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
