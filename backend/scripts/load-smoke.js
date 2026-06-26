#!/usr/bin/env node
/**
 * Lightweight load smoke test — run: node backend/scripts/load-smoke.js [baseUrl]
 */
const base = process.argv[2] || 'http://127.0.0.1:5000';

async function hit(path) {
  const started = Date.now();
  const res = await fetch(`${base}${path}`);
  return { path, status: res.status, ms: Date.now() - started };
}

async function main() {
  const paths = ['/', '/health', '/metrics'];
  const results = await Promise.all(paths.map(hit));
  console.table(results);
  const failed = results.filter((r) => r.status >= 500);
  process.exit(failed.length ? 1 : 0);
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
