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
- Demo mode with two simulated batteries for UI and non-BLE workflow testing
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
- Debug APK `1.3.1+5` was built, installed, and launched successfully on `R3CY30DH49E`.
- Demo mode was added and validated as a two-battery simulated data path.
- Demo/live mode separation was hardened so demo slot labels do not leak into live slot assignment.
- Leaving demo mode now auto-reconnects the previously saved live batteries.
- Combined summary cell-spread calculation was restored while the compact cell grid remains intentionally hidden for now.
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
- App deploy/start on Android succeeds via build + adb install/launch workflow.
- Current release target version is `1.3.4`.
- Demo mode is available for UI validation when live batteries are not connected.
- Protocol investigation conclusion (validated across long load captures):
	- Pack-level registers are stable and readable.
	- Cell-level values are not exposed for the tested SmartBat-A protocol path.
	- Aggressive ALT/MODE probe sweeps did not produce reliable `Lion Cell[n]` outputs and are not considered release behavior.

Next execution focus:
- Maintain stable pack-level monitoring behavior on Android hardware.
- Keep capture/debug tooling available for future battery variants without changing release defaults.
- Create and publish tagged releases after validated device deploys.

Release checkpoint (2026-07-02):
- Version bumped to `1.3.4+8`.
- IMPL-001 (remove JBD protocol), IMPL-002 (fix dual-battery power calc), IMPL-007 (remove Error1 registers) completed and deployed.
- Git updated and pushed on `main` with tag `v1.3.4`.

Release checkpoint (2026-06-22):
- Working tree reset to git baseline before release prep.
- Version bumped to `1.3.3+7` for next official release candidate.
- Deployment runbook and project brain synced to release target.

Hotfix note (2026-06-22):
- In-app About/version label was hardcoded (`v1.3.1`) while package version had moved forward.
- Release `1.3.3` aligns displayed app version and Android package version.

## 8) Implementation Notes (pending code changes)

### IMPL-001 — Remove JBD protocol completely ✅ DONE 2026-07-02

File: `smartbat_bms/lib/bms_service.dart`

Completed: All JBD constants, parsers, fallback logic, and probe-switching removed.
`connect()` now calls `_switchToLion()` directly. `_poll()` is Lion-only.
Dead methods removed: `_parseBasic`, `_parseCells`, `_activeCommandProfile`,
`_tryParseSmartBatFrame`, `_hasValidChecksum`, `_looksLikeEchoFrame`, `_u16`, `_s16`.

### IMPL-002 — Fix combined power / net-consumption calculation ✅ DONE 2026-07-02

File: `smartbat_bms/lib/battery_screen.dart`, method `_buildSummaryStrip`

Completed: `totalPower` now sums each battery's discharge contribution only
(clamp to negative, abs). When one battery charges and the other discharges,
the charging power no longer inflates the displayed wattage.

### IMPL-003 — Confirmed BLE advertisement name prefix

Validated 2026-07-02: one battery is advertising as `SmartBat-A05301` via the
camper ESP32-C3 Bluetooth proxy. The name format is `SmartBat-XXXXXX` (6-char
alphanumeric suffix, uppercase).

This confirms:
- The advertisement is visible to HA through the BT proxy.
- Auto-discovery by name prefix `SmartBat-` is viable for both the app and a
  future custom HA integration.
- No MAC-address lookup is needed for device identification — the name is stable
  and human-readable.

### IMPL-004 — XOR command encoding must be applied in any reimplementation

File: `smartbat_bms/lib/bms_service.dart` → `calcLionXorKey`, `_poll` TX path

All +RAA commands sent to the battery are XOR-encoded byte-by-byte before
transmission. A reimplementation (e.g. the custom HA integration) that sends
plain ASCII will be silently ignored by the BMS.

Algorithm (from `EncryptUtils.java` in LionCheck APK, implemented in
`calcLionXorKey`):

```
ENCRYPT_BYTE_TABLE = [2, 5, 4, 3, 1, 4, 1, 6, 8, 3, 7, 2, 5, 8, 9, 3]

key = sum(ENCRYPT_BYTE_TABLE[nibble] for nibble in hex(numeric_suffix)) + offset
  offset = +5 for type A (e.g. SmartBat-A05301)
  offset = +8 for type B

encoded_command = bytes(b ^ key for b in "+RAA0802".encode("ascii"))
```

