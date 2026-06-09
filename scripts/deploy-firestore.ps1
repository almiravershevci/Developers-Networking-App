# Deploy Firestore security rules and composite indexes to Firebase.
# Prerequisite: run `firebase login` with the Google account that OWNS this Firebase project.

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot\..

Write-Host "Logged-in Firebase accounts:" -ForegroundColor Cyan
firebase login:list

Write-Host "`nActive Firebase project:" -ForegroundColor Cyan
firebase use

Write-Host "`nDeploying Firestore rules + indexes..." -ForegroundColor Cyan
firebase deploy --only firestore
if ($LASTEXITCODE -ne 0) {
    Write-Host "`nDeploy FAILED (exit $LASTEXITCODE)." -ForegroundColor Red
    Write-Host "If you see HTTP 403: sign in with the project owner account:" -ForegroundColor Yellow
    Write-Host "  firebase logout" -ForegroundColor Yellow
    Write-Host "  firebase login" -ForegroundColor Yellow
    Write-Host "Then rerun: .\scripts\deploy-firestore.ps1" -ForegroundColor Yellow
    exit $LASTEXITCODE
}

Write-Host "`nDeploy complete. Wait 2-10 minutes for new indexes to show Status: Enabled." -ForegroundColor Green
