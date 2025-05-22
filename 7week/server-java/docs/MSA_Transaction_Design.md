# 마이크로서비스 아키텍처 설계 문서

## 1. 도메인 분리 및 배포 단위 설계

현재 이커머스 서비스를 MSA로 전환하기 위한 도메인 분리 및 배포 단위를 다음과 같이 설계합니다.

### 1.1 도메인 서비스 분리

| 서비스명 | 주요 기능 | 포함 도메인 |
|---------|---------|------------|
| **주문 서비스** | 주문 생성 및 관리 | order |
| **상품 서비스** | 상품 및 재고 관리, 상품 랭킹 | product, product-stock, product-rank |
| **사용자 서비스** | 사용자 정보, 포인트, 쿠폰 관리 | user, point, coupon |
| **결제 서비스** | 결제 처리 및 이력 관리 | payment |
| **데이터 플랫폼 서비스** | 통계 및 데이터 분석 | data-platform |

### 1.2 서비스 분리 다이어그램

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│                 │    │                 │    │                 │
│  주문 서비스     │◄───►│  상품 서비스     │◄───►│  사용자 서비스   │
│  (Order)        │    │  (Product)      │    │  (User)         │
│                 │    │                 │    │                 │
└────────┬────────┘    └─────────────────┘    └────────┬────────┘
         │                                             │
         │                                             │
         ▼                                             ▼
┌─────────────────┐                         ┌─────────────────┐
│                 │                         │                 │
│  결제 서비스     │◄────────────────────────►│  데이터 플랫폼   │
│  (Payment)      │                         │  (Data Platform)│
│                 │                         │                 │
└─────────────────┘                         └─────────────────┘
```

## 2. 트랜잭션 처리의 한계와 문제점

### 2.1 모놀리식 아키텍처에서의 트랜잭션 처리

현재 모놀리식 아키텍처에서는 다음과 같은 트랜잭션 처리가 가능합니다:

```java
@Transactional
public void order(OrderCommand command) {
    // 회원 정보 조회
    userService.findById(command.getUserId());
    
    // 상품 정보 조회 및 재고 차감
    for (OrderItem item : command.getItems()) {
        DomainProduct product = productService.getProduct(item.getProductId());
        productStockService.delivering(product.getId(), item.getQuantity());
        
        // 쿠폰 사용 처리
        if (item.getCouponId() != null) {
            userCouponService.getUseCoupon(command.getUserId(), item.getProductId());
        }
        // ...
    }
    
    // 주문 생성
    DomainOrder order = service.create(createOrder);
    
    // 포인트 차감
    pointService.use(command.getUserId(), totalPrice);
    pointHistoryService.useHistory(command.getUserId(), totalPrice);
    
    // 외부 데이터 전송 (이벤트로 분리)
    orderEventHandler.publishOrderCreated(order);
}
```

### 2.2 MSA에서의 트랜잭션 처리 한계

MSA 환경에서는 다음과 같은 트랜잭션 처리 문제가 발생합니다:

1. **분산 트랜잭션 문제:**
   - 각 서비스가 독립적인 데이터베이스를 사용하므로 ACID 트랜잭션을 적용할 수 없음
   - 주문 프로세스가 여러 서비스에 걸친 데이터 일관성 보장이 어려움

2. **데이터 일관성 문제:**
   - 상품 재고 차감, 쿠폰 사용, 포인트 차감 등의 작업이 분산됨
   - 일부 서비스 실패 시 롤백 처리가 복잡함

3. **서비스 간 통신 실패:**
   - 네트워크 지연 또는 서비스 일시적 장애 발생 가능성
   - 장애 시 데이터 불일치 발생 가능성

## 3. 분산 트랜잭션 해결 방안

### 3.1 Saga 패턴 적용

Saga 패턴을 사용하여 마이크로서비스 간 트랜잭션을 조율합니다:

#### 3.1.1 Choreography-based Saga (이벤트 기반 사가)

각 서비스가 로컬 트랜잭션을 완료한 후 이벤트를 발행하고, 다음 서비스가 이 이벤트를 수신하여 트랜잭션을 진행하는 방식

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 주문 서비스  │───►│ 상품 서비스  │───►│ 사용자 서비스 │───►│ 결제 서비스  │
└─────┬───────┘    └─────┬───────┘    └─────┬───────┘    └─────┬───────┘
      │                  │                  │                  │
   주문생성이벤트      재고차감이벤트      쿠폰사용이벤트      결제완료이벤트
      │                  │                  │                  │
      ▼                  ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────────────────┐
│                           이벤트 버스/메시지 브로커                    │
└─────────────────────────────────────────────────────────────────────┘
```

#### 3.1.2 보상 트랜잭션 구현

