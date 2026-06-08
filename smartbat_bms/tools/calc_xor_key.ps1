$encryptByte = @(2, 5, 4, 3, 1, 4, 1, 6, 8, 3, 7, 2, 5, 8, 9, 3)
$deviceName = 'SmartBat-A19681'

$input6 = $deviceName.Substring($deviceName.Length - 6)
Write-Host "Input to getType: '$input6'"

$first = $input6.Substring(0,1)
$numPart = $input6.Substring(1)
$hexVal = [Convert]::ToInt32($numPart)
$hex = [Convert]::ToString($hexVal, 16).ToUpper()
while ($hex.Length -lt 4) { $hex = '0' + $hex }
Write-Host "numPart='$numPart'  hex='$hex'"

$i2 = 0
foreach ($c in $hex.ToCharArray()) {
    $idx = [Convert]::ToInt32([string]$c, 16) -band 15
    $add = $encryptByte[$idx]
    Write-Host "  char $c -> idx $idx -> encryptByte[$idx]=$add"
    $i2 += $add
}

$type = if ($first.ToUpper() -eq 'A') { 1 } else { 2 }
$resouce = if ($type -eq 1) { $i2 + 5 } else { $i2 + 8 }
$resouceHex = '{0:X2}' -f $resouce
Write-Host ""
Write-Host "i2=$i2  type=$type  resouce=$resouce (0x$resouceHex)"
Write-Host ""

# Show encoded form of all +RAA commands
$commands = @('+RAA1002','+RAA0A03','+RAA0802','+RAA0C02','+RAA0403','+RAA3C03',
              '+RAA0603','+RAA1802','+RAA1A02','+RAA2802','+RAA4802','+RAA0202','+RAA2C02')
Write-Host "Encoded commands (XOR 0x$resouceHex):"
foreach ($cmd in $commands) {
    $xored = ($cmd.ToCharArray() | ForEach-Object { '{0:X2}' -f (([int][char]$_) -bxor $resouce) }) -join ' '
    Write-Host "  $cmd -> $xored"
}
