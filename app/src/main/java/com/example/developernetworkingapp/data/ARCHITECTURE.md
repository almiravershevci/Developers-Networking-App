# Data layer architecture

## Runtime wiring (`AppContainer`)

All features use **Firestore** implementations except REST trends:

- `repository.impl.AuthRepositoryFirebase`
- `repository.impl.DashboardRepositoryFirestore`, `ProfileRepositoryFirestore`, `ProjectsRepositoryFirestore`, `TasksRepositoryFirestore`
- `repository.impl.ChatRepositoryFirestore`, `EventsRepositoryFirestore`, `NotificationsRepositoryFirestore`, `SearchRepositoryFirestore`, `AdminRepositoryFirestore`
- `ApiTechTrendsRepository` → `datasource/remote/TechTrendsApi`

`repository/fake/*` is for **@Preview** only.

## Package layout

| Package | Role |
|---------|------|
| `data.repository` | Interfaces + shared types (`AuthUser`, etc.) |
| `data.repository.impl` | Firestore repository implementations (import `data.repository.*` interfaces explicitly) |
| `data.repository.fake` | In-memory / fake (previews) |
| `data.datasource.firebase` | Firebase Auth + Firestore access |
| `data.datasource.firebase.schema` | Firestore document DTOs |
| `data.datasource.remote` | Retrofit APIs |
| `data.mapper` | DTO → domain conversions |
| `domain.model` | UI-facing models (ViewModels) |

## Data flow

```
Firestore document  →  Mapper  →  domain model  →  ViewModel  →  Compose UI
```
