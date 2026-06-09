# Backend API — Repository Layer

DevConnect uses **MVVM** with **one repository microservice per feature**. Repositories expose domain-friendly APIs; Firestore and Firebase Auth details stay in `data/datasource/firebase/`.

**Production wiring:** `di/AppContainer.kt` → `*RepositoryFirestore` implementations.

---

## Architecture summary

| Layer | Package | Role |
|-------|---------|------|
| UI | `ui/viewmodel`, `ui/screens` | Observes `Flow` from repositories |
| Repository | `data/repository` | Feature contracts + auth gates |
| Data source | `data/datasource/firebase` | Firestore paths, queries, writes |
| Identity | Firebase Auth | Email/password, Google, username login |
| System of record | Cloud Firestore | Profiles, projects, chat, inbox, etc. |
| External REST | Hacker News (Algolia) | Tech trends on Search screen |
| Push (local) | `LocalNotificationDispatcher` | On-device reminders (FCM planned) |

---

## AuthRepository

**Implementation:** `AuthRepositoryFirebase`  
**Identity:** Firebase Auth · **Profile/registry:** Firestore

| Method | Description | Firestore / Auth |
|--------|-------------|------------------|
| `currentUser: StateFlow<AuthUser?>` | Session snapshot for UI/admin gating | Auth + `users/{uid}` |
| `login(identifier, password, rememberMe)` | Email **or username** + password | Auth sign-in; `usernames/{lower}` → email lookup |
| `signInWithGoogle(idToken, rememberMe)` | Google OAuth via ID token | Auth credential; creates `users` + `usernames` if new |
| `signup(name, username, email, password, rememberMe)` | Register + profile bootstrap | Auth create; `users/{uid}`, `usernames/{usernameLower}` |
| `requestPasswordReset(identifier)` | Reset email | Auth email; username → email via `usernames` |
| `requestEmailVerification(email)` | Resend verification link | Auth |
| `verifyEmailCode(email, code)` | Reload session after link opened | Auth reload; `users/{uid}.emailVerified` |
| `deleteAccount(password?, googleIdToken?)` | Delete Auth user + Firestore cleanup | Auth `delete()`; removes `users`, `usernames`, user `inbox` |
| `logout()` | Sign out + clear prefs | Auth |

---

## ProfileRepository

**Implementation:** `ProfileRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeProfile(): Flow<ProfileContent>` | Live profile for signed-in user | `users/{uid}`, `userStats/{uid}` |
| `updateProfile(displayName, headline, bio)` | Merge profile fields | `users/{uid}` update |

---

## DashboardRepository

**Implementation:** `DashboardRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeDashboardContent(): Flow<DashboardContent>` | Home feed (stats, matches, news, activity) | `userStats`, `collaboratorSuggestions`, `newsHighlights`, `activity`, `events`, `projects`, `users` |

---

## ProjectsRepository

**Implementation:** `ProjectsRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeProjects(): Flow<ProjectBoardContent>` | Kanban columns (todo / in progress / done) | `projects/{id}`, `projects/{id}/tasks`, `projects/{id}/members`, `users` |

---

## TasksRepository

**Implementation:** `TasksRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeTasks(): Flow<TaskContent>` | Task list lines for Tasks screen | `projects/{id}/tasks`, `users` (assignee names) |
| `moveTask(taskId, boardColumn)` | Move task on board | `projects/{id}/tasks/{taskId}` update `boardColumn` |

---

## ChatRepository

**Implementation:** `ChatRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeChat(): Flow<ChatContent>` | Inbox of conversations | `conversations` (query `participantIds` array-contains uid) |
| `observeConversation(conversationId)` | Realtime thread + read receipts | `conversations/{id}`, `conversations/{id}/messages` |
| `sendMessage(conversationId, body)` | Send text message | `conversations/{id}/messages` create; conversation patch |

---

## NotificationsRepository

**Implementation:** `NotificationsRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeNotifications(): Flow<NotificationContent>` | Per-user inbox | `inbox` (query `recipientUserId`) |
| `markAsRead(notificationId)` | Mark notification read | `inbox/{id}` update `read` |

---

## SearchRepository

**Implementation:** `SearchRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeSearch(): Flow<SearchContent>` | Public project catalog + filters | `projects` (public), `users` (owner names) |

---

## EventsRepository

**Implementation:** `EventsRepositoryFirestore`

| Method | Description | Firestore |
|--------|-------------|-----------|
| `observeEvents(): Flow<EventContent>` | Curated events calendar (read-only client) | `events` |

---

## TechTrendsRepository

**Implementation:** `ApiTechTrendsRepository`  
**External:** `GET https://hn.algolia.com/api/v1/search?tags=story`

| Method | Description | Backend |
|--------|-------------|---------|
| `loadTrendingTopics()` | Top Hacker News story titles | Algolia REST (no Firestore) |

---

## AdminRepository

**Implementation:** `AdminRepositoryFirestore` (admin role required)

| Method | Description | Firestore |
|--------|-------------|-----------|
| `snapshot: StateFlow<AdminDashboardSnapshot>` | Admin console state | Aggregated reads |
| `deactivateUser` / `activateUser` / `banUser` | Account moderation | `users/{uid}.accountRole` |
| `updateUserProfile(userId, techStack, bio)` | Admin edit user | `users/{uid}` |
| `approveProject` / `rejectProject` / `archiveProject` / `updateProject` | Project lifecycle | `projects/{id}` |
| `sendNotification(title, body, audience)` | Broadcast inbox rows | `inbox` create (admin) |
| `resolveMatchRequest` (via data source) | Accept/decline match | `matchRequests/{id}` |
| Other admin UI actions | Audit log, CSV export, settings stubs | Mixed (some in-memory audit) |

---

## NotificationDispatcher (non-Firestore)

**Implementation:** `LocalNotificationDispatcher`

| Method | Description |
|--------|-------------|
| `showLocalNotification(title, message)` | Android notification tray (task reminders) |

---

## Data ownership model

| Written by **clients** (signed-in users) | Written by **server/admin** (rules block client create) |
|------------------------------------------|-----------------------------------------------------------|
| `users`, `usernames` (signup) | `userStats` |
| `projects`, `members`, `tasks` | `events`, `newsHighlights` |
| `conversations`, `messages` | `inbox` (create), `activity`, `collaboratorSuggestions` |
| Profile updates, task moves, chat send | Aggregates, curated feed, push payloads (FCM planned) |

Security enforced in `firestore.rules` at collection level.

---

## Related docs

- [FIRESTORE_SCHEMA.md](FIRESTORE_SCHEMA.md) — collection field reference
- [DEPLOYMENT.md](DEPLOYMENT.md) — rules, indexes, seed, functions
- [../firestore/CHAT_TEST_STEPS.md](../firestore/CHAT_TEST_STEPS.md) — chat QA checklist
- [PRESENTATION_SCRIPT.md](PRESENTATION_SCRIPT.md) — 30-second submission pitch

---

## Unit tests

Repository logic is covered in `app/src/test/.../data/repository/impl/`:

- `TasksRepositoryFirestoreTest` — moveTask, auth gate, TaskItem mapping
- `ChatRepositoryFirestoreTest` — sendMessage validation
- `NotificationsRepositoryFirestoreTest` — markAsRead

Run: `./gradlew test`
