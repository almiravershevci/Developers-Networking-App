# Prepare Render env var value from local service account.
# Run from repo root (requires GOOGLE_APPLICATION_CREDENTIALS env var).

$ErrorActionPreference = "Stop"

$credPath = $env:GOOGLE_APPLICATION_CREDENTIALS
if (-not $credPath -or -not (Test-Path $credPath)) {
    Write-Host "Set GOOGLE_APPLICATION_CREDENTIALS to your service account JSON path first." -ForegroundColor Red
    exit 1
}

$json = (Get-Content -Raw -Path $credPath).Trim()
$renderUrl = "https://dashboard.render.com/select-repo?type=blueprint"

Write-Host ""
Write-Host "=== Render deploy checklist ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Open Render Blueprint:"
Write-Host "   $renderUrl"
Write-Host ""
Write-Host "2. Connect repo: almiravershevci/Developers-Networking-App"
Write-Host "3. Render reads render.yaml and creates devconnect-api"
Write-Host ""
Write-Host "4. In the new service -> Environment, add:"
Write-Host "   GOOGLE_APPLICATION_CREDENTIALS_JSON = (paste JSON below)"
Write-Host "   FIREBASE_PROJECT_ID = developers-networking-app"
Write-Host ""
Write-Host "5. After deploy, copy HTTPS URL (e.g. https://devconnect-api.onrender.com)"
Write-Host "6. Update app/build.gradle.kts release buildConfigField API_BASE_URL"
Write-Host ""

try {
    Set-Clipboard -Value $json
    Write-Host "Service account JSON copied to clipboard for Render." -ForegroundColor Green
} catch {
    Write-Host "Copy JSON manually from: $credPath" -ForegroundColor Yellow
}

Start-Process $renderUrl
