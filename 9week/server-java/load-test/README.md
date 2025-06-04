# E-commerce 부하 테스트 & 장애 대응

STEP 19, 20 과제를 위한 종합적인 부하 테스트 및 장애 대응 시스템입니다.

## 📁 프로젝트 구조

```
load-test/
├── k6-scripts/
│   └── ecommerce-load-test.js     # K6 부하 테스트 스크립트
├── docker-optimization/
│   └── docker-perf-test.sh        # Docker 리소스 최적화 테스트
├── docs/
├── reports/                       # 테스트 결과 저장 디렉토리
├── run-tests.sh                   # 통합 테스트 실행 스크립트
└── README.md                      # 이 파일
```

## 🚀 빠른 시작

### 1. 사전 요구사항

- **K6**: 부하 테스트 도구
- **jq**: JSON 처리 도구
- **Docker** (선택사항): Docker 최적화 테스트용

### 2. 애플리케이션 실행

```bash
# Spring Boot 애플리케이션 실행
./gradlew bootRun

# 또는 Docker로 실행
docker-compose up -d
```

### 3. 부하 테스트 실행

#### 기본 실행 (모든 시나리오)
```bash
# Linux/macOS
./load-test/run-tests.sh --all --report

# Windows
bash load-test/run-tests.sh --all --report
```

#### 특정 시나리오만 실행
```bash
./load-test/run-tests.sh --scenario baseline
```

## 📊 테스트 시나리오

1. **Baseline Test**: 정상 상태 성능 측정 (50 vUsers, 5분)
2. **Peak Load Test**: 최대 부하 성능 검증 (200 vUsers, 10분)
3. **Spike Test**: 급증 부하 대응 능력 (500 vUsers, 3분)
4. **Stress Test**: 시스템 한계점 탐색 (1000 vUsers, 15분)

## 🎯 성능 목표 지표

| API | P95 목표 | 허용 오류율 |
|-----|----------|-------------|
| 상품 조회 | < 500ms | < 1% |
| 주문 처리 | < 2000ms | < 3% |
| 포인트 충전 | < 1000ms | < 2% |

## 📈 결과 분석

테스트 결과는 `load-test/reports/` 디렉토리에 저장됩니다:
- JSON 상세 결과
- Markdown 요약 보고서
- 시스템 정보

## 🚨 장애 대응 프로세스

### 즉시 대응 (0-15분)
1. 장애 탐지 및 평가
2. 대응팀 소집
3. 긴급 조치 실행

### 중기 대응 (15분-1시간)
1. 근본 원인 분석
2. 임시 조치 적용
3. 상황 커뮤니케이션

### 장기 대응 (1시간 이상)
1. 완전 복구
2. Post-mortem 분석
3. 예방 대책 수립

## 📋 체크리스트

### 테스트 실행 전
- [ ] 애플리케이션 정상 실행 확인
- [ ] 데이터베이스 연결 확인
- [ ] 충분한 디스크 공간 확보

### 테스트 완료 후
- [ ] 성능 목표 달성 여부 확인
- [ ] 병목 지점 분석
- [ ] 개선 방안 도출

## 🐛 문제 해결

### 자주 발생하는 문제
- **연결 오류**: 애플리케이션 실행 상태 확인
- **높은 오류율**: 서버 리소스 및 DB 설정 확인
- **느린 응답**: 캐시 및 쿼리 성능 확인

## 📚 참고 자료

- [K6 Documentation](https://k6.io/docs/)
- [Load Testing Best Practices](https://k6.io/docs/testing-guides/load-testing-best-practices/)
- [SRE Workbook](https://sre.google/workbook/)

---

**참고**: 자세한 내용은 STEP 19, 20 보고서를 참조하세요.
