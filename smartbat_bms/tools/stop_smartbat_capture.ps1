param(
    [string]$OutRoot = ".\captures",
    [switch]$WithBugreport
)

$ErrorActionPreference = "Stop"

$statePath = Join-Path $OutRoot "current_capture_state.json"
if (-not (Test-Path $statePath)) {
    throw "No active capture state found at: $statePath"
}

$state = Get-Content $statePath | ConvertFrom-Json
$adb = $state.adbPath
$serial = $state.serial
$outDir = $state.outDir

if (-not (Test-Path $outDir)) {
    throw "Capture output directory missing: $outDir"
}

function Invoke-Adb {
    param(
        [string]$AdbPath,
        [string]$Serial,
        [string[]]$Args
    )
    if ([string]::IsNullOrWhiteSpace($Serial)) {
        & $AdbPath @Args
    } else {
        & $AdbPath -s $Serial @Args
    }
}

# Stop logcat process if still alive.
try {
    $p = Get-Process -Id $state.logcatPid -ErrorAction Stop
    Stop-Process -Id $p.Id -Force
} catch {
    "Logcat process already stopped." | Out-File (Join-Path $outDir "warnings.txt") -Append
}

# Collect Bluetooth diagnostics.
Invoke-Adb -AdbPath $adb -Serial $serial -Args @("shell", "dumpsys", "bluetooth_manager") | Out-File (Join-Path $outDir "dumpsys_bluetooth_manager.txt")
Invoke-Adb -AdbPath $adb -Serial $serial -Args @("shell", "dumpsys", "bluetooth_gatt") | Out-File (Join-Path $outDir "dumpsys_bluetooth_gatt.txt")
Invoke-Adb -AdbPath $adb -Serial $serial -Args @("shell", "dumpsys", "activity", "processes") | Out-File (Join-Path $outDir "dumpsys_activity_processes.txt")

# Pull likely HCI snoop locations (availability depends on Android/OEM).
$paths = @(
    "/sdcard/btsnoop_hci.log",
    "/sdcard/Android/data/btsnoop_hci.log",
    "/data/misc/bluetooth/logs/btsnoop_hci.log"
)

foreach ($remote in $paths) {
    $safeName = ($remote -replace "[:/\\]", "_").Trim("_")
    $target = Join-Path $outDir ("pull_" + $safeName)
    try {
        Invoke-Adb -AdbPath $adb -Serial $serial -Args @("pull", $remote, $target) | Out-Null
    } catch {
        "Could not pull $remote" | Out-File (Join-Path $outDir "warnings.txt") -Append
    }
}

if ($WithBugreport) {
    # This can take several minutes and produce large files.
    $bugreportZip = Join-Path $outDir "bugreport.zip"
    try {
        Invoke-Adb -AdbPath $adb -Serial $serial -Args @("bugreport", $bugreportZip)
    } catch {
        "Bugreport failed: $($_.Exception.Message)" | Out-File (Join-Path $outDir "warnings.txt") -Append
    }
}

# Save final state summary.
Get-ChildItem -Path $outDir -File | Select-Object Name, Length, LastWriteTime | Out-File (Join-Path $outDir "capture_manifest.txt")
Remove-Item $statePath -Force

Write-Host "Capture stopped and files collected."
Write-Host "OutDir: $outDir"
Write-Host "Next: share observed values and this folder for analysis."
