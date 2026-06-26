const { ApiError } = require('./errors');

/**
 * Wrap async Express handlers — forwards errors to the global error handler.
 * @param {import('express').RequestHandler} handler
 */
function asyncHandler(handler) {
  return (req, res, next) => {
    Promise.resolve(handler(req, res, next)).catch(next);
  };
}

/**
 * Throw typed HTTP errors from route handlers.
 */
function httpError(status, code, message) {
  throw new ApiError(status, code, message);
}

module.exports = { asyncHandler, httpError };
