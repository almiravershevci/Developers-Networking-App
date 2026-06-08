# API changelog

All public HTTP changes are documented here. The stable surface is **`/api/v1/*`**.

## Policy

- **Breaking changes** only ship in a new major version (`/api/v2`).
- Legacy **`/api/*`** routes remain as aliases until **2026-11-01** (see `Sunset` response header).
- Android clients may keep Firestore SDK paths; REST changes are optional.

## v1.0.0 — 2026-06-08

### Added
- Versioned REST BFF at `/api/v1`
- OpenAPI 3 spec + Swagger UI at `/docs`
- Cursor pagination on `GET /inbox`, `GET /projects`
- Prometheus metrics at `GET /metrics`
- `Idempotency-Key` support on POST write routes
- Weak `ETag` on `GET /projects`, `GET /inbox`
- Admin audit log entries on broadcast
- Redis-backed rate limiting when `REDIS_URL` is set

### Deprecated
- Unversioned `/api/*` prefix (use `/api/v1/*`)

### Security
- Helmet security headers on Node BFF
- BFF authorization mirrors Firestore rules for project reads
