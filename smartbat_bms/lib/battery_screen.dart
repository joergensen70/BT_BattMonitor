import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'battery_data.dart';
import 'bms_service.dart';
import 'scan_screen.dart';

// ── Design tokens (from dual_battery_app_spec.md §7) ────────────────────────
const _kBg            = Color(0xFF0A0F14);
const _kSurface       = Color(0xFF121A23);
const _kSurfaceElev   = Color(0xFF182330);
const _kAccent        = Color(0xFF35D07F);
const _kInfo          = Color(0xFF4DB3FF);
const _kWarning       = Color(0xFFFFB020);
const _kCritical      = Color(0xFFFF5D5D);
const _kTextPrimary   = Color(0xFFEAF2FF);
const _kTextSecondary = Color(0xFF9FB0C8);

// ── Per-battery slot state ────────────────────────────────────────────────────
class _BattSlot {
  BluetoothDevice? device;
  BmsService? service;
  BatteryData? data;
  String status = '';
  DateTime? lastDataAt;
  List<String> logs = [];

  StreamSubscription? dataSub;
  StreamSubscription? connSub;
  StreamSubscription<String>? statusSub;
  StreamSubscription<String>? logSub;

  bool get isConnected => device != null;
  bool get hasData => data != null || lastDataAt != null;

  /// Synchronous cancel — safe to call from dispose().
  void cancelAll() {
    dataSub?.cancel(); dataSub = null;
    connSub?.cancel(); connSub = null;
    statusSub?.cancel(); statusSub = null;
    logSub?.cancel(); logSub = null;
    service?.dispose();
    service = null;
  }

  /// Full async teardown — resets all state including device reference.
  Future<void> teardown() async {
    dataSub?.cancel(); dataSub = null;
    connSub?.cancel(); connSub = null;
    statusSub?.cancel(); statusSub = null;
    logSub?.cancel(); logSub = null;
    await service?.disconnect();
    service = null;
    device = null;
    data = null;
    lastDataAt = null;
    status = '';
    logs.clear();
  }
}

// ── Main widget ───────────────────────────────────────────────────────────────
class BatteryScreen extends StatefulWidget {
  const BatteryScreen({super.key});

  @override
  State<BatteryScreen> createState() => _BatteryScreenState();
}

class _BatteryScreenState extends State<BatteryScreen> {
  final _slotA = _BattSlot();
  final _slotB = _BattSlot();
  bool _disposed = false;
  bool _isRefreshingA = false;
  bool _isRefreshingB = false;
  static const int _maxRetries = 5;
  static const Duration _retryDelay = Duration(seconds: 2);

  bool get _anyConnected => _slotA.isConnected || _slotB.isConnected;

  static const _prefKeyA = 'saved_mac_a';
  static const _prefKeyB = 'saved_mac_b';

  @override
  void initState() {
    super.initState();
    _autoReconnect();
  }

  Future<void> _saveSlotMac(String key, String? mac) async {
    final prefs = await SharedPreferences.getInstance();
    if (mac == null) {
      await prefs.remove(key);
    } else {
      await prefs.setString(key, mac);
    }
  }

  Future<void> _autoReconnect() async {
    final prefs = await SharedPreferences.getInstance();
    final macA = prefs.getString(_prefKeyA);
    final macB = prefs.getString(_prefKeyB);
    if (macA == null && macB == null) return;

    // Give BT stack a moment to be ready
    await Future.delayed(const Duration(milliseconds: 800));
    if (_disposed || !mounted) return;

    final devices = FlutterBluePlus.connectedDevices;

    Future<void> trySlot(_BattSlot slot, String? mac, String prefKey) async {
      if (mac == null || _disposed || !mounted) return;
      // Check if already connected (e.g. system kept connection)
      BluetoothDevice? found;
      try {
        found = devices.firstWhere((d) => d.remoteId.str == mac);
      } catch (_) {}
      // Fall back to scanning briefly for the saved MAC
      found ??= BluetoothDevice.fromId(mac);
      await slot.teardown();
      if (!mounted) return;
      setState(() { slot.status = 'Auto-connecting…'; slot.device = found; });
      slot.service = BmsService();
      _attachStreams(slot);
      _connect(slot);
    }

    await trySlot(_slotA, macA, _prefKeyA);

    // Wait for slot A to get data (or timeout) before starting slot B —
    // BLE cannot reliably handle two simultaneous connection setups.
    if (macB != null && macA != null) {
      for (int i = 0; i < 40; i++) {
        await Future.delayed(const Duration(milliseconds: 200));
        if (_disposed || !mounted) return;
        if (_slotA.hasData) break;
      }
    }

    await trySlot(_slotB, macB, _prefKeyB);
  }

