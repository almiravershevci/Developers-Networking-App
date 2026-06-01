# Backend Engineer Guide — DevConnect (Kotlin + Firestore)

You are **not** using “fake demo data” for the main app anymore. `AppContainer` wires **real Firestore repositories** for every feature. Seed data is only **initial rows** in the same database (like production bootstrap), not a separate fake app.

---

## 1. Course requirements → your app

| Requirement | Status | Where in the project |
|-------------|--------|----------------------|
| **Auth** — login, register, forgot password | Done | `AuthRepository` → `AuthRepositoryFirebase` + Firestore `users` / `usernames` |
| **Auth** — email verification | Done | Firebase Auth + verify screen |
| **Auth** — third-party login | Not done | Optional: Google Sign-In in `FirebaseAuthDataSource` |
| **≥ 3 core modules** | Done (6+) | Dashboard, Profile, Projects, Tasks, Chat, Events, Search |
| **REST API** | Done | Hacker News via `TechTrendsApi` (Search “Live API Trends”) |
| **Notifications** | Done | Firestore `inbox` + local Android notifications + badge on Alerts tab |
| **Responsive UI** | Teammate | You supply data via repositories only |
| **Admin / analytics** | Done | Admin dashboard + Firestore catalog |
| **Microservices (MVVM)** | Done | One `*Repository` + `*DataSource` per feature |

---

## 2. What “microservice” means here (professional)

You are **not** running 10 separate servers. You use **bounded contexts** inside one Android app:

```
Screen  →  ViewModel  →  Repository (interface)  →  DataSource  →  Firebase / REST
```

- **ViewModel** — UI state; never imports Firestore directly.
- **Repository** — business rules, auth gates, mapping to UI models.
- **DataSource** — Firestore queries, listeners, writes.
- **Mapper** (optional layer) — `FirestoreDoc` → `domain` model.

`AppContainer.kt` is your **service locator** (manual DI). Production apps often use Hilt; this is fine for a course.

---

## 3. Fake vs real data (clear answer)

| Layer | Fake? | Explanation |
|-------|-------|-------------|
| `Fake*Repository` / `InMemoryAdminRepository` | Yes | Only for **Compose previews** and offline UI tests. **Not used** at runtime. |
| `*RepositoryFirestore` in `AppContainer` | **Real** | Reads/writes Firebase Auth + Firestore. |
| `firestore/SEED_COPY_PASTE.json` | Bootstrap | One-time sample **documents** in real collections. |
| Your chat message “VotoniLuanin” | **Real** | Created by your Auth UID in `messages`. |
| `demo_alex_uid` in seed | Demo rows | Still real Firestore docs; add **your UID** to `participantIds` / `recipientUserId` to see them |

**Rule:** If `AppContainer` points to `*Firestore`, that feature is production-style. Seed only fills an empty database.

---

## 4. What you already completed (summary)

| # | Module | Repository | Firestore / API |
|---|--------|------------|-----------------|
| 1 | Auth | `impl.AuthRepositoryFirebase` | Auth + `users`, `usernames` |
| 2 | Dashboard | `impl.DashboardRepositoryFirestore` | stats, news, suggestions, projects |
| 3 | Profile | `impl.ProfileRepositoryFirestore` | `users/{uid}` |
| 4 | Projects (Kanban) | `impl.ProjectsRepositoryFirestore` | `projects`, `tasks` |
| 5 | Tasks list | `impl.TasksRepositoryFirestore` | same project tasks |
| 6 | Chat | `impl.ChatRepositoryFirestore` | `conversations`, `messages` |
| 7 | Events | `impl.EventsRepositoryFirestore` | `events` |
| 8 | Notifications | `impl.NotificationsRepositoryFirestore` | `inbox` |
| 9 | Search | `impl.SearchRepositoryFirestore` | public `projects` |
| 10 | Admin | `impl.AdminRepositoryFirestore` | moderation + broadcast inbox |
| 11 | REST | `ApiTechTrendsRepository` | Hacker News Algolia API |

**Your job as backend lead:** publish rules, seed data, indexes, test each screen, document setup for teammates.

---

## 5. Target folder structure (backend)

