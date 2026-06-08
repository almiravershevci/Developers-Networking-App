const crypto = require('crypto');

/**
 * Weak ETag for cacheable GET JSON responses.
 */
function attachWeakEtag(res, payload) {
  const body = typeof payload === 'string' ? payload : JSON.stringify(payload);
  const tag = `"${crypto.createHash('sha1').update(body).digest('hex')}"`;
  res.setHeader('ETag', tag);
  return tag;
}

function sendJsonWithEtag(req, res, payload, status = 200) {
  const body = JSON.stringify(payload);
  const tag = attachWeakEtag(res, body);
  if (req.headers['if-none-match'] === tag) {
    res.status(304).end();
    return;
  }
  res.status(status).type('json').send(body);
}

module.exports = { attachWeakEtag, sendJsonWithEtag };
