const fs = require('fs');
const os = require('os');
const path = require('path');

/**
 * Cloud hosts (Render/Railway) inject credentials as JSON text, not a file path.
 * Writes a temp file once per process when GOOGLE_APPLICATION_CREDENTIALS_JSON is set.
 */
function bootstrapCredentialsFromEnv() {
  if (process.env.GOOGLE_APPLICATION_CREDENTIALS) {
    return;
  }

  const json = process.env.GOGLE_APPLICATION_CREDENTIALS_JSON;
  if (!json || !json.trim()) {
    return;
  }

  const target = path.join(os.tmpdir(), 'devconnect-firebase-admin.json');
  fs.writeFileSync(target, json, { encoding: 'utf8', mode: 0o600 });
  process.env.GOOGLE_APPLICATION_CREDENTIALS = target;
}

bootstrapCredentialsFromEnv();

module.exports = { bootstrapCredentialsFromEnv };
