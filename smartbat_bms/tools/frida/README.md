# Frida Hooks For LionCheck

## Purpose
Capture app-level BLE payloads and potential crypto transformations to avoid guesswork in protocol mapping.

## Files
- lioncheck_ble_crypto_hook.js: hooks BLE and common crypto APIs.

## Prerequisites
- Android device connected via ADB
- Frida tools installed on host
- For non-root (jailed) devices: Frida Gadget flow may be required for target apps

## Attach command
Use package name from capture evidence (LionCheck):

frida -U -f com.gddai.lioncheck -l tools/frida/lioncheck_ble_crypto_hook.js

If app is already running:

frida -U -n com.gddai.lioncheck -l tools/frida/lioncheck_ble_crypto_hook.js

If frida is not on PATH, use full executable path:

C:\Users\joerg\AppData\Local\Programs\Python\Python314\Scripts\frida.exe -U -f com.gddai.lioncheck -l C:\git\BT_BattMonitor\smartbat_bms\tools\frida\lioncheck_ble_crypto_hook.js

## Known blocker seen on this setup

- Device is reachable by Frida, but direct live attach to LionCheck on jailed Android was unstable:
	- spawn/attach attempts can end with closed remote frida-server session
- In this case, fastest path is:
	- static APK analysis + controlled capture delta matrix
	- optionally move to rooted test device or patched test APK with Frida Gadget

## Recommended parallel capture
1. Start host capture script (tools/start_smartbat_capture.ps1)
2. Start Frida hook command and redirect output to file
3. Reproduce one controlled test run
4. Stop host capture and archive both logs together

## Output hints
Look for:
- [FRIDA][GATT_WRITE] and [FRIDA][CHAR_SET_VALUE]
- [FRIDA][CRYPTO_IN]/[FRIDA][CRYPTO_OUT]
- Correlate timestamps with logcat/HCI events
