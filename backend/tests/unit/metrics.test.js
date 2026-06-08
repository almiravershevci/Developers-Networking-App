const { prometheusText, recordRequest } = require('../../lib/metrics');

describe('metrics', () => {
  test('prometheusText exposes counters', () => {
    recordRequest('GET', '/health', 200, 12);
    const body = prometheusText();
    expect(body).toContain('devconnect_http_requests_total');
    expect(body).toContain('devconnect_uptime_seconds');
  });
});
