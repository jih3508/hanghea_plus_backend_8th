import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 스트레스 테스트 - 시스템 한계점 테스트
export let options = {
  stages: [
    { duration: '1m', target: 50 },   // 워밍업
    { duration: '2m', target: 100 },  // 정상 부하
    { duration: '3m', target: 200 },  // 스트레스 부하
    { duration: '2m', target: 300 },  // 극한 부하
    { duration: '1m', target: 200 },  // 부하 감소
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 95% of requests must complete below 5s
    http_req_failed: ['rate<0.20'],    // error rate must be below 20%
    concurrent_orders: ['avg<100'],    // average concurrent orders
  },
};

const BASE_URL = 'http://host.docker.internal:8080';

// 커스텀 메트릭
const concurrentOrders = new Trend('concurrent_orders');
const systemErrors = new Rate('system_errors');

export function setup() {
  console.log('⚡ 스트레스 테스트 시작 - 시스템 한계점 테스트');
  
  return {
    startTime: new Date(),
    users: generateStressTestUsers(100)
  };
}

function generateStressTestUsers(count) {
  const users = [];
  for (let i = 1; i <= count; i++) {
    users.push({
      id: i,
      email: `stress_test_${i}@example.com`,
      password: 'stress123',
      name: `StressUser${i}`
    });
  }
  return users;
}

export default function (data) {
  const user = data.users[Math.floor(Math.random() * data.users.length)];
  
  // 고부하 시나리오 실행
  // 시나리오 1: 동시 주문 처리 (60%)
  if (Math.random() < 0.6) {
    concurrentOrderScenario(user);
  }
  // 시나리오 2: 대량 데이터 조회 (25%)
  else if (Math.random() < 0.85) {
    bulkDataQueryScenario();
  }
  // 시나리오 3: 복합 트랜잭션 (15%)
  else {
    complexTransactionScenario(user);
  }
  
  // 스트레스 상황에서는 대기 시간을 최소화
  sleep(Math.random() * 0.5);
}

function concurrentOrderScenario(user) {
  // 빠른 연속 주문 생성
  for (let i = 0; i < 3; i++) {
    let productId = Math.floor(Math.random() * 100) + 1;
    let quantity = Math.floor(Math.random() * 5) + 1;
    
    let orderPayload = JSON.stringify({
      productId: productId,
      quantity: quantity,
      userId: user.id
    });
    
    let orderRes = http.post(`${BASE_URL}/api/orders`, orderPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    let orderSuccess = check(orderRes, {
      '동시 주문 처리': (r) => r.status === 200 || r.status === 201 || r.status === 409,
      '주문 응답시간 < 10s': (r) => r.timings.duration < 10000,
    });
    
    if (!orderSuccess) {
      systemErrors.add(1);
    }
    
    concurrentOrders.add(i + 1);
    
    // 매우 짧은 대기
    sleep(0.1);
  }
}

function bulkDataQueryScenario() {
  // 대량 데이터 조회 요청
  let queries = [
    '/api/products?page=0&size=100',
    '/api/orders?page=0&size=100',
    '/api/users?page=0&size=100',
    '/api/points/history?page=0&size=100',
  ];
  
  queries.forEach(query => {
    let res = http.get(`${BASE_URL}${query}`);
    check(res, {
      '대량 데이터 조회': (r) => r.status === 200 || r.status === 404,
      '조회 응답시간 < 15s': (r) => r.timings.duration < 15000,
    });
    
    if (res.status >= 500) {
      systemErrors.add(1);
    }
    
    sleep(0.1);
  });
}

function complexTransactionScenario(user) {
  // 복합 트랜잭션: 포인트 충전 -> 상품 주문 -> 결제
  
  // 1. 포인트 충전
  let chargePayload = JSON.stringify({
    userId: user.id,
    amount: 100000
  });
  
  let chargeRes = http.post(`${BASE_URL}/api/users/${user.id}/points/charge`, chargePayload, {
    headers: { 'Content-Type': 'application/json' },
  });
  
  check(chargeRes, {
    '포인트 충전 성공': (r) => r.status === 200 || r.status === 404,
  });
  
  sleep(0.2);
  
  // 2. 여러 상품 주문
  for (let i = 0; i < 2; i++) {
    let productId = Math.floor(Math.random() * 50) + 1;
    let orderPayload = JSON.stringify({
      productId: productId,
      quantity: 1,
      userId: user.id
    });
    
    let orderRes = http.post(`${BASE_URL}/api/orders`, orderPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    check(orderRes, {
      '복합 트랜잭션 주문': (r) => r.status === 200 || r.status === 201 || r.status === 400,
    });
    
    if (orderRes.status >= 500) {
      systemErrors.add(1);
    }
    
    sleep(0.1);
  }
  
  // 3. 포인트 잔액 확인
  let pointsRes = http.get(`${BASE_URL}/api/users/${user.id}/points`);
  check(pointsRes, {
    '포인트 잔액 확인': (r) => r.status === 200 || r.status === 404,
  });
}

export function teardown(data) {
  console.log('✅ 스트레스 테스트 완료');
  console.log(`⏱️ 테스트 시간: ${(new Date() - data.startTime) / 1000}초`);
  console.log('📈 시스템 한계점을 확인하세요');
  console.log('💡 높은 응답시간이나 오류율이 발생한 구간을 분석하세요');
}
