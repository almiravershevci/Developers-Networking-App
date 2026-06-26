const rateLimit = require('express-rate-limit');
const { logger } = require('../lib/logger');

let redisClient = null;
let RedisStore = null;

function getRedisStore(prefix) {
  const url = process.env.REDIS_URL?.trim();
  if (!url) return undefined;

  try {
    if (!RedisStore) {
      // Optional dependency — falls back to in-memory when unavailable.
      // eslint-disable-next-line global-require
      RedisStore = require('rate-limit-redis').RedisStore;
    }
    if (!redisClient) {
      // eslint-disable-next-line global-require
      const Redis = require('ioredis');
      redisClient = new Redis(url, {
        maxRetriesPerRequest: 1,
        enableOfflineQueue: false,
      });
      redisClient.on('error', (error) => {
        logger.error('redis_rate_limit_error', { message: error.message });
      });
    }
    return new RedisStore({
      sendCommand: (...args) => redisClient.call(...args),
      prefix: `devconnect:${prefix}:`,
    });
  } catch (error) {
    logger.warn('redis_rate_limit_unavailable', { message: error.message });
    return undefined;
  }
}

function buildLimiter({ windowMs, max, prefix, message }) {
  const store = getRedisStore(prefix);
  return rateLimit({
    windowMs,
    max,
    standardHeaders: true,
    legacyHeaders: false,
    store,
    message,
  });
}

const publicLimiter = buildLimiter({
  windowMs: 15 * 60 * 1000,
  max: Number(process.env.RATE_LIMIT_PUBLIC_MAX || 120),
  prefix: 'public',
  message: {
    error: 'rate_limit_exceeded',
    message: 'Too many requests. Try again later.',
    code: 'rate_limit_exceeded',
  },
});

const writeLimiter = buildLimiter({
  windowMs: 60 * 1000,
  max: Number(process.env.RATE_LIMIT_WRITE_MAX || 30),
  prefix: 'write',
  message: {
    error: 'rate_limit_exceeded',
    message: 'Too many write requests. Try again later.',
    code: 'rate_limit_exceeded',
  },
});

module.exports = { publicLimiter, writeLimiter };
