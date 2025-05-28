# Kafka 기초 개념 학습

## 1. Apache Kafka란?

Apache Kafka는 분산 스트리밍 플랫폼으로, 대용량의 실시간 데이터 스트리밍을 처리하는데 사용됩니다. LinkedIn에서 개발되어 Apache Software Foundation에 기부된 오픈소스 프로젝트입니다.

### 주요 특징
- **높은 처리량(High Throughput)**: 초당 수백만 건의 메시지 처리 가능
- **확장성(Scalability)**: 수평적 확장을 통한 대용량 데이터 처리
- **내구성(Durability)**: 디스크에 데이터를 저장하여 장애 시에도 데이터 보존
- **실시간 처리**: 낮은 지연시간으로 실시간 데이터 스트리밍 지원

## 2. Kafka 핵심 구성요소

### 2.1 Broker (브로커)
- Kafka 클러스터의 각 서버 인스턴스
- 메시지를 저장하고 클라이언트의 요청을 처리
- 일반적으로 3개 이상의 브로커로 클러스터 구성 (고가용성)

```
[Producer] → [Broker 1] → [Consumer]
              [Broker 2]
              [Broker 3]
```

### 2.2 Topic (토픽)
- 메시지가 저장되는 논리적 단위
- 데이터베이스의 테이블과 유사한 개념
- 메시지의 카테고리 또는 분류 기준

**예시:**
- `order-events`: 주문 관련 이벤트
- `user-activity`: 사용자 활동 로그
- `payment-notifications`: 결제 알림

### 2.3 Partition (파티션)
- 토픽을 물리적으로 분할한 단위
- 병렬 처리와 확장성을 위해 사용
- 각 파티션은 순서가 보장되는 불변의 메시지 시퀀스

```
Topic: order-events
├── Partition 0: [msg1] [msg2] [msg3] ...
├── Partition 1: [msg4] [msg5] [msg6] ...
└── Partition 2: [msg7] [msg8] [msg9] ...
```

**파티션의 장점:**
- **병렬 처리**: 여러 컨슈머가 동시에 다른 파티션을 처리
- **확장성**: 파티션 수를 늘려 처리량 향상
- **순서 보장**: 같은 파티션 내에서는 메시지 순서 보장

### 2.4 Producer (프로듀서)
- 토픽에 메시지를 발행(publish)하는 애플리케이션
- 메시지를 어떤 파티션에 보낼지 결정

**파티션 선택 전략:**
1. **라운드로빈**: 파티션에 순차적으로 분배
2. **키 기반 해시**: 메시지 키의 해시값으로 파티션 결정
3. **사용자 정의**: 커스텀 파티셔너 구현

### 2.5 Consumer (컨슈머)
- 토픽에서 메시지를 소비(consume)하는 애플리케이션
- 컨슈머 그룹을 통해 병렬 처리 지원

### 2.6 Consumer Group (컨슈머 그룹)
- 동일한 토픽을 소비하는 컨슈머들의 논리적 그룹
- 각 파티션은 그룹 내 하나의 컨슈머에게만 할당

```
Consumer Group A:
- Consumer 1 → Partition 0, 1
- Consumer 2 → Partition 2

Consumer Group B:
- Consumer 3 → Partition 0, 1, 2
```

### 2.7 Offset (오프셋)
- 각 파티션 내 메시지의 고유한 순번
- 컨슈머가 어디까지 읽었는지 추적하는 포인터
- 컨슈머 재시작 시 이전 위치부터 처리 가능

## 3. Kafka 데이터 흐름

### 3.1 기본 데이터 플로우
```
[Producer] → [Kafka Cluster] → [Consumer]
    ↓           ↓                 ↓
  메시지 발행   메시지 저장      메시지 소비
```

### 3.2 상세 데이터 플로우
```
Producer
  ↓ (send message)
Broker (Leader Partition)
  ↓ (replicate)
Broker (Follower Partitions)
  ↓ (acknowledgment)
Producer (확인)

Consumer
  ↑ (fetch message)
Broker (partition)
  ↑ (commit offset)
Consumer (처리 완료)
```

## 4. Producer, Partition, Consumer 수에 따른 데이터 흐름

### 4.1 시나리오 1: Single Producer, Single Partition, Single Consumer
```
Producer → Partition 0 → Consumer
```
- **처리량**: 낮음 (순차 처리)
- **순서 보장**: 완벽 보장
- **확장성**: 제한적

### 4.2 시나리오 2: Single Producer, Multiple Partitions, Multiple Consumers
```
Producer → Partition 0 → Consumer 1
        → Partition 1 → Consumer 2
        → Partition 2 → Consumer 3
```
- **처리량**: 높음 (병렬 처리)
- **순서 보장**: 파티션 내에서만 보장
- **확장성**: 우수

### 4.3 시나리오 3: Multiple Producers, Multiple Partitions, Multiple Consumers
```
Producer 1 → Partition 0 → Consumer 1
Producer 2 → Partition 1 → Consumer 2
Producer 3 → Partition 2 → Consumer 3
```
- **처리량**: 매우 높음
- **순서 보장**: 파티션 내에서만 보장
- **확장성**: 매우 우수
- **복잡도**: 높음 (메시지 순서 관리 필요)

### 4.4 컨슈머 수와 파티션 수의 관계

**최적의 성능:**
- 컨슈머 수 = 파티션 수
- 각 컨슈머가 하나의 파티션을 담당

**컨슈머 수 > 파티션 수:**
- 일부 컨슈머는 유휴 상태
- 리소스 낭비 발생

**컨슈머 수 < 파티션 수:**
- 일부 컨슈머가 여러 파티션 처리
- 처리 부하 증가

## 5. Kafka vs 전통적인 메시징 시스템

| 특성 | Kafka | 전통적 메시징 (RabbitMQ, ActiveMQ) |
|------|-------|--------------------------------------|
| 메시지 저장 | 디스크에 영구 저장 | 메모리 기반, 일시적 저장 |
| 처리량 | 매우 높음 | 중간 수준 |
| 확장성 | 수평적 확장 용이 | 제한적 |
| 메시지 순서 | 파티션 내 보장 | 큐 전체에서 보장 |
| 복제 | 내장된 복제 기능 | 별도 설정 필요 |
| 사용 사례 | 스트리밍, 로그 수집 | 트랜잭션 메시징 |

## 6. 이커머스에서의 Kafka 활용 사례

### 6.1 주문 처리 파이프라인
```
주문 생성 → [order-created] → 재고 서비스
         → [order-created] → 결제 서비스  
         → [order-created] → 알림 서비스
         → [order-created] → 분석 서비스
```

### 6.2 실시간 데이터 분석
- 사용자 행동 추적
- 실시간 추천 시스템
- 매출 대시보드

### 6.3 마이크로서비스 간 통신
- 서비스 간 느슨한 결합
- 이벤트 소싱 패턴
- CQRS (Command Query Responsibility Segregation)

## 다음 단계
이제 로컬에서 Kafka를 설치하고 실제 애플리케이션에서 Producer와 Consumer를 구현해보겠습니다.
