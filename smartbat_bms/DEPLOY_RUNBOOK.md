# SmartBat Deploy Runbook (Android)

This is the standard repeatable workflow for test deployments.

## Preconditions

- Phone connected via USB and visible in ADB.
- Target package name: `com.smartbat.bms`.
- Project root for build: `bt_batt_monitor`.

## Standard flow

1. Verify device connection:
   - `adb devices`
2. Go to Flutter app folder:
   - `cd bt_batt_monitor`
3. Rebuild from clean state:
   - `flutter clean`
   - `flutter pub get`
   - `flutter build apk --debug --target lib/main.dart --android-skip-build-dependency-validation`
4. Stop app if currently running:
   - `adb shell am force-stop com.smartbat.bms`
5. Install fresh APK:
   - `adb install -r build\app\outputs\flutter-apk\app-debug.apk`
6. Launch app:
   - `adb shell monkey -p com.smartbat.bms -c android.intent.category.LAUNCHER 1`

## Notes

- `-r` keeps app data while replacing the APK.
- If install fails because of signatures, uninstall first:
  - `adb uninstall com.smartbat.bms`
  - then run install again.

## Live RX Capture Flow

Use this when the app is already running and you want to capture BLE traffic while opening a battery.

1. Start a fresh capture and clear old logcat buffer:
   - `adb logcat -c`
2. Start filtered live log capture into a timestamped file:
   - `adb logcat -v threadtime "*:S" "flutter:D" "BluetoothGatt:D" "BtGatt.GattService:D" "bt_btif_gattc:D" "bt_bta_gattc:D" > captures\live_rx_<timestamp>\logcat_live.txt`
3. In the app, open a battery and stay on the values page for 10-20 seconds.
4. Stop capture (Ctrl+C in terminal or kill the capture terminal).
5. Analyze key markers:
   - `TX Lion ASCII`
   - `onWriteCharacteristic()`
   - `GATT_INVALID_ATTRIBUTE_LENGTH`
   - `onCharacteristicChanged`