트랜잭션 실패 시 이전 단계의 변경사항을 취소하는 보상 트랜잭션 구현:

예시) 결제 실패 시 보상 트랜잭션 흐름:
```
결제 실패 → 결제실패 이벤트 발행 → 사용자 서비스 쿠폰/포인트 반환 → 상품 서비스 재고 복구 → 주문 서비스 주문 취소
```

### 3.2 이벤트 소싱 패턴 적용

상태 변경을 이벤트로 저장하여 데이터 일관성 보장:

1. 모든 도메인 변경 사항을 이벤트로 저장
2. 현재 상태는 이벤트 스트림을 재생하여 결정
3. 이벤트 저장소(Event Store)를 통한 데이터 복구 가능성 확보

### 3.3 최종 일관성(Eventual Consistency) 도입

모든 서비스가 즉시 일관된 상태를 유지하지 않아도 되지만, 최종적으로는 일관된 상태에 도달하도록 설계:

1. 비동기 메시징을 통한 서비스 간 통신
2. 재시도 메커니즘 구현
3. 정기적인 데이터 동기화 프로세스

## 4. 구체적인 MSA 트랜잭션 구현 방안

### 4.1 주문 처리 흐름

```
┌────────────────────┐
│     클라이언트      │
└──────────┬─────────┘
           │ 1. 주문 요청
           ▼
┌────────────────────┐  2. 주문 생성    ┌────────────────────┐
│    주문 서비스      │─────────────────►│   이벤트 버스       │
└──────────┬─────────┘                 └──────────┬─────────┘
           │ 9. 주문 완료 응답                     │
           ▼                                      │
┌────────────────────┐                           │
│     클라이언트      │                           │
└────────────────────┘                           │
                                                 │
           ┌──────────────────────────────────────┘
           │
           │ 3. 주문 생성 이벤트
           │
           ├────────────────────┬─────────────────┐
           │                    │                 │
           ▼                    ▼                 ▼
┌────────────────────┐ ┌────────────────────┐ ┌────────────────────┐
│   상품 서비스       │ │   사용자 서비스     │ │   결제 서비스      │
│   (재고 처리)       │ │ (쿠폰/포인트 처리)  │ │                    │
└──────────┬─────────┘ └──────────┬─────────┘ └──────────┬─────────┘
           │                      │                       │
           │ 4. 재고 처리 이벤트   │ 5. 쿠폰/포인트 처리 이벤트 │ 7. 결제 완료 이벤트
           ▼                      ▼                       ▼
┌────────────────────────────────────────────────────────────────────┐
│                         이벤트 버스                                 │
└──────────────────────────────────┬─────────────────────────────────┘
                                   │ 8. 주문 완료 이벤트
                                   ▼
                        ┌────────────────────┐
                        │  데이터 플랫폼 서비스 │
                        └────────────────────┘
```

### 4.2 트랜잭션 처리 구현 세부 사항

#### 4.2.1 메시지 브로커 기반 통신

1. **Kafka 또는 RabbitMQ 활용:**
   - 내구성 있는 메시지 전달
   - 메시지 재전송 및 순서 보장
   - 장애 복구 기능

2. **메시지 포맷:**
   ```json
   {
     "eventId": "uuid-here",
     "eventType": "ORDER_CREATED",
     "timestamp": "2023-08-01T12:00:00Z",
     "data": {
       "orderId": "ORD12345",
       "userId": 1001,
       "items": [
         {"productId": 101, "quantity": 2, "price": 10000}
       ],
       "totalAmount": 20000
     },
     "source": "order-service"
   }
   ```

#### 4.2.2 데이터 동기화 및 일관성 유지

1. **Command Query Responsibility Segregation (CQRS):**
   - 쓰기 작업과 읽기 작업 분리
   - 각 서비스는 필요한 데이터 복제본 유지
   - 주기적인 데이터 동기화

2. **아웃박스 패턴(Outbox Pattern):**
   - 로컬 트랜잭션에 이벤트 저장
   - 별도 프로세스에서 이벤트 발행
   - 메시지 전송 보장

## 5. 구현 예시 코드

### 5.1 주문 서비스 - 주문 생성 및 이벤트 발행

```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderOutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public Order createOrder(CreateOrderCommand command) {
        // 주문 생성 로직
        Order order = new Order(command.getUserId(), command.getItems());
        orderRepository.save(order);
        
        // 아웃박스에 이벤트 저장
        OrderCreatedEvent event = new OrderCreatedEvent(order);
        outboxRepository.save(new OrderOutboxEvent(event));
        
        return order;
    }
}

// 별도 스케줄러에서 실행
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final OrderOutboxRepository outboxRepository;
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    @Scheduled(fixedRate = 1000)
    public void publishEvents() {
        List<OrderOutboxEvent> pendingEvents = outboxRepository.findPendingEvents();
        
        for (OrderOutboxEvent event : pendingEvents) {
            try {
                kafkaTemplate.send("order-events", event.getEvent());
                event.markAsPublished();
                outboxRepository.save(event);
            } catch (Exception e) {
                // 로깅 및 재시도 로직
            }
        }
    }
}
```

