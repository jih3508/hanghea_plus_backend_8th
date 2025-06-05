import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 스파이크 테스트 - 급격한 트래픽 증가 대응 테스트
export let options = {
  stages: [
    { duration: '30s', target: 20 },   // 정상 상태
    { duration: '30s', target: 200 },  // 급격한 증가 (10배)
    { duration: '1m', target: 200 },   // 스파이크 유지
    { duration: '30s', target: 20 },   // 급격한 감소
    { duration: '1m', target: 20 },    // 정상 상태 복구
    { duration: '30s', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 스파이크 중에도 95%는 3초 이내
    http_req_failed: ['rate<0.10'],    // 오류율 10% 이하
    spike_recovery_time: ['avg<30000'], // 스파이크 복구 시간
  },
};

const BASE_URL = 'http://host.docker.internal:8080';

// 커스텀 메트릭
const spikeRecoveryTime = new Trend('spike_recovery_time');
const trafficSpike = new Rate('traffic_spike');

export function setup() {
  console.log('📈 스파이크 테스트 시작 - 급격한 트래픽 증가 대응');
  
  return {
    startTime: new Date(),
    spikeStarted: false,
    normalTrafficBaseline: 0
  };
}

export default function (data) {
  const currentVUs = __ENV.K6_VUS || 1;
  
  // 스파이크 감지 (사용자가 급격히 증가했을 때)
  if (currentVUs > 100 && !data.spikeStarted) {
    data.spikeStarted = true;
    data.spikeStartTime = new Date();
    trafficSpike.add(1);
  }
  
  // 스파이크 상황에서의 시나리오
  if (currentVUs > 100) {
    spikeTrafficScenario();
  } else {
    normalTrafficScenario();
    
    // 스파이크 복구 시간 측정
    if (data.spikeStarted && data.spikeStartTime) {
      let recoveryTime = new Date() - data.spikeStartTime;
      spikeRecoveryTime.add(recoveryTime);
      data.spikeStarted = false;
    }
  }
  
  sleep(Math.random() * 1);
}

function spikeTrafficScenario() {
  // 스파이크 상황에서 가장 자주 호출되는 API들을 테스트
  
  // 1. 인기 상품 조회 (캐시 효과 테스트)
  let popularProducts = [1, 2, 3, 5, 8, 13, 21]; // 피보나치 수열로 인기 상품 시뮬레이션
  let productId = popularProducts[Math.floor(Math.random() * popularProducts.length)];
  
  let productRes = http.get(`${BASE_URL}/api/products/${productId}`);
  check(productRes, {
    '스파이크 상품 조회': (r) => r.status === 200 || r.status === 404,
    '스파이크 상품 조회 < 5s': (r) => r.timings.duration < 5000,
  });
  
  sleep(0.1);
  
  // 2. 재고 확인 (동시성 테스트)
  let stockRes = http.get(`${BASE_URL}/api/products/${productId}/stock`);
  check(stockRes, {
    '스파이크 재고 확인': (r) => r.status === 200 || r.status === 404,
    '스파이크 재고 확인 < 3s': (r) => r.timings.duration < 3000,
  });
  
  sleep(0.1);
  
  // 3. 주문 시도 (부하 분산 테스트)
  if (Math.random() < 0.3) { // 30% 확률로 주문 시도
    let orderPayload = JSON.stringify({
      productId: productId,
      quantity: 1,
      userId: Math.floor(Math.random() * 100) + 1
    });
    
    let orderRes = http.post(`${BASE_URL}/api/orders`, orderPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    check(orderRes, {
      '스파이크 주문 처리': (r) => r.status === 200 || r.status === 201 || r.status === 409 || r.status === 503,
      '스파이크 주문 < 10s': (r) => r.timings.duration < 10000,
    });
  }
}

function normalTrafficScenario() {
  // 정상 트래픽 시나리오
  
  // 1. 상품 목록 조회
  let productsRes = http.get(`${BASE_URL}/api/products?page=0&size=20`);
  check(productsRes, {
    '정상 상품 목록 조회': (r) => r.status === 200,
    '정상 응답시간 < 1s': (r) => r.timings.duration < 1000,
  });
  
  sleep(1);
  
  // 2. 사용자 정보 조회
  let userId = Math.floor(Math.random() * 50) + 1;
  let userRes = http.get(`${BASE_URL}/api/users/${userId}`);
  check(userRes, {
    '사용자 정보 조회': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(1);
  
  // 3. 포인트 조회
  let pointsRes = http.get(`${BASE_URL}/api/users/${userId}/points`);
  check(pointsRes, {
    '포인트 조회': (r) => r.status === 200 || r.status === 404,
  });
}

export function teardown(data) {
  console.log('✅ 스파이크 테스트 완료');
  console.log(`⏱️ 총 테스트 시간: ${(new Date() - data.startTime) / 1000}초`);
  console.log('📊 스파이크 대응 분석:');
  console.log('   - 급격한 트래픽 증가 시 응답시간 변화');
  console.log('   - 시스템 복구 시간');
  console.log('   - 캐시 효과 및 부하 분산');
  console.log('💡 스파이크 상황에서 503 오류가 많다면 Rate Limiting 고려');
  console.log('💡 응답시간이 급격히 증가한다면 Auto Scaling 정책 검토');
}
