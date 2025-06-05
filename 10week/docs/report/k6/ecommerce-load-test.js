import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { Rate, Trend, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const orderDuration = new Trend('order_duration', true);
const productViewDuration = new Trend('product_view_duration', true);
const pointChargeDuration = new Trend('point_charge_duration', true);
const couponIssueDuration = new Trend('coupon_issue_duration', true);
const apiCallCounter = new Counter('api_calls_total');

// Configuration
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const SCENARIO = __ENV.SCENARIO || 'baseline';

// Test data - 실제 데이터베이스와 동기화
const users = Array.from({length: 1000}, (_, i) => i + 1);  // 1-1000 사용자 ID
const products = Array.from({length: 100}, (_, i) => i + 1); // 1-100 상품 ID
const coupons = Array.from({length: 10}, (_, i) => i + 1);   // 1-10 쿠폰 ID

// 카테고리별 상품 ID 범위 (실제 데이터와 매칭)
const productCategories = {
    FOOD: Array.from({length: 40}, (_, i) => i + 1),              // 1-40: FOOD
    ELECTRONIC_DEVICES: Array.from({length: 35}, (_, i) => i + 41), // 41-75: ELECTRONIC_DEVICES  
    ETC: Array.from({length: 25}, (_, i) => i + 76)               // 76-100: ETC
};

// 실제 사용 가능한 쿠폰 번호들
const availableCoupons = [
    'WELCOME5000', 'FLAT10000', 'NEWBIE3000', 'SPECIAL20000', 'SUMMER15000',
    'RATE10', 'RATE20', 'RATE15', 'VIP30', 'PREMIUM25'
];

// Scenario configurations
const scenarios = {
  baseline: {
    executor: 'constant-vus',
    vus: 50,
    duration: '5m',
    tags: { scenario: 'baseline' }
  },
  peak: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 200 },
      { duration: '6m', target: 200 },
      { duration: '2m', target: 0 },
    ],
    tags: { scenario: 'peak' }
  },
  spike: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '30s', target: 500 },
      { duration: '2m', target: 500 },
      { duration: '30s', target: 0 },
    ],
    tags: { scenario: 'spike' }
  },
  stress: {
    executor: 'ramping-vus',
    startVUs: 0,
    stages: [
      { duration: '2m', target: 100 },
      { duration: '2m', target: 200 },
      { duration: '2m', target: 300 },
      { duration: '2m', target: 400 },
      { duration: '2m', target: 500 },
      { duration: '3m', target: 1000 },
      { duration: '2m', target: 0 },
    ],
    tags: { scenario: 'stress' }
  }
};

export let options = {
  scenarios: {
    [SCENARIO]: scenarios[SCENARIO]
  },
  thresholds: {
    http_req_duration: ['p(95)<1000', 'p(99)<2000'],
    http_req_failed: ['rate<0.1'],
    'errors': ['rate<0.05'],
    'product_view_duration': ['p(95)<500'],
    'order_duration': ['p(95)<2000'],
    'point_charge_duration': ['p(95)<1000'],
    'coupon_issue_duration': ['p(95)<1500'],
  }
};

// Helper functions
function getRandomUser() {
  return users[Math.floor(Math.random() * users.length)];
}

function getRandomProduct() {
  return products[Math.floor(Math.random() * products.length)];
}

function getRandomFoodProduct() {
  const foodProducts = productCategories.FOOD;
  return foodProducts[Math.floor(Math.random() * foodProducts.length)];
}

function getRandomElectronicsProduct() {
  const electronicsProducts = productCategories.ELECTRONIC_DEVICES;
  return electronicsProducts[Math.floor(Math.random() * electronicsProducts.length)];
}

function getRandomCoupon() {
  return coupons[Math.floor(Math.random() * coupons.length)];
}

function getRandomAmount() {
  return (Math.floor(Math.random() * 10) + 1) * 1000; // 1000-10000 포인트
}

function logApiCall(method, endpoint, status, duration) {
  apiCallCounter.add(1, { 
    method: method, 
    endpoint: endpoint, 
    status: status 
  });
}

