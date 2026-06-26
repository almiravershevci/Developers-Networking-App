# Data retention

| Collection | Retention | Mechanism |
|------------|-----------|-----------|
| `inbox` | 90 days read items | Future scheduled cleanup (Functions cron) |
| `activity` | 180 days | Future TTL / batch delete |
| `messages` | Indefinite | User-facing chat history |
| Audit via `activity` (admin) | 1 year | Ops review |

**v1:** Document policy; automated purge tracked in `BACKEND_ROADMAP.md`.
