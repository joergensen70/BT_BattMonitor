# SmartBat Dual Battery App - Product Specification

Version: v0.1 (Draft)
Date: 2026-06-07
Status: Ready for implementation planning

## 1. Purpose and Vision
Build a mobile app for monitoring two LiFePO4 batteries connected in parallel.
The app should present both batteries on one screen, show aggregate system values, support one-time battery assignment, allow removing/replacing batteries, and provide a dark-mode-first experience.

Additionally, a home screen widget should show key values and update automatically when batteries are nearby.

## 2. Scope
### In scope
- Dual battery monitoring (Battery A + Battery B) on one dashboard
- Per-battery and aggregate values
- One-time battery assignment and persistent device mapping
- Delete/replace mapped batteries
- Dark mode UI with clear status hierarchy
- Auto-reconnect behavior for known batteries in proximity
- Home screen widget with key metrics
- Nice-to-have: BMS restart action (if protocol/device supports it)

### Out of scope (initial release)
- Firmware update over BLE
- Long-term cloud history and remote telemetry
- iOS full background BLE parity with Android

## 3. Users and Usage Context
### Primary users
- Camper/van users with parallel battery systems
- DIY solar/off-grid users monitoring battery health

### Core usage moments
- Quick status check before usage
- Load/charge verification while operating
- Fault diagnosis when protections trigger

## 4. High-Level UX Goals
- Instant understanding of overall system state
- Both batteries visible without navigation
- Fast detection of imbalance (voltage/current/cell delta)
- Minimal interaction for daily checks
- Expert depth available in detail views

## 5. Information Architecture
### Main navigation
- Dual Dashboard (default)
- Battery Details (A/B)
- Device Management
- Settings

### First-run flow
1. BLE permissions and adapter state check
2. Scan devices
3. Assign Battery A
4. Assign Battery B
5. Confirm names and save mapping

## 6. Functional Requirements
### FR-01 Dual Dashboard
The dashboard shall display both batteries simultaneously, including:
- SoC (percent)
- Voltage (V)
- Current (A, signed)
- Power (W)
- Remaining Ah and Nominal Ah
- Temperature summary (min/max or selected sensors)
- Active protection alerts
- Connection status and last update timestamp

### FR-02 Aggregate Parallel Values
The app shall compute and display aggregate values:
- Aggregate current = currentA + currentB
- Aggregate power = powerA + powerB
- Aggregate remaining capacity = remAhA + remAhB
- Aggregate nominal capacity = nomAhA + nomAhB
- Aggregate SoC = (remAhA + remAhB) / (nomAhA + nomAhB) * 100
- Pack voltage indicator (mean and difference)

### FR-03 Imbalance Detection
The app shall highlight imbalance:
- Delta voltage between A and B
- Delta current share between A and B
- Cell spread per battery (max cell - min cell)
- Visual severity states: normal, warning, critical

### FR-04 Battery Detail Screens
For each battery, the app shall provide:
- Overview metrics
- Cell voltages with min/max emphasis
- Temperature sensors
- Protection status list
- BLE/protocol debug log

### FR-05 Device Management
The app shall provide device management features:
- Assign battery A and B to discovered BLE devices
- Rename battery labels
- Remove a mapped battery
- Replace a mapped battery
- Persist mappings locally across app restarts

### FR-06 Connection and Reconnect
The app shall:
- Attempt reconnect to known devices when app starts
- Attempt reconnect when known devices appear in scan
- Recover gracefully if only one battery is available
- Expose clear connection state per battery

### FR-07 Home Screen Widget
The widget shall:
- Show aggregate SoC and connection state
- Show A/B compact metrics (SoC, V, A)
- Show alarm badge when protections are active
- Open app to dashboard on tap
- Refresh on new data and periodic fallback schedule

### FR-08 Error Handling
The app shall provide understandable errors for:
- Bluetooth disabled
- Permissions denied
- Missing service/characteristics
- Parse or protocol errors
- Timeouts/disconnects

