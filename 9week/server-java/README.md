## 프로젝트

이커머스 플랫폼의 백엔드 서비스입니다. 상품 조회, 주문 처리, 포인트 관리, 쿠폰 시스템 등의 핵심 기능을 제공합니다.

## 주요 기능

- **상품 관리**: 상품 조회, 인기 상품 목록
- **주문 처리**: 주문 생성, 결제 처리
- **포인트 시스템**: 포인트 충전, 조회, 사용
- **쿠폰 시스템**: 쿠폰 발급, 조회, 사용
- **부하 테스트**: K6 기반 성능 테스트 시스템

## Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d
```

### 애플리케이션 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 IDE에서 ServerApplication.java 실행
```

## 🚀 부하 테스트 (STEP 19 & 20)

### 빠른 테스트 실행

```bash
# 모든 시나리오 실행
bash load-test/run-tests.sh --all --report

# 특정 시나리오만 실행
bash load-test/run-tests.sh --scenario baseline

# Docker 최적화 테스트 포함
bash load-test/run-tests.sh --all --docker --report
```

### 간단한 테스트

```bash
# 30초 빠른 테스트
k6 run --env BASE_URL=http://localhost:8080 load-test/examples/quick-test.js
```

### 상세 가이드

- **부하 테스트 가이드**: [load-test/README.md](./load-test/README.md)
- **STEP 19 보고서**: 부하 테스트 계획 및 실행
- **STEP 20 보고서**: 장애 대응 및 성능 분석

## API 엔드포인트

| 기능 | Method | Endpoint | 설명 |
|------|--------|----------|------|
| 상품 조회 | GET | `/api/products/{id}` | 특정 상품 상세 정보 |
| 인기 상품 | GET | `/api/products/top` | 인기 상품 목록 |
| 주문 생성 | POST | `/api/orders/{userId}` | 주문 처리 |
| 포인트 조회 | GET | `/api/point/{userId}` | 사용자 포인트 조회 |
| 포인트 충전 | POST | `/api/point/charge/{userId}` | 포인트 충전 |
| 쿠폰 조회 | GET | `/api/coupons/{userId}` | 사용자 쿠폰 목록 |
| 쿠폰 발급 | POST | `/api/coupons/issue/{userId}` | 쿠폰 발급 |