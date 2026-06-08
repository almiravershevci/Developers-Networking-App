# DevConnect REST API

Node **BFF / analytics microservice** over the same Firestore database as the Android app.  
Auth: **Firebase ID token** (`Authorization: Bearer …`) — works for every teammate.

## Quick start

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
$env:FIREBASE_PROJECT_ID="developers-networking-app"
cd backend
npm install
npm start
```

Open http://localhost:5000/ for the route list.  
Health: http://localhost:5000/health (includes Firestore connectivity check).

## Tests

```powershell
cd backend
npm test
```

Uses Jest + supertest with mocked Firestore — no service account required for CI.

## Production deploy

See [docs/PRODUCTION_DEPLOYMENT.md](../docs/PRODUCTION_DEPLOYMENT.md) for Render/Railway, Docker, and GitHub Actions.

## Authorization

The Admin SDK **bypasses Firestore security rules**. REST handlers enforce the same policies in code via `lib/authorization.js` (mirrors `canReadProjectDoc` in `firestore.rules`).

## Implemented endpoints (aligned with Android repositories)

| Method | Path | Frontend feature |
|--------|------|------------------|
| GET | `/api/me` | Profile |
| PATCH | `/api/me` | Profile update |
| GET | `/api/dashboard/stats` | Dashboard stats |
| GET | `/api/projects` | Search / dashboard |
| GET | `/api/projects/:id` | Project detail |
| GET | `/api/projects/:id/tasks` | Tasks / Kanban |
| GET | `/api/events` | Events feed |
| POST/DELETE | `/api/events/:id/registrations/me` | Event RSVP |
| GET | `/api/inbox` | Alerts |
| PATCH | `/api/inbox/:id/read` | Mark notification read |
| GET/POST | `/api/match-requests/*` | Match invites |
| GET/POST | `/api/conversations/*` | Chat (BFF snapshot) |
| POST | `/api/admin/inbox/broadcast` | Admin broadcast |

**Not REST (by design):** register/login → Firebase Auth SDK on mobile.

Legacy MongoDB routes were removed — Firestore is the only data store.

## Test with curl

```bash
curl -H "Authorization: Bearer YOUR_FIREBASE_ID_TOKEN" http://localhost:5000/api/me
```
