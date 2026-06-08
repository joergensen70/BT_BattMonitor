#Requires -Version 5.1
<#
.SYNOPSIS
    BMS Research Gateway Result Monitor.
    Parses GATEWAY_RESULT JSON lines from a logcat capture and prints the
    permutation matrix with highlighted payload hits.

.USAGE
    # Analyze latest capture automatically:
    .\tools\gateway_monitor.ps1

    # Analyze a specific capture dir:
    .\tools\gateway_monitor.ps1 -CaptureDir "captures\capture_20260608_HHMMSS"

    # Watch live (re-reads every 5s until done):
    .\tools\gateway_monitor.ps1 -Live
#>
param(
    [string]$CaptureDir = '',
    [switch]$Live
)

$basePath = 'C:\git\BT_BattMonitor\smartbat_bms\captures'

function Get-LatestCapture {
    Get-ChildItem $basePath -Directory |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}

function Show-Matrix($logFile) {
    if (-not (Test-Path $logFile)) {
        Write-Host "Logfile not found: $logFile" -ForegroundColor Red
        return $false
    }

    $results = Select-String -Path $logFile -Pattern 'GATEWAY_RESULT: (\{.+\})' |
        ForEach-Object {
            $json = [regex]::Match($_.Line, 'GATEWAY_RESULT: (\{.+\})').Groups[1].Value
            try { ConvertFrom-Json $json } catch { $null }
        } |
        Where-Object { $_ -ne $null }

    $charProps = Select-String -Path $logFile -Pattern 'Characteristic fff\d .+' |
        ForEach-Object { [regex]::Match($_.Line, 'Characteristic (fff\d) R:(\w+) W:(\w+) WNR:(\w+)') } |
        Where-Object { $_.Success } |
        ForEach-Object { [PSCustomObject]@{
            Char  = $_.Groups[1].Value
            Read  = $_.Groups[2].Value
            Write = $_.Groups[3].Value
            WNR   = $_.Groups[4].Value
        }}

    Clear-Host
    Write-Host '=' * 90 -ForegroundColor Cyan
    Write-Host '  BMS RESEARCH GATEWAY - PERMUTATION MATRIX' -ForegroundColor Cyan
    Write-Host '  File: ' -NoNewline; Write-Host $logFile -ForegroundColor DarkCyan
    Write-Host '=' * 90 -ForegroundColor Cyan

    if ($charProps) {
        Write-Host "`nCharacteristic properties:" -ForegroundColor Yellow
        foreach ($cp in $charProps | Sort-Object Char -Unique) {
            $flag = if ($cp.Write -eq 'True' -and $cp.WNR -eq 'True') { 'WRITE+WNR' }
                    elseif ($cp.Write -eq 'True') { 'WRITE-only' }
                    elseif ($cp.WNR -eq 'True') { 'WNR-only' }
                    else { 'read-only' }
            Write-Host ("  {0,-6} R:{1,-5} WRITE_REQ:{2,-5} WRITE_CMD:{3,-5}  [{4}]" -f `
                $cp.Char, $cp.Read, $cp.Write, $cp.WNR, $flag) -ForegroundColor White
        }
        Write-Host ''
    }

    if (-not $results) {
        Write-Host 'No GATEWAY_RESULT lines found yet. Gateway may still be connecting...' -ForegroundColor Yellow
        return $false
    }

    # Header
    $hdr = "{0,-12} {1,-5} {2,-4} {3,5} {4,5} {5,5} {6,5} {7,9} {8,5}  frames" -f `
        'CMD', 'CHAR', 'WR', 'WOK', 'WERR', 'HB', 'ECHO', 'PAYLOAD', 'UNK'
    Write-Host $hdr -ForegroundColor DarkGray
    Write-Host ('-' * 90) -ForegroundColor DarkGray

    $hitCount = 0
    $prevChar = ''
    foreach ($r in $results) {
        $wr    = if ($r.wr) { 'REQ' } else { 'CMD' }
        $isHit = $r.payload -gt 0
        if ($isHit) { $hitCount++ }

        $payloadStr = if ($isHit) { "*** $($r.payload) ***" } else { $r.payload.ToString() }
        $frameStr   = if ($r.frames -and $r.frames.Count -gt 0) { ($r.frames -join ' | ') } else { '-' }

        $line = "{0,-12} {1,-5} {2,-4} {3,5} {4,5} {5,5} {6,5} {7,9}  {8,5}  {9}" -f `
            $r.cmd, $r.char, $wr, $r.wok, $r.werr, $r.hb, $r.echo, $payloadStr, $r.unk, $frameStr

        if ($r.char -ne $prevChar) {
            Write-Host '' # blank line between characteristic groups
            $prevChar = $r.char
        }

        if ($isHit) {
            Write-Host $line -ForegroundColor Green
        } elseif ($r.werr -gt $r.wok / 2) {
            Write-Host $line -ForegroundColor DarkRed   # mostly write errors
        } else {
            Write-Host $line
        }
    }

    Write-Host ('-' * 90) -ForegroundColor DarkGray

    # Summary
    $total = 72  # 12 cmds x 3 chars x 2 write modes
    $done = Select-String -Path $logFile -Pattern 'GATEWAY_MATRIX_DONE'
    if ($done) {
        Write-Host "`n  GATEWAY COMPLETE: $($results.Count)/$total permutations tested" -ForegroundColor Green
        if ($hitCount -gt 0) {
            Write-Host "  *** $hitCount PAYLOAD HITS FOUND! ***" -ForegroundColor Green -BackgroundColor DarkGreen
        } else {
            Write-Host "  No payload hits in any permutation." -ForegroundColor Yellow
        }
    } else {
        $pct = [int]($results.Count * 100 / $total)
        Write-Host "`n  Progress: $($results.Count)/$total permutations ($pct%)" -ForegroundColor Yellow
        if ($hitCount -gt 0) {
            Write-Host "  *** $hitCount PAYLOAD HITS so far! ***" -ForegroundColor Green
        }
    }

    return $done -ne $null
}

# ── Main ──────────────────────────────────────────────────────────────────────

if (-not $CaptureDir) {
    $CaptureDir = Get-LatestCapture
}
if (-not $CaptureDir) {
    Write-Host "No capture directory found under $basePath" -ForegroundColor Red
    exit 1
}

$logFile = Join-Path $CaptureDir 'logcat_threadtime.txt'
Write-Host "Monitoring: $logFile" -ForegroundColor Cyan

if ($Live) {
    Write-Host "Live mode - refreshing every 5s. Press Ctrl+C to stop." -ForegroundColor Yellow
    while ($true) {
        $done = Show-Matrix $logFile
        if ($done) { break }
        Start-Sleep -Seconds 5
    }
} else {
    Show-Matrix $logFile | Out-Null
}
