# Phase 10 — Final QA Checklist (Pre-Submission)

Run date: **2026-06-09**  
Environment: Windows · Gradle JDK 21 · `./gradlew test` + `./gradlew assembleDebug` verified locally.

**Legend:** ✅ Verified (automated/code) · 📱 Manual (device + Firebase) · ⚠️ Partial · ❌ Not implemented

---

## Checklist

| # | Test | Status | Notes |
|---|------|--------|-------|
| 1 | Register new user → `users/{uid}` + `usernames/{lower}` created | 📱 | Implemented in `AuthRepositoryFirebase.signup()` → `FirestoreUserDataSource.createUserProfile()`. Confirm in Firebase Console after signup. |
| 2 | Verify email → gated screens unlock | ✅ 📱 | Gates in Chat, Tasks, Projects, Search, Events, Notifications (`isEmailVerified`). Confirm dashboard/chat load after verification flow. |
| 3 | Forgot password email received | 📱 | `requestPasswordReset()` resolves username → email → Firebase `sendPasswordResetEmail`. Check inbox/spam. |
| 4 | Home dashboard loads stats, activity, news | 📱 | `DashboardRepositoryFirestore` reads `userStats`, `activity`, `newsHighlights`, `collaboratorSuggestions`, `projects`, `events`. Requires seed + verified sign-in. |
| 5 | Projects Kanban loads from `projects/*/tasks` | 📱 | `ProjectsRepositoryFirestore` maps tasks by `boardColumn`. Read-only UI; seed `proj_devconnect_mobile` tasks. |
| 6 | Create / move / delete task persists in Firestore | ⚠️ | **Move:** backend `TasksRepository.moveTask()` + unit tests ✅ — **not wired to Kanban UI**. **Create/delete:** not implemented in app. Manual move via Console or call repository from debugger only. |
| 7 | Chat sends message → `messages` subcollection updates realtime | 📱 | `ChatRepositoryFirestore.sendMessage()` + snapshot listeners. Add your UID to `participantIds` ([CHAT_TEST_STEPS.md](../firestore/CHAT_TEST_STEPS.md)). |
| 8 | Events list loads from `events` | 📱 | `EventsRepositoryFirestore` → `FirestoreEventsDataSource`. Admin/seed-written collection. |
| 9 | Search finds public projects + HN trends API | ✅ 📱 | Firestore: `SearchRepositoryFirestore`. HN Algolia API: **HTTP 200** from this machine (`hn.algolia.com`). Offline: search shows status message; HN needs network. |
| 10 | Inbox loads for your UID; mark read works | ✅ 📱 | `NotificationsRepositoryFirestore` + unit test for `markAsRead`. Seed inbox with `npm run inbox:copy-demo` or admin send. |
| 11 | Admin dashboard (if admin role set) | 📱 | Set `users/{uid}.accountRole` = `admin` or register `admin@devconnect.app`. `AdminRepositoryFirestore` + rules `isAdmin()`. |
| 12 | `./gradlew test` passes | ✅ | **BUILD SUCCESSFUL** — 12 unit tests (Tasks, Chat, Notifications repos + example). |
| 13 | Rules published; no `PERMISSION_DENIED` in Logcat | 📱 | Rules file present at `firestore.rules`. Deploy: `firebase deploy --only firestore:rules`. Filter Logcat: `Firestore`, `PERMISSION_DENIED`. |

---

## Automated runs (this session)

```powershell
cd Developers-Networking-App
.\gradlew test          # PASS
.\gradlew assembleDebug # PASS
```

Hacker News API probe:

```
GET https://hn.algolia.com/api/v1/search?tags=story&hitsPerPage=3 → HTTP 200
```

---

## Manual smoke test script (~15 min)

1. **Deploy backend** (once): `firebase deploy --only firestore` · `cd firestore && npm run seed`
2. **Register** new account → Console: `users/{uid}`, `usernames/{name}`
3. **Verify email** (link on device) → open Chat/Projects (should not show “verify” gate)
4. **Forgot password** from login → check email
5. **Dashboard** → stats, news, activity cards (not all “Sign in to load”)
6. **Projects** → three Kanban columns with seeded task titles
7. **Chat** → send message; refresh Console `conversations/.../messages`
8. **Events** → list lines from seed
9. **Search** → public projects; scroll to HN trends section (online)
10. **Notifications** → inbox items; tap mark read
11. **Admin** (optional) → profile shows Admin Dashboard button
12. **Logcat** → no repeated `PERMISSION_DENIED` during normal navigation

---

## Known gaps before submission

| Gap | Impact | Workaround for demo |
|-----|--------|---------------------|
| No UI for create/delete/move tasks | Checklist item 6 partial | Show seeded Kanban; mention `moveTask` in unit tests / BACKEND_API |
| Google Sign-In needs `default_web_client_id` | Google button only | Demo email/username login |
| Cloud Functions not deployed | Server aggregates manual | Seed + admin client documented in DEPLOYMENT.md |
| `userStats` / `inbox` client create blocked | Empty stats until seed | Run seed scripts |

---

## Pre-submission commands

```powershell
.\gradlew test
.\gradlew assembleDebug
firebase deploy --only firestore
```

---

## Presentation one-liner

See [PRESENTATION_SCRIPT.md](PRESENTATION_SCRIPT.md).
