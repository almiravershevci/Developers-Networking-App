# Paste Firebase service account JSON into GitHub Actions secret.
# Run from repo root (requires GOOGLE_APPLICATION_CREDENTIALS env var).

$ErrorActionPreference = "Stop"

$credPath = $env:GOOGLE_APPLICATION_CREDENTIALS
if (-not $credPath -or -not (Test-Path $credPath)) {
    Write-Host "Set GOOGLE_APPLICATION_CREDENTIALS to your service account JSON path first." -ForegroundColor Red
    Write-Host 'Example: $env:GOOGLE_APPLICATION_CREDENTIALS="C:\path\to\serviceAccount.json"'
    exit 1
}

$json = Get-Content -Raw -Path $credPath
$repo = "almiravershevci/Developers-Networking-App"
$secretUrl = "https://github.com/$repo/settings/secrets/actions/new"

Write-Host ""
Write-Host "=== GitHub secret: FIREBASE_SERVICE_ACCOUNT_JSON ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. Opening GitHub secret page..."
Start-Process $secretUrl

Write-Host "2. Name:  FIREBASE_SERVICE_ACCOUNT_JSON"
Write-Host "3. Value: paste the ENTIRE JSON below (one blob):"
Write-Host ""
Write-Host $json
Write-Host ""
Write-Host "4. Save, then go to Actions -> Deploy Firebase -> Run workflow"
Write-Host ""

# Copy to clipboard on Windows if available
try {
    Set-Clipboard -Value $json
    Write-Host "JSON copied to clipboard." -ForegroundColor Green
} catch {
    Write-Host "Copy the JSON above manually." -ForegroundColor Yellow
}