```
app/.../data/
├── repository/              # Contracts + domain-facing API
│   ├── AuthRepository.kt
│   ├── ProjectsRepository.kt
│   ├── …
│   ├── impl/                  # Firestore implementations (*RepositoryFirestore)
│   └── fake/                  # Previews only (Fake*, InMemory*)
├── datasource/
│   ├── firebase/              # Auth + Firestore IO
│   │   ├── schema/            # Document DTOs (FirestoreSchema, paths)
│   │   ├── FirebaseAuthDataSource.kt
│   │   ├── FirestoreUserDataSource.kt
│   │   └── …
│   └── remote/                # REST (Retrofit)
│       └── TechTrendsApi.kt
├── mapper/                    # Doc → domain mapping
└── local/                     # Device-only prefs (ChatMuteStore)
```

`domain/model/` stays for **UI models** (what ViewModels consume). Firestore document shapes live under `datasource/firebase/schema/`.

---

## 6. Step-by-step — what you should do (in order)

### Phase A — Firebase project (once)

1. Create / open Firebase project; add Android app → download `google-services.json` into `app/`.
2. Enable **Authentication** → Email/Password.
3. Create **Firestore** database (production mode).
4. **Publish rules** — paste `firestore/RULES_PASTE_IN_CONSOLE.rules` → Publish.
5. **Deploy indexes** — `firebase deploy --only firestore:indexes` (or create when Console shows links).
6. **Seed data** — `cd firestore` → `npm install` → set service account → `npm run seed`.

### Phase B — Personal test account (once)

1. Sign up in the app; verify email.
2. Note your **Auth UID** (Console → Authentication).
3. For chat: add UID to `conversations/*/participantIds` (see `firestore/CHAT_TEST_STEPS.md`).
4. For notifications: `npm run inbox:copy-demo` or set `recipientUserId` on inbox docs.
5. For admin: set `users/{yourUid}.accountRole` = `admin` (see `firestore/ADMIN_SETUP.md`).

### Phase C — Prove each requirement (demo script)

1. **Auth** — register, login, forgot password, verify email.
2. **Modules** — open Home, Projects, Tasks, Chat, Events, Search, Alerts, Profile.
3. **REST** — Search tab → “Live API Trends” chips load.
4. **Notifications** — unread badge on Alerts without opening tab; mark read → badge clears.
5. **Admin** — Profile → Admin Dashboard → see Firestore user/project counts.

### Phase D — Team handoff

1. Share: rules file, seed JSON, test UIDs doc, `BACKEND_ENGINEER_GUIDE.md`.
2. Do **not** commit `google-services.json` or service account keys.
3. Frontend only changes **ViewModels** if repository interfaces change (rare).

### Phase E — Optional polish (professional extra)

1. Cloud Functions for `userStats`, `inbox` on signup.
2. Google Sign-In.
3. FCM push (tray notifications).
4. Node `backend/` for heavy admin jobs (keep folder; optional).

---

## 7. Daily workflow as backend engineer

1. Change **DataSource** or **rules** for new fields.
2. Update **mapper** + **repository** if UI needs new shapes.
3. Test in app + Firestore Console.
4. Never edit Compose screens unless coordinating with frontend.

---

## 8. Node.js `backend/` folder

Keep it. Course allows “self-developed REST API.” Primary API today is **Retrofit → Hacker News** inside the app. Node can later expose `/admin` or webhooks without replacing Firestore.

---

## 9. Troubleshooting

| Symptom | Fix |
|---------|-----|
| PERMISSION_DENIED | Publish latest `firestore.rules` |
| Empty chat inbox | Add your UID to `participantIds` |
| Empty notifications | Set `recipientUserId` to your UID |
| Admin empty | Set `accountRole: admin` on your user doc |
| Index error | Deploy `firestore.indexes.json` |

---

## 10. What to say in presentation (30 seconds)

> “We use MVVM with a repository per feature. Firebase Auth handles identity; Firestore is the system of record with security rules per collection. The Android app only talks through data sources; seed data bootstraps demos; user-generated content is real-time. We also integrate Hacker News REST API on Search, and local notifications for the inbox.”

That is accurate and professional.
