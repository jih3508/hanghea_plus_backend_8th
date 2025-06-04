// 빠른 테스트를 위한 간단한 K6 스크립트 예시
import { check, sleep } from 'k6';
import http from 'k6/http';

// 간단한 테스트 설정
export let options = {
  vus: 10,        // 10명의 가상 사용자
  duration: '30s', // 30초 동안 실행
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95%의 요청이 1초 이내
    http_req_failed: ['rate<0.1'],     // 오류율 10% 미만
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function() {
  // 1. 인기 상품 조회
  let response = http.get(`${BASE_URL}/api/products/top`);
  check(response, {
    'top products status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
  
  // 2. 특정 상품 조회
  let productId = Math.floor(Math.random() * 100) + 1;
  response = http.get(`${BASE_URL}/api/products/${productId}`);
  check(response, {
    'product detail status is 200': (r) => r.status === 200,
    'response time < 1000ms': (r) => r.timings.duration < 1000,
  });
  
  sleep(1);
  
  // 3. 포인트 조회
  let userId = Math.floor(Math.random() * 1000) + 1;
  response = http.get(`${BASE_URL}/api/point/${userId}`);
  check(response, {
    'point check status is 200': (r) => r.status === 200,
    'response time < 800ms': (r) => r.timings.duration < 800,
  });
  
  sleep(2); // 사용자 대기 시간
}

// 테스트 시작 전 실행
export function setup() {
  console.log('빠른 테스트 시작...');
  console.log(`대상 URL: ${BASE_URL}`);
  
  // 서버 상태 확인
  let response = http.get(`${BASE_URL}/actuator/health`);
  if (response.status !== 200) {
    console.warn('서버 상태 확인 실패, 테스트를 계속 진행합니다.');
  }
}

// 테스트 완료 후 실행
export function teardown(data) {
  console.log('빠른 테스트 완료!');
}