### 5.2 상품 서비스 - 재고 처리

```java
@Service
@RequiredArgsConstructor
public class ProductStockService {
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final CompensationEventRepository compensationRepository;
    
    @KafkaListener(topics = "order-events")
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            processStockReduction(event);
        } catch (Exception e) {
            // 보상 이벤트 발행
            publishCompensationEvent(event);
        }
    }
    
    @Transactional
    public void processStockReduction(OrderCreatedEvent event) {
        // 재고 차감 로직
        for (OrderItem item : event.getItems()) {
            Stock stock = stockRepository.findByProductId(item.getProductId());
            if (stock.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException();
            }
            
            stock.reduceQuantity(item.getQuantity());
            stockRepository.save(stock);
        }
        
        // 재고 차감 완료 이벤트 발행
        kafkaTemplate.send("stock-events", new StockReducedEvent(event.getOrderId()));
    }
    
    private void publishCompensationEvent(OrderCreatedEvent event) {
        StockCompensationEvent compensationEvent = new StockCompensationEvent(event.getOrderId());
        compensationRepository.save(compensationEvent);
        kafkaTemplate.send("stock-compensation-events", compensationEvent);
    }
    
    // 보상 트랜잭션 - 재고 복구
    @KafkaListener(topics = "order-cancellation-events")
    @Transactional
    public void handleOrderCancellationEvent(OrderCancellationEvent event) {
        Order order = orderRepository.findById(event.getOrderId());
        
        for (OrderItem item : order.getItems()) {
            Stock stock = stockRepository.findByProductId(item.getProductId());
            stock.increaseQuantity(item.getQuantity());
            stockRepository.save(stock);
        }
    }
}
```

### 5.3 사용자 서비스 - 포인트/쿠폰 처리

```java
@Service
@RequiredArgsConstructor
public class UserPointService {
    private final UserRepository userRepository;
    private final PointRepository pointRepository;
    private final CouponRepository couponRepository;
    
    @KafkaListener(topics = "stock-events")
    @Transactional
    public void handleStockReducedEvent(StockReducedEvent event) {
        try {
            Order order = fetchOrderDetails(event.getOrderId());
            User user = userRepository.findById(order.getUserId());
            
            // 포인트 차감
            if (order.getUsePoint() > 0) {
                if (user.getPoint() < order.getUsePoint()) {
                    throw new InsufficientPointException();
                }
                
                user.usePoint(order.getUsePoint());
                userRepository.save(user);
            }
            
            // 쿠폰 사용
            if (order.getCouponId() != null) {
                Coupon coupon = couponRepository.findById(order.getCouponId());
                coupon.markAsUsed();
                couponRepository.save(coupon);
            }
            
            // 포인트/쿠폰 처리 완료 이벤트 발행
            kafkaTemplate.send("user-events", new UserPointProcessedEvent(event.getOrderId()));
        } catch (Exception e) {
            // 보상 이벤트 발행
            publishCompensationEvent(event);
        }
    }
    
    // 보상 트랜잭션 - 포인트/쿠폰 복구
    @KafkaListener(topics = "order-cancellation-events")
    @Transactional
    public void handleOrderCancellationEvent(OrderCancellationEvent event) {
        Order order = fetchOrderDetails(event.getOrderId());
        User user = userRepository.findById(order.getUserId());
        
        // 포인트 복구
        if (order.getUsePoint() > 0) {
            user.addPoint(order.getUsePoint());
            userRepository.save(user);
        }
        
        // 쿠폰 복구
        if (order.getCouponId() != null) {
            Coupon coupon = couponRepository.findById(order.getCouponId());
            coupon.markAsUnused();
            couponRepository.save(coupon);
        }
    }
    
    private Order fetchOrderDetails(String orderId) {
        // REST API 또는 다른 방법으로 주문 서비스에서 주문 정보 조회
        return orderServiceClient.getOrder(orderId);
    }
}
```

### 5.4 데이터 플랫폼 서비스 - 데이터 전송

