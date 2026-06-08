# Hybrid API architecture

How the Android app, Firestore SDK, Cloud Functions, and Node REST BFF work together.

---

## Principle

| Layer | Role |
|-------|------|
| **Firestore SDK (Android)** | System of record + realtime UX |
| **Cloud Functions** | Privileged automation (inbox, stats, FCM, RSVP counts) |
| **Node REST `/api/v1`** | Analytics snapshots, admin, Postman/OpenAPI contract |

---

## Mobile: SDK vs REST

| Feature | Mobile path | REST path |
|---------|-------------|-----------|
| Auth | Firebase Auth SDK | Bearer token on REST only |
| Chat | Firestore listeners | `GET/POST /api/v1/conversations/*` (snapshot/BFF) |
| Tasks / Kanban | Firestore writes | `GET /api/v1/projects/:id/tasks` |
| Match requests | Firestore realtime | `GET/POST /api/v1/match-requests/*` (duplicate OK for admin/tools) |
| Inbox / alerts | Firestore listeners | `GET /api/v1/inbox` (paginated snapshot) |
| Dashboard stats | Firestore `userStats` + optional REST overlay | `GET /api/v1/dashboard/stats` |
| Events RSVP | Firestore subcollection | `POST/DELETE .../registrations/me` |

**Rule:** User-facing screens use **Firestore** for live data. REST is for aggregates, integrations, and documentation — not a replacement for realtime listeners.

---

## userStats sync

`userStats/{uid}` counters are **server-maintained**:

- `openTasksCount` — `onTaskUpdated` Cloud Function
- Welcome inbox — `onUserCreate`
- Inbox rows — Functions / admin only (rules block client create)

REST `GET /api/v1/dashboard/stats` **reads** the same documents the app reads via SDK.

**Requires:** Cloud Functions deployed (Firebase Blaze).

---

## API versioning

- **Current:** `/api/v1/*`
- **Legacy alias:** `/api/*` (deprecated, same handlers)
- **Contract:** `backend/openapi.yaml` + Swagger UI at `/docs`

---

## Secrets management

| Environment | Credential source |
|-------------|-------------------|
| Local dev | `GOOGLE_APPLICATION_CREDENTIALS` file path |
| Render / Railway | `GOOGLE_APPLICATION_CREDENTIALS_JSON` env var (full JSON) |
| GitHub Actions | `FIREBASE_SERVICE_ACCOUNT_JSON` secret |
| Android app | Never holds service account — only Firebase user ID tokens |

See `backend/lib/bootstrapCredentials.js`.

---

## Postman import

1. Start API: `cd backend && npm start`
2. Open http://localhost:5000/openapi.yaml
3. Postman → Import → Link or file
4. Set collection auth: Bearer Token = Firebase ID token

---

## Related docs

- [API_DESIGN.md](./API_DESIGN.md) — endpoint inventory
- [PRODUCTION_DEPLOYMENT.md](./PRODUCTION_DEPLOYMENT.md) — deploy runbook
- [FIRESTORE_BACKUPS.md](./FIRESTORE_BACKUPS.md) — scheduled exports
- [DASHBOARD_FEED.md](./DASHBOARD_FEED.md) — demo feed vs real data
