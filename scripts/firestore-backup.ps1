# Trigger a Firestore export to GCS (requires gcloud + Blaze plan).
param(
    [string]$Bucket = "gs://developers-networking-app-backups",
    [string]$Project = "developers-networking-app"
)

$ErrorActionPreference = "Stop"
$stamp = Get-Date -Format "yyyyMMdd-HHmm"
$destination = "$Bucket/manual-$stamp"

Write-Host "Exporting Firestore -> $destination" -ForegroundColor Cyan
gcloud config set project $Project
gcloud firestore export $destination

Write-Host "Done. See docs/FIRESTORE_BACKUPS.md for scheduled backups." -ForegroundColor Green
