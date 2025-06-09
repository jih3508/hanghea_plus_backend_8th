import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 쿠폰 조회 API 전용 테스트
export const options = {
  stages: [
    { duration: '30s', target: 15 },  // 워밍업
    { duration: '2m', target: 60 },   // 정상 부하
    { duration: '3m', target: 120 },  // 높은 부하
    { duration: '1m', target: 180 },  // 스파이크
    { duration: '30s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<1200'], // 쿠폰 조회 1.2초 이하
    http_req_failed: ['rate<0.02'],    // 에러율 2% 이하
    'coupon_query_success_rate': ['rate>0.98'],
  },
};

// 커스텀 메트릭
const couponQuerySuccessRate = new Rate('coupon_query_success_rate');
const personalizedDataTime = new Trend('personalized_data_time');

const BASE_URL = 'http://localhost:8080';
const USER_IDS = Array.from({length: 80}, (_, i) => i + 1); // 1-80 사용자

function getRandomUserId() {
  return USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
}

export default function() {
  const userId = getRandomUserId();
  
  const response = http.get(`${BASE_URL}/api/coupons/${userId}`, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'K6-CouponQueryTest/1.0',
    },
    timeout: '8s',
  });
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200': (r) => r.status === 200,
    '응답시간 1.2초 이하': (r) => r.timings.duration < 1200,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
    'Content-Type이 JSON': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  couponQuerySuccessRate.add(isSuccess);
  personalizedDataTime.add(response.timings.duration);
  
  // 응답 데이터 검증
  if (response.status === 200) {
    const validCoupons = check(response, {
      '배열 형태 응답': (r) => {
        try {
          const data = JSON.parse(r.body);
          return Array.isArray(data);
        } catch (e) {
          return false;
        }
      },
      '쿠폰 기본 정보 포함': (r) => {
        try {
          const data = JSON.parse(r.body);
          if (data.length === 0) return true; // 빈 배열도 유효
          
          return data.every(coupon => 
            coupon.id && coupon.name && coupon.discountType && coupon.discountValue !== undefined
          );
        } catch (e) {
          return false;
        }
      },
      '유효 기간 정보': (r) => {
        try {
          const data = JSON.parse(r.body);
          if (data.length === 0) return true;
          
          return data.every(coupon => 
            coupon.validFrom && coupon.validTo
          );
        } catch (e) {
          return false;
        }
      },
      '사용 가능한 쿠폰만': (r) => {
        try {
          const data = JSON.parse(r.body);
          if (data.length === 0) return true;
          
          return data.every(coupon => 
            coupon.isUsed === false && coupon.isExpired === false
          );
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 쿠폰 목록을 확인하는 시간
  sleep(Math.random() * 1.5 + 0.3); // 0.3-1.8초
}

export function setup() {
  console.log('=== 쿠폰 조회 API 부하 테스트 시작 ===');
  console.log(`대상 API: GET ${BASE_URL}/api/coupons/{userId}`);
  console.log(`테스트 사용자 범위: 1-${USER_IDS.length}`);
  console.log('개인화 데이터 조회 성능 검증');
  console.log('==============================');
}