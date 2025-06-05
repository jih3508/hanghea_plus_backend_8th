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
# 1. 테스트 데이터 설정 (MySQL + Redis)
bash load-test/data/setup-database.sh --with-redis

# 2. 애플리케이션 실행
./gradlew bootRun

# 또는 IDE에서 ServerApplication.java 실행
```
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