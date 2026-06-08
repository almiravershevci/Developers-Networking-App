# Developer Networking App

Android app for developer collaboration: project boards, realtime chat, talent search, events, notifications, and admin tooling. Built with **Jetpack Compose**, **MVVM**, and **Firebase** (Auth + Firestore).

## Architecture

- **UI**: `ui/screens` (Route + Screen), `ui/viewmodel`, `ui/state`, `ui/navigation`
- **Data**: `data/repository` + Firestore implementations
- **DI**: `di/AppContainer` + `di/ViewModelFactory` + `appViewModel()` in Compose

See [ui/ARCHITECTURE.md](app/src/main/java/com/example/developernetworkingapp/ui/ARCHITECTURE.md) for layer rules.

## Presentation demo flow (5 minutes)

1. **Sign up / log in** — Create an account or use a seeded test user.
2. **Verify email** — Open the Firebase verification link on the device, return to the app, enter any 6 digits, tap **Verify** → lands on the dashboard.
3. **Dashboard** — Review feed, collaborator matches (tap **View** for full profile), and shortcuts to Tasks / Events / Chat.
4. **Projects** — Kanban board; deep-link with `projects?project=DevConnect Mobile` for showcase workspace.
5. **Chat** — Use quick rooms (Project Room, Mentorship, etc.) or inbox threads.
6. **Profile & Settings** — Edit profile; settings persist locally (notifications, privacy toggles).
7. **Admin** (admin role only) — Open from profile; full admin console with user/project/content tools.

## Team setup

**Teammates:** pull → sync Gradle → sign up → verify email → run.  
Details: [docs/TEAM_SETUP_AFTER_PULL.md](docs/TEAM_SETUP_AFTER_PULL.md)

**Backend lead:** deploy rules/indexes/functions + `cd firestore && npm run team:sync-access` so Tasks and Chat work for every Auth user.

## Build & run

**Requirements:** Android Studio, JDK 17+, `google-services.json` in `app/` (already in repo).

```bash
./gradlew assembleDebug
```

Install the debug APK on a device or emulator (API 24+).

## Firebase setup

1. Add your Android app in the Firebase console and place `google-services.json` under `app/`.
2. Enable **Email/Password** authentication.
3. Deploy Firestore rules and seed data (see `firestore/` if present in the repo).
4. Sign in with a seeded user or register a new account and verify email.

Without seed data, the app still runs: screens show friendly empty states instead of raw developer error strings.

## Key packages

| Package | Purpose |
|---------|---------|
| `ui/navigation/AppNavHost.kt` | Root navigation graph |
| `ui/navigation/MainAppScaffold.kt` | Bottom nav shell |
| `di/ViewModelFactory.kt` | ViewModel construction |
| `data/repository/impl/*` | Firestore-backed repositories |

## License

Academic / portfolio use — see repository owner for terms.
