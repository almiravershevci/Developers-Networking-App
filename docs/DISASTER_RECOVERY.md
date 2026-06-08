# Disaster recovery

## RTO / RPO (targets)

| Asset | RTO | RPO |
|-------|-----|-----|
| Firestore data | 4 h | 24 h (daily export) |
| Cloud Functions | 1 h | last deploy |
| Node BFF | 1 h | last deploy |

## Firestore restore

1. Identify backup in GCS (`docs/FIRESTORE_BACKUPS.md`)
2. Import to staging project first when possible
3. Validate rules + indexes before production import

## Team access after restore

Run `npm run team:sync-access` in `firestore/` to re-grant showcase project + chat membership.

## Contacts

Repository maintainer / course lead owns Firebase Console and service accounts.
