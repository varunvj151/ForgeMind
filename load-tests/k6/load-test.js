import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 }, // simulate ramp-up of traffic from 1 to 50 users over 30 seconds.
    { duration: '1m', target: 50 },  // stay at 50 users for 1 minute
    { duration: '30s', target: 0 },  // ramp-down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
  },
};

export default function () {
  const res = http.get('http://api.forgemind.dev/actuator/health');
  check(res, {
    'is status 200': (r) => r.status === 200,
  });
  sleep(1);
}
