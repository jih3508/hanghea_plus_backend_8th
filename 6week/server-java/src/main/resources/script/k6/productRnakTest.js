import http from 'k6/http';
import { sleep } from 'k6';

export const options = {
    stages: [
        { duration: '5m', target: 500 },  // 점진적 증가
        { duration: '10m', target: 500 }, // 안정 부하
        { duration: '2m', target: 1000 },  // 피크 부하
        { duration: '3m', target: 0 },     // 점진적 감소
    ],
};

export default function() {
    http.get('http://localhost:8080/products/today-ranks');
    sleep(1);
}