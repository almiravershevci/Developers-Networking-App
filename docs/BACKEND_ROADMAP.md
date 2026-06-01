# DevConnect Backend Roadmap (Backend Engineer Guide)



## Architecture (professional + course-ready)



```

UI (Compose) → ViewModel (MVVM) → Repository (interface) → DataSource → Firebase / REST

```



**Microservices on mobile:** one repository + datasource pair per feature (auth, dashboard, projects, chat, …). ViewModels never import Firebase directly.



**Server-side:** Firebase Auth + Firestore are the production data plane. Optional Node `backend/` or **Cloud Functions** for admin writes, aggregates, and push (FCM).



---



## Seed data vs real data (when to use which)



This is **not** “fake app vs real app.” It is **how you bootstrap** a real system.



| Layer | Seed / demo | Real (production) |

|--------|-------------|-------------------|

| **Purpose** | Empty database bootstrap, demos, tests, course submission | Every day after launch |

| **Who writes** | `seed-firestore.mjs`, one-time import, or Admin script | Users, admins, Cloud Functions |

| **IDs** | `demo_alex_uid`, fixed doc ids like `conv_team_neon` | Firebase Auth UIDs, auto-generated message ids |

| **When you stop using seed** | After first successful seed **or** when you have real users—you **don’t delete** seed docs; you **add** real docs alongside them | Ongoing |



### Rule of thumb (backend)



1. **Repository interface + Firestore implementation = real system.**  

   `FakeXRepository` is only for previews, unit tests, or offline UI work. **Ship with `XRepositoryFirestore` in `AppContainer`.**



2. **Seed = initial content in real collections**, not a parallel database.  

   Chat messages you send (`K2Otw4cl…`) are already “real data.” Seed is just pre-filled rows in the same `conversations` / `events` collections.



3. **User-owned data is always real once the feature writes Firestore:**  

   - Auth → `users/{uid}` on signup  

   - Profile save → `users/{uid}` update  

   - Chat send → `messages` subcollection  

   - (Future) RSVP / join event → `events/{id}/registrations` or similar  



4. **Curated / aggregate data stays server-written** (professional default):  

   - `events`, `newsHighlights`, `userStats`, `inbox`, `activity` → rules `allow create: if false` on client; fill via **Admin SDK**, **Console**, or **Cloud Functions** when counts change.  

   - Prevents clients from spoofing participant counts or global stats.



5. **Personalized rows need the viewer’s Auth UID:**  

   - `collaboratorSuggestions.viewerUserId`  

   - `activity.audienceUserId`  

   - `inbox.recipientUserId`  

   - `conversations.participantIds`  

   Seed uses `demo_*` until you duplicate rows for your UID or Functions generate them on signup.



### Migration path (course → production)



| Phase | What you do |

|-------|-------------|

| **Now** | Firestore repos in app; seed for global content; real Auth users |

| **Next** | Cloud Functions on user create → `userStats`, welcome `inbox` row |

| **Later** | Admin panel or `backend/` to publish events; FCM for notifications |

| **Production** | Remove or gate `Fake*Repository` from release builds; no client writes to curated collections |



---



## Requirements mapping



| Requirement | Implementation |

|-------------|----------------|

| Auth | `AuthRepositoryFirebase` + `users` / `usernames` |

| Task management | `ProjectsRepository` + `TasksRepository` |

| Event calendar | `EventsRepositoryFirestore` → `events` |

| Messaging | `ChatRepositoryFirestore` → `conversations` / `messages` |

| Analytics dashboard | `DashboardRepositoryFirestore` |

| REST API | `TechTrendsRepository` → Hacker News |

| Notifications | `NotificationsRepository` (inbox) + `LocalNotificationDispatcher` |

| Responsive UI | Teammate — you supply repositories |



---



## Module status (step-by-step)



| Step | Module | Status | Firestore collections |

|------|--------|--------|------------------------|

| 1 | Auth | Done | `users`, `usernames` |

| 2 | Dashboard | Done | `userStats`, `newsHighlights`, `collaboratorSuggestions`, … |

| 3 | Profile | Done | `users/{uid}` |

| 4 | Projects + Tasks | Done | `projects`, `tasks` |

| 5 | Chat | Done | `conversations`, `messages` |

| 6 | **Events** | **Done** | `events` (read-only client) |

| 7 | **Notifications / Inbox** | **Done** | `inbox` (read + mark read) |

| 8 | **Search** | **Done** | `projects` (public catalog) |
| 9 | **Admin** | **Done** | Firestore catalog + admin rules |

| 10 | Cloud Functions | Planned | stats, inbox, activity automation |



---



## Per-feature checklist (repeat every module)



1. Read `FakeXRepository` + `XRepository` interface  

2. Add `FirestoreXDataSource.kt`  

3. Add `XRepositoryFirestore.kt`  

4. Swap one line in `AppContainer.kt`  

5. Test screen + Firestore Console  

6. Deploy rules/indexes if new queries  



---



## Firebase Console habits



- **Authentication** — users, email verification  

- **Firestore → Rules** — publish `firestore.rules` after changes  

- **Firestore → Indexes** — deploy `firestore.indexes.json` when prompted  

- **Firestore → Data** — verify documents after each feature  



---



## Events module (step 6) — test



1. Ensure `events/*` docs exist (seed or Console).  

2. Sign in + verified email → open **Events** tab (or Events route in nav).  

3. List should match seed titles (AI Builders Jam, etc.).  

4. Rules: `events` → `allow read: if signedIn()`; client cannot create (curated).  



## Notifications module (step 7) — test

1. Seed `inbox/*` or run `npm run inbox:copy-demo` with `INBOX_RECIPIENT_UID` = your Auth UID.  
2. Sign in + verified email → **Alerts** tab.  
3. Tap **Mark read** → `read: true` in Firestore.  
4. Rules: client cannot create inbox rows (Functions/Admin only); user can read/update own rows.

## Search module (step 8) — test

1. Seed public `projects/*` docs.  
2. Sign in → **Search** tab → see DevConnect Mobile, CloudForge, PixelPair, etc.  
3. Type `kotlin` or tap a quick filter chip.  
4. **View details** opens the Kanban board for that `projectId`.

## Admin module (step 9) — test

See `firestore/ADMIN_SETUP.md`. Set `accountRole: admin` on your user doc, publish rules, Profile → Admin Dashboard.

All core app modules now use Firestore repositories in `AppContainer`.


