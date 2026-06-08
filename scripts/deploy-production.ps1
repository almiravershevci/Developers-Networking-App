# Run from repo root after Blaze upgrade and service account setup.
# Requires: firebase login, Java 17+ (for rules tests), Node 20.

param(
    [switch]$SkipTests,
    [switch]$SkipFunctions,
    [switch]$SkipBackend
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path

Write-Host "=== DevConnect production prep ===" -ForegroundColor Cyan

if (-not $SkipTests) {
    Write-Host "`n[1/4] Backend tests..." -ForegroundColor Yellow
    Push-Location "$root\backend"
    npm test
    Pop-Location

    Write-Host "`n[2/4] Functions build..." -ForegroundColor Yellow
    Push-Location "$root\functions"
    npm run build
    Pop-Location

    Write-Host "`n[3/4] Firestore rules tests (requires Java)..." -ForegroundColor Yellow
    Push-Location "$root\firestore"
    npm run test:rules
    Pop-Location
} else {
    Write-Host "Skipping tests (-SkipTests)" -ForegroundColor DarkGray
}

Write-Host "`n[4/4] Firebase deploy (rules + indexes + functions)..." -ForegroundColor Yellow
Push-Location $root
firebase deploy --only firestore:rules,firestore:indexes,functions --project developers-networking-app
Pop-Location

if (-not $SkipBackend) {
    Write-Host "`nNode API: deploy via Render Blueprint (render.yaml) — see docs/PRODUCTION_DEPLOYMENT.md" -ForegroundColor Green
}

Write-Host "`nDone." -ForegroundColor Green
