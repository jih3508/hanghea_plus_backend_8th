import { check, group, sleep } from 'k6';
import http from 'k6/http';
import { Rate, Trend, Counter } from 'k6/metrics';

export const options = {
    stages: [
        { duration: '30s', target: 20 },
        { duration: '1m30s', target: 10 },
        { duration: '20s', target: 0 },
    ],

}

export default function () {
    http.get('https://test.k6.io');
    sleep(1);
}