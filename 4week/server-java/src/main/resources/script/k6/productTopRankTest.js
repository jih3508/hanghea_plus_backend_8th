// 상품 랭킹 조회 K6 테스트 스크립트
import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export let options = {
    vus: 200,  // 200명의 가상 사용자
    duration: '30s',  // 30초 테스트
};

export default function() {
    let res = http.get('${BASE_URL}/api/products/top');

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 100ms': (r) => r.timings.duration < 100
    });

    sleep(0.1);
}