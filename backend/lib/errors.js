/**
 * Consistent API error envelope: { error, message, requestId? }
 */
function sendError(res, status, message, code = 'error', requestId = null) {
  const body = { error: code, message };
  if (requestId) body.requestId = requestId;
  return res.status(status).json(body);
}

function sendValidationError(res, issues, requestId = null) {
  const body = {
    error: 'validation_error',
    message: 'Request validation failed.',
    details: issues,
  };
  if (requestId) body.requestId = requestId;
  return res.status(400).json(body);
}

class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.code = code;
  }
}

module.exports = { ApiError, sendError, sendValidationError };
