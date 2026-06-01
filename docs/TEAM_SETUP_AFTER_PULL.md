# After you pull — teammate checklist

Follow this so Android Studio builds with **no compile errors**.

## 1. Sync the project

1. Open the repo in **Android Studio** (latest stable).
2. **File → Sync Project with Gradle Files**.
3. Wait until sync finishes (no red errors in the Build tool window).

## 2. Required files (already in git)

| File | Purpose |
|------|---------|
| `app/google-services.json` | Firebase Android config — **must exist** for Auth + Firestore |
| `gradle/libs.versions.toml` | Dependency versions |

You do **not** need a local Firebase setup just to **compile**. You need Firebase Console + seed data to **run** features (chat, inbox, etc.).

## 3. Build once

- **Build → Make Project**, or run the **app** configuration on an emulator/device.

If sync fails:

- Install **JDK 17** (Android Studio bundles one: use *Settings → Build → Gradle → Gradle JDK* → Embedded JDK).
- Install Android SDK **API 36** if prompted.

## 4. Runtime (Firestore data)

The app talks to the shared Firebase project in `google-services.json`.

- Publish rules: copy `firestore/RULES_PASTE_IN_CONSOLE.rules` into Firebase Console → Firestore → Rules → **Publish**.
- Optional seed: see `firestore/README.txt`.
- Use your own Auth account; link seed docs to your UID (chat `participantIds`, inbox `recipientUserId`) — see `docs/BACKEND_ENGINEER_GUIDE.md`.

## 5. Data layer layout (if you edit backend code)

```
data/repository/           ← interfaces (AuthRepository, ChatRepository, …)
data/repository/impl/      ← Firestore implementations — import parent interfaces explicitly
data/datasource/firebase/  ← Firestore + Auth IO
data/datasource/remote/    ← Retrofit APIs
```

Implementations in `repository.impl` **must** import `com.example.developernetworkingapp.data.repository.*` (subpackages do not inherit parent types).

## 6. Still broken?

1. **File → Invalidate Caches → Invalidate and Restart**
2. Delete `.gradle` in the project root and sync again
3. Confirm you pulled **all** changes (including **deleted** old paths under `data/firestore/` and `data/remote/`)
