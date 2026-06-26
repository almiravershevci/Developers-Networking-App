const LEGACY_SUNSET = process.env.API_LEGACY_SUNSET || 'Sat, 01 Nov 2026 00:00:00 GMT';

function legacyApiDeprecationHeaders(req, res, next) {
  res.setHeader('Deprecation', 'true');
  res.setHeader('Sunset', LEGACY_SUNSET);
  res.setHeader('Link', '</api/v1>; rel="successor-version"');
  next();
}

module.exports = { legacyApiDeprecationHeaders };