### FR-09 Nice-to-Have: BMS Restart
The app may provide a BMS restart action if supported.
Requirements:
- Hidden under Advanced/Service tools
- Safety confirmation dialog with explicit warning
- Action disabled while charging/discharging above configurable threshold
- Rate limited (cooldown)
- Full operation logging (request, response, result)
- Fallback behavior if command not supported by BMS

Note:
- Protocol support for restart is vendor/model dependent and must be validated against target BMS command set.

## 7. Visual Design Specification (Dark Mode)
### Design direction
- Technical cockpit look with calm dark surfaces and high readability
- Strong hierarchy for warnings and critical alerts

### Color tokens
- Background: #0A0F14
- Surface: #121A23
- Surface elevated: #182330
- Primary accent: #35D07F
- Info accent: #4DB3FF
- Warning: #FFB020
- Critical: #FF5D5D
- Text primary: #EAF2FF
- Text secondary: #9FB0C8

### Typography
- Compact heading font for labels and sections
- Monospaced numeric style for live values (V, A, W, Ah, mV)

### Component behavior
- Subtle value-transition animations on updates (150-250 ms)
- Color transitions only on threshold crossing
- No flashing for normal updates

## 8. Data Model (Logical)
### BatteryDevice
- deviceId
- displayName
- lastSeenAt
- isMapped (A/B)

### BatterySnapshot
- timestamp
- voltage
- current
- power
- remainingAh
- nominalAh
- soc
- cycles
- temperatures[]
- cellVoltages[]
- chargeFet
- dischargeFet
- protectionStatus

### DualSnapshot
- snapshotA (optional)
- snapshotB (optional)
- aggregateSoC
- aggregateCurrent
- aggregatePower
- aggregateRemainingAh
- aggregateNominalAh
- voltageDelta
- currentShareA
- currentShareB
- activeAlerts[]

## 9. Platform and Technical Requirements
### Mobile app
- Flutter app as primary client
- BLE stack with robust reconnect state machine
- Local persistence for mappings/settings

### Widget
- Android first implementation for reliable background updates
- iOS support with platform background limitations acknowledged

### Performance
- UI updates should remain smooth under 1-2 second polling
- Initial dashboard render target under 1 second after data availability

## 10. States and Edge Cases
- Both batteries connected: full aggregate mode
- Only one connected: partial mode with explicit fallback
- None connected: offline mode with last-known timestamp
- Stale data detection and staleness indicator
- Unknown cell count handling
- Missing temperature sensors handling

## 11. Security and Safety Considerations
- No unsafe control action without explicit confirmation
- Advanced control functions (like BMS restart) gated behind warnings
- Persisted IDs stored locally; avoid leaking identifiers in external logs

## 12. Telemetry and Logging (Local)
- Connection/disconnection events
- Command TX/RX summaries
- Parse failures
- User actions: map/remove/replace/restart
- Optional exportable debug report for diagnosis

## 13. Acceptance Criteria
### AC-01 Dashboard clarity
Users can identify both battery states and aggregate state within 3 seconds.

### AC-02 Mapping persistence
After app restart, previously mapped Battery A/B are restored and reconnect is attempted.

### AC-03 Fault visibility
Active protections are visible on dashboard and detail screens with severity coloring.

### AC-04 Partial operation
If only one battery is available, app remains usable and clearly indicates degraded mode.

### AC-05 Widget usefulness
Widget shows usable status at a glance and opens app to dashboard.

### AC-06 Restart safety (nice-to-have)
If restart is enabled, action requires explicit confirmation and logs result.

## 14. Implementation Roadmap (Suggested)
1. Data orchestration for two devices + aggregate calculations
2. Device mapping persistence and management UI
3. Dual dashboard and detail screens
4. Reconnect strategy and degraded-mode UX
5. Android widget implementation
6. Advanced tools area with optional BMS restart
7. Validation, field testing, threshold tuning

## 15. Open Questions
- Which exact BMS models/protocol variants must be supported?
- Is BMS restart command available and documented for target hardware?
- Preferred default thresholds for imbalance and warning levels?
- Minimum widget refresh cadence acceptable for battery impact?
- Should user be able to lock one battery as primary reference?
