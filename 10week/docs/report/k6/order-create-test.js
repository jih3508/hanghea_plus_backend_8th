import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 주문 생성 API 전용 테스트 (가장 중요한 API)
export const options = {
  stages: [
    { duration: '1m', target: 10 },   // 워밍업
    { duration: '3m', target: 30 },   // 정상 부하
    { duration: '5m', target: 50 },   // 높은 부하
    { duration: '2m', target: 80 },   // 스파이크 (동시성 테스트)
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<3000'], // 주문은 3초 이하
    http_req_failed: ['rate<0.05'],    // 에러율 5% 이하
    'order_success_rate': ['rate>0.95'],
    'concurrent_order_conflicts': ['rate<0.02'], // 동시성 충돌 2% 이하
  },
};

// 커스텀 메트릭
const orderSuccessRate = new Rate('order_success_rate');
const concurrentOrderConflicts = new Rate('concurrent_order_conflicts');
const orderProcessingTime = new Trend('order_processing_time');

const BASE_URL = 'http://localhost:8080';
const USER_IDS = Array.from({length: 50}, (_, i) => i + 1); // 1-50 사용자
const PRODUCT_IDS = Array.from({length: 20}, (_, i) => i + 1); // 1-20 상품

function getRandomUserId() {
  return USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
}

function getRandomProductId() {
  return PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
}

export default function() {
  const userId = getRandomUserId();
  const productId = getRandomProductId();
  const quantity = Math.floor(Math.random() * 3) + 1; // 1-3개
  
  // 주문 데이터 생성
  const orderData = {
    productId: productId,
    quantity: quantity,
    couponId: Math.random() > 0.7 ? Math.floor(Math.random() * 10) + 1 : null, // 30% 확률로 쿠폰 사용
  };
  
  const startTime = Date.now();
  
  const response = http.post(
    `${BASE_URL}/api/orders/${userId}`,
    JSON.stringify(orderData),
    {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'K6-OrderTest/1.0',
      },
      timeout: '15s',
    }
  );
  
  const processingTime = Date.now() - startTime;
  orderProcessingTime.add(processingTime);
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200 또는 201': (r) => r.status === 200 || r.status === 201,
    '응답시간 3초 이하': (r) => r.timings.duration < 3000,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
  });
  
  orderSuccessRate.add(isSuccess);
  
  // 동시성 충돌 감지 (재고 부족, 중복 처리 등)
  if (response.status === 409 || response.status === 422) {
    concurrentOrderConflicts.add(1);
  } else {
    concurrentOrderConflicts.add(0);
  }
  
  // 성공 응답 검증
  if (response.status === 200 || response.status === 201) {
    const validOrder = check(response, {
      '주문 ID 생성': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.orderId !== undefined && data.orderId !== null;
        } catch (e) {
          return false;
        }
      },
      '주문 금액 정보': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.totalAmount !== undefined && data.totalAmount > 0;
        } catch (e) {
          return false;
        }
      },
      '주문 상태 정보': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.status && ['PENDING', 'CONFIRMED', 'PROCESSING'].includes(data.status);
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 에러 응답 분석
  if (response.status >= 400) {
    console.log(`주문 실패 - User: ${userId}, Product: ${productId}, Status: ${response.status}, Body: ${response.body}`);
  }
  
  // 주문 후 확인하는 시간
  sleep(Math.random() * 2 + 1); // 1-3초
}

export function setup() {
  console.log('=== 주문 생성 API 부하 테스트 시작 ===');
  console.log(`대상 API: POST ${BASE_URL}/api/orders/{userId}`);
  console.log(`테스트 사용자 범위: 1-${USER_IDS.length}`);
  console.log(`테스트 상품 범위: 1-${PRODUCT_IDS.length}`);
  console.log('동시성 제어 및 트랜잭션 안정성 검증');
  console.log('=======================================');
}

export function teardown(data) {
  console.log('=== 주문 생성 테스트 완료 ===');
  console.log('주문 처리 성능 및 동시성 제어 결과를 확인하세요.');
}