# Production deployment

Step-by-step guide for the team lead to move DevConnect from **local dev** to **production-ready** infrastructure.

---

## Prerequisites

| Requirement | Why |
|-------------|-----|
| Firebase **Blaze** plan | Cloud Functions billing |
| Service account JSON (vault only) | Node API + deploy scripts |
| GitHub repo secrets (optional) | CI deploy workflow |
| Render or Railway account | Hosted Node REST API |

---

## 1. Deploy Cloud Functions

Functions automate inbox, activity, stats, FCM push, RSVP counts, and teammate onboarding.

```powershell
cd functions
npm install
npm run build
firebase login
firebase deploy --only functions --project developers-networking-app
```

Verify in Firebase Console → **Functions** — you should see:

- `onUserCreate`
- `onTaskUpdated`
- `onMessageCreated`
- `onEventRegistrationCreated` / `onEventRegistrationDeleted`
- `onInboxCreated`

After deploy, new signups get `userStats`, welcome inbox, collaborator suggestions, and showcase project/chat access automatically.

**Fallback (before Functions deploy):**

```powershell
cd firestore
npm run team:sync-access
```

---

## 2. Deploy Firestore rules & indexes

```powershell
firebase deploy --only firestore:rules,firestore:indexes --project developers-networking-app
```

CI runs rules unit tests on every PR (`firestore/tests/rules.test.js`).

---

## 3. Deploy Node REST API (Render)

### Option A — Blueprint (recommended)

1. Push repo to GitHub.
2. [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint** → connect repo.
3. Render reads `render.yaml` and creates `devconnect-api`.
4. Set secret env var **`GOOGLE_APPLICATION_CREDENTIALS_JSON`**:
   - Paste the **full** service account JSON (single line is fine).
5. Deploy. Copy the HTTPS URL (e.g. `https://devconnect-api.onrender.com`).

### Option B — Docker manual

```powershell
cd backend
docker build -t devconnect-api .
docker run -p 5000:5000 `
  -e FIREBASE_PROJECT_ID=developers-networking-app `
  -e GOOGLE_APPLICATION_CREDENTIALS_JSON='{"type":"service_account",...}' `
  devconnect-api
```

Health check: `GET /health` → `{ "status": "ok", "firestore": "connected" }`.

### Option C — Railway

1. New project → Deploy from GitHub → root directory `backend/`.
2. Set `GOOGLE_APPLICATION_CREDENTIALS_JSON` and `FIREBASE_PROJECT_ID`.
3. Railway detects `Dockerfile` automatically.

---

## 4. Point the Android app at production

In `app/build.gradle.kts`, update the **release** `API_BASE_URL`:

```kotlin
buildConfigField("String", "API_BASE_URL", "\"https://YOUR-RENDER-URL/\"")
```

Debug builds keep `http://10.0.2.2:5000/` for emulator + local `npm start`.

For **release APK testing on a physical device**, use your LAN IP in debug config temporarily, or install a release build with the hosted URL.

---

## 5. GitHub Actions CI/CD

### Automatic (every PR)

`.github/workflows/ci.yml` runs:

- Backend Jest + supertest
- Cloud Functions TypeScript build
- Firestore rules tests (emulator)
- Android `assembleDebug`

### Manual Firebase deploy

1. GitHub → **Settings** → **Secrets** → add `FIREBASE_SERVICE_ACCOUNT_JSON`.
2. **Actions** → **Deploy Firebase** → **Run workflow**.
3. Choose: `all`, `functions`, `firestore-rules`, or `firestore-indexes`.

---

## 6. Security checklist

- [ ] Service account JSON never committed (`.gitignore` + vault)
- [ ] Node API enforces project visibility (`backend/lib/authorization.js`) — mirrors Firestore rules
- [ ] Admin routes gated by `requireAdmin` + `accountRole == admin`
- [ ] Legacy MongoDB auth removed — Firebase Auth only
- [ ] CORS locked down via `CORS_ORIGINS` if you add a web admin portal

---

## 7. Verify end-to-end

1. Sign up on a fresh account → welcome inbox appears (Functions).
2. Move a Kanban task → assignee gets inbox + activity (Functions).
3. Send chat message → recipient inbox + FCM (Functions + FCM).
4. Register for event → `participantCount` increments (Functions).
5. `curl -H "Authorization: Bearer TOKEN" https://YOUR-API/api/me` → profile JSON.

---

## Environment reference

| Variable | Where | Purpose |
|----------|-------|---------|
| `FIREBASE_PROJECT_ID` | Node API | `developers-networking-app` |
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | Render/Railway | Admin SDK credentials |
| `GOOGLE_APPLICATION_CREDENTIALS` | Local dev | Path to JSON file |
| `PORT` | Node API | Default `5000` |
| `CORS_ORIGINS` | Node API | Optional browser allowlist |

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Tasks `PERMISSION_DENIED` | Run `npm run team:sync-access` or deploy `onUserCreate` |
| No push notifications | Deploy Functions; verify `users/{uid}.fcmTokens` |
| `/health` returns 503 | Check service account + Firestore API enabled |
| Functions deploy fails | Confirm Blaze plan + `npm run build` passes locally |
