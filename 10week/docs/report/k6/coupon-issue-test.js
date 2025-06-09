import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 쿠폰 발급 API 전용 테스트
export const options = {
  stages: [
    { duration: '1m', target: 5 },    // 워밍업
    { duration: '2m', target: 15 },   // 정상 부하
    { duration: '3m', target: 30 },   // 높은 부하
    { duration: '2m', target: 50 },   // 스파이크 (한정 수량 테스트)
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<4000'], // 쿠폰 발급 4초 이하
    http_req_failed: ['rate<0.1'],     // 에러율 10% 이하 (재고 한정)
    'coupon_issue_success_rate': ['rate>0.8'], // 성공률 80% (재고 고려)
    'stock_management_accuracy': ['rate>0.95'], // 재고 관리 정확성
  },
};

// 커스텀 메트릭
const couponIssueSuccessRate = new Rate('coupon_issue_success_rate');
const stockManagementAccuracy = new Rate('stock_management_accuracy');
const issueProcessingTime = new Trend('issue_processing_time');
const stockOutCount = new Counter('stock_out_count');

const BASE_URL = 'http://localhost:8080';
const USER_IDS = Array.from({length: 40}, (_, i) => i + 1); // 1-40 사용자
const COUPON_TYPES = [
  { type: 'DISCOUNT', value: 1000, maxQuantity: 100 },
  { type: 'CASHBACK', value: 2000, maxQuantity: 50 },
  { type: 'FREE_SHIPPING', value: 0, maxQuantity: 200 },
  { type: 'PERCENT_DISCOUNT', value: 10, maxQuantity: 80 },
];

function getRandomUserId() {
  return USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
}

function getRandomCouponType() {
  return COUPON_TYPES[Math.floor(Math.random() * COUPON_TYPES.length)];
}

export default function() {
  const userId = getRandomUserId();
  const couponConfig = getRandomCouponType();
  
  // 쿠폰 발급 데이터
  const issueData = {
    couponType: couponConfig.type,
    value: couponConfig.value,
    requestId: `issue_${userId}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 중복 방지
  };
  
  const startTime = Date.now();
  
  const response = http.post(
    `${BASE_URL}/api/coupons/issue/${userId}`,
    JSON.stringify(issueData),
    {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'K6-CouponIssueTest/1.0',
        'Idempotency-Key': issueData.requestId,
      },
      timeout: '15s',
    }
  );
  
  const processingTime = Date.now() - startTime;
  issueProcessingTime.add(processingTime);
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200 또는 201': (r) => r.status === 200 || r.status === 201,
    '응답시간 4초 이하': (r) => r.timings.duration < 4000,
  });
  
  // 재고 부족 응답도 정상적인 처리로 간주
  if (response.status === 409 && response.body.includes('재고')) {
    stockOutCount.add(1);
    stockManagementAccuracy.add(1); // 재고 부족을 제대로 처리함
  } else if (response.status === 200 || response.status === 201) {
    couponIssueSuccessRate.add(1);
    stockManagementAccuracy.add(1);
  } else {
    couponIssueSuccessRate.add(0);
    stockManagementAccuracy.add(0);
  }
  
  // 성공 응답 검증
  if (response.status === 200 || response.status === 201) {
    const validIssue = check(response, {
      '쿠폰 ID 생성': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.couponId !== undefined && data.couponId !== null;
        } catch (e) {
          return false;
        }
      },
      '쿠폰 타입 일치': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.couponType === issueData.couponType;
        } catch (e) {
          return false;
        }
      },
      '할인 값 일치': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.discountValue === issueData.value;
        } catch (e) {
          return false;
        }
      },
      '유효 기간 설정': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.validFrom && data.validTo;
        } catch (e) {
          return false;
        }
      },
      '발급 시간 기록': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.issuedAt !== undefined;
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 재고 부족 응답 검증
  if (response.status === 409) {
    check(response, {
      '재고 부족 메시지': (r) => r.body.includes('재고') || r.body.includes('sold out') || r.body.includes('unavailable'),
    });
  }
  
  // 에러 응답 분석
  if (response.status >= 400 && response.status !== 409) {
    console.log(`쿠폰 발급 실패 - User: ${userId}, Type: ${issueData.couponType}, Status: ${response.status}, Body: ${response.body}`);
  }
  
  // 쿠폰 발급 후 확인하는 시간
  sleep(Math.random() * 2 + 0.5); // 0.5-2.5초
}

// 동시 발급 테스트 (재고 관리 검증)
export function concurrentIssueTest() {
  if (Math.random() > 0.05) return; // 5% 확률로만 실행
  
  const couponConfig = getRandomCouponType();
  const users = [getRandomUserId(), getRandomUserId(), getRandomUserId()]; // 3명이 동시에
  
  const issueData = {
    couponType: couponConfig.type,
    value: couponConfig.value,
  };
  
  // 동시에 같은 쿠폰 발급 요청
  const responses = users.map(userId => {
    return http.post(
      `${BASE_URL}/api/coupons/issue/${userId}`,
      JSON.stringify({
        ...issueData,
        requestId: `concurrent_${userId}_${Date.now()}`,
      }),
      {
        headers: {
          'Content-Type': 'application/json',
        },
        timeout: '15s',
      }
    );
  });
  
  // 재고 관리가 정확한지 확인
  const successCount = responses.filter(r => r.status === 200 || r.status === 201).length;
  const stockOutCount = responses.filter(r => r.status === 409).length;
  
  check(responses, {
    '동시 발급 처리 정확성': () => successCount + stockOutCount === responses.length,
  });
}

export function setup() {
  console.log('=== 쿠폰 발급 API 부하 테스트 시작 ===');
  console.log(`대상 API: POST ${BASE_URL}/api/coupons/issue/{userId}`);
  console.log(`테스트 사용자 범위: 1-${USER_IDS.length}`);
  console.log('쿠폰 타입:', COUPON_TYPES.map(c => c.type).join(', '));
  console.log('재고 관리 및 동시 발급 처리 검증');
  console.log('===================================');
}

export function teardown(data) {
  console.log('=== 쿠폰 발급 테스트 완료 ===');
  console.log('재고 관리 정확성 및 동시성 제어 결과를 확인하세요.');
}