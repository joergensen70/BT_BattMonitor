param(
    [string]$NameLike = '',
    [int]$SelectIndex = -1,
    [string]$DeviceAddress = '',
    [switch]$LiveScan,
    [int]$LiveScanSeconds = 20,
    [switch]$Connect,
    [switch]$ReadValues,
    [switch]$JbdWriteTest
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

Add-Type -AssemblyName System.Runtime.WindowsRuntime
[void][Windows.Devices.Bluetooth.BluetoothLEDevice, Windows.Devices.Bluetooth, ContentType = WindowsRuntime]
[void][Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementWatcher, Windows.Devices.Bluetooth, ContentType = WindowsRuntime]
[void][Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementReceivedEventArgs, Windows.Devices.Bluetooth, ContentType = WindowsRuntime]
[void][Windows.Devices.Enumeration.DeviceInformation, Windows.Devices.Enumeration, ContentType = WindowsRuntime]
[void][Windows.Foundation.TypedEventHandler`2, Windows.Foundation, ContentType = WindowsRuntime]
[void][Windows.Storage.Streams.DataReader, Windows.Storage.Streams, ContentType = WindowsRuntime]
[void][Windows.Security.Cryptography.CryptographicBuffer, Windows.Security.Cryptography, ContentType = WindowsRuntime]

function ConvertTo-TaskResult {
    param(
        [Parameter(Mandatory = $true)]$Operation,
        [Parameter(Mandatory = $true)][Type]$ResultType
    )

    $method = [System.WindowsRuntimeSystemExtensions].GetMethods() |
        Where-Object {
            $_.Name -eq 'AsTask' -and
            $_.IsGenericMethodDefinition -and
            $_.GetGenericArguments().Count -eq 1 -and
            $_.GetParameters().Count -eq 1
        } |
        Select-Object -First 1

    $generic = $method.MakeGenericMethod(@($ResultType))
    $task = $generic.Invoke($null, @($Operation))
    return $task.GetAwaiter().GetResult()
}

function ConvertTo-BluetoothAddress {
    param([string]$AddressText)

    $clean = $AddressText.Replace(':', '').Replace('-', '').Trim()
    if ($clean -notmatch '^[0-9A-Fa-f]{12}$') {
        throw "Invalid DeviceAddress format '$AddressText'. Use format AA:BB:CC:DD:EE:FF"
    }
    return [Convert]::ToUInt64($clean, 16)
}

function Format-AddressHex {
    param([UInt64]$Address)

    $hex = $Address.ToString('X12')
    return ($hex -replace '..(?!$)', '$0:')
}

function Format-Bytes {
    param([byte[]]$Bytes)

    return ($Bytes | ForEach-Object { $_.ToString('X2') }) -join ' '
}

function ConvertTo-ByteArray {
    param([Windows.Storage.Streams.IBuffer]$Buffer)

    $reader = [Windows.Storage.Streams.DataReader]::FromBuffer($Buffer)
    try {
        $bytes = New-Object byte[] $Buffer.Length
        $reader.ReadBytes($bytes)
        return $bytes
    } finally {
        $reader.Dispose()
    }
}

function Get-BleDeviceRows {
    $selector = [Windows.Devices.Bluetooth.BluetoothLEDevice]::GetDeviceSelector()
    $op = [Windows.Devices.Enumeration.DeviceInformation]::FindAllAsync($selector)
    $list = ConvertTo-TaskResult -Operation $op -ResultType ([Windows.Devices.Enumeration.DeviceInformationCollection])

    $rows = foreach ($item in $list) {
        $addressMatch = [regex]::Match($item.Id, 'BluetoothLE#BluetoothLE([0-9A-F]{12})', 'IgnoreCase')
        $addressHex = if ($addressMatch.Success) {
            ($addressMatch.Groups[1].Value.ToUpper() -replace '..(?!$)', '$0:')
        } else {
            ''
        }

        [pscustomobject]@{
            Name = if ([string]::IsNullOrWhiteSpace($item.Name)) { '(no name)' } else { $item.Name }
            Id = $item.Id
            AddressHex = $addressHex
        }
    }

    if ($NameLike) {
        $rows = $rows | Where-Object {
            $_.Name -like "*$NameLike*" -or $_.AddressHex -like "*$NameLike*"
        }
    }

    $index = 0
    return $rows | Sort-Object Name | ForEach-Object {
        [pscustomobject]@{
            Index = $index++
            Name = $_.Name
            AddressHex = $_.AddressHex
            Id = $_.Id
        }
    }
}

function Get-LiveBleRows {
    param([int]$Seconds = 20)

    $watcher = [Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementWatcher]::new()
    $watcher.ScanningMode = [Windows.Devices.Bluetooth.Advertisement.BluetoothLEScanningMode]::Active
    $rows = [System.Collections.Generic.Dictionary[string, object]]::new()

    $handler = [Windows.Foundation.TypedEventHandler[
        Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementWatcher,
        Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementReceivedEventArgs
    ]] {
        param($sender, $eventArgs)

        $name = $eventArgs.Advertisement.LocalName
        if ([string]::IsNullOrWhiteSpace($name)) {
            return
        }

        $addressHex = ('{0:X12}' -f $eventArgs.BluetoothAddress)
        $formatted = ($addressHex -replace '..(?!$)', '$0:')
        $key = $formatted
        $item = [pscustomobject]@{
            Name = $name
            AddressHex = $formatted
            Id = ''
            Rssi = $eventArgs.RawSignalStrengthInDBm
        }
        $rows[$key] = $item
    }

    Write-Host "Running live BLE scan for $Seconds seconds..." -ForegroundColor Cyan
    $token = $watcher.add_Received($handler)
    $wait = [System.Threading.ManualResetEventSlim]::new($false)
    try {
        $watcher.Start()
        [void]$wait.Wait([Math]::Max($Seconds, 1) * 1000)
    } finally {
        $wait.Dispose()
        if ($watcher.Status -eq [Windows.Devices.Bluetooth.Advertisement.BluetoothLEAdvertisementWatcherStatus]::Started) {
            $watcher.Stop()
        }
        $watcher.remove_Received($token)
    }

    $list = @($rows.Values)
    if ($NameLike) {
        $list = $list | Where-Object {
            $_.Name -like "*$NameLike*" -or $_.AddressHex -like "*$NameLike*"
        }
    }

    $index = 0
    return $list | Sort-Object Rssi -Descending | ForEach-Object {
        [pscustomobject]@{
            Index = $index++
            Name = $_.Name
            AddressHex = $_.AddressHex
            Id = $_.Id
            Rssi = $_.Rssi
        }
    }
}

function Get-ReadableValue {
    param($Characteristic)

    $readFlag = [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCharacteristicProperties]::Read
    if (($Characteristic.CharacteristicProperties -band $readFlag) -eq 0) {
        return $null
    }

    $op = $Characteristic.ReadValueAsync([Windows.Devices.Bluetooth.BluetoothCacheMode]::Uncached)
    $result = ConvertTo-TaskResult -Operation $op -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattReadResult])
    if ($result.Status -ne [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus]::Success) {
        return "Read failed: $($result.Status)"
    }

    if (-not $result.Value -or $result.Value.Length -eq 0) {
        return '<empty>'
    }

    $bytes = ConvertTo-ByteArray -Buffer $result.Value
    return Format-Bytes -Bytes $bytes
}

function Invoke-JbdWriteProbe {
    param($Services)

    $svcFff0 = $Services | Where-Object { $_.Uuid.ToString().ToLower() -eq '0000fff0-0000-1000-8000-00805f9b34fb' } | Select-Object -First 1
    if (-not $svcFff0) {
        Write-Host 'JBD write test skipped: FFF0 service not found.' -ForegroundColor Yellow
        return
    }

    $charOp = $svcFff0.GetCharacteristicsAsync([Windows.Devices.Bluetooth.BluetoothCacheMode]::Uncached)
    $charResult = ConvertTo-TaskResult -Operation $charOp -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattCharacteristicsResult])
    if ($charResult.Status -ne [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus]::Success) {
        Write-Host "JBD write test skipped: cannot read chars ($($charResult.Status))." -ForegroundColor Yellow
        return
    }

    $chars = @($charResult.Characteristics)
    $fff1 = $chars | Where-Object { $_.Uuid.ToString().ToLower() -eq '0000fff1-0000-1000-8000-00805f9b34fb' } | Select-Object -First 1
    $fff2 = $chars | Where-Object { $_.Uuid.ToString().ToLower() -eq '0000fff2-0000-1000-8000-00805f9b34fb' } | Select-Object -First 1

    if (-not $fff1 -or -not $fff2) {
        Write-Host 'JBD write test skipped: FFF1/FFF2 not available.' -ForegroundColor Yellow
        return
    }

    $writeProps = [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCharacteristicProperties]::Write
    $writeNoRespProps = [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCharacteristicProperties]::WriteWithoutResponse

    $writeChar = if (($fff1.CharacteristicProperties -band ($writeProps -bor $writeNoRespProps)) -ne 0) { $fff1 } else { $fff2 }
    if (-not $writeChar) {
        Write-Host 'JBD write test skipped: no writable characteristic found.' -ForegroundColor Yellow
        return
    }

    $cmdBasic = [byte[]](0xDD, 0xA5, 0x03, 0x00, 0xFF, 0xFD, 0x77)
    $cmdCells = [byte[]](0xDD, 0xA5, 0x04, 0x00, 0xFF, 0xFC, 0x77)
    $writeOption = if (($writeChar.CharacteristicProperties -band $writeProps) -ne 0) {
        [Windows.Devices.Bluetooth.GenericAttributeProfile.GattWriteOption]::WriteWithResponse
    } else {
        [Windows.Devices.Bluetooth.GenericAttributeProfile.GattWriteOption]::WriteWithoutResponse
    }

    Write-Host ("TX basic: {0}" -f (Format-Bytes -Bytes $cmdBasic)) -ForegroundColor Cyan
    $buf1 = [Windows.Security.Cryptography.CryptographicBuffer]::CreateFromByteArray($cmdBasic)
    $op1 = $writeChar.WriteValueAsync($buf1, $writeOption)
    $wr1 = ConvertTo-TaskResult -Operation $op1 -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus])
    Write-Host "Write basic status: $wr1"

    Write-Host ("TX cells: {0}" -f (Format-Bytes -Bytes $cmdCells)) -ForegroundColor Cyan
    $buf2 = [Windows.Security.Cryptography.CryptographicBuffer]::CreateFromByteArray($cmdCells)
    $op2 = $writeChar.WriteValueAsync($buf2, $writeOption)
    $wr2 = ConvertTo-TaskResult -Operation $op2 -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus])
    Write-Host "Write cells status: $wr2"

    $readBack = Get-ReadableValue -Characteristic $fff2
    if ($readBack) {
        Write-Host "Readback from FFF2: $readBack" -ForegroundColor DarkCyan
    }
}

Write-Host 'Enumerating BLE devices known to Windows...' -ForegroundColor Cyan
$found = if ($LiveScan) {
    @(Get-LiveBleRows -Seconds $LiveScanSeconds)
} else {
    @(Get-BleDeviceRows)
}

if (-not $found -or $found.Count -eq 0) {
    if ($LiveScan) {
        Write-Host 'No BLE advertisements received during live scan.' -ForegroundColor Yellow
        Write-Host 'Make sure the BMS is awake, in range, and not currently connected to another central device.' -ForegroundColor DarkYellow
    } else {
        Write-Host 'No BLE devices found in Windows cache. Pair the target in Windows first or use -LiveScan.' -ForegroundColor Yellow
    }
    exit 0
}

Write-Host ''
Write-Host 'BLE devices:' -ForegroundColor Green
if ($LiveScan) {
    $found | Select-Object Index, Name, AddressHex, Rssi | Format-Table -AutoSize
} else {
    $found | Select-Object Index, Name, AddressHex | Format-Table -AutoSize
}

if (-not $Connect) {
    Write-Host ''
    Write-Host 'Tip: Use -Connect -SelectIndex <n> (or -DeviceAddress AA:BB:CC:DD:EE:FF).' -ForegroundColor DarkGray
    exit 0
}

$targetAddress = $null
$targetId = $null
if (-not [string]::IsNullOrWhiteSpace($DeviceAddress)) {
    $targetAddress = ConvertTo-BluetoothAddress -AddressText $DeviceAddress
} elseif ($SelectIndex -ge 0) {
    $pick = $found | Where-Object { $_.Index -eq $SelectIndex } | Select-Object -First 1
    if (-not $pick) {
        throw "Invalid SelectIndex $SelectIndex"
    }
    if (-not [string]::IsNullOrWhiteSpace($pick.AddressHex)) {
        $targetAddress = ConvertTo-BluetoothAddress -AddressText $pick.AddressHex
    } else {
        $targetId = $pick.Id
    }
} else {
    $pick = $found | Select-Object -First 1
    if (-not [string]::IsNullOrWhiteSpace($pick.AddressHex)) {
        $targetAddress = ConvertTo-BluetoothAddress -AddressText $pick.AddressHex
        Write-Host "No SelectIndex provided, defaulting to: $($pick.Name) [$($pick.AddressHex)]" -ForegroundColor DarkYellow
    } else {
        $targetId = $pick.Id
        Write-Host "No SelectIndex provided, defaulting to: $($pick.Name) [Id-only]." -ForegroundColor DarkYellow
    }
}

Write-Host ''
if ($targetAddress) {
    Write-Host ("Connecting to {0} ..." -f (Format-AddressHex -Address $targetAddress)) -ForegroundColor Cyan
    $connectOp = [Windows.Devices.Bluetooth.BluetoothLEDevice]::FromBluetoothAddressAsync($targetAddress)
    $device = ConvertTo-TaskResult -Operation $connectOp -ResultType ([Windows.Devices.Bluetooth.BluetoothLEDevice])
} else {
    Write-Host 'Connecting by Windows device id ...' -ForegroundColor Cyan
    $connectOp = [Windows.Devices.Bluetooth.BluetoothLEDevice]::FromIdAsync($targetId)
    $device = ConvertTo-TaskResult -Operation $connectOp -ResultType ([Windows.Devices.Bluetooth.BluetoothLEDevice])
}
if (-not $device) {
    throw 'Could not open BLE device. Ensure it is in range and paired if required.'
}

try {
    Write-Host "Connected device name: $($device.Name)" -ForegroundColor Green

    $svcOp = $device.GetGattServicesAsync([Windows.Devices.Bluetooth.BluetoothCacheMode]::Uncached)
    $svcResult = ConvertTo-TaskResult -Operation $svcOp -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattDeviceServicesResult])
    if ($svcResult.Status -ne [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus]::Success) {
        throw "GetGattServicesAsync failed with status $($svcResult.Status)"
    }

    $services = @($svcResult.Services)
    Write-Host "Services found: $($services.Count)" -ForegroundColor Green

    foreach ($svc in $services) {
        $svcUuid = $svc.Uuid.ToString().ToLower()
        Write-Host "- Service $svcUuid" -ForegroundColor Magenta

        $charOp = $svc.GetCharacteristicsAsync([Windows.Devices.Bluetooth.BluetoothCacheMode]::Uncached)
        $charResult = ConvertTo-TaskResult -Operation $charOp -ResultType ([Windows.Devices.Bluetooth.GenericAttributeProfile.GattCharacteristicsResult])
        if ($charResult.Status -ne [Windows.Devices.Bluetooth.GenericAttributeProfile.GattCommunicationStatus]::Success) {
            Write-Host "  ! Characteristic query failed: $($charResult.Status)" -ForegroundColor Yellow
            continue
        }

        foreach ($ch in $charResult.Characteristics) {
            $chUuid = $ch.Uuid.ToString().ToLower()
            $props = [string]$ch.CharacteristicProperties
            Write-Host "  - Char $chUuid [$props]"

            if ($ReadValues) {
                $value = Get-ReadableValue -Characteristic $ch
                if ($null -ne $value) {
                    Write-Host "    Read: $value" -ForegroundColor DarkCyan
                }
            }
        }
    }

    if ($JbdWriteTest) {
        Write-Host ''
        Write-Host 'Running JBD write test (DD A5 03/04)...' -ForegroundColor Cyan
        Invoke-JbdWriteProbe -Services $services
    }
} finally {
    $device.Dispose()
}
