const { ApiError, sendError } = require('../lib/errors');
const { logger } = require('../lib/logger');

function notFoundHandler(req, res) {
  sendError(res, 404, 'Route not found.', 'not_found', req.requestId);
}

function errorHandler(err, req, res, _next) {
  if (err instanceof ApiError) {
    return sendError(res, err.status, err.message, err.code, req.requestId);
  }

  if (err.message === 'Not allowed by CORS') {
    return sendError(res, 403, 'Origin not allowed.', 'cors_forbidden', req.requestId);
  }

  logger.error('unhandled_error', {
    requestId: req.requestId || null,
    message: err.message,
    stack: process.env.NODE_ENV === 'production' ? undefined : err.stack,
  });

  sendError(res, 500, 'Internal server error.', 'internal_error', req.requestId);
}

module.exports = { notFoundHandler, errorHandler };
