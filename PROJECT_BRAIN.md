# BT_BattMonitor Project Brain

This file tracks what we are building, what we validated, what is blocked, and what to do next.

## 1) Project Goal

Build a reliable battery monitoring app for SmartBat BMS.

Current priority (MVP):
- BLE scan
- Connect to battery
- Read BMS data
- Show data clearly

Later priority:
- Show and monitor two batteries at the same time
- Improve reliability, reconnection, and diagnostics
- Build/install Android APKs more smoothly

## 2) Current Scope and App State

Workspace structure:
- smartbat_bms/: Flutter app
- tools/: local BLE diagnostics scripts

Flutter app currently includes:
- Scan screen (BLE discovery list)
- Connect flow to selected device
- BMS service for JBD-style protocol (FFF0/FFF1/FFF2)
- Dashboard with SoC, voltage/current/power, temperatures, cells, alerts
- Additional status/debug logging in-app for connection and protocol visibility

## 3) What We Learned (Validated)

### Device/BMS facts
- At least one battery name is: SmartBat-A19681.
- User reports two batteries are in range.
- User confirms batteries are visible in nRF Connect.
- User confirms batteries are not currently connected to the phone.
- User confirms BMS is not sleeping.

### Windows test findings
- Local Windows BLE POC was implemented and executed.
- Windows host repeatedly reported zero BLE advertisements received during live scan runs.
- Windows known-device enumeration and live-scan attempts did not discover SmartBat devices.
- This strongly indicates a Windows-side BLE scanning limitation/blocker in this environment (not a battery advertising issue).

### Build/toolchain findings
- Flutter CLI is now available and working in terminal (`flutter --version` OK).
- Android Studio installation exists at `C:\\Program Files\\Android\\Android Studio`.
- Android SDK is now detected at `C:\\Users\\joerg\\AppData\\Local\\Android\\sdk`.
- SDK folders currently include `platform-tools`, `platforms`, `build-tools`, and `emulator`.
- `cmdline-tools` is still missing, so `flutter doctor -v` reports Android toolchain incomplete.
- Android license status is still unknown (licenses not yet accepted via Flutter tooling).
- `adb` is callable in terminal (currently from `C:\\Program Files\\Wondershare\\drfone\\adb.exe`).
- Windows does detect the phone via USB (`S25+ von JOERG`, Samsung USB composite/modem entries), but both SDK `adb` and current PATH `adb` still show no attached devices.
- This indicates USB connection is present, but ADB debugging interface/authorization is still not active.
- Re-check confirmed same state: USB composite/MTP/modem interfaces are present, but no `Android ADB Interface` is exposed to ADB yet.
- New progress (2026-06-07): phone now appears in ADB as `R3CY30DH49E`, status `unauthorized`.
- `SAMSUNG Android ADB Interface` is now visible in Windows device list.
- Flutter also reports the Android device but blocks run until USB debugging authorization is accepted on the phone.
- Final status update (2026-06-07): ADB device is now authorized and visible as `R3CY30DH49E` / `SM_S936B` (state `device`).
- `flutter devices` now includes the Android phone as a valid run target.
- Important workspace finding: `smartbat_bms/bt_batt_monitor` is a separate default Flutter demo project (counter app), while the real SmartBat app entry point is `smartbat_bms/lib/main.dart`.
- Android build scripts in `smartbat_bms/android` were migrated to modern Flutter plugin DSL and updated for current Flutter/Gradle compatibility.
- Real SmartBat app now builds and installs successfully on device in release mode (`flutter run --release` from `smartbat_bms`).
- Field issue observed: device could connect but showed `BMS service not found`.
- Fix applied in `smartbat_bms/lib/bms_service.dart`: UUID matching now supports variant formatting and fallback service selection via FFF1/FFF2 characteristic discovery.
- Updated app redeployed successfully to `SM_S936B` in release mode for verification.
- Additional hardening: connect flow no longer fails hard when `FFF0` is missing; it now performs global characteristic discovery and generic write/notify fallback across all discovered services.

## 4) What We Want To Do Next

Primary next step:
- Test directly on Android device (where SmartBat devices are already visible).

Android flow target:
1. Scan and discover SmartBat battery
2. Connect
3. Verify service/characteristic detection
4. Verify TX/RX protocol messages
5. Verify parsed values on dashboard

After single-battery MVP is stable:
- Add two-battery parallel monitoring UX and service orchestration

## 5) Known Risks / Open Questions

- Are all SmartBat units fully JBD-compatible on command/response framing?
- Do some units require timing or characteristic role fallback differences?
- Will dual simultaneous connections be stable on target Android hardware?
- Need final confirmation of exact Android build environment paths once installed.

## 6) Useful Local Assets

Windows BLE POC script:
- tools/windows-ble-poc.ps1

Notes:
- Script supports Windows-known device enumeration, optional live scan, connect, read, and optional JBD write probe.
- Current environment showed no BLE advertisements in live scan tests.

## 7) Current Status Snapshot

Current phase: MVP validation

Status:
- Flutter app core flow exists and was improved for diagnostics.
- Windows BLE validation path is currently unreliable on this machine.
- Android test path is the recommended and intended route now.
- Android device (`SM_S936B` / `R3CY30DH49E`) is now connected, authorized, and usable as Flutter run target.
- App deploy/start on Android succeeds (`flutter run` builds and installs `app-debug.apk`, VM service available).

Next execution focus:
- Configure Android SDK path and platform-tools (`adb`) on this machine
- Re-run `flutter doctor -v` until Android toolchain is green
- Connect Android device and verify detection in `flutter devices`
- Build and run on Android device
- Confirm end-to-end data read from SmartBat-A19681

## 8) Update Rules for This Brain

When we learn something important, append/update:
- Facts that were actually validated
- What changed in code/behavior
- New blockers and decisions
- Next concrete action
