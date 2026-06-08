# Teammate verification checklist

Quick checks so **everyone** can pull, build, and run without backend setup.

---

## For every teammate (after `git pull`)

| Step | Expected result |
|------|-----------------|
| Android Studio → Sync Gradle | ✅ No errors |
| Run on emulator | ✅ App opens |
| Sign up + verify email | ✅ Dashboard loads |
| Open Tasks | ✅ Kanban board visible |
| Open Chat | ✅ Conversations listed |
| Open Alerts | ✅ Inbox items (may be empty for new users) |

**You do NOT need:** Node backend, service account, Render, or Blaze plan.

---

## What uses what

| Feature | Data source | Needs local backend? |
|---------|-------------|-------------------|
| Login, profile | Firebase Auth + Firestore | No |
| Tasks, chat, match, events | Firestore SDK | No |
| Dashboard stats | Firestore `userStats` | No |
| Dashboard REST overlay | Node API | No (disabled: `ENABLED = false`) |
| Inbox automation, push | Cloud Functions | No for *using* app; lead deploys once |

---

## If something fails

| Symptom | Fix (team lead) |
|---------|-----------------|
| `PERMISSION_DENIED` on tasks | `cd firestore && npm run team:sync-access` |
| Empty chat | Same as above |
| No inbox after task move | Deploy Cloud Functions (Blaze) |
| No push notifications | Deploy Functions + allow notification permission |
| Gradle / JDK error | Android Studio → Embedded JDK 17 |

---

## Lead verification (before telling team "all good")

```powershell
cd backend && npm test                    # 18/18 pass
cd functions && npm run build               # TypeScript compiles
firebase deploy --only firestore:rules,firestore:indexes   # already done
cd firestore && npm run team:sync-access    # all Auth users onboarded
```

Optional: `cd backend && npm start` → http://localhost:5000/health returns `"status":"ok"`.

---

## Automated CI (GitHub)

On every push/PR, `.github/workflows/ci.yml` runs:
- Backend tests
- Functions build
- Firestore rules tests
- Android `assembleDebug`

If CI is green, teammates are safe to pull.
