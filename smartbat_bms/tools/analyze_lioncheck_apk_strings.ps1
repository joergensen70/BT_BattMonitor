param(
    [string]$ApkPath = ".\analysis\lioncheck_apk\base.apk"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path $ApkPath)) {
    throw "APK not found: $ApkPath"
}

$bytes = [System.IO.File]::ReadAllBytes((Resolve-Path $ApkPath))
$text = [System.Text.Encoding]::ASCII.GetString($bytes)

$patterns = @(
    '\+RAA[0-9A-F]{4}',
    'writeCharacteristic',
    'BluetoothGatt',
    'MessageDigest',
    'Cipher',
    'Mac',
    'AES',
    'CRC',
    'Checksum',
    'FFF[0-9A-F]'
)

$regex = ($patterns -join '|')

$matches = ($text | Select-String -Pattern $regex -AllMatches).Matches.Value
if (-not $matches -or $matches.Count -eq 0) {
    Write-Host "No marker strings found."
    exit 0
}

$matches |
    Group-Object |
    Sort-Object Count -Descending |
    ForEach-Object { "{0}x {1}" -f $_.Count, $_.Name }
