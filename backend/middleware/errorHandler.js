const { sendError } = require('../lib/errors');

function notFoundHandler(_req, res) {
  sendError(res, 404, 'Route not found.', 'not_found');
}

function errorHandler(err, req, res, _next) {
  if (err.message === 'Not allowed by CORS') {
    return sendError(res, 403, 'Origin not allowed.', 'cors_forbidden');
  }

  console.error(
    JSON.stringify({
      level: 'error',
      requestId: req.requestId || null,
      message: err.message,
      stack: process.env.NODE_ENV === 'production' ? undefined : err.stack,
    }),
  );

  sendError(res, 500, 'Internal server error.', 'internal_error');
}

module.exports = { notFoundHandler, errorHandler };
