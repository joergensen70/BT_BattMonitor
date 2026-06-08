# Odin SmartBat - Release Build Script
# Usage: .\release.ps1 [-Version "1.0"]

param(
    [string]$Version = "1.0"
)

$ErrorActionPreference = "Stop"
$ProjectDir  = $PSScriptRoot
$RelDir      = Join-Path $ProjectDir "rel"
$ApkSrc      = Join-Path $ProjectDir "build\app\outputs\flutter-apk\app-release.apk"
$ApkDest     = Join-Path $RelDir "odin-smartbat-v$Version.apk"

Write-Host ""
Write-Host "=== Odin SmartBat Release Build v$Version ===" -ForegroundColor Cyan
Write-Host ""

# Build
Write-Host "[1/2] Building release APK..." -ForegroundColor Yellow
Push-Location $ProjectDir
try {
    flutter build apk --release
    if ($LASTEXITCODE -ne 0) { throw "flutter build failed (exit $LASTEXITCODE)" }
} finally {
    Pop-Location
}

# Copy to rel/
Write-Host ""
Write-Host "[2/2] Copying to rel\ ..." -ForegroundColor Yellow
if (-not (Test-Path $RelDir)) { New-Item -ItemType Directory -Path $RelDir | Out-Null }
Copy-Item -Path $ApkSrc -Destination $ApkDest -Force

Write-Host ""
Write-Host "Release ready:" -ForegroundColor Green
Write-Host "  $ApkDest" -ForegroundColor Green
Write-Host ""