```java
@Service
@RequiredArgsConstructor
public class DataPlatformService {
    private final MessageRepository messageRepository;
    
    @KafkaListener(topics = {"order-events", "payment-events"})
    public void handleEvents(Object event) {
        try {
            if (event instanceof OrderCompletedEvent) {
                processOrderData((OrderCompletedEvent) event);
            } else if (event instanceof PaymentCompletedEvent) {
                processPaymentData((PaymentCompletedEvent) event);
            }
        } catch (Exception e) {
            // 재시도 로직 또는 Dead Letter Queue로 이동
            messageRepository.save(new FailedMessage(event, e.getMessage()));
        }
    }
    
    private void processOrderData(OrderCompletedEvent event) {
        // 주문 데이터 처리 및 저장
        DataPlatformOrderDto dto = mapper.toDataPlatformDto(event);
        restTemplate.postForEntity("http://data-platform-api/orders", dto, Void.class);
    }
    
    private void processPaymentData(PaymentCompletedEvent event) {
        // 결제 데이터 처리 및 저장
        DataPlatformPaymentDto dto = mapper.toDataPlatformDto(event);
        restTemplate.postForEntity("http://data-platform-api/payments", dto, Void.class);
    }
}
```

## 6. 데이터 일관성 및 장애 대응 전략

### 6.1 데이터 일관성 방안

1. **벌크헤드 패턴(Bulkhead Pattern):**
   - 서비스 장애가 전체 시스템으로 전파되지 않도록 격리
   - 각 서비스는 독립적으로 장애 처리
   - 리소스 풀을 분리하여 장애 전파 방지

2. **서킷브레이커 패턴(Circuit Breaker Pattern):**
   - 서비스 호출 실패 시 폴백 메커니즘 제공
   - 장애 서비스에 대한 반복적인 호출 방지
   - 서비스 복구 시 자동 복구

3. **재시도 메커니즘(Retry Mechanism):**
   - 일시적인 장애에 대해 점진적으로 재시도
   - 재시도 간격을 지수적으로 증가(Exponential Backoff)
   - 최대 재시도 횟수 설정

### 6.2 데이터 싱크 불일치 모니터링 및 복구

1. **데이터 불일치 모니터링:**
   - 정기적인 데이터 검증 프로세스 실행
   - 각 서비스의 데이터 상태 비교
   - 불일치 감지 시 알림 발송

2. **자동 복구 프로세스:**
   - 데이터 불일치 발견 시 자동 복구 절차 실행
   - 이벤트 재생을 통한 데이터 상태 복구
   - 이벤트 소싱 패턴 활용한 장애 복구

3. **장애 대응 매뉴얼:**
   - 서비스별 장애 시나리오 및 대응 방안 문서화
   - 수동 복구 절차 정의
   - 장애 보고 및 분석 프로세스 수립

## 7. 결론 및 고려사항

### 7.1 MSA 전환 접근 방법

1. **단계적 전환:**
   - 모놀리식 애플리케이션에서 점진적으로 서비스 분리
   - 우선순위가 높은 도메인부터 분리 시작
   - 각 단계에서 충분한 테스트 및 검증

2. **분산 트랜잭션 패턴 적용:**
   - Saga 패턴을 통한 분산 트랜잭션 관리
   - 보상 트랜잭션 구현으로 데이터 일관성 보장
   - 이벤트 기반 아키텍처로 서비스 간 결합도 감소

3. **모니터링 및 운영 고려:**
   - 분산 로깅 및 추적 시스템 구축
   - 서비스 상태 및 성능 모니터링
   - 장애 대응 체계 수립

### 7.2 비즈니스 무결성 보장 방안

1. **업무 규칙 정의:**
   - 주문 생성, 재고 처리, 포인트 사용 등의 업무 규칙 명확화
   - 각 서비스의 책임과 권한 정의
   - 데이터 정합성 검증 규칙 수립

2. **트랜잭션 경계 설정:**
   - 비즈니스적으로 허용 가능한 일시적 데이터 불일치 범위 정의
   - 최종 일관성(Eventual Consistency) 모델 적용
   - 비즈니스 요구사항에 맞는 복구 정책 수립

3. **장애 테스트 및 시뮬레이션:**
   - 카오스 엔지니어링을 통한 장애 대응 능력 검증
   - 다양한 장애 시나리오에 대한 테스트 자동화
   - 정기적인 재해 복구 훈련 실시

### 7.3 성능 및 확장성 고려

1. **비동기 통신 최적화:**
   - 메시지 브로커 성능 튜닝
   - 배치 처리를 통한 메시지 처리 효율화
   - 메시지 우선순위 설정

2. **데이터 접근 최적화:**
   - 각 서비스에 필요한 데이터 복제
   - 캐싱 전략 적용
   - 읽기 성능 최적화를 위한 CQRS 패턴 적용

3. **서비스 확장성:**
   - 수평적 확장이 가능한 아키텍처 설계
   - 무상태(Stateless) 서비스 구현
   - 자동 스케일링 정책 수립