// API call functions
function viewTopProducts() {
  return group('View Top Products', function() {
    const response = http.get(`${BASE_URL}/api/products/top`);
    
    const success = check(response, {
      'top products status is 200': (r) => r.status === 200,
      'top products response time < 500ms': (r) => r.timings.duration < 500,
      'top products has content': (r) => r.body.length > 0,
    });
    
    logApiCall('GET', '/api/products/top', response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function viewProductDetail(productId) {
  return group('View Product Detail', function() {
    const response = http.get(`${BASE_URL}/api/products/${productId}`);
    
    const success = check(response, {
      'product detail status is 200': (r) => r.status === 200,
      'product detail response time < 1000ms': (r) => r.timings.duration < 1000,
      'product detail has content': (r) => r.body.length > 0,
    });
    
    productViewDuration.add(response.timings.duration);
    logApiCall('GET', `/api/products/${productId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function checkUserPoint(userId) {
  return group('Check User Point', function() {
    const response = http.get(`${BASE_URL}/api/point/${userId}`);
    
    const success = check(response, {
      'point check status is 200': (r) => r.status === 200,
      'point check response time < 800ms': (r) => r.timings.duration < 800,
    });
    
    logApiCall('GET', `/api/point/${userId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function chargeUserPoint(userId, amount) {
  return group('Charge User Point', function() {
    const payload = { amount: amount };
    const params = {
      headers: { 'Content-Type': 'application/json' }
    };
    
    const response = http.post(
      `${BASE_URL}/api/point/charge/${userId}`,
      JSON.stringify(payload),
      params
    );
    
    const success = check(response, {
      'point charge status is 200': (r) => r.status === 200,
      'point charge response time < 1000ms': (r) => r.timings.duration < 1000,
    });
    
    pointChargeDuration.add(response.timings.duration);
    logApiCall('POST', `/api/point/charge/${userId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function getUserCoupons(userId) {
  return group('Get User Coupons', function() {
    const response = http.get(`${BASE_URL}/api/coupons/${userId}`);
    
    const success = check(response, {
      'get coupons status is 200': (r) => r.status === 200,
      'get coupons response time < 800ms': (r) => r.timings.duration < 800,
    });
    
    logApiCall('GET', `/api/coupons/${userId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function issueCoupon(userId, couponId) {
  return group('Issue Coupon', function() {
    const payload = { couponId: couponId };
    const params = {
      headers: { 'Content-Type': 'application/json' }
    };
    
    const response = http.post(
      `${BASE_URL}/api/coupons/issue/${userId}`,
      JSON.stringify(payload),
      params
    );
    
    const success = check(response, {
      'coupon issue status is 200': (r) => r.status === 200,
      'coupon issue response time < 1500ms': (r) => r.timings.duration < 1500,
    });
    
    couponIssueDuration.add(response.timings.duration);
    logApiCall('POST', `/api/coupons/issue/${userId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

function placeOrder(userId, productId, quantity = 1, couponId = null) {
  return group('Place Order', function() {
    const payload = {
      items: [{ productId: productId, quantity: quantity }],
      couponId: couponId
    };
    const params = {
      headers: { 'Content-Type': 'application/json' }
    };
    
    const response = http.post(
      `${BASE_URL}/api/orders/${userId}`,
      JSON.stringify(payload),
      params
    );
    
    const success = check(response, {
      'order status is 200': (r) => r.status === 200,
      'order response time < 3000ms': (r) => r.timings.duration < 3000,
    });
    
    orderDuration.add(response.timings.duration);
    logApiCall('POST', `/api/orders/${userId}`, response.status, response.timings.duration);
    errorRate.add(!success);
    
    return success;
  });
}

// Main test function - 실제 이커머스 사용자 행동 패턴
export default function() {
  const userId = getRandomUser();
  const productId = getRandomProduct();
  const couponId = getRandomCoupon();
  const chargeAmount = getRandomAmount();
  
  group('E-commerce User Journey', function() {
    // 1. 인기 상품 조회 (90% 확률) - 메인페이지 방문
    if (Math.random() < 0.9) {
      viewTopProducts();
      sleep(0.5);
    }
    
    // 2. 상품 상세 조회 (80% 확률) - 관심 상품 클릭
    if (Math.random() < 0.8) {
      viewProductDetail(productId);
      sleep(1);
    }
    
    // 3. 포인트 조회 (50% 확률) - 구매 전 잔액 확인
    if (Math.random() < 0.5) {
      checkUserPoint(userId);
      sleep(0.3);
    }
    
    // 4. 포인트 충전 (20% 확률) - 잔액 부족 시 충전
    if (Math.random() < 0.2) {
      chargeUserPoint(userId, chargeAmount);
      sleep(0.5);
    }
    
    // 5. 쿠폰 조회 (30% 확률) - 할인 혜택 확인
    if (Math.random() < 0.3) {
      getUserCoupons(userId);
      sleep(0.3);
    }
    
    // 6. 쿠폰 발급 (15% 확률) - 이벤트 참여
    if (Math.random() < 0.15) {
      issueCoupon(userId, couponId);
      sleep(0.5);
    }
    
    // 7. 주문 처리 (10% 확률) - 실제 구매
    if (Math.random() < 0.1) {
      const useCoupon = Math.random() < 0.3 ? couponId : null;
      placeOrder(userId, productId, 1, useCoupon);
      sleep(1);
    }
  });
  
  // Think time - 사용자가 다음 액션까지 고민하는 시간
  sleep(Math.random() * 2 + 1); // 1-3초 랜덤
}

// Setup function - 테스트 시작 전 실행
export function setup() {
  console.log(`Starting ${SCENARIO} test scenario against ${BASE_URL}`);
  
  // Health check
  try {
    const healthResponse = http.get(`${BASE_URL}/actuator/health`);
    if (healthResponse.status !== 200) {
      console.warn('Health check failed, but continuing with test...');
    }
  } catch (e) {
    console.warn('Health check endpoint not available, continuing with test...');
  }
  
  return { 
    startTime: new Date().toISOString(),
    scenario: SCENARIO 
  };
}

// Teardown function - 테스트 종료 후 실행
export function teardown(data) {
  console.log(`Test completed. Started at: ${data.startTime}, Scenario: ${data.scenario}`);
}
