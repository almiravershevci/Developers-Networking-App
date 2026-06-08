# Team setup — works for everyone (not just one developer)

Everyone uses the **same shared Firebase project** (`developers-networking-app`).  
No personal UIDs, no “only works on my machine” steps.

---

## What every teammate does (5 minutes)

1. **Pull** the repo and open in Android Studio.
2. **Sync Gradle** — `app/google-services.json` is already in git (same Firebase app for all).
3. **Sign up** in the app with your own email (or use an account a lead created for you).
4. **Verify your email** — many features are gated until verified.
5. **Run the app** — you should see Dashboard, Tasks, Chat, Alerts, etc.

That is it for normal developers. You do **not** need:
- A service account JSON to build or run the app
- The Node backend running (`backend/npm start`) — dashboard uses Firestore directly
- Render or any hosted REST API

---

## What the team lead / backend dev does once (shared infrastructure)

These steps affect the **whole team**, not one person:

| Step | Command / action | Who |
|------|------------------|-----|
| Blaze plan (Cloud Functions) | [Upgrade project](https://console.firebase.google.com/project/developers-networking-app/usage/details) | Lead (once) |
| Deploy Firestore rules | `firebase deploy --only firestore:rules` | Backend lead |
| Deploy indexes | `firebase deploy --only firestore:indexes` | Backend lead |
| Deploy Cloud Functions | `firebase deploy --only functions` | Backend lead |
| Seed demo data (optional) | `cd firestore && npm run seed` | Backend lead |
| **Sync access for all Auth users** | `cd firestore && npm run team:sync-access` | Backend lead |

### Service account (maintainers only)

- Download from Firebase Console → Project settings → Service accounts.
- Store in **1Password / team vault** — **never commit** `*firebase-adminsdk*.json` to git.
- Each maintainer sets locally:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
$env:FIREBASE_PROJECT_ID="developers-networking-app"
cd firestore
npm install
npm run team:sync-access
```

`team:sync-access` adds **every** Firebase Auth user to:

- `projects/proj_devconnect_mobile/members/{uid}` → Tasks / Kanban writes work
- Showcase `conversations/*` `participantIds` → Chat inbox works

Run it again whenever a **new teammate signs up** (until Cloud Functions are deployed).

---

## Automatic onboarding (after Functions deploy)

When `onUserCreate` is deployed, **new signups** automatically get:

- `userStats/{uid}` (zeros)
- Welcome `inbox` notification
- `collaboratorSuggestions` (from public profiles)
- Showcase **project member** + **chat** access

Existing accounts created before deploy still need **one** `npm run team:sync-access`.

---

## Per-feature expectations (whole team)

| Feature | Works for everyone when… |
|---------|---------------------------|
| Login / signup | `google-services.json` in repo |
| Tasks / Kanban move | User is project member (auto or `team:sync-access`) |
| Chat | User in conversation `participantIds` (auto or `team:sync-access`) |
| Alerts / inbox | Cloud Functions deployed + assignee set on tasks |
| Push notifications (FCM) | Functions deployed + user signed in with notifications allowed + `fcmTokens` on `users/{uid}` |
| Event RSVP | Signed in + rules deployed + optional Functions for `participantCount` |
| Dashboard Node API line | **Optional** — `DevConnectApiConfig.ENABLED = false` by default; stats from Firestore |
| Match invites | Any verified user → any other user (real Auth UIDs) |
| Dashboard stats | `userStats` doc exists (Functions or seed) |

**Demo seed UIDs** (`demo_alex_uid`, etc.) are sample rows only. Your real account uses **your** Auth UID — that is correct.

---

## Build troubleshooting

1. **JDK 17** — Android Studio → Settings → Build → Gradle → Embedded JDK.
2. **Sync fails** — File → Invalidate Caches → Restart.
3. **PERMISSION_DENIED on tasks** — Lead runs `npm run team:sync-access`.
4. **Empty Chat** — Verify email, then lead runs `team:sync-access` or redeploy Functions.
5. **No inbox after task move** — Deploy Functions; task must have an `assigneeUserId`.
6. **No push in tray** — Allow notifications (Android 13+), sign in + verify email, redeploy Functions (`onInboxCreated`). Check `users/{uid}.fcmTokens` in Firestore.
7. **Logcat "Dashboard REST stats unavailable"** — Harmless if you see it; REST is disabled by default (`DevConnectApiConfig.ENABLED = false`). Lead can set `ENABLED = true` when running `backend/npm start`.

### Verify FCM (any teammate)

1. Sign in, verify email, allow notification permission.
2. Firestore → `users/{your-uid}` → `fcmTokens` should contain a token after app launch.
3. Background the app → have a teammate move a task assigned to you (or send a chat).
4. Tray notification appears; **Alerts** tab still loads from Firestore `inbox` (repository unchanged).

---

## Data layer layout (if you edit backend code)

```
data/repository/           ← interfaces
data/repository/impl/      ← Firestore implementations
data/datasource/firebase/  ← Firestore + Auth IO
```

Implementations in `repository.impl` must import `com.example.developernetworkingapp.data.repository.*` explicitly.
