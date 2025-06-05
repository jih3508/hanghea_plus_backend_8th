# HH Plus 배포 및 부하 테스트 스크립트

이 디렉토리에는 HH Plus 프로젝트의 배포와 부하 테스트를 위한 스크립트들이 포함되어 있습니다.

## 📋 목차

- [배포 스크립트](#배포-스크립트)
- [부하 테스트 스크립트](#부하-테스트-스크립트)
- [사용법](#사용법)
- [리소스 설정](#리소스-설정)
- [모니터링](#모니터링)

## 🚀 배포 스크립트

### deploy.sh

Docker Compose를 사용하여 애플리케이션을 배포합니다.

```bash
# 기본 사용법
./scripts/deploy.sh [environment] [build_option]

# 예시
./scripts/deploy.sh local build      # 로컬 환경에서 새로 빌드
./scripts/deploy.sh test no-build    # 테스트 환경에서 기존 이미지 사용
./scripts/deploy.sh prod rebuild     # 운영 환경에서 완전 재빌드
```

#### 환경 옵션
- `local`: 로컬 개발 환경 (기본값)
- `test`: 테스트 환경 (모니터링 포함)
- `prod`: 운영 환경 (모든 서비스 + 모니터링)

#### 빌드 옵션
- `build`: 새로 빌드 후 배포 (기본값)
- `no-build`: 빌드 없이 기존 이미지로 배포
- `rebuild`: 완전히 새로 빌드 (캐시 무시)

## ⚡ 부하 테스트 스크립트

### load-test.sh

K6를 사용하여 다양한 타입의 부하 테스트를 실행합니다.

```bash
# 기본 사용법
./scripts/load-test.sh [test_type] [duration] [users]

# 예시
./scripts/load-test.sh smoke 30s 5        # 스모크 테스트
./scripts/load-test.sh load 5m 50         # 부하 테스트
./scripts/load-test.sh stress 10m 200     # 스트레스 테스트
./scripts/load-test.sh spike 5m 100       # 스파이크 테스트
./scripts/load-test.sh volume 15m 100     # 볼륨 테스트
```

#### 테스트 타입
- `smoke`: 스모크 테스트 - 기본 기능 확인
- `load`: 부하 테스트 - 정상 트래픽 시뮬레이션
- `stress`: 스트레스 테스트 - 시스템 한계점 테스트
- `spike`: 스파이크 테스트 - 급격한 트래픽 증가
- `volume`: 볼륨 테스트 - 대용량 데이터 처리

## 🔧 사용법

### 1. 스크립트 실행 권한 설정

```bash
chmod +x scripts/deploy.sh
chmod +x scripts/load-test.sh
```

### 2. 애플리케이션 배포

```bash
# 로컬 환경 배포
./scripts/deploy.sh

# 또는 상세 옵션 지정
./scripts/deploy.sh local build
```

### 3. 부하 테스트 실행

```bash
# 기본 스모크 테스트
./scripts/load-test.sh

# 본격적인 부하 테스트
./scripts/load-test.sh load 5m 100
```

### 4. 결과 확인

테스트 결과는 `load-test/results/` 디렉토리에 저장됩니다:
- `results.json`: K6 테스트 결과 (JSON)
- `results.csv`: K6 테스트 결과 (CSV)
- `summary.txt`: 테스트 요약
- `docker_stats.log`: Docker 컨테이너 리소스 사용량

## 🖥️ 리소스 설정

### CPU 및 메모리 제한

각 서비스별로 다음과 같이 리소스가 제한됩니다:

#### Spring Boot 애플리케이션
- **CPU**: 2.0 cores (제한), 1.0 cores (예약)
- **메모리**: 2GB (제한), 1GB (예약)

#### MySQL
- **CPU**: 1.0 cores (제한), 0.5 cores (예약)
- **메모리**: 1GB (제한), 512MB (예약)

#### Redis
- **CPU**: 0.5 cores (제한), 0.25 cores (예약)
- **메모리**: 512MB (제한), 256MB (예약)

#### Kafka
- **CPU**: 1.0 cores (제한), 0.5 cores (예약)
- **메모리**: 1GB (제한), 512MB (예약)

#### Zookeeper
- **CPU**: 0.5 cores (제한), 0.25 cores (예약)
- **메모리**: 512MB (제한), 256MB (예약)

### 리소스 설정 변경

`docker-compose.yml`에서 다음 섹션을 수정하여 리소스를 조정할 수 있습니다:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'      # CPU 제한
      memory: 2G       # 메모리 제한
    reservations:
      cpus: '1.0'      # CPU 예약
      memory: 1G       # 메모리 예약
```

## 📊 모니터링

### 접속 정보

배포 완료 후 다음 서비스에 접속할 수 있습니다:

| 서비스 | URL | 설명 |
|--------|-----|------|
| 애플리케이션 | http://localhost:8080 | 메인 애플리케이션 |
| Swagger UI | http://localhost:8080/swagger-ui/index.html | API 문서 |
| Health Check | http://localhost:8080/actuator/health | 헬스체크 |
| Prometheus | http://localhost:9090 | 메트릭 수집 |
| Grafana | http://localhost:3000 | 모니터링 대시보드 |

### 외부 서비스

| 서비스 | 포트 | 접속 정보 |
|--------|------|----------|
| MySQL | 3306 | root/root, application/application |
| Redis | 6379 | 비밀번호 없음 |
| Kafka | 9094 | localhost:9094 |

### 유용한 명령어

```bash
# 서비스 로그 보기
docker-compose logs -f app
docker-compose logs -f mysql
docker-compose logs -f redis

# 서비스 상태 확인
docker-compose ps

# 서비스 재시작
docker-compose restart app

# 서비스 중지
docker-compose down

# 완전 정리 (볼륨 포함)
docker-compose down -v
```

## 🔍 트러블슈팅

### 일반적인 문제들

1. **포트 충돌**
   - 기존에 실행 중인 서비스가 있는지 확인
   - `docker-compose down`으로 기존 컨테이너 정리

2. **메모리 부족**
   - Docker Desktop 메모리 설정 확인 (최소 8GB 권장)
   - 불필요한 컨테이너 정리

3. **애플리케이션 시작 실패**
   - `docker-compose logs app`으로 로그 확인
   - 데이터베이스 연결 상태 확인

4. **부하 테스트 실패**
   - 애플리케이션이 정상 실행 중인지 확인
   - `curl http://localhost:8080/actuator/health` 테스트

### 성능 최적화 팁

1. **JVM 튜닝**
   - Dockerfile에서 JVM 옵션 조정
   - 힙 메모리 크기 최적화

2. **데이터베이스 최적화**
   - 인덱스 추가
   - 커넥션 풀 설정 조정

3. **캐시 활용**
   - Redis 캐시 전략 수립
   - 애플리케이션 레벨 캐시 활용

4. **모니터링 기반 최적화**
   - Grafana 대시보드 활용
   - 병목 지점 식별 및 개선

## 📝 참고사항

- 부하 테스트는 실제 운영 환경과 유사한 조건에서 실행하는 것이 좋습니다
- 테스트 결과는 하드웨어 스펙과 네트워크 환경에 따라 달라질 수 있습니다
- 정기적인 부하 테스트를 통해 성능 저하를 조기에 발견할 수 있습니다
- 모니터링 데이터를 기반으로 지속적인 성능 개선을 진행하세요
