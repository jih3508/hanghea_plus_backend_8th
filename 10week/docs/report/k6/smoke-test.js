import http from 'k6/http';
import { check, sleep } from 'k6';

// 스모크 테스트 - 기본 기능 동작 확인
export let options = {
  vus: 1, // 1 virtual user
  duration: '30s',
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% of requests must complete below 1s
    http_req_failed: ['rate<0.01'], // error rate must be below 1%
  },
};

const BASE_URL = 'http://host.docker.internal:8080';

export function setup() {
  console.log('🔥 스모크 테스트 시작 - 기본 기능 확인');
}

export default function () {
  // 1. 헬스체크
  let healthCheck = http.get(`${BASE_URL}/actuator/health`);
  check(healthCheck, {
    '헬스체크 성공': (r) => r.status === 200,
    '헬스체크 응답시간 < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);

  // 2. Swagger UI 접근
  let swagger = http.get(`${BASE_URL}/swagger-ui/index.html`);
  check(swagger, {
    'Swagger UI 접근 성공': (r) => r.status === 200,
  });

  sleep(1);

  // 3. 애플리케이션 정보 확인
  let info = http.get(`${BASE_URL}/actuator/info`);
  check(info, {
    '애플리케이션 정보 조회 성공': (r) => r.status === 200,
  });

  sleep(2);
}

export function teardown() {
  console.log('✅ 스모크 테스트 완료');
}
