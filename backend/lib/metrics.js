const STARTED_AT = Date.now();

const counters = {
  httpRequestsTotal: 0,
  httpErrorsTotal: 0,
};

const histogram = new Map();

function recordRequest(method, path, status, durationMs) {
  counters.httpRequestsTotal += 1;
  if (status >= 500) counters.httpErrorsTotal += 1;

  const key = `${method} ${normalizePath(path)}`;
  const bucket = histogram.get(key) || { count: 0, totalMs: 0, maxMs: 0 };
  bucket.count += 1;
  bucket.totalMs += durationMs;
  bucket.maxMs = Math.max(bucket.maxMs, durationMs);
  histogram.set(key, bucket);
}

function normalizePath(path) {
  return String(path || '')
    .replace(/\/api\/v1\/projects\/[^/]+/g, '/api/v1/projects/:id')
    .replace(/\/api\/v1\/events\/[^/]+/g, '/api/v1/events/:id')
    .replace(/\/api\/v1\/inbox\/[^/]+/g, '/api/v1/inbox/:id')
    .replace(/\/api\/v1\/match-requests\/[^/]+/g, '/api/v1/match-requests/:id')
    .replace(/\/api\/v1\/conversations\/[^/]+/g, '/api/v1/conversations/:id');
}

function prometheusText() {
  const lines = [
    '# HELP devconnect_uptime_seconds Process uptime in seconds',
    '# TYPE devconnect_uptime_seconds gauge',
    `devconnect_uptime_seconds ${Math.floor((Date.now() - STARTED_AT) / 1000)}`,
    '# HELP devconnect_http_requests_total Total HTTP requests',
    '# TYPE devconnect_http_requests_total counter',
    `devconnect_http_requests_total ${counters.httpRequestsTotal}`,
    '# HELP devconnect_http_errors_total Total HTTP 5xx responses',
    '# TYPE devconnect_http_errors_total counter',
    `devconnect_http_errors_total ${counters.httpErrorsTotal}`,
  ];

  for (const [route, bucket] of histogram.entries()) {
    const label = route.replace(/"/g, '\\"');
    lines.push(
      `devconnect_http_request_duration_ms_sum{route="${label}"} ${bucket.totalMs}`,
      `devconnect_http_request_duration_ms_count{route="${label}"} ${bucket.count}`,
      `devconnect_http_request_duration_ms_max{route="${label}"} ${bucket.maxMs}`,
    );
  }

  return `${lines.join('\n')}\n`;
}

function metricsMiddleware(req, res, next) {
  const started = Date.now();
  res.on('finish', () => {
    recordRequest(req.method, req.path, res.statusCode, Date.now() - started);
  });
  next();
}

module.exports = { metricsMiddleware, prometheusText, recordRequest };
