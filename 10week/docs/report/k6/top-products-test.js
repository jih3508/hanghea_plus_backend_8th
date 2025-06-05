import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 인기 상품 API 전용 테스트
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // 워밍업
    { duration: '2m', target: 100 },  // 정상 부하
    { duration: '3m', target: 200 },  // 높은 부하 (캐시 효과 확인)
    { duration: '1m', target: 300 },  // 스파이크
    { duration: '30s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 캐시된 데이터라 빨라야 함
    http_req_failed: ['rate<0.01'],    // 에러율 1% 이하
    'top_products_success_rate': ['rate>0.99'],
  },
};

// 커스텀 메트릭
const topProductsSuccessRate = new Rate('top_products_success_rate');
const cacheEfficiency = new Rate('cache_efficiency');

const BASE_URL = 'http://localhost:8080';

export default function() {
  const response = http.get(`${BASE_URL}/api/products/top`, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'K6-TopProductsTest/1.0',
    },
    timeout: '5s',
  });
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200': (r) => r.status === 200,
    '응답시간 1초 이하': (r) => r.timings.duration < 1000,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
    'Content-Type이 JSON': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  topProductsSuccessRate.add(isSuccess);
  
  // 캐시 효율성 확인
  if (response.timings.duration < 100) {
    cacheEfficiency.add(1); // 100ms 이하면 캐시로 간주
  } else {
    cacheEfficiency.add(0);
  }
  
  // 응답 데이터 검증
  if (response.status === 200) {
    const validData = check(response, {
      '배열 형태 응답': (r) => {
        try {
          const data = JSON.parse(r.body);
          return Array.isArray(data);
        } catch (e) {
          return false;
        }
      },
      '상품 개수 적절': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.length > 0 && data.length <= 20; // 최대 20개
        } catch (e) {
          return false;
        }
      },
      '상품 필수 정보 포함': (r) => {
        try {
          const data = JSON.parse(r.body);
          if (data.length === 0) return true; // 빈 배열도 유효
          return data.every(product => 
            product.id && product.name && product.price !== undefined
          );
        } catch (e) {
          return false;
        }
      },
      '인기도 순 정렬': (r) => {
        try {
          const data = JSON.parse(r.body);
          if (data.length <= 1) return true;
          
          // 인기도 필드가 있다면 내림차순 확인
          if (data[0].popularity !== undefined) {
            for (let i = 1; i < data.length; i++) {
              if (data[i-1].popularity < data[i].popularity) {
                return false;
              }
            }
          }
          return true;
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 인기 상품을 보는 사용자 패턴 (빠르게 스캔)
  sleep(Math.random() * 1 + 0.2); // 0.2-1.2초
}

export function setup() {
  console.log('=== 인기 상품 API 부하 테스트 시작 ===');
  console.log(`대상 API: GET ${BASE_URL}/api/products/top`);
  console.log('캐시 효율성 및 동시 접근 성능 검증');
  console.log('======================================');
}