# Dashboard feed — architecture note

## Status: **UI demo layer (not persisted)**

The Dashboard **project feed** (posts, likes, comments, expand/collapse) is intentionally **client-side only**. It is **not** backed by Firestore or the Node REST API.

---

## What is real vs demo

| Feature | Backend | Source |
|---------|---------|--------|
| Dashboard stats | ✅ Firestore `userStats` + optional `GET /api/dashboard/stats` | Server-maintained |
| Collaborator suggestions | ✅ Firestore `collaboratorSuggestions` | Seeded + Functions on signup |
| Activity timeline | ✅ Firestore `activity` | Cloud Functions on task updates |
| News highlights | ✅ Firestore `newsHighlights` | Admin/seed |
| **Feed posts / likes / comments** | ❌ **Not persisted** | `DashboardViewModel` local state |

---

## How it works in the app

1. `DashboardRepositoryFirestore` loads **real** projects and maps them to `ProjectPost` cards (title, stack, description from Firestore projects).
2. `DashboardViewModel` wraps each post in `FeedPostState` and stores **likes, comments, and drafts in memory only**.
3. User-created posts (`publishPost`) append to the in-memory list — they disappear on app restart.

This matches the product decision in `docs/API_DESIGN.md`: no `/posts` or `/comments` REST routes because the frontend does not persist them.

---

## Why this is acceptable for the course project

- Demonstrates **Compose UI** and **ViewModel state** without scope creep.
- Real networking features (chat, match, tasks, events, inbox) **are** fully backed by Firestore + Functions.
- Clear separation: **social feed polish** vs **production collaboration data**.

---

## Future Phase 7 (optional — if product requires persistence)

If the team wants a real feed microservice later:

### Firestore schema (sketch)

```
projectPosts/{postId}
  projectId, authorUserId, body, createdAt, likeCount

projectPosts/{postId}/comments/{commentId}
  authorUserId, body, createdAt

projectPosts/{postId}/likes/{userId}
  createdAt
```

### Automation

- Cloud Function `onPostCreated` → activity + inbox for project members
- Rules: only project members can read/write posts for that project

### REST (optional BFF)

- `GET /api/projects/:id/posts`
- `POST /api/projects/:id/posts`

**No frontend changes required until the team explicitly scopes this phase.**

---

## Presentation talking point

> “The dashboard feed cards show **live project data** from Firestore, while likes and comments are a **UI prototype**. All collaboration workflows — tasks, chat, match requests, events, and notifications — are fully server-backed.”
