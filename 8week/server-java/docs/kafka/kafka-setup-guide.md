# Kafka 설치 및 실습 가이드

## 1. Docker Compose로 Kafka 환경 구축

### 1.1 서비스 시작
```bash
# 모든 서비스 시작
docker-compose up -d

# Kafka 관련 서비스만 시작
docker-compose up -d zookeeper kafka kafka-ui

# 서비스 상태 확인
docker-compose ps
```

### 1.2 서비스 접속 정보
- **Kafka Broker**: localhost:9092
- **Zookeeper**: localhost:2181
- **Kafka UI**: http://localhost:8081
- **MySQL**: localhost:3306
- **Redis**: localhost:6379

### 1.3 로그 확인
```bash
# Kafka 로그 확인
docker-compose logs -f kafka

# Zookeeper 로그 확인
docker-compose logs -f zookeeper

# 모든 서비스 로그 확인
docker-compose logs -f
```

## 2. Kafka 토픽 생성 및 관리

### 2.1 토픽 생성 스크립트 실행
```bash
# 권한 부여 (Linux/Mac)
chmod +x kafka-scripts/create-topics.sh

# 토픽 생성
./kafka-scripts/create-topics.sh
```

### 2.2 수동 토픽 생성
```bash
# 개별 토픽 생성
docker exec kafka kafka-topics --create \
  --topic order-events \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# 토픽 목록 확인
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 토픽 상세 정보 확인
docker exec kafka kafka-topics --describe \
  --topic order-events \
  --bootstrap-server localhost:9092
```

### 2.3 토픽 파티션 및 설정 확인
```bash
# 모든 토픽의 상세 정보
docker exec kafka kafka-topics --describe --bootstrap-server localhost:9092

# 특정 토픽의 오프셋 확인
docker exec kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic order-events
```

## 3. 기본 Producer/Consumer 테스트

### 3.1 콘솔 Producer 테스트
```bash
# Producer 스크립트 실행
./kafka-scripts/test-producer.sh

# 또는 직접 실행
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic order-events
```

### 3.2 콘솔 Consumer 테스트
```bash
# Consumer 스크립트 실행 (새 터미널에서)
./kafka-scripts/test-consumer.sh

# 또는 직접 실행
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning
```

### 3.3 Consumer Group 테스트
```bash
# Consumer Group으로 실행
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --group test-group \
  --from-beginning

# Consumer Group 목록 확인
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --list

# Consumer Group 상세 정보
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group test-group \
  --describe
```

## 4. Spring Boot 애플리케이션 테스트

### 4.1 애플리케이션 빌드 및 실행
```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/server-*.jar
```

### 4.2 주문 생성을 통한 Kafka 이벤트 테스트

#### 4.2.1 사전 준비 (데이터베이스 설정)
```sql
-- 테스트 사용자 생성
INSERT INTO user (id, name, created_at, updated_at) VALUES (1, 'test-user', NOW(), NOW());

-- 포인트 추가
INSERT INTO point (user_id, point, created_at, updated_at) VALUES (1, 100000, NOW(), NOW());

-- 테스트 상품 생성
INSERT INTO product (id, name, price, category, created_at, updated_at) 
VALUES (1, 'Test Product', 10000, 'ELECTRONICS', NOW(), NOW());

-- 상품 재고 추가
INSERT INTO product_stock (product_id, stock, created_at, updated_at) 
VALUES (1, 100, NOW(), NOW());
```

#### 4.2.2 API 테스트
```bash
# 주문 생성 API 호출
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "X-USER-ID: 1" \
  -d '{
    "items": [
      {
        "productId": 1,
        "quantity": 2
      }
    ]
  }'
```

### 4.3 Kafka 이벤트 확인

#### 4.3.1 애플리케이션 로그 확인
```bash
# Spring Boot 로그에서 Kafka 이벤트 확인
tail -f logs/application.log | grep -i kafka

# 또는 콘솔 출력 확인
docker-compose logs -f app
```

