param(
    [ValidateSet("run1", "run2", "run3", "custom", "gateway")]
    [string]$Plan = "run1",
    [int]$DurationSec = 60,
    [int]$EventOnSec = 20,
    [int]$EventOffSec = 40,
    [int]$StartLeadSec = 15,
    [int]$PreAlertSec = 10,
    [switch]$SkipReadyPrompt,
    [switch]$NoCapture,
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"

function Write-Banner {
    param([string]$Text)
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor DarkGray
    Write-Host $Text -ForegroundColor Cyan
    Write-Host "============================================================" -ForegroundColor DarkGray
}

function Format-Sec {
    param([int]$Sec)
    $m = [int][math]::Floor($Sec / 60)
    $s = [int]($Sec % 60)
    return ("{0:D2}:{1:D2}" -f $m, $s)
}

function Build-Events {
    param(
        [string]$PlanName,
        [int]$OnAt,
        [int]$OffAt,
        [int]$TotalSec
    )

    $events = New-Object System.Collections.Generic.List[object]

    switch ($PlanName) {
        "run1" {
            $events.Add([pscustomobject]@{ sec = 0; msg = "RUN1 START: App offen, mit SmartBat-A19681 verbinden, auf Detailseite bleiben." })
            $events.Add([pscustomobject]@{ sec = $TotalSec; msg = "RUN1 ENDE: Keine Aenderung mehr. STOP ausfuehren." })
        }
        "run2" {
            $events.Add([pscustomobject]@{ sec = 0; msg = "RUN2 START: Ruhig bleiben, keine Aenderung." })
            $events.Add([pscustomobject]@{ sec = $OnAt; msg = "JETZT LOAD_ON: Verbraucher EIN schalten und Marker notieren (LOAD_ON)." })
            $events.Add([pscustomobject]@{ sec = $OffAt; msg = "JETZT LOAD_OFF: Verbraucher AUS schalten und Marker notieren (LOAD_OFF)." })
            $events.Add([pscustomobject]@{ sec = $TotalSec; msg = "RUN2 ENDE: Keine Aenderung mehr. STOP ausfuehren." })
        }
        "run3" {
            $events.Add([pscustomobject]@{ sec = 0; msg = "RUN3 START: Ruhig bleiben, keine Aenderung." })
            $events.Add([pscustomobject]@{ sec = $OnAt; msg = "JETZT CHARGE_ON: Ladegeraet EIN und Marker notieren (CHARGE_ON)." })
            $events.Add([pscustomobject]@{ sec = $OffAt; msg = "JETZT CHARGE_OFF: Ladegeraet AUS und Marker notieren (CHARGE_OFF)." })
            $events.Add([pscustomobject]@{ sec = $TotalSec; msg = "RUN3 ENDE: Keine Aenderung mehr. STOP ausfuehren." })
        }
        "custom" {
            $events.Add([pscustomobject]@{ sec = 0; msg = "CUSTOM START: Verbinden und auf Detailseite bleiben." })
            $events.Add([pscustomobject]@{ sec = $OnAt; msg = "CUSTOM EVENT_ON erreicht." })
            $events.Add([pscustomobject]@{ sec = $OffAt; msg = "CUSTOM EVENT_OFF erreicht." })
            $events.Add([pscustomobject]@{ sec = $TotalSec; msg = "CUSTOM ENDE." })
        }
        "gateway" {
            $events.Add([pscustomobject]@{ sec = 0; msg = "GATEWAY START: App verbinden - Gateway laeuft autonom (72 Perms x ~6s + Reconnects ~650s)." })
            $events.Add([pscustomobject]@{ sec = $TotalSec; msg = "GATEWAY ENDE: gateway_monitor.ps1 wird automatisch ausgefuehrt." })
        }
    }

    return $events | Sort-Object sec
}

if ($DurationSec -lt 10) {
    throw "DurationSec muss >= 10 sein."
}
if ($StartLeadSec -lt 3) {
    throw "StartLeadSec muss >= 3 sein."
}
if ($PreAlertSec -lt 3) {
    throw "PreAlertSec muss >= 3 sein."
}
if ($EventOnSec -lt 1 -or $EventOnSec -ge $DurationSec) {
    throw "EventOnSec muss zwischen 1 und DurationSec-1 liegen."
}
if ($EventOffSec -le $EventOnSec -or $EventOffSec -ge $DurationSec) {
    throw "EventOffSec muss > EventOnSec und < DurationSec sein."
}

$projectRoot = Split-Path -Parent $PSScriptRoot
$startScript = Join-Path $PSScriptRoot "start_smartbat_capture.ps1"
$stopScript = Join-Path $PSScriptRoot "stop_smartbat_capture.ps1"
$statePath = Join-Path $projectRoot "captures\current_capture_state.json"

# Normalize cwd so helper scripts always write to the same project-local captures folder.
Set-Location $projectRoot

$events = Build-Events -PlanName $Plan -OnAt $EventOnSec -OffAt $EventOffSec -TotalSec $DurationSec
$markerLog = New-Object System.Collections.Generic.List[object]

Write-Banner "SmartBat Run Orchestrator - Plan: $Plan - Dauer: $DurationSec s"
Write-Host "Event-Plan:" -ForegroundColor Yellow
foreach ($e in $events) {
    Write-Host ("  t={0,3}s -> {1}" -f $e.sec, $e.msg)
}

$firstAction = $events | Sort-Object sec | Select-Object -First 1
$nextAction = $events | Sort-Object sec | Select-Object -Skip 1 -First 1

Write-Host ""
Write-Host "JETZT als erstes:" -ForegroundColor Green
Write-Host ("  {0}" -f $firstAction.msg)
if ($null -ne $nextAction) {
    Write-Host "ALS naechstes:" -ForegroundColor Green
    Write-Host ("  Bei t={0}s -> {1}" -f $nextAction.sec, $nextAction.msg)
}

if (-not $SkipReadyPrompt) {
    Write-Host ""
    Read-Host "Wenn du bereit bist, ENTER druecken"
}

if (-not $NoCapture) {
    Write-Host ""
    Write-Host "Capture wird gestartet..." -ForegroundColor Yellow
    & $startScript
}

$captureOutDir = ""
if (Test-Path $statePath) {
    try {
        $state = Get-Content $statePath | ConvertFrom-Json
        $captureOutDir = $state.outDir
    } catch {
        $captureOutDir = ""
    }
}

Write-Host ""
Write-Host ("Los geht's in {0} Sekunden..." -f $StartLeadSec) -ForegroundColor Green
if (-not $DryRun) {
    for ($i = $StartLeadSec; $i -ge 1; $i--) {
        if ($i -le 5 -or ($i % 5 -eq 0)) {
            Write-Host ("Start in {0}..." -f $i)
        }
        Start-Sleep -Seconds 1
    }
}

$startTime = Get-Date
$emitted = @{}
$preWarned = @{}

for ($sec = 0; $sec -le $DurationSec; $sec++) {
    $remaining = $DurationSec - $sec

    $nextEvent = $events |
        Where-Object { $_.sec -gt $sec } |
        Sort-Object sec |
        Select-Object -First 1

    foreach ($future in ($events | Where-Object { $_.sec -gt $sec })) {
        $toFuture = $future.sec - $sec
        if ($toFuture -eq $PreAlertSec -and -not $preWarned.ContainsKey($future.sec)) {
            Write-Host ""
            Write-Host ("VORWARNUNG: In {0}s folgt -> {1}" -f $PreAlertSec, $future.msg) -ForegroundColor Yellow
            $preWarned[$future.sec] = $true
        }
    }

    foreach ($e in $events) {
        if ($e.sec -eq $sec -and -not $emitted.ContainsKey($sec)) {
            Write-Host ""
            Write-Host ("[t={0}s | {1}] {2}" -f $sec, (Get-Date -Format "HH:mm:ss"), $e.msg) -ForegroundColor Magenta
            $markerLog.Add([pscustomobject]@{
                second = $sec
                wallTime = (Get-Date).ToString("HH:mm:ss")
                message = $e.msg
            }) | Out-Null
            $emitted[$sec] = $true
        }
    }

    if ($null -ne $nextEvent) {
        $toNext = $nextEvent.sec - $sec
        Write-Host (
            "Zeit: +{0}s | Rest: {1} | Naechster Schritt in: {2} ({3})" -f
            $sec,
            (Format-Sec -Sec $remaining),
            (Format-Sec -Sec $toNext),
            $nextEvent.msg
        ) -ForegroundColor DarkCyan
    } else {
        Write-Host ("Zeit: +{0}s | Rest: {1} | Naechster Schritt: keiner" -f $sec, (Format-Sec -Sec $remaining)) -ForegroundColor DarkCyan
    }

    if ($sec -lt $DurationSec -and -not $DryRun) {
        Start-Sleep -Seconds 1
    }
}

$endTime = Get-Date

if (-not $NoCapture) {
    Write-Host ""
    Write-Host "Capture wird gestoppt..." -ForegroundColor Yellow
    & $stopScript
}

Write-Banner "Run abgeschlossen"
Write-Host ("Start: {0}" -f $startTime.ToString("HH:mm:ss"))
Write-Host ("Ende : {0}" -f $endTime.ToString("HH:mm:ss"))
if (-not [string]::IsNullOrWhiteSpace($captureOutDir)) {
    Write-Host ("Capture OutDir: {0}" -f $captureOutDir)
}

Write-Host ""
Write-Host "Marker-Zusammenfassung:" -ForegroundColor Yellow
foreach ($m in $markerLog) {
    Write-Host ("  +{0}s ({1}) -> {2}" -f $m.second, $m.wallTime, $m.message)
}

Write-Host ""
Write-Host "Empfehlung fuer Chat-Marker:" -ForegroundColor Yellow
switch ($Plan) {
    "run2" {
        Write-Host "  Bei Event: LOAD_ON / LOAD_OFF"
    }
    "run3" {
        Write-Host "  Bei Event: CHARGE_ON / CHARGE_OFF"
    }
    "gateway" {
        Write-Host "  Gateway-Matrix wird automatisch ausgewertet..." -ForegroundColor Cyan
        if (-not [string]::IsNullOrWhiteSpace($captureOutDir)) {
            $monitorScript = Join-Path $PSScriptRoot "gateway_monitor.ps1"
            if (Test-Path $monitorScript) {
                Write-Host ""
                & $monitorScript -CaptureDir $captureOutDir
            } else {
                Write-Host "gateway_monitor.ps1 nicht gefunden: $monitorScript" -ForegroundColor Red
            }
        }
    }
    default {
        Write-Host "  Bei Event: START/STOP bestaetigen"
    }
}