  @override
  void dispose() {
    _disposed = true;
    _slotA.cancelAll();
    _slotB.cancelAll();
    super.dispose();
  }

  // ── Scan & connect ──────────────────────────────────────────────────────────

  Future<void> _openScanPicker() async {
    final slotChoice = await showModalBottomSheet<String>(
      context: context,
      backgroundColor: _kSurface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (_) => SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 40, height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                    color: _kTextSecondary.withOpacity(0.4),
                    borderRadius: BorderRadius.circular(2)),
              ),
              const Text('Select slot to connect',
                  style: TextStyle(
                      color: _kTextPrimary,
                      fontWeight: FontWeight.bold,
                      fontSize: 16)),
              const SizedBox(height: 16),
              Row(children: [
                Expanded(child: _slotButton('Battery A', 'A',
                    _slotA.isConnected ? _slotA.device!.platformName : null)),
                const SizedBox(width: 12),
                Expanded(child: _slotButton('Battery B', 'B',
                    _slotB.isConnected ? _slotB.device!.platformName : null)),
              ]),
            ],
          ),
        ),
      ),
    );
    if (slotChoice == null || !mounted) return;

    final device = await Navigator.push<BluetoothDevice>(
      context,
      MaterialPageRoute(builder: (_) => const ScanScreen()),
    );
    if (device == null || !mounted) return;

    final slot = slotChoice == 'A' ? _slotA : _slotB;
    final prefKey = slotChoice == 'A' ? _prefKeyA : _prefKeyB;
    await slot.teardown();
    if (!mounted) return;
    setState(() { slot.status = 'Connecting…'; slot.device = device; });
    await _saveSlotMac(prefKey, device.remoteId.str);
    slot.service = BmsService();
    _attachStreams(slot);
    _connect(slot);
  }

  Widget _slotButton(String label, String value, String? currentName) {
    return ElevatedButton(
      style: ElevatedButton.styleFrom(
        backgroundColor: _kSurfaceElev,
        foregroundColor: _kTextPrimary,
        padding: const EdgeInsets.symmetric(vertical: 14),
      ),
      onPressed: () => Navigator.pop(context, value),
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        Text(label,
            style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 15)),
        if (currentName != null) ...[
          const SizedBox(height: 4),
          Text(currentName,
              style: const TextStyle(fontSize: 11, color: _kAccent),
              overflow: TextOverflow.ellipsis),
        ] else
          const Text('— empty —',
              style: TextStyle(fontSize: 11, color: _kTextSecondary)),
      ]),
    );
  }

  void _attachStreams(_BattSlot slot) {
    slot.statusSub?.cancel();
    slot.logSub?.cancel();
    slot.statusSub = slot.service!.statusStream.listen((s) {
      if (!mounted) return;
      setState(() {
        if (slot.hasData) {
          final l = s.toLowerCase();
          if (l.contains('connecting') || l.contains('connected') ||
              l.contains('discovering') || l.contains('waiting')) return;
        }
        slot.status = s;
      });
    });
    slot.logSub = slot.service!.debugStream.listen((line) {
      if (!mounted) return;
      setState(() {
        final ts = _timeLabel(DateTime.now());
        slot.logs.insert(0, '$ts  $line');
        if (slot.logs.length > 40) slot.logs.removeRange(40, slot.logs.length);
        if (!slot.hasData && line.startsWith('RX ')) {
          slot.status = 'Connected – receiving…';
        }
      });
    });
  }

  Future<void> _connect(_BattSlot slot) async {
    slot.dataSub?.cancel();
    slot.connSub?.cancel();
    slot.connSub = slot.device!.connectionState.listen((state) {
      if (mounted && state == BluetoothConnectionState.disconnected && !_disposed) {
        setState(() => slot.status = 'Disconnected');
      }
    });
    slot.dataSub = slot.service!.dataStream.listen((d) {
      if (mounted) setState(() {
        slot.data = d;
        slot.lastDataAt = DateTime.now();
        slot.status = 'Data received';
      });
    });

    for (int attempt = 1; attempt <= _maxRetries; attempt++) {
      if (_disposed || !mounted) return;
      try {
        if (attempt > 1) {
          if (mounted) setState(() => slot.status = 'Retrying ($attempt/$_maxRetries)…');
          await Future.delayed(_retryDelay);
          if (_disposed || !mounted) return;
          await slot.service!.disconnect();
          await Future.delayed(const Duration(milliseconds: 500));
          if (_disposed || !mounted) return;
        }
        await slot.service!.connect(slot.device!);
        if (mounted && !slot.hasData) setState(() => slot.status = 'Connected – waiting…');
        return;
      } catch (e) {
        if (_disposed || !mounted) return;
        setState(() => slot.status = attempt == _maxRetries
            ? 'Failed after $_maxRetries attempts'
            : 'Attempt $attempt failed, retrying…');
      }
    }
  }

  Future<void> _disconnectSlot(_BattSlot slot) async {
    final prefKey = slot == _slotA ? _prefKeyA : _prefKeyB;
    await _saveSlotMac(prefKey, null);
    await slot.teardown();
    if (mounted) setState(() {});
  }

  Future<void> _refreshSlot(_BattSlot slot) async {
    await slot.service?.requestSnapshot();
  }

  String _timeLabel(DateTime value) {
    final hh = value.hour.toString().padLeft(2, '0');
    final mm = value.minute.toString().padLeft(2, '0');
    final ss = value.second.toString().padLeft(2, '0');
    return '$hh:$mm:$ss';
  }
  // â”€â”€ Build â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Image.asset('assets/Odin_Kopf.png', width: 26, height: 26),
            const SizedBox(width: 8),
            const Flexible(
              child: Text('SmartBat BMS',
                  overflow: TextOverflow.ellipsis,
                  style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            ),
          ],
        ),
        backgroundColor: _kSurface,
        foregroundColor: _kTextPrimary,
        actions: [
          IconButton(
            icon: const Icon(Icons.bluetooth_searching),
            tooltip: 'Connect battery',
            onPressed: _openScanPicker,
          ),
        ],
      ),
      backgroundColor: _kBg,
      body: (!_anyConnected) ? _buildIdle() : _buildBody(),
    );
  }

  Widget _buildIdle() {
    return Center(
      child: Column(mainAxisSize: MainAxisSize.min, children: [
        Image.asset('assets/Odin_Kopf.png', width: 80, height: 80,
            color: _kTextSecondary.withOpacity(0.3),
            colorBlendMode: BlendMode.modulate),
        const SizedBox(height: 24),
        const Text('No battery connected',
            style: TextStyle(color: _kTextSecondary, fontSize: 16)),
        const SizedBox(height: 8),
        Row(mainAxisSize: MainAxisSize.min, children: [
          const Text('Tap ', style: TextStyle(color: _kTextSecondary, fontSize: 13)),
          const Icon(Icons.bluetooth_searching, color: _kInfo, size: 16),
          const Text(' to connect a battery',
              style: TextStyle(color: _kTextSecondary, fontSize: 13)),
        ]),
      ]),
    );
  }

  Widget _buildBody() {
    final bothHaveData = (_slotA.data != null) && (_slotB.data != null);
    return SingleChildScrollView(
      padding: const EdgeInsets.all(12),
      child: Column(children: [
        _buildBatteryCard(_slotA, 'Battery A',
            _isRefreshingA, (v) => setState(() => _isRefreshingA = v)),
        if (_slotB.isConnected) const SizedBox(height: 12),
        _buildBatteryCard(_slotB, 'Battery B',
            _isRefreshingB, (v) => setState(() => _isRefreshingB = v)),
        if (bothHaveData) ...[
          const SizedBox(height: 12),
          _buildSummaryStrip(_slotA.data!, _slotB.data!),
        ],
        const SizedBox(height: 24),
      ]),
    );
  }

  // â”€â”€ Battery card â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  Widget _buildBatteryCard(_BattSlot slot, String label, bool isRefreshing,
      void Function(bool) setRefreshing) {
    if (!slot.isConnected) return const SizedBox.shrink();

    final name = slot.device!.platformName.isNotEmpty
        ? slot.device!.platformName : label;

    return Card(
      color: _kSurface,
      clipBehavior: Clip.hardEdge,
      child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
        Container(
          color: _kSurfaceElev,
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          child: Row(children: [
            Text(label, style: const TextStyle(
                color: _kTextSecondary, fontSize: 12, fontWeight: FontWeight.w600)),
            const SizedBox(width: 8),
            Expanded(child: Text(name,
                overflow: TextOverflow.ellipsis,
                style: const TextStyle(
                    color: _kTextPrimary, fontSize: 13, fontWeight: FontWeight.bold))),
            if (slot.status.isNotEmpty && slot.data == null)
              Text(slot.status,
                  style: const TextStyle(color: _kTextSecondary, fontSize: 11)),
            const SizedBox(width: 4),
            InkWell(
              onTap: isRefreshing ? null : () async {
                setRefreshing(true);
                await _refreshSlot(slot);
                setRefreshing(false);
              },
              child: isRefreshing
                  ? const SizedBox(width: 16, height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2, color: _kAccent))
                  : const Icon(Icons.refresh, size: 18, color: _kTextSecondary),
            ),
            const SizedBox(width: 8),
            InkWell(
              onTap: () => _showSlotInfoSheet(slot),
              child: const Icon(Icons.info_outline, size: 18, color: _kTextSecondary),
            ),
            const SizedBox(width: 8),
            InkWell(
              onTap: () => _disconnectSlot(slot),
              child: const Icon(Icons.bluetooth_disabled, size: 18, color: _kCritical),
            ),
          ]),
        ),

        if (slot.data == null)
          Padding(
            padding: const EdgeInsets.all(20),
            child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              const SizedBox(width: 18, height: 18,
                  child: CircularProgressIndicator(strokeWidth: 2, color: _kAccent)),
              const SizedBox(width: 14),
              Text(slot.status,
                  style: const TextStyle(color: _kTextSecondary, fontSize: 13)),
            ]),
          )
        else
          _buildSlotData(slot.data!),
      ]),
    );
  }

  Widget _buildSlotData(BatteryData d) {
    final Color socColor = d.soc > 60 ? _kAccent : d.soc > 25 ? _kWarning : _kCritical;

    String? timeHint;
    if (d.isCharging && (d.attfMin ?? 0) > 0 && d.attfMin != 65535) {
      final h = d.attfMin! ~/ 60; final m = d.attfMin! % 60;
      timeHint = h > 0 ? 'CHG  ${h}h ${m}min to full' : 'CHG  ${m}min to full';
    } else if (d.isDischarging && (d.atteMin ?? 0) > 0 && d.atteMin != 65535) {
      final h = d.atteMin! ~/ 60; final m = d.atteMin! % 60;
      timeHint = h > 0 ? 'DSC  ${h}h ${m}min left' : 'DSC  ${m}min left';
    }

    return Padding(
      padding: const EdgeInsets.fromLTRB(10, 8, 10, 6),
      child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
        Row(crossAxisAlignment: CrossAxisAlignment.center, children: [
          SizedBox(width: 95, height: 95,
            child: CustomPaint(
              painter: _GaugePainter(d.soc / 100.0, socColor),
              child: Center(child: Column(mainAxisSize: MainAxisSize.min, children: [
                Text('${d.soc}%',
                    style: TextStyle(fontSize: 20, fontWeight: FontWeight.normal,
                        color: socColor)),
                Icon(
                  d.isCharging ? Icons.bolt
                      : d.isDischarging ? Icons.battery_4_bar
                      : Icons.horizontal_rule,
                  size: 14,
                  color: d.isCharging ? _kAccent : _kInfo,
                ),
              ])),
            ),
          ),
          const SizedBox(width: 10),
          Expanded(child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
            _inlineStatRow('Voltage', '${d.voltage.toStringAsFixed(3)} V',
                Icons.flash_on, Colors.yellow),
            const SizedBox(height: 4),
            _inlineStatRow('Current',
                '${d.current >= 0 ? '+' : ''}${d.current.toStringAsFixed(3)} A',
                Icons.compare_arrows, d.isCharging ? _kAccent : _kInfo),
            const SizedBox(height: 4),
            _inlineStatRow('Power', '${d.power.toStringAsFixed(1)} W',
                Icons.bolt, _kWarning),
            const SizedBox(height: 4),
            _inlineStatRow('Cycles', '${d.cycles}',
                Icons.loop, Colors.purpleAccent),
          ])),
        ]),

        const SizedBox(height: 4),
        Text(
            '${d.remainingAh.toStringAsFixed(1)} Ah  /  ${d.nominalAh.toStringAsFixed(1)} Ah',
            textAlign: TextAlign.center,
            style: const TextStyle(color: _kTextSecondary, fontSize: 12)),
        if (timeHint != null) ...[
          const SizedBox(height: 2),
          Text(timeHint, textAlign: TextAlign.center,
              style: const TextStyle(color: _kTextSecondary, fontSize: 11)),
        ],

        if (d.temperatures.isNotEmpty) ...[
          const SizedBox(height: 6),
          Wrap(spacing: 6, runSpacing: 2,
            children: d.temperatures.asMap().entries.map((e) {
              final degC = e.value;
              final degF = ((degC * 1.8 + 32.0) * 10).round() / 10.0;
              final color = degC > 45 ? _kCritical : degC > 35 ? _kWarning : _kAccent;
              return Chip(
                materialTapTargetSize: MaterialTapTargetSize.shrinkWrap,
                backgroundColor: _kSurfaceElev,
                padding: const EdgeInsets.symmetric(horizontal: 4),
                label: Text(
                    '${e.key == 0 ? 'Temperature' : 'NTC${e.key + 1}'}: ${degC.toStringAsFixed(1)}°C / ${degF.toStringAsFixed(1)}°F',
                    style: TextStyle(color: color, fontSize: 12)),
              );
            }).toList()),
        ],

        if (d.cellVoltages.isNotEmpty) ...[
          const SizedBox(height: 6),
          _buildCellsCompact(d),
        ],

        if (d.activeProtections.isNotEmpty) ...[
          const SizedBox(height: 8),
          ...d.activeProtections.map((p) => Row(children: [
            const Icon(Icons.circle, size: 6, color: _kCritical),
            const SizedBox(width: 6),
            Text(p, style: const TextStyle(color: _kCritical, fontSize: 13)),
          ])),
        ],
      ]),
    );
  }

  Widget _inlineStatRow(String label, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
          color: _kSurfaceElev, borderRadius: BorderRadius.circular(6)),
      child: Row(children: [
        Icon(icon, size: 14, color: color),
        const SizedBox(width: 6),
        Text(label, style: const TextStyle(fontSize: 11, color: _kTextSecondary)),
        const Spacer(),
        Text(value, style: const TextStyle(
            fontSize: 14, fontWeight: FontWeight.bold, color: _kTextPrimary)),
      ]),
    );
  }

  Widget _buildCellsCompact(BatteryData d) {
    final voltages = d.cellVoltages;
    final minV = voltages.reduce(min);
    final maxV = voltages.reduce(max);
    final deltaMs = ((maxV - minV) * 1000).round();
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Row(children: [
        const Icon(Icons.grid_view, color: _kAccent, size: 14),
        const SizedBox(width: 4),
        const Text('Cells', style: TextStyle(color: _kTextSecondary, fontSize: 12)),
        const Spacer(),
        Text('Î” $deltaMs mV',
            style: TextStyle(fontSize: 11,
                color: deltaMs > 50 ? _kWarning : _kTextSecondary)),
      ]),
      const SizedBox(height: 4),
      GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        padding: EdgeInsets.zero,
        gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4, childAspectRatio: 1.7,
            mainAxisSpacing: 4, crossAxisSpacing: 4),
        itemCount: voltages.length,
        itemBuilder: (_, i) {
          final v = voltages[i];
          final isMin = voltages.length > 1 && v == minV;
          final isMax = voltages.length > 1 && v == maxV;
          return Container(
            decoration: BoxDecoration(
              color: _kSurfaceElev,
              borderRadius: BorderRadius.circular(4),
              border: Border.all(
                  color: isMin ? _kCritical : isMax ? _kAccent : Colors.transparent,
                  width: 1.5),
            ),
            child: Column(mainAxisAlignment: MainAxisAlignment.center, children: [
              Text('C${i + 1}',
                  style: const TextStyle(fontSize: 9, color: _kTextSecondary)),
              Text('${(v * 1000).round()}',
                  style: const TextStyle(
                      fontSize: 11, fontWeight: FontWeight.bold, color: _kTextPrimary)),
            ]),
          );
        },
      ),
    ]);
  }

  // â”€â”€ Summary strip â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  Widget _buildSummaryStrip(BatteryData a, BatteryData b) {
    final totalRemainingAh = a.remainingAh + b.remainingAh;
    final totalNominalAh   = a.nominalAh   + b.nominalAh;
    final totalCurrent     = a.current     + b.current;
    final totalPower       = a.voltage * a.current.abs() + b.voltage * b.current.abs();
    final combinedSoc      = totalNominalAh > 0
        ? (totalRemainingAh / totalNominalAh * 100).round() : 0;
    final isCharging    = totalCurrent > 0.1;
    final isDischarging = totalCurrent < -0.1;

    return Card(
      color: _kSurfaceElev,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Row(children: [
            const Icon(Icons.summarize, color: _kInfo, size: 16),
            const SizedBox(width: 6),
            const Text('Combined', style: TextStyle(
                color: _kTextPrimary, fontWeight: FontWeight.bold, fontSize: 14)),
            const Spacer(),
            Text('$combinedSoc%',
                style: TextStyle(
                    color: combinedSoc > 60 ? _kAccent
                        : combinedSoc > 25 ? _kWarning : _kCritical,
                    fontWeight: FontWeight.bold,
                    fontSize: 16)),
          ]),
          const SizedBox(height: 10),
          Row(children: [
            Expanded(child: _summaryCell('Capacity',
                '${totalRemainingAh.toStringAsFixed(1)} / ${totalNominalAh.toStringAsFixed(1)} Ah',
                Icons.battery_full, _kAccent)),
            const SizedBox(width: 8),
            Expanded(child: _summaryCell('Current',
                '${totalCurrent >= 0 ? '+' : ''}${totalCurrent.toStringAsFixed(2)} A',
                Icons.compare_arrows,
                isCharging ? _kAccent : isDischarging ? _kInfo : _kTextSecondary)),
            const SizedBox(width: 8),
            Expanded(child: _summaryCell('Power',
                '${totalPower.toStringAsFixed(1)} W',
                Icons.bolt, _kWarning)),
          ]),
        ]),
      ),
    );
  }

  Widget _summaryCell(String label, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      decoration: BoxDecoration(
          color: _kSurface, borderRadius: BorderRadius.circular(6)),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Row(children: [
          Icon(icon, size: 12, color: color),
          const SizedBox(width: 4),
          Text(label, style: const TextStyle(fontSize: 10, color: _kTextSecondary)),
        ]),
        const SizedBox(height: 4),
        Text(value,
            style: TextStyle(fontSize: 13, fontWeight: FontWeight.bold, color: color),
            overflow: TextOverflow.ellipsis),
      ]),
    );
  }

  // â”€â”€ Info sheet â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

  void _showSlotInfoSheet(_BattSlot slot) {
    final deviceId = slot.device?.remoteId.toString() ?? 'â€”';
    final lastData = slot.lastDataAt == null ? 'No data yet' : _timeLabel(slot.lastDataAt!);
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: _kSurface,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (_) => DraggableScrollableSheet(
        expand: false, initialChildSize: 0.55, maxChildSize: 0.92,
        builder: (_, scrollController) => ListView(
          controller: scrollController,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          children: [
            Center(child: Container(
              width: 40, height: 4,
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                  color: _kTextSecondary.withOpacity(0.4),
                  borderRadius: BorderRadius.circular(2)),
            )),
            _infoRow('Device', slot.device?.platformName.isNotEmpty == true
                ? slot.device!.platformName : 'â€”'),
            _infoRow('MAC', deviceId),
            _infoRow('Status', slot.status),
            _infoRow('Last update', lastData),
            const SizedBox(height: 16),
            const Text('BLE / Protocol Log',
                style: TextStyle(color: _kTextPrimary,
                    fontWeight: FontWeight.bold, fontSize: 14)),
            const SizedBox(height: 8),
            if (slot.logs.isEmpty)
              const Text('No log entries yet.',
                  style: TextStyle(color: _kTextSecondary, fontSize: 12))
            else
              ...slot.logs.map((line) => Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Text(line,
                    style: const TextStyle(
                        color: _kTextSecondary, fontSize: 11,
                        fontFamily: 'monospace')),
              )),
          ],
        ),
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
        SizedBox(width: 100,
            child: Text(label,
                style: const TextStyle(color: _kTextSecondary, fontSize: 13))),
        Expanded(child: Text(value,
            style: const TextStyle(
                color: _kTextPrimary, fontSize: 13, fontFamily: 'monospace'))),
      ]),
    );
  }
}

// ──────────────────────────────────────────────
// Custom arc gauge painter
// ──────────────────────────────────────────────
class _GaugePainter extends CustomPainter {
  final double value; // 0.0 – 1.0
  final Color color;

  const _GaugePainter(this.value, this.color);

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = min(size.width, size.height) / 2 - 12;

    final bgPaint = Paint()
      ..color = Colors.white12
      ..style = PaintingStyle.stroke
      ..strokeWidth = 14
      ..strokeCap = StrokeCap.round;

    final fgPaint = Paint()
      ..color = color
      ..style = PaintingStyle.stroke
      ..strokeWidth = 14
      ..strokeCap = StrokeCap.round;

    const startAngle = pi * 0.75;       // 135°
    const sweepFull  = pi * 1.5;        // 270° total arc

    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      startAngle,
      sweepFull,
      false,
      bgPaint,
    );
    if (value > 0) {
      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        startAngle,
        sweepFull * value.clamp(0.0, 1.0),
        false,
        fgPaint,
      );
    }
  }

  @override
  bool shouldRepaint(_GaugePainter old) =>
      old.value != value || old.color != color;
}
