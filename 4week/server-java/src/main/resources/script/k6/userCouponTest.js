// 사용자 쿠폰 조회 K6 테스트 스크립트
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export let options = {
    vus: 100,  // 100명의 가상 사용자
    duration: '30s',  // 30초 테스트
};

export default function() {
    const userId = Math.floor(Math.random() * 1000) + 1;

    let res = http.get(`${BASE_URL}/api/users/${userId}/coupons`);

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 200ms': (r) => r.timings.duration < 200
    });

    sleep(0.1);
}