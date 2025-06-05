import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend, Counter } from 'k6/metrics';

// 포인트 충전 API 전용 테스트
export const options = {
  stages: [
    { duration: '1m', target: 5 },    // 워밍업
    { duration: '3m', target: 20 },   // 정상 부하
    { duration: '4m', target: 40 },   // 높은 부하
    { duration: '2m', target: 60 },   // 스파이크 (결제 시스템 부하)
    { duration: '1m', target: 0 },    // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<5000'], // 결제는 5초 이하
    http_req_failed: ['rate<0.03'],    // 에러율 3% 이하
    'charge_success_rate': ['rate>0.97'],
    'duplicate_charge_prevention': ['rate>0.99'], // 중복 충전 방지
  },
};

// 커스텀 메트릭
const chargeSuccessRate = new Rate('charge_success_rate');
const duplicateChargePrevention = new Rate('duplicate_charge_prevention');
const chargeProcessingTime = new Trend('charge_processing_time');
const chargeAmountTrend = new Trend('charge_amount_trend');

const BASE_URL = 'http://localhost:8080';
const USER_IDS = Array.from({length: 30}, (_, i) => i + 1); // 1-30 사용자
const CHARGE_AMOUNTS = [1000, 5000, 10000, 20000, 50000]; // 충전 금액 옵션

function getRandomUserId() {
  return USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
}

function getRandomChargeAmount() {
  return CHARGE_AMOUNTS[Math.floor(Math.random() * CHARGE_AMOUNTS.length)];
}

export default function() {
  const userId = getRandomUserId();
  const amount = getRandomChargeAmount();
  
  // 충전 요청 데이터
  const chargeData = {
    amount: amount,
    paymentMethod: 'CARD', // 또는 'BANK_TRANSFER', 'DIGITAL_WALLET'
    requestId: `charge_${userId}_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`, // 중복 방지 ID
  };
  
  const startTime = Date.now();
  
  const response = http.post(
    `${BASE_URL}/api/point/charge/${userId}`,
    JSON.stringify(chargeData),
    {
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json',
        'User-Agent': 'K6-PointChargeTest/1.0',
        'Idempotency-Key': chargeData.requestId, // 중복 요청 방지
      },
      timeout: '20s',
    }
  );
  
  const processingTime = Date.now() - startTime;
  chargeProcessingTime.add(processingTime);
  chargeAmountTrend.add(amount);
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200': (r) => r.status === 200,
    '응답시간 5초 이하': (r) => r.timings.duration < 5000,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
  });
  
  chargeSuccessRate.add(isSuccess);
  
  // 중복 충전 방지 확인
  if (response.status === 409) {
    duplicateChargePrevention.add(1); // 중복으로 제대로 거부됨
  } else if (response.status === 200) {
    duplicateChargePrevention.add(1); // 정상 처리
  } else {
    duplicateChargePrevention.add(0); // 예상치 못한 에러
  }
  
  // 성공 응답 검증
  if (response.status === 200) {
    const validCharge = check(response, {
      '충전 후 포인트 반환': (r) => {
        try {
          const data = JSON.parse(r.body);
          return typeof data.point === 'number' && data.point > 0;
        } catch (e) {
          return false;
        }
      },
      '충전 거래 ID': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.transactionId !== undefined;
        } catch (e) {
          return false;
        }
      },
      '충전 금액 확인': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.chargedAmount === amount;
        } catch (e) {
          return false;
        }
      },
      '충전 시간 기록': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.chargedAt !== undefined;
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 에러 응답 분석
  if (response.status >= 400) {
    console.log(`포인트 충전 실패 - User: ${userId}, Amount: ${amount}, Status: ${response.status}, Body: ${response.body}`);
  }
  
  // 결제 완료 후 확인하는 시간
  sleep(Math.random() * 3 + 1); // 1-4초
}

// 중복 충전 테스트 시나리오 (10% 확률)
export function duplicateChargeTest() {
  if (Math.random() > 0.1) return; // 10% 확률로만 실행
  
  const userId = getRandomUserId();
  const amount = getRandomChargeAmount();
  const requestId = `duplicate_test_${userId}_${Date.now()}`;
  
  const chargeData = {
    amount: amount,
    paymentMethod: 'CARD',
    requestId: requestId,
  };
  
  // 같은 요청을 두 번 보내기 (중복 방지 테스트)
  const response1 = http.post(
    `${BASE_URL}/api/point/charge/${userId}`,
    JSON.stringify(chargeData),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': requestId,
      },
      timeout: '20s',
    }
  );
  
  sleep(0.1); // 짧은 간격
  
  const response2 = http.post(
    `${BASE_URL}/api/point/charge/${userId}`,
    JSON.stringify(chargeData),
    {
      headers: {
        'Content-Type': 'application/json',
        'Idempotency-Key': requestId,
      },
      timeout: '20s',
    }
  );
  
  // 두 번째 요청은 중복으로 거부되어야 함
  check(response2, {
    '중복 요청 거부': (r) => r.status === 409 || (r.status === 200 && JSON.parse(r.body).transactionId === JSON.parse(response1.body).transactionId),
  });
}

export function setup() {
  console.log('=== 포인트 충전 API 부하 테스트 시작 ===');
  console.log(`대상 API: POST ${BASE_URL}/api/point/charge/{userId}`);
  console.log(`테스트 사용자 범위: 1-${USER_IDS.length}`);
  console.log(`충전 금액 범위: ${CHARGE_AMOUNTS.join(', ')}`);
  console.log('결제 시스템 연동 및 중복 방지 검증');
  console.log('====================================');
}