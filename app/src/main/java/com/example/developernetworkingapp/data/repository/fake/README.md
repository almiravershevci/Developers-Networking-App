# Preview-only fakes

`Fake*Repository` classes live next to their interfaces in `repository/` for Compose `@Preview`.
They are **not** wired in `AppContainer` — runtime uses `repository.impl.*Firestore` classes.

`InMemoryAdminRepository` seeds admin UI placeholders (tickets/feedback) until those collections exist in Firestore.
