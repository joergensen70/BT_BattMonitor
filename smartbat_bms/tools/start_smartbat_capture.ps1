param(
    [string]$AdbPath = "C:\Users\joerg\AppData\Local\Android\Sdk\platform-tools\adb.exe",
    [string]$Serial = "",
    [string]$OutRoot = ".\captures"
)

$ErrorActionPreference = "Stop"

function Get-AdbPrefix {
    param([string]$Adb, [string]$DeviceSerial)
    if ([string]::IsNullOrWhiteSpace($DeviceSerial)) {
        return @($Adb)
    }
    return @($Adb, "-s", $DeviceSerial)
}

if (-not (Test-Path $AdbPath)) {
    throw "ADB not found at: $AdbPath"
}

$adb = Get-AdbPrefix -Adb $AdbPath -DeviceSerial $Serial

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outDir = Join-Path $OutRoot ("capture_" + $timestamp)
New-Item -ItemType Directory -Path $outDir -Force | Out-Null

$statePath = Join-Path $OutRoot "current_capture_state.json"

& $AdbPath start-server | Out-Null
if ([string]::IsNullOrWhiteSpace($Serial)) {
    & $AdbPath wait-for-device
} else {
    & $AdbPath -s $Serial wait-for-device
}

# Try to enable Bluetooth HCI snoop log through global settings.
try {
    if ([string]::IsNullOrWhiteSpace($Serial)) {
        & $AdbPath shell settings put global bluetooth_btsnoop_log_mode full | Out-Null
        & $AdbPath shell settings get global bluetooth_btsnoop_log_mode | Out-File (Join-Path $outDir "btsnoop_mode.txt")
    } else {
        & $AdbPath -s $Serial shell settings put global bluetooth_btsnoop_log_mode full | Out-Null
        & $AdbPath -s $Serial shell settings get global bluetooth_btsnoop_log_mode | Out-File (Join-Path $outDir "btsnoop_mode.txt")
    }
} catch {
    "Failed to set bluetooth_btsnoop_log_mode: $($_.Exception.Message)" | Out-File (Join-Path $outDir "warnings.txt") -Append
}

# Clean old logs on host side and clear Android logcat buffer.
if ([string]::IsNullOrWhiteSpace($Serial)) {
    & $AdbPath logcat -c
    & $AdbPath shell getprop ro.product.model | Out-File (Join-Path $outDir "device_model.txt")
    & $AdbPath shell getprop ro.build.fingerprint | Out-File (Join-Path $outDir "build_fingerprint.txt")
} else {
    & $AdbPath -s $Serial logcat -c
    & $AdbPath -s $Serial shell getprop ro.product.model | Out-File (Join-Path $outDir "device_model.txt")
    & $AdbPath -s $Serial shell getprop ro.build.fingerprint | Out-File (Join-Path $outDir "build_fingerprint.txt")
}

$logcatFile = Join-Path $outDir "logcat_threadtime.txt"
$stderrFile = Join-Path $outDir "logcat_stderr.txt"

$logcatArgs = @()
if (-not [string]::IsNullOrWhiteSpace($Serial)) {
    $logcatArgs += @("-s", $Serial)
}
$logcatArgs += @("logcat", "-v", "threadtime", "flutter:D", "BluetoothGatt:D", "BtGatt.GattService:D", "BluetoothManagerService:D", "*:S")

$proc = Start-Process -FilePath $AdbPath -ArgumentList $logcatArgs -RedirectStandardOutput $logcatFile -RedirectStandardError $stderrFile -PassThru

$state = [ordered]@{
    timestamp = $timestamp
    outDir = (Resolve-Path $outDir).Path
    adbPath = $AdbPath
    serial = $Serial
    logcatPid = $proc.Id
    createdAt = (Get-Date).ToString("o")
}
$state | ConvertTo-Json | Out-File -Encoding utf8 $statePath

Write-Host "Capture started."
Write-Host "OutDir: $((Resolve-Path $outDir).Path)"
Write-Host "Logcat PID: $($proc.Id)"
Write-Host "When done, run: .\tools\stop_smartbat_capture.ps1"
