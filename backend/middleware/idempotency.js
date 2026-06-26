const crypto = require('crypto');

const TTL_MS = Number(process.env.IDEMPOTENCY_TTL_MS || 24 * 60 * 60 * 1000);
const store = new Map();

function purgeExpired() {
  const now = Date.now();
  for (const [key, entry] of store.entries()) {
    if (entry.expiresAt <= now) store.delete(key);
  }
}

/**
 * Idempotency-Key support for POST writes (in-memory; use Redis in multi-instance prod).
 */
function idempotencyMiddleware(req, res, next) {
  if (req.method !== 'POST') return next();

  const key = req.header('Idempotency-Key')?.trim();
  if (!key) return next();

  purgeExpired();
  const scope = `${req.user?.uid || 'anon'}:${req.method}:${req.originalUrl}:${key}`;
  const existing = store.get(scope);
  if (existing) {
    res.setHeader('Idempotency-Replayed', 'true');
    return res.status(existing.status).json(existing.body);
  }

  const originalJson = res.json.bind(res);
  res.json = (body) => {
    store.set(scope, {
      status: res.statusCode || 200,
      body,
      expiresAt: Date.now() + TTL_MS,
    });
    res.setHeader('Idempotency-Key', key);
    return originalJson(body);
  };

  next();
}

module.exports = { idempotencyMiddleware };