#### 4.3.2 Kafka UI에서 메시지 확인
1. 브라우저에서 http://localhost:8081 접속
2. Topics 메뉴에서 `order-events` 선택
3. Messages 탭에서 발행된 메시지 확인

#### 4.3.3 콘솔 Consumer로 실시간 모니터링
```bash
# 주문 이벤트 모니터링
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --from-beginning \
  --property print.key=true \
  --property key.separator=": "
```

## 5. 고급 기능 테스트

### 5.1 여러 Consumer Group으로 병렬 처리 테스트

#### 터미널 1: 분석 그룹
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --group order-analytics-group \
  --from-beginning
```

#### 터미널 2: 알림 그룹
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --group order-notification-group \
  --from-beginning
```

#### 터미널 3: 데이터 플랫폼 그룹
```bash
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --group data-platform-group \
  --from-beginning
```

### 5.2 파티션별 처리 확인
```bash
# 파티션별 메시지 수 확인
docker exec kafka kafka-run-class kafka.tools.GetOffsetShell \
  --broker-list localhost:9092 \
  --topic order-events \
  --time -1

# 특정 파티션에서만 소비
docker exec -it kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --partition 0 \
  --from-beginning
```

### 5.3 메시지 키를 통한 파티션 라우팅 테스트
```bash
# 키와 함께 메시지 발행
docker exec -it kafka kafka-console-producer \
  --bootstrap-server localhost:9092 \
  --topic order-events \
  --property parse.key=true \
  --property key.separator=:

# 입력 예시:
# user-123:{"orderId": 1, "userId": 123, "amount": 50000}
# user-456:{"orderId": 2, "userId": 456, "amount": 30000}
```

## 6. 성능 테스트

### 6.1 Producer 성능 테스트
```bash
# 대량 메시지 발행 테스트
docker exec kafka kafka-producer-perf-test \
  --topic order-events \
  --num-records 10000 \
  --record-size 1000 \
  --throughput 1000 \
  --producer-props bootstrap.servers=localhost:9092
```

### 6.2 Consumer 성능 테스트
```bash
# Consumer 성능 테스트
docker exec kafka kafka-consumer-perf-test \
  --topic order-events \
  --messages 10000 \
  --bootstrap-server localhost:9092
```

## 7. 모니터링 및 디버깅

### 7.1 JMX 메트릭 확인
```bash
# JMX 포트를 통한 메트릭 확인 (별도 도구 필요)
# JConsole 또는 기타 JMX 클라이언트로 localhost:9101 접속
```

### 7.2 로그 분석
```bash
# Kafka 서버 로그
docker exec kafka cat /var/log/kafka/server.log

# 특정 토픽의 로그 파일 확인
docker exec kafka ls -la /var/lib/kafka/data/order-events-*
```

### 7.3 오프셋 리셋 (개발 시에만 사용)
```bash
# Consumer Group 오프셋 리셋
docker exec kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group order-analytics-group \
  --reset-offsets \
  --to-earliest \
  --topic order-events \
  --execute
```

## 8. 문제 해결

### 8.1 일반적인 문제들
- **Connection refused**: Kafka 서비스가 시작되지 않았거나 포트가 다름
- **Topic not found**: 토픽이 생성되지 않았음
- **Consumer lag**: Consumer가 Producer보다 느림

### 8.2 문제 해결 명령어
```bash
# 서비스 재시작
docker-compose restart kafka

# 데이터 볼륨 초기화 (주의: 모든 데이터 삭제)
docker-compose down -v
docker-compose up -d

# 네트워크 연결 확인
docker exec kafka ping zookeeper
```

## 9. 정리

### 9.1 서비스 종료
```bash
# 모든 서비스 종료
docker-compose down

# 볼륨까지 삭제 (데이터 완전 삭제)
docker-compose down -v
```

### 9.2 리소스 정리
```bash
# 사용하지 않는 Docker 이미지 삭제
docker system prune -f

# Kafka 관련 볼륨 확인
docker volume ls | grep kafka
```