# Firestore Schema Reference

Canonical Kotlin models: `app/.../data/datasource/firebase/schema/FirestoreSchema.kt`  
Path constants: `FirestorePaths.kt` · String enums: `FirestoreConstants.kt`

All documents include `schemaVersion` (int) where noted. Timestamps use Firestore `Timestamp` / `@ServerTimestamp`.

---

## Top-level collections

### `users/{userId}`

User profile. Document ID = Firebase Auth UID.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | Currently `2` on signup |
| `displayName` | string | Shown on profile, chat |
| `usernameLower` | string | Case-insensitive handle |
| `email` | string | Lowercase |
| `accountRole` | string | `user`, `admin`, `banned`, `deactivated` |
| `emailVerified` | boolean | Mirrored from Auth after verify |
| `headline` | string | Role / tagline |
| `bio` | string | About text |
| `photoUrl` | string? | Avatar URL |
| `skillTags` | array\<string\> | Stack chips |
| `portfolioLinks` | map | `github`, `linkedin`, `portfolio` |
| `profileVisibility` | string | `public`, `network_only`, `private` |
| `gitInsightsSummary` | string? | GitHub sync placeholder |
| `createdAt` | timestamp | Server |
| `updatedAt` | timestamp | Server |
| `lastActiveAt` | timestamp | Activity |

**Rules:** Owner or admin can update/delete. Public profiles readable without auth (for username login).

---

### `usernames/{usernameLower}`

Username registry for unique handles and login-by-username.

| Field | Type | Notes |
|-------|------|-------|
| `usernameLower` | string | Same as document ID |
| `userId` | string | Firebase Auth UID |

**Rules:** `get` allowed without auth (signup + login lookup). No list (anti-scraping).

---

### `userStats/{userId}`

Denormalized counters for dashboard/profile.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `activeProjectsCount` | int | |
| `openTasksCount` | int | |
| `unreadMessagesCount` | int | |
| `pendingMatchRequestsCount` | int | |
| `collaborationsCount` | int | |
| `ratingAggregate` | number? | |
| `updatedAt` | timestamp | |

**Rules:** Client **read only**; writes via Admin SDK / Cloud Functions.

---

### `projects/{projectId}`

Project workspace metadata.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `title` | string | |
| `subtitle` | string | |
| `description` | string | |
| `primaryStackLabel` | string | e.g. Kotlin · Firebase |
| `stackTags` | array\<string\> | Search filters |
| `ownerUserId` | string | Auth UID |
| `locationKind` | string | `remote`, `hybrid`, `onsite` |
| `cityName` | string? | |
| `openRoleLabels` | array\<string\> | Roles recruiting |
| `capacityTotal` | int | |
| `spotsOpen` | int | |
| `memberCount` | int | Denormalized |
| `progressPercent` | int? | |
| `lifecycleStatus` | string | `draft`, `recruiting`, `active`, `archived` |
| `visibility` | string | `public`, `unlisted`, `private` |
| `projectIntent` | string? | `product`, `recruitment` |
| `searchKeywords` | array\<string\> | |
| `createdAt` | timestamp | |
| `updatedAt` | timestamp | |

---

### `projects/{projectId}/members/{memberUserId}`

| Field | Type | Notes |
|-------|------|-------|
| `memberRole` | string | `owner`, `maintainer`, `contributor`, `viewer` |
| `joinedAt` | timestamp | |

Document ID = member's Auth UID.

---

### `projects/{projectId}/tasks/{taskId}`

Kanban task card.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `title` | string | |
| `boardColumn` | string | `todo`, `in_progress`, `done`, `blocked` |
| `priority` | string | `low`, `medium`, `high`, `urgent` |
| `assigneeUserId` | string? | Auth UID |
| `createdByUserId` | string | Auth UID |
| `createdAt` | timestamp | |
| `updatedAt` | timestamp | |

---

### `conversations/{conversationId}`

