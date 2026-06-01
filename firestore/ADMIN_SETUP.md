# Admin console setup (Firestore)

The Admin dashboard uses **real Firestore data** when your account has `accountRole: admin`.

## 1. Publish rules

Paste `firestore/RULES_PASTE_IN_CONSOLE.rules` (or deploy `firestore.rules`) so `isAdmin()` is active.

Admin can:

- Read/write all `users` and `projects`
- Create `inbox` notifications (broadcast)
- Manage `events` and `newsHighlights`
- Read/update `matchRequests`

## 2. Grant yourself admin

Firebase Console → **Firestore** → `users` → **your Auth UID document**

Add or edit field:

| Field | Value |
|-------|--------|
| `accountRole` | `admin` |

(Keep `schemaVersion`, `displayName`, `email`, etc. unchanged.)

**Alternative:** Sign up with `admin@devconnect.app` — signup assigns `accountRole: admin` automatically.

## 3. Open Admin in the app

1. Sign in with the admin account  
2. **Profile** → **Admin Dashboard**  
3. Overview should show: `Firestore · N users · M projects`

## 4. What is real vs local

| Section | Source |
|---------|--------|
| Users, Projects | Firestore directory |
| Content queue | Pending `matchRequests` |
| Send notification | Writes to `inbox` for each recipient |
| Audit log | In-app session log (not persisted yet) |
| Support tickets | Demo placeholders until a `supportTickets` collection exists |

## 5. Moderation actions

- **Ban / Deactivate** → sets `accountRole` to `banned` / `deactivated` (login blocked)
- **Approve project** → sets `lifecycleStatus` to `active`
- **Approve all queue** → accepts pending match requests in Firestore
