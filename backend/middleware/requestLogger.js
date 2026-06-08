const crypto = require('crypto');
const { logger } = require('../lib/logger');

function requestLogger(req, res, next) {
  const requestId = req.header('x-request-id') || crypto.randomUUID();
  req.requestId = requestId;
  res.setHeader('x-request-id', requestId);

  const started = Date.now();
  res.on('finish', () => {
    logger.info('http_request', {
      requestId,
      method: req.method,
      path: req.originalUrl,
      status: res.statusCode,
      durationMs: Date.now() - started,
      uid: req.user?.uid || null,
    });
  });

  next();
}

module.exports = { requestLogger };
