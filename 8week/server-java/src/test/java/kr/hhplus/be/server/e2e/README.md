# E2E 테스트 구성

이 패키지는 항해99 프로젝트의 API 엔드포인트를 위한 E2E(End-to-End) 테스트를 포함하고 있습니다.

## 테스트 구조

1. `E2ETest.java`: 모든 E2E 테스트의 기본 클래스로, 공통 설정을 제공합니다.
2. `PointControllerE2ETest.java`: 포인트 API의 E2E 테스트
3. `ProductControllerE2ETest.java`: 상품 API의 E2E 테스트
4. `CouponControllerE2ETest.java`: 쿠폰 API의 E2E 테스트
5. `OrderControllerE2ETest.java`: 주문 API의 E2E 테스트
6. `E2ETestSuite.java`: 모든 E2E 테스트를 실행하기 위한 테스트 스위트

## 테스트 데이터

- 테스트 데이터는 `src/test/resources/sql/test-data.sql` 파일에 정의되어 있습니다.
- 각 테스트 메소드 실행 전에 데이터베이스가 초기화되고 테스트 데이터가 로드됩니다.

## 실행 방법

### 전체 E2E 테스트 실행

```bash
./gradlew test --tests "kr.hhplus.be.server.e2e.E2ETestSuite"
```

### 개별 컨트롤러 테스트 실행

```bash
# 포인트 컨트롤러 테스트
./gradlew test --tests "kr.hhplus.be.server.e2e.PointControllerE2ETest"

# 상품 컨트롤러 테스트
./gradlew test --tests "kr.hhplus.be.server.e2e.ProductControllerE2ETest"

# 쿠폰 컨트롤러 테스트
./gradlew test --tests "kr.hhplus.be.server.e2e.CouponControllerE2ETest"

# 주문 컨트롤러 테스트
./gradlew test --tests "kr.hhplus.be.server.interfaces.api.order.OrderControllerE2ETest"
```

## 주의사항

- E2E 테스트는 실제 데이터베이스와 연결되므로 `application-test.yml` 설정이 올바르게 구성되어 있어야 합니다.
- 테스트 실행 시 데이터베이스 테이블의 데이터가 초기화되므로, 실제 운영 환경의 데이터베이스에서 실행하지 않도록 주의하세요.
