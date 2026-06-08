# Firestore scheduled backups

Production-grade backup strategy using **Google Cloud Firestore managed exports** to a GCS bucket.

---

## Prerequisites

- Firebase **Blaze** plan
- `gcloud` CLI installed and authenticated
- A GCS bucket in the same region as Firestore (e.g. `developers-networking-app-backups`)

---

## One-time setup

```powershell
# 1. Login and set project
gcloud auth login
gcloud config set project developers-networking-app

# 2. Create backup bucket (adjust region if needed)
gsutil mb -l us-central1 gs://developers-networking-app-backups

# 3. Grant Firestore service account export permission
# Find the service account in Firebase Console → Project Settings → Service accounts
# Then:
gsutil iam ch serviceAccount:service-639111310168@gcp-sa-firestore.iam.gserviceaccount.com:objectAdmin gs://developers-networking-app-backups
```

Replace the service account email with your project's Firestore service agent (Project number from Firebase Console).

---

## Manual export

```powershell
gcloud firestore export gs://developers-networking-app-backups/manual-$(Get-Date -Format yyyyMMdd-HHmm)
```

---

## Scheduled daily backup (Cloud Scheduler)

Run once from Cloud Shell or local gcloud:

```bash
gcloud scheduler jobs create http firestore-daily-backup \
  --location=us-central1 \
  --schedule="0 3 * * *" \
  --uri="https://firestore.googleapis.com/v1/projects/developers-networking-app/databases/(default):exportDocuments" \
  --http-method=POST \
  --oauth-service-account-email=YOUR_SERVICE_ACCOUNT@developers-networking-app.iam.gserviceaccount.com \
  --message-body='{"outputUriPrefix":"gs://developers-networking-app-backups/scheduled"}'
```

Or use the helper script:

```powershell
.\scripts\firestore-backup.ps1
```

---

## Restore (disaster recovery)

```powershell
gcloud firestore import gs://developers-networking-app-backups/manual-YYYYMMDD-HHMM
```

**Warning:** Import overwrites existing data. Test on a staging Firebase project first.

---

## Retention policy

Set bucket lifecycle to delete objects older than 30 days:

```json
{
  "lifecycle": {
    "rule": [{
      "action": { "type": "Delete" },
      "condition": { "age": 30 }
    }]
  }
}
```

Apply: `gsutil lifecycle set lifecycle.json gs://developers-networking-app-backups`

---

## Course project note

For demos, a **manual export before major releases** is sufficient. Scheduled backups show production maturity in presentations.
