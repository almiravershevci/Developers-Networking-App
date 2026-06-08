# Security model — DevConnect

Lightweight threat model for reviewers and teammates.

## Trust boundaries

| Layer | Trust | Enforcement |
|-------|-------|-------------|
| Android app | Untrusted client | Firebase Auth + Firestore rules |
| Node REST BFF | Semi-trusted service | Firebase JWT + `authorization.js` |
| Cloud Functions | Trusted server | Admin SDK + triggers only |
| Firestore | Source of truth | `firestore.rules` deny-by-default |

## Authentication

- **Clients:** Firebase ID token (`Authorization: Bearer`)
- **BFF:** Verifies token via Firebase Admin (`middleware/firebaseAuth.js`)
- **Admin routes:** Additional `accountRole == admin` check (`requireAdmin.js`)

## Authorization highlights

### Firestore rules block
- Direct writes to `userStats`, `activity` create, client `inbox` create
- Private project reads without membership
- Cross-user inbox reads
- Username list scraping (`usernames` list: false)

### BFF must enforce (Admin SDK bypasses rules)
- Project read access (`lib/authorization.js` mirrors `canReadProjectDoc`)
- Conversation participant checks before messages
- Inbox notification ownership on mark-read

## Transport

- Production BFF should use HTTPS (Render/Railway)
- Helmet headers enabled on Node API
- Rate limits: 120 req/15m (read), 30 req/min (write); Redis when configured

## Secrets

- Never commit service account JSON
- Use `GOOGLE_APPLICATION_CREDENTIALS_JSON` on hosted BFF
- GitHub `FIREBASE_SERVICE_ACCOUNT_JSON` for deploy workflow only

## Reporting

Course / team project — report issues to the repository maintainer.
