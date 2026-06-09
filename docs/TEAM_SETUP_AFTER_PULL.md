# Team setup — works for everyone (not just one developer)

Everyone uses the **same shared Firebase project** (`developers-networking-app`).  
Follow this so Android Studio **builds and runs** with no compile errors.

---

## What every teammate does (5 minutes)

1. **Pull** the repo and open in Android Studio.
2. **Sync Gradle** — `app/google-services.json` is already in git (same Firebase app for all).
3. Copy `local.properties.example` → `local.properties` if Android Studio does not create it.
4. **Sign up** in the app with your own email (or use an account a lead created for you).
5. **Verify your email** — login and most features are gated until verified.
6. **Run the app** — Dashboard, Tasks, Chat, Alerts, etc.

You do **not** need a service account JSON to **build** the app. You need Firebase + seed/sync for **full demo content**.

```powershell
.\gradlew assembleDebug
.\gradlew test
```

---

## Required files (in git)

| File | Purpose |
|------|---------|
| `app/google-services.json` | Firebase Android config — **required** for Auth + Firestore |
| `gradle/libs.versions.toml` | Dependency versions |
| `local.properties.example` | Template for SDK path (each machine has its own `local.properties`) |

---

## What the team lead / backend dev does once (shared infrastructure)

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

`team:sync-access` adds **every** Firebase Auth user to project members and showcase chat conversations. Run again when a **new teammate signs up** (until Cloud Functions are deployed).

### Per-user demo content (alternative to team-wide sync)

After seed + signup + verify email:

```powershell
$env:DEMO_USER_UID="your-firebase-auth-uid"
npm run setup:demo-user
```

---

## Automatic onboarding (after Functions deploy)

When `onUserCreate` is deployed, **new signups** automatically get `userStats`, welcome inbox, collaborator suggestions, and showcase project/chat access. Existing accounts created before deploy still need **one** `npm run team:sync-access`.

---

## Per-feature expectations (whole team)

| Feature | Works for everyone when… |
|---------|---------------------------|
| Login / signup | `google-services.json` in repo |
| Email verification | User opens Firebase link, taps **Verify email** in app |
| Tasks / Kanban | User is project member (auto, `team:sync-access`, or `setup:demo-user`) |
| Chat | User in conversation `participantIds` |
| Alerts / inbox | Functions deployed + assignee set on tasks |
| Push notifications (FCM) | Functions deployed + notifications allowed + `fcmTokens` on `users/{uid}` |
| Dashboard stats | `userStats` doc exists (Functions, seed, or `setup:demo-user`) |

---

## Build troubleshooting

1. **JDK 21** — Android Studio → Settings → Build → Gradle → Gradle JDK.
2. Install Android SDK **API 36** if prompted.
3. **Sync fails** — File → Invalidate Caches → Restart.
4. **PERMISSION_DENIED on tasks** — Lead runs `npm run team:sync-access`.
5. **Empty Chat / Dashboard** — Verify email, then lead runs `team:sync-access` or run `setup:demo-user` for your UID.

---

## Data layer layout

```
data/repository/           ← interfaces
data/repository/impl/      ← Firestore implementations
data/datasource/firebase/  ← Firestore + Auth IO
data/datasource/remote/    ← Hacker News API (Search trends)
```

Runtime uses **Firestore repositories only** (`di/AppContainer.kt`). `Fake*Repository` classes are for Compose previews.

## Still broken?

1. Confirm `app/google-services.json` exists.
2. Delete project `.gradle` folder and sync again.
3. See [DEPLOYMENT.md](DEPLOYMENT.md) and [BACKEND_ENGINEER_GUIDE.md](BACKEND_ENGINEER_GUIDE.md).
