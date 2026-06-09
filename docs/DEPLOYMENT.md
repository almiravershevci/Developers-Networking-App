# Deployment Guide

How to deploy Firestore rules, indexes, seed data, and (optionally) Cloud Functions for the DevConnect Android app.

**Firebase project:** Must match `app/google-services.json` (`developers-networking-app` in the bundled config).

---

## Prerequisites

| Tool | Purpose |
|------|---------|
| [Firebase CLI](https://firebase.google.com/docs/cli) | Deploy rules and indexes |
| Node.js 18+ | Run seed scripts in `firestore/` |
| Service account JSON | Admin SDK seed (local only — **never commit**) |
| Android Studio + JDK 21 | Build and run the app |

```powershell
npm install -g firebase-tools
firebase login
firebase use developers-networking-app   # your project ID
```

Create `local.properties` with your Android SDK path (gitignored):

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk
```

---

## 1. Firebase Authentication

In [Firebase Console](https://console.firebase.google.com/) → **Authentication** → **Sign-in method**:

1. Enable **Email/Password**
2. (Optional) Enable **Google** — add SHA-1/SHA-256 fingerprints; set `default_web_client_id` in `app/src/main/res/values/strings.xml`
3. Add authorized domains for password reset / email verification links

Enable sign-in before testing signup or chat (verified email required for most features).

---

## 2. Deploy Firestore rules

Rules file: **`firestore.rules`** (repo root).  
Config: **`firebase.json`**.

```powershell
# From repository root
firebase deploy --only firestore:rules
```

**Manual alternative:** Console → Firestore → **Rules** → paste from `firestore/RULES_PASTE_IN_CONSOLE.rules` → **Publish**.

### What the rules enforce

- **Username login:** unauthenticated `get` on `usernames/{usernameLower}`
- **Profiles:** users read/update own `users/{uid}`; admin override
- **Projects/tasks:** member-based read; managers update tasks
- **Chat:** `participantIds` must include caller for conversation/message access
- **Server-only writes:** `userStats`, `activity`, `collaboratorSuggestions` — client create blocked
- **Curated content:** `events`, `newsHighlights` — admin write only
- **Inbox:** users read/mark-read own docs; create via admin

After rule changes, force-stop the app and re-test affected screens.

---

## 3. Deploy Firestore indexes

Index definitions: **`firestore.indexes.json`**

```powershell
firebase deploy --only firestore:indexes
```

Or deploy rules + indexes together:

```powershell
firebase deploy --only firestore
```

### Key composite indexes

| Collection | Fields | Used by |
|------------|--------|---------|
| `conversations` | `participantIds` ARRAY + `lastMessageAt` DESC | Chat inbox |
| `inbox` | `recipientUserId` + `createdAt` DESC | Notifications |
| `projects` | `visibility` + `lifecycleStatus` + `updatedAt` | Dashboard, search |
| `collaboratorSuggestions` | `viewerUserId` + `rank` | Dashboard matches |
| `tasks` (collection group) | `boardColumn` + `updatedAt` | Task queries |

If a query fails with `FAILED_PRECONDITION`, follow the Console link to create the index, or redeploy `firestore.indexes.json`.

---

## 4. Seed Firestore data

Scripts live in **`firestore/`**. Sample document shapes: **`SEED_COPY_PASTE.json`**.

### Option A — Admin SDK script (recommended)

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
$env:FIREBASE_PROJECT_ID="developers-networking-app"
cd firestore
npm install
npm run seed
```

Dry run (no writes):

```powershell
npm run seed:dry
```

### Option B — Manual Console import

Add documents in Firebase Console using paths from `SEED_COPY_PASTE.json` (e.g. `users/demo_alex_uid`, `projects/proj_devconnect_mobile`).

### Post-seed scripts

After you **register in the app** and **verify email**, link demo content to your Firebase Auth UID:

```powershell
$env:DEMO_USER_UID="your-firebase-auth-uid"
npm run setup:demo-user
```

| npm script | Purpose |
|------------|---------|
| `npm run setup:demo-user` | **All-in-one:** chat participants, inbox, collaborator suggestions, activity, userStats |
| `npm run chat:add-me` | Add your Auth UID to seeded `participantIds` only |
| `npm run inbox:copy-demo` | Copy demo inbox rows to your UID only |

See [firestore/CHAT_TEST_STEPS.md](../firestore/CHAT_TEST_STEPS.md) for chat QA and [TEAM_SETUP_AFTER_PULL.md](TEAM_SETUP_AFTER_PULL.md) for the teammate checklist.

---

## 5. Cloud Functions (planned / course narrative)

The repo does **not** yet include a `functions/` directory. Production automation is documented as the target for:

| Trigger | Action |
|---------|--------|
| Auth user created | Initialize `userStats/{uid}`, welcome `inbox` row |
| Task moved / message sent | Update `userStats` counters, optional FCM push |
| Admin publish | Write `events`, `newsHighlights`, broadcast `inbox` |

**Current substitutes:**

- **Seed script** (`seed-firestore.mjs`) — populates curated collections
- **Admin app** (`AdminRepositoryFirestore`) — admin-role client writes where rules allow
- **Helper scripts** — `add-inbox-for-user.mjs`, `add-chat-participant.mjs`

When adding Cloud Functions:

```powershell
firebase init functions
# Implement triggers; then:
firebase deploy --only functions
```

Set Functions service account IAM for Firestore Admin access. Keep privileged writes out of the mobile client.

---

## 6. Android app build

```powershell
.\gradlew assembleDebug
.\gradlew test          # repository unit tests (Phase 8)
```

Install APK from `app/build/outputs/apk/debug/`.  
Place `google-services.json` in `app/` before building.

---

## 7. Verification checklist

| Step | Verify |
|------|--------|
| Rules deployed | Signup succeeds; username lookup works |
| Indexes deployed | Chat inbox loads without index error |
| Seed run | Dashboard shows projects, events, news |
| Chat participant | Your UID in `conversations/*/participantIds` |
| Email verified | Chat, tasks, projects show data (not gated) |
| Unit tests | `./gradlew test` green |

---

## 8. CI / team workflow

1. Pull latest `firestore.rules` and `firestore.indexes.json`
2. `firebase deploy --only firestore` against shared dev project
3. Re-run seed or participant scripts if collections were reset
4. Do **not** commit service account keys or `local.properties`

See also [TEAM_SETUP_AFTER_PULL.md](TEAM_SETUP_AFTER_PULL.md) and [BACKEND_ENGINEER_GUIDE.md](BACKEND_ENGINEER_GUIDE.md).

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `PERMISSION_DENIED` on signup | Deploy rules; `usernames` get must be public |
| Chat inbox empty / error | Publish rules; add UID to `participantIds` |
| Notifications empty | Seed `inbox` or run `inbox:copy-demo` |
| Gradle JDK error | Ensure JDK 21 available (Gradle toolchain) |
| Google Sign-In fails | Set `default_web_client_id`; enable Google provider |
