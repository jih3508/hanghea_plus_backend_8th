# Redis 기반 랭킹 시스템 및 비동기 쿠폰 발급 기능 설계 보고서

## 1. 프로젝트 배경 및 목표

### 1.1 배경
이커머스 시스템에서 사용자 경험 향상과 서비스 효율성을 개선하기 위해 두 가지 주요 기능을 Redis 기반으로 구현하게 되었습니다:
1. **인기 상품 랭킹 시스템**: 사용자에게 인기 있는 상품을 추천하여 구매 의사결정을 도와줄 수 있는 기능
2. **선착순 쿠폰 발급 시스템**: 한정된 수량의 쿠폰을 효율적으로 관리하고 대규모 트래픽에도 안정적으로 운영할 수 있는 기능

### 1.2 목표
- Redis SortedSet을 활용한 실시간 상품 랭킹 시스템 구현
- Redis를 활용한 비동기 쿠폰 발급 시스템 설계 및 구현
- 기존 RDBMS 기반 로직의 Redis 기반 마이그레이션
- Redis 특성을 활용한 고가용성, 고성능 시스템 구현

## 2. Redis 자료구조 선택 이유

### 2.1 랭킹 시스템 - SortedSet
- **선택 이유**: SortedSet은 각 요소에 score를 부여하고 이를 기준으로 정렬된 상태를 유지하는 자료구조로, 상품 ID를 member로, 판매량을 score로 설정하여 효율적인 랭킹 관리가 가능합니다.
- **장점**:
  - O(log(N)) 시간 복잡도로 요소 추가/삭제 가능
  - 범위 기반 조회(ZRANGE, ZREVRANGE)가 O(log(N)+M) 시간 복잡도로 효율적
  - 이미 정렬된 상태로 저장되어 별도 정렬 작업 불필요
  - ZUNIONSTORE를 통해 여러 날짜의 데이터를 효율적으로 합산 가능

### 2.2 쿠폰 발급 시스템 - String
- **선택 이유**: 쿠폰의 남은 수량을 관리하는 데 단순하면서도 효율적인 String 자료구조를 활용하여 atomic 연산(INCR, DECR) 지원
- **장점**:
  - DECR, INCR 명령을 통해 원자적(atomic) 연산 보장
  - O(1) 시간 복잡도로 빠른 연산 가능
  - 여러 서버에서도 일관된 상태 유지 가능
  - 구현이 단순

## 3. 시스템 설계

### 3.1 랭킹 시스템 설계

#### 3.1.1 주요 컴포넌트
- **ProductRankRedisRepository**: Redis SortedSet을 직접 조작하는 레포지토리
- **ProductRankRedisService**: 비즈니스 로직을 담당하는 서비스 레이어
- **OrderFacadeRedis**: 주문 처리와 함께 랭킹 정보 업데이트 로직 포함
- **ProductRankController**: API 엔드포인트 제공

#### 3.1.2 키 설계
- **형식**: `product:rank:yyyyMMdd`
- **예시**: `product:rank:20250515`
- **장점**: 
  - 날짜별로 데이터 분리 가능
  - 필요에 따라 특정 날짜 또는 기간의 랭킹 조회 용이
  - TTL 설정으로 데이터 자동 정리 가능

#### 3.1.3 데이터 흐름
1. 상품 주문 발생 시 SortedSet에 상품ID와 주문수량(score) 기록
2. 오늘, 내일, 모레 날짜키에 동일 데이터 기록 (3일치 데이터 관리)
3. 랭킹 조회 시 어제 날짜 기준 상위 5개 상품 조회

### 3.2 쿠폰 발급 시스템 설계

#### 3.2.1 키 설계
- **형식**: `coupon:quantity:{couponId}`
- **예시**: `coupon:quantity:123`
- **값**: 남은 쿠폰 수량 (정수)

#### 3.2.2 데이터 흐름
1. 쿠폰 발급 시도: DECR 명령으로 원자적으로 수량 감소
2. 수량이 0 이하가 되면 발급 거부
3. 발급 성공 시 DB에 쿠폰 발급 정보 기록
4. DB 트랜잭션 실패 시 INCR 명령으로 수량 롤백

## 4. 구현 상세

### 4.1 랭킹 시스템 구현

![랭킹 시스템](./img/랭킹%20시스템.png)

