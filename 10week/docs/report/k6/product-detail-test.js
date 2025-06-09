import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 상품 조회 API 전용 테스트
export const options = {
  stages: [
    { duration: '30s', target: 10 },  // 워밍업
    { duration: '2m', target: 50 },   // 점진적 증가
    { duration: '3m', target: 100 },  // 피크 부하
    { duration: '1m', target: 150 },  // 스파이크
    { duration: '30s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<1500'], // 상품 조회는 빨라야 함
    http_req_failed: ['rate<0.02'],    // 에러율 2% 이하
    'product_detail_success_rate': ['rate>0.98'],
  },
};

// 커스텀 메트릭
const productDetailSuccessRate = new Rate('product_detail_success_rate');
const cacheHitRate = new Rate('cache_hit_rate');

const BASE_URL = 'http://localhost:8080';
const PRODUCT_IDS = Array.from({length: 100}, (_, i) => i + 1); // 1-100 상품

function getRandomProductId() {
  return PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
}

export default function() {
  const productId = getRandomProductId();
  
  const response = http.get(`${BASE_URL}/api/products/${productId}`, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'K6-ProductTest/1.0',
    },
    timeout: '10s',
  });
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200': (r) => r.status === 200,
    '응답시간 1.5초 이하': (r) => r.timings.duration < 1500,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
    'Content-Type이 JSON': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  productDetailSuccessRate.add(isSuccess);
  
  // 캐시 히트 확인 (X-Cache 헤더 등으로)
  if (response.headers['X-Cache-Status']) {
    cacheHitRate.add(response.headers['X-Cache-Status'] === 'HIT');
  }
  
  // 응답 데이터 검증
  if (response.status === 200) {
    const validData = check(response, {
      '상품 ID 일치': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.id === productId;
        } catch (e) {
          return false;
        }
      },
      '필수 필드 존재': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.name && data.price !== undefined && data.stock !== undefined;
        } catch (e) {
          return false;
        }
      },
      '가격이 양수': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.price > 0;
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 실제 사용자처럼 상품 정보를 읽는 시간
  sleep(Math.random() * 2 + 0.5); // 0.5-2.5초
}

export function setup() {
  console.log('=== 상품 조회 API 부하 테스트 시작 ===');
  console.log(`대상 API: GET ${BASE_URL}/api/products/{id}`);
  console.log(`테스트 상품 범위: 1-${PRODUCT_IDS.length}`);
  console.log('========================================');
}