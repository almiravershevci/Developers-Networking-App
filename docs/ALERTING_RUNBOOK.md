# Alerting runbook

## BFF health check failing

**Signal:** Render/Railway health check or `GET /health` returns 503  
**Check:** Firestore connectivity, service account validity, `FIREBASE_PROJECT_ID`  
**Action:** Roll back deploy; verify `GOOGLE_APPLICATION_CREDENTIALS_JSON`

## Elevated BFF 5xx rate

**Signal:** `devconnect_http_errors_total` increasing; logs with `status >= 500`  
**Check:** Recent deploy, Firestore quota, invalid indexes  
**Action:** Scale instance; inspect `x-request-id` in logs

## Cloud Functions error spike

**Signal:** Firebase Functions dashboard error count  
**Check:** Blaze billing, missing indexes, permission errors  
**Action:** `firebase functions:log`; redeploy after fix

## Firestore quota / throttling

**Signal:** `RESOURCE_EXHAUSTED` in client or BFF logs  
**Check:** Console → Usage; composite index gaps  
**Action:** Add index; reduce fan-out queries; enable backups before bulk ops

## FCM delivery failures

**Signal:** `onInboxCreated` logs `devicesNotified: 0` or invalid token warnings  
**Check:** `users/{uid}.fcmTokens` populated; Android POST_NOTIFICATIONS granted  
**Action:** Stale tokens are auto-pruned; user re-opens app to refresh token
