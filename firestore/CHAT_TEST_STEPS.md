# Chat test checklist (Firestore + app)

You must be in **participantIds** on a conversation or Firestore rules block reads.

**Related docs:** [docs/BACKEND_API.md](../docs/BACKEND_API.md) · [docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md) · [docs/FIRESTORE_SCHEMA.md](../docs/FIRESTORE_SCHEMA.md)

---

## Step 1 — Get your Firebase Auth UID

1. Open [Firebase Console](https://console.firebase.google.com/) → your project.
2. **Build** → **Authentication** → **Users**.
3. Find your account (your teammate email).
4. Copy the **User UID** (long string — unique per account).

Keep this UID for the steps below.

---

## Step 2 — Add yourself to conversations

Pick **one** method.

### Option A — Script (fastest if you already seeded with a service account)

PowerShell from repo root:

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\your-serviceAccount.json"
$env:FIREBASE_PROJECT_ID="your-firebase-project-id"
$env:CHAT_TEST_UID="PASTE_YOUR_UID_HERE"
cd firestore
npm install
npm run chat:add-me
```

Dry run (prints only):

```powershell
npm run chat:add-me:dry
```

### Option B — Firebase Console (no script)

For **each** document below: **Firestore** → **conversations** → open doc → edit **participantIds** → add your UID as a new array item → **Update**.

| Document ID           | Current participantIds (add yours to the list)        |
|-----------------------|--------------------------------------------------------|
| `conv_team_neon`      | `demo_alex_uid`, `demo_mina_uid`, **YOUR_UID**         |
| `conv_aria_api`       | `demo_alex_uid`, `demo_aria_uid`, **YOUR_UID**         |
| `conv_khaled_platform`| `demo_alex_uid`, `demo_khaled_uid`, **YOUR_UID**       |
| `conv_hackathon_squad`| `demo_alex_uid`, `demo_mina_uid`, `demo_khaled_uid`, **YOUR_UID** |
| `conv_design_crew`    | `demo_lina_uid`, `demo_nora_uid`, `demo_alex_uid`, **YOUR_UID** |

Example for `conv_team_neon` in the console:

```json
["demo_alex_uid", "demo_mina_uid", "YOUR_UID_HERE"]
```

If a conversation document does not exist, import seed data first (`npm run seed` in `firestore/` or create docs from `SEED_COPY_PASTE.json`).

---

## Step 3 — Rules and indexes

1. **Rules** — Project root, if not deployed recently:

   ```powershell
   firebase deploy --only firestore:rules
   ```

   Or paste `firestore.rules` / `firestore/RULES_PASTE_IN_CONSOLE.rules` in Console → Firestore → **Rules** → **Publish**.

2. **Indexes** — Only if the app or Console shows an index link/error:

   ```powershell
   firebase deploy --only firestore:indexes
   ```

---

## Step 4 — Test in the Android app

1. **Sign in** with the same account whose UID you added.
2. **Verify email** (Chat requires verified email, same as Tasks/Projects).
3. Optional: test **username login** or **Google Sign-In** if configured (see README Authentication section).
4. Open **Chat** tab.
5. You should see up to **5 conversations** (titles like “Team Neon - Sprint planning thread”).
6. Tap **Open** on one thread — messages from seed should load (realtime).
7. Type a message and **Send** — it should appear and persist in Console under  
   `conversations/{id}/messages`.
8. Try a **quick room** chip (e.g. Project Room → `conv_team_neon`).
9. **Account deletion** (Settings) removes your profile and inbox rows but not shared chat history — expected per [BACKEND_API.md](../docs/BACKEND_API.md).

### If you see “Couldn’t load conversations” (0 active)

That is usually **Firestore rules blocking the inbox query**, not a network problem.

1. **Publish updated rules** (required fix):
   - Console → **Firestore** → **Rules**
   - Paste all of `firestore/RULES_PASTE_IN_CONSOLE.rules` (or `firestore.rules` from the repo)
   - Click **Publish**
   - The chat inbox needs `allow read: if request.auth.uid in resource.data.participantIds` on `conversations` (not only a `get()`-based rule).

2. **Add your Auth UID** to at least `conversations/conv_team_neon` → `participantIds` (see Step 2 above).

3. **Force-stop** the app and open **Chat** again.

### If inbox is still empty (no error card)

| Check | What to do |
|-------|------------|
| Wrong UID | UID in Firestore must match **Authentication → Users**, not a random doc id under `users/`. |
| Email not verified | Complete verification flow in app. |
| Rules not published | Deploy/publish rules (Step 3). |
| No seed data | Run `npm run seed` in `firestore/` or create `conversations/*` manually. |
| Stale UI | Force-stop app, reopen, or log out and back in. |

### If a thread opens but shows an error

- **Permission denied** — UID missing from that conversation’s `participantIds`.
- **No messages** — Open Console → `conversations/{id}/messages`; seed should have `msg_*` docs.

---

## Step 5 — Optional: confirm in Console

**Firestore** → `conversations` → `conv_team_neon` → **messages** — you should see seeded messages and any you sent from the app.

---

## Whole-team shortcut (recommended)

Backend lead runs once (adds **all** Auth users to chats + project):

```powershell
$env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"
$env:FIREBASE_PROJECT_ID="developers-networking-app"
cd firestore
npm run team:sync-access
```

See `docs/TEAM_SETUP_AFTER_PULL.md`.
