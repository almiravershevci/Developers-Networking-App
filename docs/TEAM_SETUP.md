# Team setup (5 minutes)

1. Clone repo and open in Android Studio (SDK **37**, Gradle wrapper).
2. Add `google-services.json` from Firebase Console (shared project).
3. Sync Gradle → Run app → sign up / sign in.
4. Optional lead: `cd firestore && npm ci && npm run team:sync-access`
5. Optional BFF: `cd backend && cp .env.example .env` → set credentials → `npm start`

See also: [TEAM_SETUP_AFTER_PULL.md](./TEAM_SETUP_AFTER_PULL.md), [TEAM_VERIFY.md](./TEAM_VERIFY.md).