Example — SmartBat-A05301 → key = 0x17.

### IMPL-005 — Write characteristic is NOT necessarily FFF1

File: `smartbat_bms/lib/bms_service.dart` → `writePriority`, `notifyPriority`

The app selects characteristics by priority, not by hardcoded UUID:

  Write priority:   FFF3 (100) > FFF6 (90) > FFF4 (80) > FFF1 (70)
  Notify priority:  FFF4 (100) > FFF6 (90) > FFF2 (80) > FFF1 (70)

Any reimplementation must perform the same dynamic characteristic discovery.
The actual characteristic in use on SmartBat-A05301 must be confirmed via
btsnoop HCI capture (see panther-bms-integration-spec.md §9 for procedure).

### IMPL-006 — Cross-reference to HA integration spec

A full implementation specification for the custom Home Assistant integration
has been written at:

  HomeAssistent/modules/camper/battery-monitor/panther-bms-integration-spec.md

That spec covers protocol details, XOR key derivation, register mapping, config
flow, entity definitions, coordinator design, Diagnose-Modus service, and the
ordered implementation steps including the btsnoop capture prerequisite.

### IMPL-007 — Confirmed register map and unavailable registers ✅ DONE 2026-07-02

Validated 2026-07-02 against SmartBat-A05301 (70% SoC, 13.23V, −3.75A, 25.6°C,
121 cycles, 100Ah nominal / 95.68Ah FCC). Source: logcat_threadtime.txt across
15 capture sessions.

**Registers that work (+RD,XX response):**

| Reg | +RAA cmd | Decode | Confirmed value |
|---|---|---|---|
| 0x02 | +RAA0202 | `byte[0]` = SoC% | 70 |
| 0x04 | +RAA0403 | `uint16_le(b[0:2]) × b[2] / 1000` = remaining Ah | 66.16 Ah |
| 0x06 | +RAA0603 | `uint16_le(b[0:2]) × b[2] / 1000` = FCC Ah | 95.68 Ah |
| 0x08 | +RAA0802 | `uint16_le(b[0:2])` = voltage mV | 13232 mV |
| 0x0A | +RAA0A03 | `byte[2]` = ratio (e.g. 10) | ratio=10 |
| 0x0C | +RAA0C02 | `uint16_le(b[0:2])` = K×10; (val/10)−273.15 = °C | 25.6°C |
| 0x10 | +RAA1002 | `int16_le(b[0:2]) × ratio / 1000` = current A | −3.75A |
| 0x3C | +RAA3C03 | `uint16_le(b[0:2]) × b[2] / 1000` = nominal Ah | 100.00 Ah |
| 0x48 | +RAA4802 | `uint16_le(b[0:2])` = cycles | 121 |

**Registers returning `+RD,Error1` — REMOVE from all polling:**
- 0x18 (time to empty), 0x1A (time to full), 0x28 (protection/FET)
- All +R163X alt-cell-probe commands (individual cell voltages unavailable)

**TX confirmed:** ALL commands → FFF6, writeWithoutResponse
**RX confirmed:** ALL responses → FFF4 notify, plain ASCII (no XOR on RX)
**XOR key SmartBat-A05301:** 0x11 (confirmed from every session log)

Full implementation spec: HomeAssistent/modules/camper/battery-monitor/panther-bms-integration-spec.md

## 9) Update Rules for This Brain

When we learn something important, append/update:
- Facts that were actually validated
- What changed in code/behavior
- New blockers and decisions
- Next concrete action

## 9) Resume Checkpoint

Date: 2026-07-02

Done today:
- IMPL-001: Removed JBD protocol entirely from `bms_service.dart`. App now enters
  LionCheck mode directly on connect without any JBD probe phase.
- IMPL-007: Removed Error1-returning registers (0x18/0x1A/0x28) and all +R163X
  alt-cell-probe commands from bootstrap and extended-sweep poll lists.
- IMPL-002: Fixed dual-battery combined power display in `_buildSummaryStrip`.
- Version bumped to `1.3.4+8`. In-app label, pubspec, runbook, and brain updated.
- Release APK built, committed, pushed to `main`, tagged `v1.3.4`.

Next:
- Validate on device: connect to SmartBat, confirm clean bootstrap with no
  JBD disconnect events in logcat, confirm power display in dual-battery view.
- No other pending IMPL items.

remember where to continue tomorrow.
