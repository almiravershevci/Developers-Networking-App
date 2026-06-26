const crypto = require('crypto');

function requestLogger(req, res, next) {
  const requestId = req.header('x-request-id') || crypto.randomUUID();
  req.requestId = requestId;
  res.setHeader('x-request-id', requestId);

  const started = Date.now();
  res.on('finish', () => {
    const durationMs = Date.now() - started;
    const payload = {
      requestId,
      method: req.method,
      path: req.originalUrl,
      status: res.statusCode,
      durationMs,
      uid: req.user?.uid || null,
    };
    if (res.statusCode >= 500) {
      console.error(JSON.stringify({ level: 'error', ...payload }));
      return;
    }
    console.log(JSON.stringify({ level: 'info', ...payload }));
  });

  next();
}

module.exports = { requestLogger };