#### 4.1.1 키 관리 및 데이터 저장
```java
// ProductRankRedisRepository.java
public void save(CreateOrderProductHistory create) {

        String key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + today;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + tomorrow;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

        key = RedisKeysPrefix.PRODUCT_RANK_KEY_PREFIX + dayAfterTomorrowStr;
        redisTemplate.opsForZSet().incrementScore(key, create.getProductId(), create.getQuantity());

    }

```

#### 4.1.2 상위 상품 조회
```java
// ProductRankRedisRepository.java
public Set<ZSetOperations.TypedTuple<Long>> getTopProducts() {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 5);
    }

```
### 4.2 쿠폰 발급 시스템 구현

#### 4.2.1 쿠폰 발급 (수량 감소)
```java
// CouponRedisRepository.java
    public Boolean decreaseCoupon(long couponId) {
        String key = RedisKeysPrefix.COUPON_KEY_PREFIX + couponId;
        Long value = redisTemplate.opsForValue().decrement(key);

        // 체크후 0개 미만이면 발급할수 없는 쿠폰이다.
        if (value != null && value >= 0) {
            return true;
        }

        return false;
    }
```


#### 4.2.2 쿠폰 발급 롤백 (수량 복구)
```java
// CouponRedisRepositoryImpl.java
    public void increaseCoupon(long couponId) {
        String key = RedisKeysPrefix.COUPON_KEY_PREFIX + couponId;
        redisTemplate.opsForValue().increment(key);
    }
```

#### 4.2.5 트랜잭션 처리
```java
    // CouponFacade.java
    @Transactional
    public void issue(CouponIssueCommand command) {

        // 쿠폰 개수 있지는지 먼저 조회
        service.checkCouponCounter(command.getCouponId());

        try {

            DomainUser user = userService.findById(command.getUserId());
            userCouponService.issue(user.getId(), command.getCouponId());

        }catch (RuntimeException e) {

            // 중간에 실패 하면 다시 원복 시킴
            service.resetCouponCounter(command.getCouponId());
        }

    }
```
## 5. Redis 기반 구현시 장단점

### 5.1Redis 기반 랭킹 시스템의 장점

1. **높은 성능과 효율성**
   - SortedSet 자료구조를 활용하여 O(log(N)) 시간 복잡도로 상품 판매량 업데이트 가능
   - 이미 정렬된 상태로 데이터가 유지되어 별도 정렬 작업 불필요
   - 범위 기반 조회(상위 5개 등)가 O(log(N)+M) 시간 복잡도로 매우 빠름

2. **실시간 데이터 처리**
   - 주문 발생 즉시 랭킹에 반영되어 실시간성 보장
   - 사용자에게 항상 최신 인기 상품 정보 제공 가능

3. **유연한 데이터 관리**
   - 날짜별 키 관리로 특정 기간의 랭킹 데이터 쉽게 조회 가능
   - 오늘/내일/모레 3일치 데이터를 관리하여 시간대별 분석 용이
   - TTL 설정으로 오래된 데이터 자동 정리 가능

4. **ZUNIONSTORE 기능**
   - 여러 날짜의 데이터를 효율적으로 합산하여 기간별 랭킹 생성 가능

### 5.2 Redis 기반 랭킹 시스템의 단점

1. **데이터 지속성 이슈**
   - Redis 서버 장애 시 메모리 기반 데이터 유실 가능성 존재
   - RDB나 AOF 설정 없이는 복구 어려움

2. **메모리 제한**
   - 모든 데이터가 메모리에 저장되어 대용량 데이터 처리 시 비용 증가
   - 상품 수가 많아질수록 메모리 사용량 급증 가능성

3. **복잡한 조건 쿼리의 한계**
   - 단순 순위 외 다양한 조건(카테고리별, 지역별 등)의 랭킹 조회 시 추가 작업 필요
   - RDBMS처럼 복잡한 조인이나 그룹핑 쿼리 처리 어려움

4. **데이터 동기화 이슈**
   - 분산 환경에서 여러 Redis 인스턴스 간 데이터 동기화 관리 필요

### 5.3 Redis 기반 쿠폰 발급 시스템의 장점

1. **원자적 연산으로 동시성 보장**
   - DECR/INCR 명령을 통해 동시 요청 시에도 정확한 수량 관리 가능
   - 별도의 락(Lock) 메커니즘 없이도 동시성 문제 해결

