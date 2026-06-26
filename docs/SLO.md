# Service levels (SLO) — DevConnect backend

Targets for the **Node REST BFF** and **Cloud Functions** automation layer.

| Service | Availability | Latency (p99) | Notes |
|---------|--------------|---------------|-------|
| BFF `/health` | 99.5% / 30d | < 500 ms | Firestore + Auth probe |
| BFF read APIs | 99.0% / 30d | < 800 ms | Excludes client network |
| Cloud Functions triggers | 99.0% / 30d | < 5 s | Cold starts acceptable on Blaze |
| FCM dispatch (`onInboxCreated`) | Best effort | < 30 s | Depends on device tokens |

## Error budget

- **5xx rate** on BFF: alert if > 1% over 15 minutes
- **Function failures**: alert if error ratio > 5% over 1 hour

## Non-goals (v1)

- Multi-region active-active
- Sub-100 ms global API latency
- Guaranteed push delivery (FCM is best-effort)

## Measurement

- BFF: `GET /metrics` (Prometheus) + structured JSON logs with `durationMs`
- Functions: Firebase console + Cloud Logging filters on `severity>=ERROR`
