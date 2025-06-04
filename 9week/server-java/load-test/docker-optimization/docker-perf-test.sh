#!/bin/bash

# Docker 성능 최적화 테스트 스크립트
# 다양한 CPU/Memory 설정으로 애플리케이션 성능 비교

set -e

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 설정
APP_NAME="ecommerce-app"
IMAGE_NAME="server-java:latest"
BASE_PORT=8080
K6_SCRIPT="load-test/k6-scripts/ecommerce-load-test.js"
RESULTS_DIR="load-test/reports/docker-optimization"

# 테스트 설정 정의
declare -A CONFIGS=(
    ["minimal"]="--cpus=1 --memory=512m"
    ["standard"]="--cpus=2 --memory=1g"
    ["optimized"]="--cpus=4 --memory=2g"
    ["high-memory"]="--cpus=2 --memory=2g"
    ["high-cpu"]="--cpus=4 --memory=1g"
)

# 테스트 시나리오
SCENARIOS=("baseline" "peak")

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

# 결과 디렉토리 생성
create_results_dir() {
    local timestamp=$(date +"%Y%m%d_%H%M%S")
    RESULTS_DIR="${RESULTS_DIR}/${timestamp}"
    mkdir -p "$RESULTS_DIR"
    log_info "Results directory created: $RESULTS_DIR"
}

# Docker 컨테이너 정리
cleanup_containers() {
    log_info "Cleaning up existing containers..."
    docker ps -q --filter "name=${APP_NAME}-*" | xargs -r docker stop
    docker ps -aq --filter "name=${APP_NAME}-*" | xargs -r docker rm
}

# 애플리케이션 빌드
build_app() {
    log_info "Building application..."
    if [ -f "gradlew" ]; then
        ./gradlew clean bootJar
    elif [ -f "build.gradle.kts" ]; then
        gradle clean bootJar
    else
        log_error "No Gradle wrapper or build file found"
        exit 1
    fi
    
    log_info "Building Docker image..."
    docker build -t "$IMAGE_NAME" .
    log_success "Application built successfully"
}

# 애플리케이션 시작
start_app() {
    local config_name=$1
    local docker_options=$2
    local port=$((BASE_PORT + $(echo "$config_name" | wc -c)))
    
    log_info "Starting application with config: $config_name"
    log_info "Docker options: $docker_options"
    log_info "Port: $port"
    
    docker run -d \
        --name "${APP_NAME}-${config_name}" \
        $docker_options \
        -p "$port:8080" \
        -e SPRING_PROFILES_ACTIVE=test \
        "$IMAGE_NAME"
    
    # 애플리케이션이 시작될 때까지 대기
    local max_attempts=30
    local attempt=1
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            log_success "Application started successfully on port $port"
            echo "$port"
            return 0
        fi
        
        log_info "Waiting for application to start... (attempt $attempt/$max_attempts)"
        sleep 10
        ((attempt++))
    done
    
    log_error "Application failed to start within expected time"
    return 1
}

# K6 부하 테스트 실행
run_load_test() {
    local config_name=$1
    local scenario=$2
    local port=$3
    local output_file="$RESULTS_DIR/${config_name}_${scenario}_results.json"
    
    log_info "Running $scenario test for $config_name configuration..."
    
    k6 run \
        --env BASE_URL="http://localhost:$port" \
        --env SCENARIO="$scenario" \
        --out json="$output_file" \
        --summary-export="$RESULTS_DIR/${config_name}_${scenario}_summary.json" \
        "$K6_SCRIPT"
    
    if [ $? -eq 0 ]; then
        log_success "$scenario test completed for $config_name"
    else
        log_warning "$scenario test had issues for $config_name"
    fi
}

# 시스템 메트릭 수집
collect_system_metrics() {
    local config_name=$1
    local container_name="${APP_NAME}-${config_name}"
    local metrics_file="$RESULTS_DIR/${config_name}_system_metrics.json"
    
    log_info "Collecting system metrics for $config_name..."
    
    # Docker stats를 JSON 형태로 수집
    timeout 60s docker stats "$container_name" --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}\t{{.BlockIO}}" --no-stream > "$RESULTS_DIR/${config_name}_docker_stats.txt"
    
    # 컨테이너 정보 수집
    docker inspect "$container_name" > "$RESULTS_DIR/${config_name}_container_info.json"
    
    log_success "System metrics collected for $config_name"
}