Chat thread metadata.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `conversationKind` | string | `direct`, `group`, `project_thread` |
| `title` | string? | Inbox label |
| `projectId` | string? | Linked project |
| `participantIds` | array\<string\> | Auth UIDs (required for rules) |
| `createdBy` | string | Auth UID |
| `lastMessagePreview` | string? | Inbox snippet |
| `lastMessageAt` | timestamp | Sort key |
| `createdAt` | timestamp | |

---

### `conversations/{conversationId}/messages/{messageId}`

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `senderId` | string | Auth UID |
| `body` | string | Message text |
| `messageKind` | string | `text`, `system`, `mention` |
| `readByUserIds` | array\<string\> | Read receipts |
| `createdAt` | timestamp | |

---

### `matchRequests/{requestId}`

Collaboration invites between users.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `fromUserId` | string | |
| `toUserId` | string | |
| `workflowStatus` | string | `pending`, `accepted`, `declined`, `cancelled` |
| `message` | string? | |
| `createdAt` | timestamp | |
| `resolvedAt` | timestamp? | |

**Rules:** Participants can read/update; client delete disabled.

---

### `events/{eventId}`

Curated hackathons / community events.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `title` | string | |
| `summaryLine` | string | One-line card text |
| `startsAt` | timestamp | |
| `timezone` | string | Default `UTC` |
| `participantCount` | int | |
| `formatKind` | string | `online`, `in_person`, `hybrid` |
| `eventStatus` | string | `scheduled`, `live`, `ended`, `cancelled` |

**Rules:** Signed-in read; **admin-only write**.

---

### `inbox/{notificationId}`

Per-user notification feed.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `recipientUserId` | string | Auth UID |
| `notificationKind` | string | `task_update`, `message`, `match`, `project_invite`, `event`, `feed` |
| `title` | string | |
| `body` | string | |
| `deepLink` | string? | In-app route |
| `read` | boolean | |
| `createdAt` | timestamp | |

**Rules:** User can read/update/delete own rows; **create** admin/server only.

---

### `activity/{activityId}`

Personal activity timeline on dashboard.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `audienceUserId` | string | Feed owner UID |
| `verb` | string | `commented`, `status_changed`, `invited`, `joined` |
| `summary` | string | Display line |
| `relatedProjectId` | string? | |
| `relatedConversationId` | string? | |
| `relatedEventId` | string? | |
| `createdAt` | timestamp | |

**Rules:** Client read own feed; **no client writes**.

---

### `newsHighlights/{docId}`

Curated tech news on dashboard.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `title` | string | |
| `sourceName` | string | |
| `sortOrder` | int | Display order |
| `tagLine` | string? | |
| `externalUrl` | string? | |
| `publishedAt` | timestamp | |

**Rules:** Signed-in read; **admin-only write**.

---

### `collaboratorSuggestions/{docId}`

Dashboard “matches” cards.

| Field | Type | Notes |
|-------|------|-------|
| `schemaVersion` | int | |
| `viewerUserId` | string | Who sees the suggestion |
| `suggestedUserId` | string | Matched developer |
| `stackSummary` | string | e.g. Kotlin · Firebase |
| `matchScore` | int | 0–100 |
| `rank` | int | Sort order |
| `availabilityNote` | string? | |
| `updatedAt` | timestamp | |

**Rules:** Viewer can read own docs; **no client writes**.

---

## Collection group indexes

Subcollections queried across projects:

| Collection group | Used by |
|------------------|---------|
| `tasks` | Task list fallback (`FirestoreTasksDataSource`) |
| `messages` | Cross-thread analytics (index defined) |

See `firestore.indexes.json` for composite indexes.

---

## Seed document IDs (dev)

Sample paths in `firestore/SEED_COPY_PASTE.json`:

- Project: `proj_devconnect_mobile`
- Conversations: `conv_team_neon`, `conv_aria_api`, …
- Events: `event_ai_builders_jam`, …
- Demo users: `demo_alex_uid`, …

Replace demo UIDs with real Firebase Auth UIDs for production testing.

---

## Schema versioning

Increment `schemaVersion` on additive field changes. Kotlin `@IgnoreExtraProperties` is not used — unknown fields are ignored by Firestore SDK mapping. Coordinate rule changes with `firestore.rules` deploy.
