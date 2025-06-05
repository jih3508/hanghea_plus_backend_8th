import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 포인트 조회 API 전용 테스트
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // 워밍업
    { duration: '2m', target: 80 },   // 정상 부하
    { duration: '3m', target: 150 },  // 높은 부하
    { duration: '1m', target: 200 },  // 스파이크
    { duration: '30s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<800'],  // 포인트 조회는 빨라야 함
    http_req_failed: ['rate<0.01'],    // 에러율 1% 이하
    'point_query_success_rate': ['rate>0.99'],
  },
};

// 커스텀 메트릭
const pointQuerySuccessRate = new Rate('point_query_success_rate');
const dbQueryTime = new Trend('db_query_time');

const BASE_URL = 'http://localhost:8080';
const USER_IDS = Array.from({length: 100}, (_, i) => i + 1); // 1-100 사용자

function getRandomUserId() {
  return USER_IDS[Math.floor(Math.random() * USER_IDS.length)];
}

export default function() {
  const userId = getRandomUserId();
  
  const response = http.get(`${BASE_URL}/api/point/${userId}`, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'K6-PointQueryTest/1.0',
    },
    timeout: '5s',
  });
  
  // 기본 검증
  const isSuccess = check(response, {
    '상태코드가 200': (r) => r.status === 200,
    '응답시간 800ms 이하': (r) => r.timings.duration < 800,
    '응답 본문 존재': (r) => r.body && r.body.length > 0,
    'Content-Type이 JSON': (r) => r.headers['Content-Type'] && r.headers['Content-Type'].includes('application/json'),
  });
  
  pointQuerySuccessRate.add(isSuccess);
  dbQueryTime.add(response.timings.duration);
  
  // 응답 데이터 검증
  if (response.status === 200) {
    const validPoint = check(response, {
      '포인트 값이 숫자': (r) => {
        try {
          const data = JSON.parse(r.body);
          return typeof data.point === 'number';
        } catch (e) {
          return false;
        }
      },
      '포인트 값이 0 이상': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.point >= 0;
        } catch (e) {
          return false;
        }
      },
      '사용자 ID 일치': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.userId === userId;
        } catch (e) {
          return false;
        }
      },
      '마지막 업데이트 시간': (r) => {
        try {
          const data = JSON.parse(r.body);
          return data.lastUpdated !== undefined;
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  // 포인트 확인 후 잠시 대기 (실제 사용자 패턴)
  sleep(Math.random() * 0.5 + 0.1); // 0.1-0.6초
}

export function setup() {
  console.log('=== 포인트 조회 API 부하 테스트 시작 ===');
  console.log(`대상 API: GET ${BASE_URL}/api/point/{userId}`);
  console.log(`테스트 사용자 범위: 1-${USER_IDS.length}`);
  console.log('빠른 조회 성능 및 데이터 정합성 검증');
  console.log('====================================');
}