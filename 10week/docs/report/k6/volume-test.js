import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';

// 볼륨 테스트 - 대용량 데이터 처리 테스트
export let options = {
  stages: [
    { duration: '1m', target: 30 },   // 워밍업
    { duration: '10m', target: 100 }, // 지속적인 대용량 처리
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<8000'], // 대용량 처리 시에도 8초 이내
    http_req_failed: ['rate<0.02'],    // 오류율 2% 이하
    data_processing_time: ['avg<5000'], // 데이터 처리 시간
    large_response_size: ['avg>1000'],  // 응답 크기 확인
  },
};

const BASE_URL = 'http://host.docker.internal:8080';

// 커스텀 메트릭
const dataProcessingTime = new Trend('data_processing_time');
const largeResponseSize = new Trend('large_response_size');
const bulkOperations = new Counter('bulk_operations');

export function setup() {
  console.log('📦 볼륨 테스트 시작 - 대용량 데이터 처리');
  
  return {
    startTime: new Date(),
    bulkUsers: generateBulkUsers(200),
    bulkProducts: generateBulkProducts(500),
    bulkOrders: generateBulkOrders(1000)
  };
}

function generateBulkUsers(count) {
  const users = [];
  for (let i = 1; i <= count; i++) {
    users.push({
      id: i,
      email: `volume_user_${i}@example.com`,
      name: `VolumeUser${i}`,
      phone: `010-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}-${String(Math.floor(Math.random() * 10000)).padStart(4, '0')}`
    });
  }
  return users;
}

function generateBulkProducts(count) {
  const products = [];
  for (let i = 1; i <= count; i++) {
    products.push({
      id: i,
      name: `VolumeProduct${i}`,
      price: Math.floor(Math.random() * 100000) + 1000,
      stock: Math.floor(Math.random() * 1000) + 10
    });
  }
  return products;
}

function generateBulkOrders(count) {
  const orders = [];
  for (let i = 1; i <= count; i++) {
    orders.push({
      id: i,
      userId: Math.floor(Math.random() * 200) + 1,
      productId: Math.floor(Math.random() * 500) + 1,
      quantity: Math.floor(Math.random() * 5) + 1
    });
  }
  return orders;
}

export default function (data) {
  // 볼륨 테스트 시나리오 실행
  if (Math.random() < 0.4) {
    largeDataQueryScenario();
  } else if (Math.random() < 0.75) {
    batchDataProcessingScenario(data);
  } else {
    largePaginationScenario();
  }
  
  sleep(Math.random() * 2 + 1);
}

function largeDataQueryScenario() {
  const startTime = new Date();
  
  let productsRes = http.get(`${BASE_URL}/api/products?page=0&size=100&sort=id,desc`);
  check(productsRes, {
    '대용량 상품 조회 성공': (r) => r.status === 200,
    '대용량 상품 조회 < 10s': (r) => r.timings.duration < 10000,
  });
  
  if (productsRes.body) {
    largeResponseSize.add(productsRes.body.length);
  }
  
  const processingTime = new Date() - startTime;
  dataProcessingTime.add(processingTime);
}

function batchDataProcessingScenario(data) {
  const startTime = new Date();
  
  const batchSize = 3;
  for (let i = 0; i < batchSize; i++) {
    const order = data.bulkOrders[Math.floor(Math.random() * data.bulkOrders.length)];
    
    let orderPayload = JSON.stringify({
      userId: order.userId,
      productId: order.productId,
      quantity: order.quantity
    });
    
    let orderRes = http.post(`${BASE_URL}/api/orders`, orderPayload, {
      headers: { 'Content-Type': 'application/json' },
    });
    
    check(orderRes, {
      '배치 주문 처리': (r) => r.status === 200 || r.status === 201 || r.status === 400 || r.status === 409,
    });
    
    bulkOperations.add(1);
    sleep(0.2);
  }
  
  const processingTime = new Date() - startTime;
  dataProcessingTime.add(processingTime);
}

function largePaginationScenario() {
  const pageNumbers = [0, 10, 50, 100];
  const pageSize = 20;
  
  pageNumbers.forEach(page => {
    let productsRes = http.get(`${BASE_URL}/api/products?page=${page}&size=${pageSize}`);
    check(productsRes, {
      [`페이지 ${page} 조회`]: (r) => r.status === 200 || r.status === 404,
    });
    sleep(0.5);
  });
}

export function teardown(data) {
  console.log('✅ 볼륨 테스트 완료');
  console.log(`⏱️ 총 테스트 시간: ${(new Date() - data.startTime) / 1000}초`);
  console.log('📊 대용량 데이터 처리 분석 완료');
}
