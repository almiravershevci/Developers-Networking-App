# Backend review — DevConnect

Professional audit of the **backend stack** (Node REST BFF, Cloud Functions, Firestore rules, ops).  
**Frontend (Android) is intentionally out of scope** — mobile uses Firestore SDK + Firebase Auth directly.

---

## Architecture score: **A-** (course / portfolio → production-ready with Blaze deploy)

```
┌─────────────────────────────────────────────────────────────┐
│  Android app (unchanged)                                     │
│  Firebase Auth · Firestore SDK · FCM                         │
└───────────────────────────┬─────────────────────────────────┘
                            │
         ┌──────────────────┼──────────────────┐
         ▼                  ▼                  ▼
   Firestore           Cloud Functions      Node REST BFF
   (source of truth)   (automation)         (analytics/admin)
```

---

## Layer 1 — Node REST BFF (`backend/`)

| Capability | Status | Location |
|------------|--------|----------|
| Firebase JWT auth | ✅ | `middleware/firebaseAuth.js` |
| Admin role gate | ✅ | `middleware/requireAdmin.js` |
| API versioning `/api/v1` | ✅ | `lib/mountApi.js` + legacy `/api` alias |
| OpenAPI 3 + Swagger UI | ✅ | `openapi.yaml`, `/docs` |
| Zod validation | ✅ | `middleware/validate.js` |
| Cursor pagination | ✅ | `lib/pagination.js` — inbox, projects |
| Rate limiting | ✅ | `middleware/rateLimit.js` |
| Structured JSON logs | ✅ | `lib/logger.js`, `x-request-id` |
| Central error handler | ✅ | `middleware/errorHandler.js`, `ApiError` |
| Async route wrapper | ✅ | `lib/asyncHandler.js` |
| Authz mirrors Firestore rules | ✅ | `lib/authorization.js` |
| Deep health check | ✅ | `/health` — Firestore + Auth |
| Cloud secrets bootstrap | ✅ | `lib/bootstrapCredentials.js` |
| Docker + Render blueprint | ✅ | `Dockerfile`, `render.yaml` |
| Jest + supertest (16 tests) | ✅ | `backend/tests/` |

**Removed (dead code):** Mongoose models, `userRoutes.js`, JWT `auth.js`.

**Android contract:** Legacy paths `GET /api/dashboard/stats` and `GET /api/projects` remain via `/api` alias — no app changes required.

---

## Layer 2 — Cloud Functions (`functions/`)

| Function | Trigger | Purpose |
|----------|---------|---------|
| `onUserCreate` | Auth signup | userStats, welcome inbox, team access |
| `onTaskUpdated` | Task column change | inbox, activity, openTasksCount |
| `onMessageCreated` | New chat message | inbox, conversation preview |
| `onInboxCreated` | Inbox row | FCM push |
| `onEventRegistrationCreated/Deleted` | RSVP | participantCount |

**Blocker:** Requires Firebase **Blaze** plan to deploy.

---

## Layer 3 — Firestore (`firestore.rules`, indexes, scripts)

| Item | Status |
|------|--------|
| Security rules (11 collections) | ✅ Deployed |
| Composite indexes | ✅ Deployed |
| Rules unit tests | ✅ `firestore/tests/rules.test.js` |
| Team onboarding script | ✅ `npm run team:sync-access` |
| Seed data | ✅ `npm run seed` |
| Backup runbook | ✅ `docs/FIRESTORE_BACKUPS.md` |

---

## Layer 4 — CI/CD (`.github/workflows/`)

| Workflow | Runs |
|----------|------|
| `ci.yml` | Backend tests, Functions build, rules tests, Android assembleDebug |
| `deploy-firebase.yml` | Manual deploy (needs `FIREBASE_SERVICE_ACCOUNT_JSON`) |

---

## Error envelope (consistent)

```json
{
  "error": "validation_error",
  "message": "Request validation failed.",
  "requestId": "uuid",
  "details": [{ "path": "displayName", "message": "..." }]
}
```

---

## Local commands

```powershell
cd backend
npm install
npm test          # 20 tests
npm start         # http://localhost:5000/docs

cd functions
npm run build

cd firestore
npm run team:sync-access
```

---

## Remaining manual steps (team lead)

1. **Upgrade Blaze** → `firebase deploy --only functions`
2. **Optional:** Render deploy (`render.yaml`) — app works without it
3. **Optional:** GitHub secret for CI deploy workflow
4. **Optional:** Scheduled Firestore backup (`scripts/firestore-backup.ps1`)

---

## Presentation one-liner

> “We built a **Firestore-first** backend with **event-driven Cloud Functions**, a **versioned OpenAPI REST BFF** with validation and pagination, **security rules tests**, and **CI/CD** — Android stays on the realtime SDK; REST is the analytics and admin layer.”

---

## Related docs

- [API_HYBRID_ARCHITECTURE.md](./API_HYBRID_ARCHITECTURE.md)
- [PRODUCTION_DEPLOYMENT.md](./PRODUCTION_DEPLOYMENT.md)
- [API_DESIGN.md](./API_DESIGN.md)
- [backend/README.md](../backend/README.md)
