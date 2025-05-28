# STEP 18: Kafka를 활용한 비즈니스 프로세스 개선

## 1. 기존 프로세스의 한계점 분석

### 1.1 현재 주문 처리 프로세스
```
[주문 API] → [재고 차감] → [결제 처리] → [주문 완료] → [외부 시스템 전송]
    ↓           ↓           ↓           ↓              ↓
  동기적     동기적       동기적      동기적         동기적
```

### 1.2 식별된 문제점

#### 1.2.1 성능 병목점
- **동기적 처리**: 모든 단계가 순차적으로 실행되어 응답 시간 증가
- **외부 의존성**: 외부 시스템 장애 시 전체 주문 프로세스 실패
- **확장성 제한**: 트래픽 증가 시 단일 서버의 처리 한계

#### 1.2.2 안정성 문제
- **단일 실패점**: 하나의 단계 실패 시 전체 트랜잭션 롤백
- **데이터 일관성**: 분산 트랜잭션 관리의 복잡성
- **재시도 메커니즘 부족**: 일시적 장애에 대한 복구 능력 부족

#### 1.2.3 운영 복잡성
- **강결합**: 서비스 간 직접적인 의존성으로 인한 변경 영향도 증가
- **모니터링 어려움**: 분산된 처리 과정 추적의 복잡성
- **스케일링 비효율**: 특정 단계만 확장하기 어려움

### 1.3 대용량 트래픽 시나리오 분석

#### 1.3.1 플래시 세일 상황
- **초당 10,000건의 주문 요청**
- **동시 재고 차감으로 인한 락 경합**
- **결제 시스템 부하로 인한 타임아웃**

#### 1.3.2 측정된 성능 지표
```
현재 시스템 성능:
- 평균 응답 시간: 2.5초
- 최대 처리량: 200 TPS
- 장애 복구 시간: 5-10분
- 시스템 가용성: 99.5%
```

## 2. Kafka 기반 개선 설계

### 2.1 새로운 아키텍처 개요

#### 2.1.1 이벤트 드리븐 아키텍처 적용
```
[주문 API] → [주문 생성] → [ORDER_CREATED 이벤트 발행]
     ↓            ↓                    ↓
   즉시 응답   비동기 처리        이벤트 스트림
                                      ↓
              [재고 서비스] ← [INVENTORY_EVENTS]
              [결제 서비스] ← [PAYMENT_EVENTS]  
              [알림 서비스] ← [NOTIFICATION_EVENTS]
              [분석 서비스] ← [ANALYTICS_EVENTS]
```

#### 2.1.2 핵심 설계 원칙
- **느슨한 결합**: 서비스 간 직접적인 의존성 제거
- **이벤트 소싱**: 모든 비즈니스 이벤트를 이벤트 스트림으로 기록
- **CQRS 패턴**: 명령과 조회의 분리로 성능 최적화
- **Saga 패턴**: 분산 트랜잭션 관리

### 2.2 상세 비즈니스 플로우

#### 2.2.1 주문 생성 플로우 (시퀀스 다이어그램)
```
Client → OrderAPI: POST /orders
OrderAPI → OrderService: 주문 생성 요청
OrderService → OrderService: 주문 데이터 검증
OrderService → OrderService: 주문 생성 (상태: PENDING)
OrderService → Kafka: ORDER_CREATED 이벤트 발행
OrderService → OrderAPI: 주문 ID 반환 (비동기 처리 중)
OrderAPI → Client: 202 Accepted

Kafka → InventoryService: ORDER_CREATED 소비
InventoryService → InventoryService: 재고 차감 처리
InventoryService → Kafka: INVENTORY_DEDUCTED 이벤트 발행

Kafka → PaymentService: INVENTORY_DEDUCTED 소비
PaymentService → PaymentService: 결제 처리
PaymentService → Kafka: PAYMENT_COMPLETED 이벤트 발행

Kafka → OrderService: PAYMENT_COMPLETED 소비
OrderService → OrderService: 주문 상태 업데이트 (COMPLETED)
OrderService → Kafka: ORDER_COMPLETED 이벤트 발행

Kafka → NotificationService: ORDER_COMPLETED 소비
NotificationService → NotificationService: 주문 완료 알림 발송
```

#### 2.2.2 실패 처리 및 보상 트랜잭션
```
OrderService → Kafka: ORDER_CREATED 이벤트 발행
Kafka → InventoryService: ORDER_CREATED 소비
InventoryService → InventoryService: 재고 부족 확인
InventoryService → Kafka: INVENTORY_INSUFFICIENT 이벤트 발행

Kafka → OrderService: INVENTORY_INSUFFICIENT 소비
OrderService → OrderService: 주문 상태 업데이트 (CANCELLED)
OrderService → Kafka: ORDER_CANCELLED 이벤트 발행
```

## 3. 성능 개선 지표

### 3.1 예상 성능 개선
```
개선 전:
- 최대 TPS: 200
- 평균 응답 시간: 2.5초
- P99 응답 시간: 8초
- 시스템 가용성: 99.5%

개선 후 (예상):
- 최대 TPS: 2,000 (10배 증가)
- 평균 응답 시간: 0.3초 (83% 감소)
- P99 응답 시간: 1초 (87.5% 감소)
- 시스템 가용성: 99.9%
```

이 설계를 통해 이커머스 시스템의 확장성, 안정성, 성능을 크게 개선할 수 있습니다.