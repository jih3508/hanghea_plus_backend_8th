# Kafka 실습 빠른 시작 가이드

## 🚀 단계별 실습 진행

### 1단계: 환경 구축 (5분)
```bash
# 1. Docker 서비스 시작
docker-compose up -d

# 2. 서비스 상태 확인
docker-compose ps

# 3. Kafka UI 접속 확인
# 브라우저에서 http://localhost:8081 접속
```

### 2단계: Kafka 토픽 생성 (3분)
```bash
# Windows (Git Bash 사용 권장)
bash kafka-scripts/create-topics.sh

# 또는 수동 생성
docker exec kafka kafka-topics --create --topic order-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

### 3단계: 기본 Producer/Consumer 테스트 (5분)
```bash
# 터미널 1: Producer 시작
bash kafka-scripts/test-producer.sh

# 터미널 2: Consumer 시작  
bash kafka-scripts/test-consumer.sh

# Producer에서 메시지 입력하고 Consumer에서 확인
```

### 4단계: Spring Boot 애플리케이션 실행 (10분)
```bash
# 1. 애플리케이션 빌드
./gradlew build

# 2. 애플리케이션 실행
./gradlew bootRun

# 3. 애플리케이션 로그에서 Kafka 연결 확인
```

### 5단계: 주문 API를 통한 실제 이벤트 테스트 (10분)
```bash
# 1. 테스트 데이터 준비 (MySQL)
# 2. 주문 생성 API 호출
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

# 3. Kafka UI에서 메시지 확인
# 4. 애플리케이션 로그에서 Consumer 처리 확인
```

### 6단계: 고급 기능 실습 (15분)
```bash
# 1. 여러 Consumer Group으로 병렬 처리 테스트
# 2. 파티션별 메시지 분산 확인
# 3. 에러 처리 및 재시도 테스트
```

## 📊 실습 결과 확인 포인트

### ✅ 체크리스트
- [ ] Docker 서비스 모두 실행 중
- [ ] Kafka UI 접속 가능
- [ ] 토픽 4개 생성 완료 (order-events, payment-events, inventory-events, notification-events)
- [ ] 콘솔 Producer/Consumer 동작 확인
- [ ] Spring Boot 애플리케이션 실행 성공
- [ ] 주문 API 호출 성공
- [ ] Kafka 이벤트 발행 및 소비 확인
- [ ] 애플리케이션 로그에서 이벤트 처리 로그 확인

### 🔍 문제 해결
```bash
# Docker 서비스 재시작
docker-compose restart

# Kafka 연결 테스트
docker exec kafka kafka-topics --list --bootstrap-server localhost:9092

# 애플리케이션 로그 확인
docker-compose logs -f
```

## 📝 다음 단계 제안

### STEP 17 완성을 위해:
1. **문서 보완**: `docs/kafka/kafka-basic-concepts.md` 리뷰 및 수정
2. **추가 Consumer 구현**: 알림, 분석 등 다양한 비즈니스 로직
3. **테스트 코드 작성**: 더 많은 통합 테스트 추가

### STEP 18 진행을 위해:
1. **성능 측정**: 기존 vs 개선된 시스템 성능 비교
2. **시퀀스 다이어그램 작성**: Mermaid 등을 활용한 시각화
3. **상세 설계 문서**: `business-process-improvement.md` 보완

이제 실습을 시작해보세요! 문제가 발생하면 언제든 도움을 요청하세요.