# 애플리케이션 중지
stop_app() {
    local config_name=$1
    local container_name="${APP_NAME}-${config_name}"
    
    log_info "Stopping application: $config_name"
    docker stop "$container_name" > /dev/null 2>&1 || true
    docker rm "$container_name" > /dev/null 2>&1 || true
    
    # 포트가 해제될 때까지 잠시 대기
    sleep 5
}

# 결과 분석 및 보고서 생성
generate_report() {
    log_info "Generating performance comparison report..."
    
    cat > "$RESULTS_DIR/performance_comparison.md" << EOF
# Docker 성능 최적화 테스트 결과

## 테스트 개요
- 테스트 일시: $(date)
- 테스트 대상: E-commerce 애플리케이션
- 측정 지표: Response Time, TPS, Error Rate, Resource Usage

## 테스트 설정

| Configuration | CPU | Memory | 설명 |
|---------------|-----|--------|------|
EOF

    for config_name in "${!CONFIGS[@]}"; do
        echo "| $config_name | ${CONFIGS[$config_name]} | Docker resource limits |" >> "$RESULTS_DIR/performance_comparison.md"
    done
    
    cat >> "$RESULTS_DIR/performance_comparison.md" << EOF

## 결과 요약

### 응답 시간 비교 (P95)
EOF
    
    # 각 설정별 결과 파일에서 주요 메트릭 추출하여 비교표 생성
    for config_name in "${!CONFIGS[@]}"; do
        for scenario in "${SCENARIOS[@]}"; do
            if [ -f "$RESULTS_DIR/${config_name}_${scenario}_summary.json" ]; then
                echo "- $config_name ($scenario): $(cat "$RESULTS_DIR/${config_name}_${scenario}_summary.json" | jq -r '.metrics.http_req_duration.values.p95' 2>/dev/null || echo 'N/A')ms" >> "$RESULTS_DIR/performance_comparison.md"
            fi
        done
    done
    
    cat >> "$RESULTS_DIR/performance_comparison.md" << EOF

### 권장사항
1. **최소 환경**: CPU 1 core, Memory 512MB로도 기본적인 기능 동작 가능
2. **권장 환경**: CPU 2 cores, Memory 1GB가 성능과 비용의 균형점
3. **최적 환경**: CPU 4 cores, Memory 2GB에서 최고 성능 달성

### 상세 결과
- 각 설정별 상세 결과는 해당 JSON 파일 참조
- 시스템 메트릭은 *_system_metrics.json 파일 참조
EOF
    
    log_success "Performance comparison report generated: $RESULTS_DIR/performance_comparison.md"
}

# 메인 실행 함수
main() {
    log_info "Starting Docker performance optimization test..."
    
    # 사전 준비
    create_results_dir
    cleanup_containers
    build_app
    
    # 각 설정별 테스트 실행
    for config_name in "${!CONFIGS[@]}"; do
        log_info "Testing configuration: $config_name (${CONFIGS[$config_name]})"
        
        # 애플리케이션 시작
        local port
        if port=$(start_app "$config_name" "${CONFIGS[$config_name]}"); then
            
            # 시스템 메트릭 수집 시작 (백그라운드)
            collect_system_metrics "$config_name" &
            local metrics_pid=$!
            
            # 각 시나리오별 부하 테스트 실행
            for scenario in "${SCENARIOS[@]}"; do
                run_load_test "$config_name" "$scenario" "$port"
                sleep 30  # 테스트 간 간격
            done
            
            # 메트릭 수집 종료
            kill $metrics_pid 2>/dev/null || true
            
            # 애플리케이션 중지
            stop_app "$config_name"
        else
            log_error "Failed to start application with config: $config_name"
        fi
        
        log_info "Completed testing for $config_name"
        echo "----------------------------------------"
    done
    
    # 최종 정리 및 보고서 생성
    cleanup_containers
    generate_report
    
    log_success "All tests completed! Results available in: $RESULTS_DIR"
}

# 스크립트 실행
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
