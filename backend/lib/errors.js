function sendError(res, status, message, code = 'error') {
  return res.status(status).json({ error: code, message });
}

module.exports = { sendError };