2. **고성능 처리**
   - O(1) 시간 복잡도로 빠른 연산 가능
   - 대규모 트래픽에도 안정적인 처리 가능

3. **간결한 구현**
   - 코드가 단순하고 이해하기 쉬워 유지보수 용이
   - 비즈니스 로직과 쿠폰 카운터 로직의 분리로 코드 가독성 향상

4. **트랜잭션 실패 대응**
   - DB 트랜잭션 실패 시 카운터 롤백 메커니즘 구현으로 일관성 유지

### 5.4  Redis 기반 쿠폰 발급 시스템의 단점

1. **최종 일관성 모델**
   - DB 반영 전 Redis 값 감소 후 실패 시 일시적 불일치 가능성
   - 롤백 로직 실행 전 시스템 장애 발생 시 데이터 불일치 발생 가능

2. **복구 시나리오의 한계**
   - Redis 서버 장애 시 쿠폰 수량 정보 유실 가능성
   - DB와 Redis 간 동기화 메커니즘 부재

3. **확장성 이슈**
   - 쿠폰 종류가 매우 많아질 경우 키 관리 복잡도 증가
   - 단일 Redis 인스턴스 사용 시 확장성 제한

## 6. 시스템 평가 및 회고

### 6.1 달성한 목표
- Redis SortedSet을 활용한 효율적인 실시간 상품 랭킹 시스템 구현
- Redis String을 활용한 동시성 이슈 없는 쿠폰 발급 시스템 구현
- 기존 RDBMS 기반 로직의 Redis 기반 마이그레이션 완료
- 캐싱 전략을 통한 시스템 성능 향상

### 6.2 개선 사항 및 한계점

#### 6.2.1 개선 가능한 부분
- **데이터 지속성**: Redis 장애 시 데이터 유실 가능성에 대비한 전략 필요
- **이벤트 소싱**: 쿠폰 발급 이벤트를 메시지 큐(Kafka 등)를 통해 비동기 처리하는 방식 고려
- **복제 및 샤딩**: 대규모 트래픽 대응을 위한 Redis 클러스터 구성 필요
- **모니터링**: Redis 성능 및 메모리 사용량 모니터링 체계 구축

#### 6.2.2 한계점
- **최종 일관성**: 비동기 처리로 인한 일시적 데이터 불일치 가능성 존재
- **복잡한 쿼리**: 다양한 조건의 랭킹 조회 시 Redis만으로는 한계 존재
- **메모리 사용량**: 데이터 증가에 따른 메모리 사용량 증가 이슈

### 6.3 배운 점 및 적용 가능한 영역

#### 6.3.1 배운 점
- Redis 자료구조별 특성과 활용 방법
- 분산 환경에서의 원자적 연산의 중요성
- 캐싱 전략이 시스템 성능에 미치는 영향
- 트랜잭션 실패 시 롤백 메커니즘의 중요성

#### 6.3.2 적용 가능한 다른 영역
- **실시간 대시보드**: 실시간 통계 및 모니터링 시스템
- **세션 관리**: 사용자 세션 및 인증 정보 관리
- **실시간 알림 시스템**: 이벤트 발생 시 실시간 알림 처리
- **API Rate Limiting**: 사용자별 API 호출 제한 관리

### 6.4 추가로 기능 구현 해야 할것
1. 쿠폰
    - 쿠폰 생성 API
    - 쿠폰 업데이트 API
    - 쿠폰 생성과 업데이트 할때 레디스에 개수 추가 구현
2. 상품
    - 상품 추가 API
    - 상품 업데이트 API
    - 재고 입고 API

## 7. 결론

Redis를 활용한 랭킹 시스템과 선착순 쿠폰 발급 기능 개발을 통해 이커머스 시스템의 사용자 경험과 성능을 크게 향상시킬 수 있었습니다. Redis의 SortedSet과 String 자료구조를 활용함으로써 대규모 트래픽 환경에서도 안정적으로 동작하는 시스템을 구현했습니다.

특히 Redis의 원자적 연산을 활용한 쿠폰 발급 시스템은 동시성 이슈 없이 정확한 쿠폰 수량 관리를 가능하게 하였으며, SortedSet을 활용한 랭킹 시스템은 효율적인 데이터 관리와 조회를 제공했습니다.

향후 이러한 Redis 기반 패턴을 다양한 영역에 확장 적용하여 시스템 전반의 성능과 사용자 경험을 지속적으로 개선해 나갈 예정입니다.