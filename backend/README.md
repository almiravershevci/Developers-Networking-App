# DevConnect REST API

Node **BFF / analytics microservice** over Firestore. Auth: Firebase ID token (`Authorization: Bearer`).

## Quick start

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
$env:FIREBASE_PROJECT_ID="developers-networking-app"
cd backend
npm install
npm start
```

| URL | Purpose |
|-----|---------|
| http://localhost:5000/docs | Swagger UI |
| http://localhost:5000/openapi.yaml | Postman import |
| http://localhost:5000/health | Deep health check |

## Tests

```powershell
npm test
```

16 unit/integration tests — no service account required.

## API version

- **Primary:** `/api/v1/*`
- **Legacy alias:** `/api/*` (Android app uses `GET /api/dashboard/stats`, `GET /api/projects`)

## Stack

| Layer | Tech |
|-------|------|
| HTTP | Express 5 |
| Auth | Firebase Admin `verifyIdToken` |
| Data | Firestore Admin SDK |
| Validation | Zod |
| Docs | OpenAPI 3 + Swagger UI |
| Tests | Jest + supertest |

## Project layout

```
backend/
├── app.js              Express factory (testable)
├── index.js            Server entry
├── openapi.yaml        API contract
├── lib/
│   ├── authorization.js   Mirrors Firestore rules
│   ├── asyncHandler.js    Async error forwarding
│   ├── pagination.js      Cursor pagination
│   ├── logger.js          Structured JSON logs
│   └── mountApi.js        Versioned route mounting
├── middleware/
│   ├── firebaseAuth.js
│   ├── requireAdmin.js
│   ├── validate.js        Zod schemas
│   ├── rateLimit.js
│   └── errorHandler.js
├── routes/             8 routers, 21 endpoints
└── tests/
```

## Docs

- [docs/BACKEND_REVIEW.md](../docs/BACKEND_REVIEW.md) — full audit
- [docs/API_HYBRID_ARCHITECTURE.md](../docs/API_HYBRID_ARCHITECTURE.md)
- [docs/PRODUCTION_DEPLOYMENT.md](../docs/PRODUCTION_DEPLOYMENT.md)
