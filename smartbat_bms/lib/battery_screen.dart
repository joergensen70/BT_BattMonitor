import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:share_plus/share_plus.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'battery_data.dart';
import 'bms_service.dart';
import 'demo_data_generator.dart';
import 'chart_screen.dart';
import 'debug_log_service.dart';
import 'recording_service.dart';
import 'scan_screen.dart';

// ── Design tokens (from dual_battery_app_spec.md §7) ────────────────────────
const _kBg = Color(0xFF0A0F14);
const _kSurface = Color(0xFF121A23);
const _kSurfaceElev = Color(0xFF182330);
const _kAccent = Color(0xFF35D07F);
const _kInfo = Color(0xFF4DB3FF);
const _kWarning = Color(0xFFFFB020);
const _kCritical = Color(0xFFFF5D5D);
const _kTextPrimary = Color(0xFFEAF2FF);
const _kTextSecondary = Color(0xFF9FB0C8);

enum _BatteryCardViewMode { status, cells }

// ── Per-battery slot state ────────────────────────────────────────────────────
class _BattSlot {
  BluetoothDevice? device;
  BmsService? service;
  BatteryData? data;
  String status = '';
  String savedName = ''; // fallback when platformName is empty (auto-reconnect)
  DateTime? lastDataAt;
  List<String> logs = [];
  _BatteryCardViewMode viewMode = _BatteryCardViewMode.status;

  StreamSubscription? dataSub;
  StreamSubscription? connSub;
  StreamSubscription<String>? statusSub;
  StreamSubscription<String>? logSub;
  int connectEpoch = 0;
  bool connectInProgress = false;
  bool reconnectQueued = false;

  bool get isConnected => device != null;
  bool get hasData => data != null || lastDataAt != null;
  bool get isDemoSlot => device?.remoteId.str.startsWith('DEMO-') == true;

  /// Synchronous cancel — safe to call from dispose().
  void cancelAll() {
    dataSub?.cancel();
    dataSub = null;
    connSub?.cancel();
    connSub = null;
    statusSub?.cancel();
    statusSub = null;
    logSub?.cancel();
    logSub = null;
    service?.dispose();
    service = null;
  }

  /// Full async teardown — resets all state including device reference.
  Future<void> teardown() async {
    dataSub?.cancel();
    dataSub = null;
    connSub?.cancel();
    connSub = null;
    statusSub?.cancel();
    statusSub = null;
    logSub?.cancel();
    logSub = null;
    await service?.disconnect();
    service = null;
    device = null;
    data = null;
    lastDataAt = null;
    status = '';
    logs.clear();
    viewMode = _BatteryCardViewMode.status;
    connectInProgress = false;
    reconnectQueued = false;
  }
}

// ── Main widget ───────────────────────────────────────────────────────────────
class BatteryScreen extends StatefulWidget {
  const BatteryScreen({super.key});

  @override
  State<BatteryScreen> createState() => _BatteryScreenState();
}

class _BatteryScreenState extends State<BatteryScreen>
    with WidgetsBindingObserver {
  final _slotA = _BattSlot();
  final _slotB = _BattSlot();
  final _debugLog = DebugLogService.instance;
  bool _disposed = false;
  bool _isRefreshingA = false;
  bool _isRefreshingB = false;
  bool _demoMode = false;
  bool _isSwitchingMode = false;
  static const int _maxRetries = 3;
  static const Duration _retryDelay = Duration(seconds: 3);
  static const Duration _connectTimeout = Duration(seconds: 15);
  static const Duration _staleThreshold = Duration(seconds: 30);
  static const String _kAppVersion = 'v1.3.4';
  static const double _kFrameGap = 4.0;
  Timer? _staleTimer;
  StreamSubscription<BatteryData>? _demoASub;
  StreamSubscription<BatteryData>? _demoBSub;

  bool get _anyConnected =>
      _slotA.isConnected || _slotB.isConnected || _demoMode;

  String _slotTag(_BattSlot slot) => slot == _slotA ? 'A' : 'B';

  String _slotDisplayName(_BattSlot slot, {String fallback = ''}) {
    if (slot.isDemoSlot) return 'Demo Battery ${_slotTag(slot)}';
    final platformName = slot.device?.platformName ?? '';
    if (platformName.isNotEmpty) return platformName;
    if (slot.savedName.isNotEmpty) return slot.savedName;
    return fallback;
  }

  void _pushSlotLog(_BattSlot slot, String line) {
    final ts = _timeLabel(DateTime.now());
    if (mounted) {
      slot.logs.insert(0, '$ts  $line');
      if (slot.logs.length > 40) slot.logs.removeRange(40, slot.logs.length);
    }
    _debugLog.add('slot=${_slotTag(slot)} $line');
  }

  static bool _isActivelyConnecting(String status) {
    final s = status.toLowerCase();
    return s.contains('connecting') ||
        s.contains('retrying') ||
        s.contains('scanning');
  }

  static const _prefKeyA = 'saved_mac_a';
  static const _prefKeyB = 'saved_mac_b';
  static const _prefNameA = 'saved_name_a';
  static const _prefNameB = 'saved_name_b';

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
    _autoReconnect();
    _staleTimer =
        Timer.periodic(const Duration(seconds: 10), (_) => _checkStale());
  }

  Future<void> _saveSlot(
      String macKey, String nameKey, String? mac, String? name) async {
    final prefs = await SharedPreferences.getInstance();
    if (mac == null) {
      await prefs.remove(macKey);
      await prefs.remove(nameKey);
    } else {
      await prefs.setString(macKey, mac);
      if (name != null && name.isNotEmpty) await prefs.setString(nameKey, name);
    }
  }

  Future<void> _autoReconnect() async {
    if (_demoMode) return;
    final prefs = await SharedPreferences.getInstance();
    final macA = prefs.getString(_prefKeyA);
    final macB = prefs.getString(_prefKeyB);
    final nameA = prefs.getString(_prefNameA);
    final nameB = prefs.getString(_prefNameB);
    if (macA == null && macB == null) return;

    await Future.delayed(const Duration(milliseconds: 800));
    if (_disposed || !mounted) return;

    final connected = FlutterBluePlus.connectedDevices;

    Future<void> trySlot(_BattSlot slot, String? mac, String? savedName) async {
      if (mac == null || _disposed || !mounted) return;
      BluetoothDevice? found;
      try {
        found = connected.firstWhere((d) => d.remoteId.str == mac);
      } catch (_) {}
      found ??= BluetoothDevice.fromId(mac);
      final nameForKey =
          (found.platformName.isNotEmpty ? found.platformName : savedName) ??
              '';
      await slot.teardown();
      if (!mounted) return;
      setState(() {
        slot.status = 'Auto-connecting…';
        slot.device = found;
        slot.savedName = nameForKey;
      });
      slot.service = BmsService();
      if (nameForKey.isNotEmpty) slot.service!.overrideDeviceName(nameForKey);
      _attachStreams(slot);
      _connect(slot);
    }

    await trySlot(_slotA, macA, nameA);

    if (macB != null && macA != null) {
      for (int i = 0; i < 40; i++) {
        await Future.delayed(const Duration(milliseconds: 200));
        if (_disposed || !mounted) return;
        if (_slotA.hasData) break;
      }
    }

    await trySlot(_slotB, macB, nameB);
  }

  @override
  void dispose() {
    _disposed = true;
    _staleTimer?.cancel();
    WidgetsBinding.instance.removeObserver(this);
    _stopDemoStreams();
    _slotA.cancelAll();
    _slotB.cancelAll();
    if (_demoMode) DemoDataGenerator.instance.stop();
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    if (state == AppLifecycleState.resumed) {
      _checkStale();
    }
  }

  void _checkStale() {
    if (_disposed || !mounted || _demoMode || _isSwitchingMode) return;
    for (final slot in [_slotA, _slotB]) {
      if (!slot.isConnected) continue;
      if (slot.lastDataAt == null) continue;
      final age = DateTime.now().difference(slot.lastDataAt!);
      if (age > _staleThreshold) {
        _reconnectSlot(slot);
      }
    }
  }

  Future<void> _reconnectSlot(_BattSlot slot,
      {bool userRequested = false}) async {
    if (_disposed || !mounted || _demoMode || _isSwitchingMode) return;
    if (slot.connectInProgress) return;
    final device = slot.device;
    final savedName = slot.savedName;
    if (device == null) return;
    _pushSlotLog(slot,
        userRequested ? 'UI reconnect requested' : 'Auto reconnect scheduled');
    setState(() {
      slot.status = slot.hasData ? 'Verbunden' : 'Verbinden…';
    });
    await slot.service?.disconnect();
    slot.dataSub?.cancel();
    slot.connSub?.cancel();
    if (!mounted || _disposed) return;
    slot.service ??= BmsService();
    if (savedName.isNotEmpty) slot.service!.overrideDeviceName(savedName);
    _attachStreams(slot);
    await _connect(slot);
  }

  void _scheduleReconnect(_BattSlot slot) {
    if (slot.reconnectQueued || slot.connectInProgress) return;
    slot.reconnectQueued = true;
    unawaited(Future<void>.delayed(_retryDelay, () {
      slot.reconnectQueued = false;
      if (!_disposed && mounted && !_demoMode && !_isSwitchingMode) {
        unawaited(_reconnectSlot(slot));
      }
    }));
  }

  Future<void> _refreshAllSlots() async {
    if (_demoMode || _isSwitchingMode) return;
    _debugLog.add('UI pull-to-refresh requested');
    if (_slotA.isConnected) await _reconnectSlot(_slotA, userRequested: true);
    if (_slotB.isConnected) await _reconnectSlot(_slotB, userRequested: true);
  }

  Future<void> _toggleDemoMode() async {
    if (_isSwitchingMode) return;
    if (mounted) {
      setState(() => _isSwitchingMode = true);
    }

    if (_demoMode) {
      _stopDemoStreams();
      DemoDataGenerator.instance.stop();
      await _slotA.teardown();
      await _slotB.teardown();
      if (!mounted) return;
      setState(() {
        _demoMode = false;
        _isSwitchingMode = false;
      });
      await _autoReconnect();
      return;
    }

    // Capture active services and cancel streams immediately so mode switch
    // is responsive even while BLE connect timeouts/retries are running.
    final prevAService = _slotA.service;
    final prevBService = _slotB.service;
    _slotA.cancelAll();
    _slotB.cancelAll();

    _stopDemoStreams();
    setState(() {
      _demoMode = true;
      _slotA.device = BluetoothDevice.fromId('DEMO-A');
      _slotA.status = 'Demo mode';
      _slotA.data = null;
      _slotA.lastDataAt = null;
      _slotB.device = BluetoothDevice.fromId('DEMO-B');
      _slotB.status = 'Demo mode';
      _slotB.data = null;
      _slotB.lastDataAt = null;
    });

    if (prevAService != null) unawaited(prevAService.disconnect());
    if (prevBService != null) unawaited(prevBService.disconnect());

    _listenDemoStreams();
    DemoDataGenerator.instance.start();

    if (mounted) {
      setState(() => _isSwitchingMode = false);
    }
  }

  void _stopDemoStreams() {
    _demoASub?.cancel();
    _demoASub = null;
    _demoBSub?.cancel();
    _demoBSub = null;
  }

  void _listenDemoStreams() {
    _stopDemoStreams();
    _demoASub = DemoDataGenerator.instance.aStream.listen((d) {
      if (!mounted || !_demoMode || _isSwitchingMode) return;
      setState(() {
        _slotA.data = d;
        _slotA.lastDataAt = DateTime.now();
        _slotA.status = 'Demo data';
        ChartBuffer.instance.addA(d);
        RecordingService.instance.addA(d);
      });
    });
    _demoBSub = DemoDataGenerator.instance.bStream.listen((d) {
      if (!mounted || !_demoMode || _isSwitchingMode) return;
      setState(() {
        _slotB.data = d;
        _slotB.lastDataAt = DateTime.now();
        _slotB.status = 'Demo data';
        ChartBuffer.instance.addB(d);
        RecordingService.instance.addB(d);
      });
    });
  }

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
                width: 40,
                height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                    color: _kTextSecondary.withValues(alpha: 0.4),
                    borderRadius: BorderRadius.circular(2)),
              ),
              const Text('Select slot to connect',
                  style: TextStyle(
                      color: _kTextPrimary,
                      fontWeight: FontWeight.bold,
                      fontSize: 16)),
              const SizedBox(height: 10),
              const Text(
                'Hinweis bei neuer Verbindung: Standard-Bluetooth-Code ist 000000.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  color: _kTextSecondary,
                  fontSize: 12,
                ),
              ),
              const SizedBox(height: 16),
              Row(children: [
                Expanded(
                    child: _slotButton('Battery A', 'A',
                        _slotA.isConnected ? _slotDisplayName(_slotA) : null)),
                const SizedBox(width: 12),
                Expanded(
                    child: _slotButton('Battery B', 'B',
                        _slotB.isConnected ? _slotDisplayName(_slotB) : null)),
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
    final macKey = slotChoice == 'A' ? _prefKeyA : _prefKeyB;
    final nameKey = slotChoice == 'A' ? _prefNameA : _prefNameB;
    await slot.teardown();
    if (!mounted) return;
    setState(() {
      slot.status = 'Verbinden…';
      slot.device = device;
    });
    await _saveSlot(macKey, nameKey, device.remoteId.str, device.platformName);
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
        if (currentName != null && currentName.isNotEmpty) ...[
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
          if (l.contains('connecting') ||
              l.contains('connected') ||
              l.contains('discovering') ||
              l.contains('waiting') ||
              l.contains('data received') ||
              l.contains('disconnecting') ||
              l.contains('disconnected')) return;
        }
        final l = s.toLowerCase();
        if (l.contains('connect')) {
          slot.status = 'Verbinden…';
        } else if (l.contains('missing') || l.contains('failed')) {
          slot.status = 'Keine Verbindung';
        } else {
          slot.status = s;
        }
      });
      _debugLog.add('slot=${_slotTag(slot)} status=$s');
    });
    slot.logSub = slot.service!.debugStream.listen((line) {
      if (!mounted) return;
      setState(() {
        _pushSlotLog(slot, line);
        if (!slot.hasData && line.startsWith('RX ')) slot.status = 'Verbunden';
      });
    });
  }

  Future<void> _connect(_BattSlot slot) async {
    if (slot.connectInProgress) return;
    slot.connectInProgress = true;
    final epoch = ++slot.connectEpoch;

    try {
      slot.dataSub?.cancel();
      slot.connSub?.cancel();

      bool isStale() {
        return slot.connectEpoch != epoch ||
            _disposed ||
            !mounted ||
            _demoMode ||
            _isSwitchingMode ||
            !slot.isConnected;
      }

      slot.connSub = slot.device!.connectionState.listen((state) async {
        if (isStale()) return;
        if (mounted &&
            state == BluetoothConnectionState.disconnected &&
            !_disposed) {
          _pushSlotLog(slot, 'BLE connection state changed to disconnected');
          await slot.service?.disconnect();
          if (isStale()) return;
          setState(() =>
              slot.status = slot.hasData ? 'Verbunden' : 'Keine Verbindung');
          if (!isStale()) _scheduleReconnect(slot);
        }
      });

      slot.dataSub = slot.service!.dataStream.listen((d) {
        if (isStale()) return;
        if (mounted) {
          setState(() {
            slot.data = d;
            slot.lastDataAt = DateTime.now();
            slot.status = 'Verbunden';
          });
        }
        if (slot == _slotA) {
          ChartBuffer.instance.addA(d);
          RecordingService.instance.addA(d);
        } else {
          ChartBuffer.instance.addB(d);
          RecordingService.instance.addB(d);
        }
      });

      for (int attempt = 1; attempt <= _maxRetries; attempt++) {
        if (isStale()) return;
        try {
          if (attempt > 1) {
            if (isStale()) return;
            if (mounted && !slot.hasData) {
              setState(() => slot.status = 'Verbinden...');
            }
            await Future.delayed(_retryDelay);
            if (isStale()) return;
            await slot.service!.disconnect();
            await Future.delayed(const Duration(milliseconds: 500));
            if (isStale()) return;
          }
          await slot.service!.connect(slot.device!).timeout(
            _connectTimeout,
            onTimeout: () {
              throw TimeoutException(
                'Connection timed out after ${_connectTimeout.inSeconds}s',
              );
            },
          );
          if (isStale()) return;
          if (mounted && !slot.hasData) {
            setState(() => slot.status = 'Warte auf Daten...');
          }
          return;
        } catch (e) {
          if (isStale()) return;
          final isFinal = attempt == _maxRetries;
          setState(() =>
              slot.status = isFinal ? 'Keine Verbindung' : 'Verbinden...');
          if (isFinal) return;
        }
      }
    } finally {
      if (slot.connectEpoch == epoch) slot.connectInProgress = false;
    }
  }

  Future<void> _disconnectSlot(_BattSlot slot) async {
    _pushSlotLog(slot, 'UI disconnect requested');
    final macKey = slot == _slotA ? _prefKeyA : _prefKeyB;
    final nameKey = slot == _slotA ? _prefNameA : _prefNameB;
    slot.connectEpoch++;

    final oldService = slot.service;
    slot.cancelAll();
    if (mounted) {
      setState(() {
        slot.device = null;
        slot.data = null;
        slot.lastDataAt = null;
        slot.status = '';
        slot.logs.clear();
      });
    }

    if (oldService != null) {
      unawaited(oldService.disconnect());
    }

    await _saveSlot(macKey, nameKey, null, null);
  }

  Future<void> _refreshSlot(_BattSlot slot) async {
    _pushSlotLog(slot, 'UI slot refresh requested');
    await _reconnectSlot(slot, userRequested: true);
  }

  void _setSlotViewMode(_BattSlot slot, _BatteryCardViewMode mode) {
    if (!mounted || slot.viewMode == mode) return;
    setState(() => slot.viewMode = mode);
  }

  Future<void> _shareDebugLog() async {
    try {
      final path = await _debugLog.exportSnapshot();
      if (!mounted) return;
      await Share.shareXFiles(
        [XFile(path)],
        subject: 'SmartBat Debug Log',
      );
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Debug log export failed: $e'),
          backgroundColor: _kCritical,
        ),
      );
    }
  }

  void _showAppInfoDialog() {
    showDialog<void>(
      context: context,
      builder: (context) => AlertDialog(
        backgroundColor: _kSurface,
        titlePadding: const EdgeInsets.fromLTRB(24, 24, 24, 8),
        contentPadding: const EdgeInsets.fromLTRB(24, 0, 24, 12),
        actionsPadding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
        title: Row(
          children: [
            Image.asset('assets/Odin_Kopf.png', width: 40, height: 40),
            const SizedBox(width: 12),
            const Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Odin SmartBat',
                    style: TextStyle(color: _kTextPrimary, fontSize: 18),
                  ),
                  SizedBox(height: 2),
                  Text(
                    _kAppVersion,
                    style: TextStyle(color: _kTextSecondary, fontSize: 12),
                  ),
                ],
              ),
            ),
          ],
        ),
        content: const Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'SmartBat LiFePO4 Battery BMS Monitor',
              style: TextStyle(color: _kTextPrimary),
            ),
            SizedBox(height: 12),
            Text(
              'Copyright Joerg Middendorf',
              style: TextStyle(color: _kTextSecondary, fontSize: 12),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.of(context).pop(),
            child: const Text('OK'),
          ),
        ],
      ),
    );
  }

  String _timeLabel(DateTime value) {
    final hh = value.hour.toString().padLeft(2, '0');
    final mm = value.minute.toString().padLeft(2, '0');
    final ss = value.second.toString().padLeft(2, '0');
    return '$hh:$mm:$ss';
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        title: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Image.asset('assets/Odin_Kopf.png', width: 40),
            const SizedBox(width: 8),
            const Flexible(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Odin SmartBat',
                      overflow: TextOverflow.ellipsis,
                      style:
                          TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
                  Text(_kAppVersion,
                      style: TextStyle(fontSize: 9, color: Colors.white54)),
                ],
              ),
            ),
            const SizedBox(width: 8),
            GestureDetector(
              onTap: _toggleDemoMode,
              child: Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  gradient: _demoMode
                      ? LinearGradient(colors: [_kWarning, Colors.orangeAccent])
                      : LinearGradient(colors: [_kInfo, Colors.blue.shade700]),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: Colors.white54, width: 1),
                ),
                child: Text(
                  _demoMode ? 'DEMO' : 'live',
                  style: TextStyle(
                    color: _demoMode ? Colors.black : Colors.white,
                    fontSize: 11,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
            ),
          ],
        ),
        backgroundColor: _kSurface,
        foregroundColor: _kTextPrimary,
        actions: [
          IconButton(
            icon: const Icon(Icons.show_chart),
            tooltip: 'Live chart',
            onPressed: () => Navigator.push(context,
                MaterialPageRoute(builder: (_) => const ChartScreen())),
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            tooltip: 'App info',
            onPressed: _showAppInfoDialog,
          ),
          IconButton(
            icon: const Icon(Icons.bluetooth_searching),
            tooltip: 'Scan for batteries',
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
        Image.asset('assets/Odin_Kopf.png',
            width: 80,
            height: 80,
            color: _kTextSecondary.withValues(alpha: 0.3),
            colorBlendMode: BlendMode.modulate),
        const SizedBox(height: 24),
        Text(
          _demoMode ? 'Demo mode active' : 'No battery connected',
          style: const TextStyle(color: _kTextSecondary, fontSize: 16),
        ),
        const SizedBox(height: 8),
        Row(mainAxisSize: MainAxisSize.min, children: [
          GestureDetector(
            onTap: _toggleDemoMode,
            child: Row(
              children: [
                Icon(
                  _demoMode ? Icons.science : Icons.science,
                  color: _kWarning,
                  size: 16,
                ),
                Text(
                  _demoMode ? ' DEMO' : ' DEMO',
                  style: const TextStyle(color: _kTextSecondary, fontSize: 13),
                ),
              ],
            ),
          ),
          const SizedBox(width: 16),
          GestureDetector(
            onTap: _openScanPicker,
            child: Row(
              children: [
                Icon(Icons.bluetooth_searching, color: _kInfo, size: 16),
                Text(
                  ' SCAN',
                  style: const TextStyle(color: _kTextSecondary, fontSize: 13),
                ),
              ],
            ),
          ),
        ]),
      ]),
    );
  }

  Widget _buildBody() {
    final bothHaveData = (_slotA.data != null) && (_slotB.data != null);
    return RefreshIndicator(
      color: _kAccent,
      backgroundColor: _kSurface,
      onRefresh: _refreshAllSlots,
      child: SafeArea(
        top: false,
        bottom: true,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.all(_kFrameGap),
          child: Column(children: [
            _buildBatteryCard(_slotA, 'Battery A', _isRefreshingA,
                (v) => setState(() => _isRefreshingA = v)),
            if (_slotB.isConnected) const SizedBox(height: _kFrameGap),
            _buildBatteryCard(_slotB, 'Battery B', _isRefreshingB,
                (v) => setState(() => _isRefreshingB = v)),
            if (bothHaveData) ...[
              const SizedBox(height: _kFrameGap),
              _buildSummaryStrip(_slotA.data!, _slotB.data!),
            ],
          ]),
        ),
      ),
    );
  }

  Widget _buildBatteryCard(_BattSlot slot, String label, bool isRefreshing,
      void Function(bool) setRefreshing) {
    if (!slot.isConnected) return const SizedBox.shrink();

    final name = _slotDisplayName(slot, fallback: label);

    return Card(
      margin: EdgeInsets.zero,
      color: _kSurface,
      clipBehavior: Clip.hardEdge,
      child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
        Container(
          color: _kSurfaceElev,
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          child: Row(children: [
            Text(label,
                style: const TextStyle(
                    color: _kTextSecondary,
                    fontSize: 12,
                    fontWeight: FontWeight.w600)),
            const SizedBox(width: 8),
            Expanded(
                child: Text(name,
                    overflow: TextOverflow.ellipsis,
                    style: const TextStyle(
                        color: _kTextPrimary,
                        fontSize: 13,
                        fontWeight: FontWeight.bold))),
            if (slot.status.isNotEmpty && slot.data == null)
              Text(slot.status,
                  style: const TextStyle(color: _kTextSecondary, fontSize: 11)),
            if (slot.data != null) ...[
              const SizedBox(width: 8),
              _buildCardModeToggle(slot),
            ],
            const SizedBox(width: 4),
            InkWell(
              onTap: isRefreshing
                  ? null
                  : () async {
                      setRefreshing(true);
                      await _refreshSlot(slot);
                      setRefreshing(false);
                    },
              child: isRefreshing
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(
                          strokeWidth: 2, color: _kAccent))
                  : const Icon(Icons.refresh, size: 18, color: _kTextSecondary),
            ),
            const SizedBox(width: 8),
            InkWell(
              onTap: () => _showSlotInfoSheet(slot),
              child: const Icon(Icons.info_outline,
                  size: 18, color: _kTextSecondary),
            ),
            const SizedBox(width: 8),
            InkWell(
              onTap: () => _disconnectSlot(slot),
              child: const Icon(Icons.bluetooth_disabled,
                  size: 18, color: _kCritical),
            ),
          ]),
        ),
        if (slot.data == null)
          Padding(
            padding: const EdgeInsets.all(20),
            child: Row(mainAxisAlignment: MainAxisAlignment.center, children: [
              if (_isActivelyConnecting(slot.status)) ...[
                const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(
                        strokeWidth: 2, color: _kAccent)),
                const SizedBox(width: 14),
              ],
              Text(slot.status,
                  style: const TextStyle(color: _kTextSecondary, fontSize: 13)),
            ]),
          )
        else
          _buildSlotData(slot, slot.data!),
      ]),
    );
  }

  Widget _buildSlotData(_BattSlot slot, BatteryData d) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(10, 8, 10, 6),
      child: slot.viewMode == _BatteryCardViewMode.cells
          ? _buildCellsView(slot, d)
          : _buildStatusView(slot, d),
    );
  }

  Widget _buildStatusView(_BattSlot slot, BatteryData d) {
    final Color socColor = d.soc > 60
        ? _kAccent
        : d.soc > 25
            ? _kWarning
            : _kCritical;
    final IconData statusIcon = d.isCharging
        ? Icons.bolt
        : d.isDischarging
            ? Icons.battery_4_bar
            : Icons.horizontal_rule;
    final Color statusIconColor = d.isCharging ? _kAccent : _kInfo;

    String? timeHint;
    if (d.isCharging && (d.attfMin ?? 0) > 0 && d.attfMin != 65535) {
      final h = d.attfMin! ~/ 60;
      final m = d.attfMin! % 60;
      timeHint = h > 0 ? 'CHG  ${h}h ${m}min to full' : 'CHG  ${m}min to full';
    } else if (d.isDischarging && (d.atteMin ?? 0) > 0 && d.atteMin != 65535) {
      final h = d.atteMin! ~/ 60;
      final m = d.atteMin! % 60;
      timeHint = h > 0 ? 'DSC  ${h}h ${m}min left' : 'DSC  ${m}min left';
    }

    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Row(crossAxisAlignment: CrossAxisAlignment.center, children: [
        SizedBox(
          width: 95,
          height: 95,
          child: CustomPaint(
            painter: _GaugePainter(d.soc / 100.0, socColor),
            child: Center(
                child: Column(mainAxisSize: MainAxisSize.min, children: [
              Text('${d.soc}%',
                  style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.normal,
                      color: socColor)),
              SizedBox(
                width: 16,
                height: 16,
                child: Center(
                  child: Icon(
                    statusIcon,
                    size: 14,
                    color: statusIconColor,
                  ),
                ),
              ),
            ])),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
            child: Column(
                crossAxisAlignment: CrossAxisAlignment.stretch,
                children: [
              _inlineStatRow('Voltage', '${d.voltage.toStringAsFixed(3)} V',
                  Icons.flash_on, Colors.yellow),
              const SizedBox(height: 4),
              _inlineStatRow(
                  'Current',
                  '${d.current >= 0 ? '+' : ''}${d.current.toStringAsFixed(3)} A',
                  Icons.compare_arrows,
                  d.isCharging ? _kAccent : _kInfo),
              const SizedBox(height: 4),
              _inlineStatRow('Power', '${d.power.toStringAsFixed(1)} W',
                  Icons.bolt, _kWarning),
              const SizedBox(height: 4),
              _inlineStatRow(
                  'Cycles', '${d.cycles}', Icons.loop, Colors.purpleAccent),
            ])),
      ]),
      const SizedBox(height: 4),
      Text(
        timeHint == null
            ? '${d.remainingAh.toStringAsFixed(1)} Ah  /  ${d.nominalAh.toStringAsFixed(1)} Ah'
            : '${d.remainingAh.toStringAsFixed(1)} Ah  /  ${d.nominalAh.toStringAsFixed(1)} Ah   •   $timeHint',
        textAlign: TextAlign.center,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
        style: const TextStyle(color: _kTextSecondary, fontSize: 11),
      ),
      if (d.temperatures.isNotEmpty) ...[
        const SizedBox(height: 6),
        Wrap(
            spacing: 6,
            runSpacing: 2,
            children: d.temperatures.asMap().entries.map((e) {
              final degC = e.value;
              final degF = ((degC * 1.8 + 32.0) * 10).round() / 10.0;
              final color = degC > 45
                  ? _kCritical
                  : degC > 35
                      ? _kWarning
                      : _kAccent;
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
      if (d.activeProtections.isNotEmpty) ...[
        const SizedBox(height: 8),
        ...d.activeProtections.map((p) => Row(children: [
              const Icon(Icons.circle, size: 6, color: _kCritical),
              const SizedBox(width: 6),
              Text(p, style: const TextStyle(color: _kCritical, fontSize: 13)),
            ])),
      ],
    ]);
  }

  Widget _buildCellsView(_BattSlot slot, BatteryData d) {
    if (d.cellVoltages.isEmpty) {
      return Container(
        alignment: Alignment.center,
        padding: const EdgeInsets.symmetric(vertical: 24, horizontal: 12),
        child: Column(mainAxisSize: MainAxisSize.min, children: [
          const Icon(Icons.grid_view_rounded, size: 34, color: _kTextSecondary),
          const SizedBox(height: 10),
          const Text('No cell data available',
              style: TextStyle(
                  color: _kTextPrimary,
                  fontSize: 14,
                  fontWeight: FontWeight.w600)),
          const SizedBox(height: 4),
          const Text('This battery currently exposes pack-level values only.',
              textAlign: TextAlign.center,
              style: TextStyle(color: _kTextSecondary, fontSize: 12)),
          const SizedBox(height: 12),
          TextButton.icon(
            onPressed: () =>
                _setSlotViewMode(slot, _BatteryCardViewMode.status),
            icon: const Icon(Icons.arrow_back, size: 16),
            label: const Text('Back to status'),
          ),
        ]),
      );
    }

    final voltages = d.cellVoltages;
    final minV = voltages.reduce(min);
    final maxV = voltages.reduce(max);
    final avgV = voltages.reduce((a, b) => a + b) / voltages.length;
    final deltaMv = ((maxV - minV) * 1000).round();

    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      Row(children: [
        const Icon(Icons.grid_view_rounded, color: _kAccent, size: 15),
        const SizedBox(width: 6),
        const Text('Cell details',
            style: TextStyle(
                color: _kTextPrimary,
                fontSize: 13,
                fontWeight: FontWeight.w600)),
        const Spacer(),
        TextButton.icon(
          onPressed: () => _setSlotViewMode(slot, _BatteryCardViewMode.status),
          icon: const Icon(Icons.arrow_back, size: 15),
          label: const Text('Status'),
          style: TextButton.styleFrom(
            foregroundColor: _kTextSecondary,
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            minimumSize: Size.zero,
            tapTargetSize: MaterialTapTargetSize.shrinkWrap,
          ),
        ),
      ]),
      const SizedBox(height: 8),
      Row(children: [
        Expanded(
            child: _cellsStatChip(
                'Min', '${(minV * 1000).round()} mV', _kCritical)),
        const SizedBox(width: 6),
        Expanded(
            child:
                _cellsStatChip('Max', '${(maxV * 1000).round()} mV', _kAccent)),
        const SizedBox(width: 6),
        Expanded(
            child: _cellsStatChip(
                'Delta', '$deltaMv mV', deltaMv > 50 ? _kWarning : _kInfo)),
      ]),
      const SizedBox(height: 6),
      Row(children: [
        Expanded(
            child: _cellsStatChip(
                'Average', '${avgV.toStringAsFixed(3)} V', _kTextPrimary)),
        const SizedBox(width: 6),
        Expanded(
            child: _cellsStatChip(
                'Pack', '${d.voltage.toStringAsFixed(3)} V', _kWarning)),
        const SizedBox(width: 6),
        Expanded(
            child:
                _cellsStatChip('Count', '${voltages.length}', _kTextSecondary)),
      ]),
      const SizedBox(height: 8),
      _buildCellsCompact(d, expanded: true),
    ]);
  }

  Widget _inlineStatRow(
      String label, String value, IconData icon, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
          color: _kSurfaceElev, borderRadius: BorderRadius.circular(6)),
      child: Row(children: [
        Icon(icon, size: 14, color: color),
        const SizedBox(width: 6),
        Text(label,
            style: const TextStyle(fontSize: 11, color: _kTextSecondary)),
        const Spacer(),
        Text(value,
            style: const TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: _kTextPrimary)),
      ]),
    );
  }

  Widget _buildCardModeToggle(_BattSlot slot) {
    final isCells = slot.viewMode == _BatteryCardViewMode.cells;
    return Container(
      decoration: BoxDecoration(
        color: _kBg.withValues(alpha: 0.32),
        borderRadius: BorderRadius.circular(999),
        border: Border.all(color: _kTextSecondary.withValues(alpha: 0.18)),
      ),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        _modeTogglePill(slot, _BatteryCardViewMode.status, 'Status', !isCells),
        _modeTogglePill(slot, _BatteryCardViewMode.cells, 'Cells', isCells),
      ]),
    );
  }

  Widget _modeTogglePill(
      _BattSlot slot, _BatteryCardViewMode mode, String label, bool selected) {
    return InkWell(
      borderRadius: BorderRadius.circular(999),
      onTap: () => _setSlotViewMode(slot, mode),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
        decoration: BoxDecoration(
          color:
              selected ? _kAccent.withValues(alpha: 0.18) : Colors.transparent,
          borderRadius: BorderRadius.circular(999),
        ),
        child: Text(label,
            style: TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.w700,
              color: selected ? _kAccent : _kTextSecondary,
            )),
      ),
    );
  }

  Widget _cellsStatChip(String label, String value, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 7),
      decoration: BoxDecoration(
        color: _kSurfaceElev,
        borderRadius: BorderRadius.circular(6),
      ),
      child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
        Text(label,
            style: const TextStyle(fontSize: 10, color: _kTextSecondary)),
        const SizedBox(height: 2),
        Text(value,
            overflow: TextOverflow.ellipsis,
            style: TextStyle(
                fontSize: 12, fontWeight: FontWeight.w700, color: color)),
      ]),
    );
  }

  Widget _buildCellsCompact(BatteryData d, {bool expanded = false}) {
    final voltages = d.cellVoltages;
    final minV = voltages.reduce(min);
    final maxV = voltages.reduce(max);
    final deltaMs = ((maxV - minV) * 1000).round();
    return Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
      if (!expanded) ...[
        Row(children: [
          const Icon(Icons.grid_view, color: _kAccent, size: 14),
          const SizedBox(width: 4),
          const Text('Cells',
              style: TextStyle(color: _kTextSecondary, fontSize: 12)),
          const Spacer(),
          Text('Δ $deltaMs mV',
              style: TextStyle(
                  fontSize: 11,
                  color: deltaMs > 50 ? _kWarning : _kTextSecondary)),
        ]),
        const SizedBox(height: 4),
      ],
      GridView.builder(
        shrinkWrap: true,
        physics: const NeverScrollableScrollPhysics(),
        padding: EdgeInsets.zero,
        gridDelegate: SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 4,
            childAspectRatio: expanded ? 1.45 : 1.7,
            mainAxisSpacing: 4,
            crossAxisSpacing: 4),
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
                  color: isMin
                      ? _kCritical
                      : isMax
                          ? _kAccent
                          : Colors.transparent,
                  width: 1.5),
            ),
            child:
                Column(mainAxisAlignment: MainAxisAlignment.center, children: [
              Text('C${i + 1}',
                  style: const TextStyle(fontSize: 9, color: _kTextSecondary)),
              Text('${(v * 1000).round()}',
                  style: const TextStyle(
                      fontSize: 11,
                      fontWeight: FontWeight.bold,
                      color: _kTextPrimary)),
              if (expanded)
                Text('${v.toStringAsFixed(3)} V',
                    style: TextStyle(
                        fontSize: 9,
                        color: isMin
                            ? _kCritical
                            : isMax
                                ? _kAccent
                                : _kTextSecondary)),
            ]),
          );
        },
      ),
    ]);
  }

  Widget _buildSummaryStrip(BatteryData a, BatteryData b) {
    final totalRemainingAh = a.remainingAh + b.remainingAh;
    final totalNominalAh = a.nominalAh + b.nominalAh;
    final totalCurrent = a.current + b.current;
    final powerA = a.voltage * a.current;
    final powerB = b.voltage * b.current;
    final totalPower = powerA.clamp(double.negativeInfinity, 0.0).abs() +
        powerB.clamp(double.negativeInfinity, 0.0).abs();
    final combinedSoc = totalNominalAh > 0
        ? (totalRemainingAh / totalNominalAh * 100).round()
        : 0;
    final isCharging = totalCurrent > 0.1;
    final isDischarging = totalCurrent < -0.1;
    final vDelta = (a.voltage - b.voltage).abs();
    final vDeltaColor = vDelta > 0.2
        ? _kCritical
        : vDelta > 0.05
            ? _kWarning
            : _kAccent;

    double cellSpread(List<double> voltages) {
      if (voltages.length < 2) return 0.0;
      return voltages.reduce(max) - voltages.reduce(min);
    }

    final maxSpread =
        max(cellSpread(a.cellVoltages), cellSpread(b.cellVoltages));
    final spreadColor = maxSpread > 0.080
        ? _kCritical
        : maxSpread > 0.030
            ? _kWarning
            : maxSpread > 0
                ? _kAccent
                : _kTextSecondary;

    return Card(
      margin: EdgeInsets.zero,
      color: _kSurfaceElev,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(12, 8, 12, 8),
        child:
            Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
          Row(children: [
            const Icon(Icons.summarize, color: _kInfo, size: 16),
            const SizedBox(width: 6),
            const Text('Combined',
                style: TextStyle(
                    color: _kTextPrimary,
                    fontWeight: FontWeight.bold,
                    fontSize: 14)),
            const Spacer(),
            Text('$combinedSoc%',
                style: TextStyle(
                    color: combinedSoc > 60
                        ? _kAccent
                        : combinedSoc > 25
                            ? _kWarning
                            : _kCritical,
                    fontWeight: FontWeight.bold,
                    fontSize: 16)),
          ]),
          const SizedBox(height: 6),
          Row(children: [
            Expanded(
                child: _summaryCell(
                    'Capacity',
                    '${totalRemainingAh.toStringAsFixed(1)} / ${totalNominalAh.toStringAsFixed(1)} Ah',
                    Icons.battery_full,
                    _kAccent)),
            const SizedBox(width: 8),
            Expanded(
                child: _summaryCell(
                    'Current',
                    '${totalCurrent >= 0 ? '+' : ''}${totalCurrent.toStringAsFixed(2)} A',
                    Icons.compare_arrows,
                    isCharging
                        ? _kAccent
                        : isDischarging
                            ? _kInfo
                            : _kTextSecondary)),
            const SizedBox(width: 8),
            Expanded(
                child: _summaryCell(
                    'Power',
                    '${totalPower.toStringAsFixed(1)} W',
                    Icons.bolt,
                    _kWarning)),
          ]),
          const SizedBox(height: 4),
          Row(children: [
            Expanded(
                child: _summaryCell(
                    'ΔV A↔B',
                    '${(vDelta * 1000).toStringAsFixed(0)} mV',
                    Icons.balance,
                    vDeltaColor)),
            const SizedBox(width: 8),
            Expanded(
                child: _summaryCell(
                    'Cell spread',
                    '${(maxSpread * 1000).toStringAsFixed(0)} mV',
                    Icons.grid_view,
                    spreadColor)),
            const SizedBox(width: 8),
            Expanded(
                child: _summaryCell(
                    'Voltage A/B',
                    '${a.voltage.toStringAsFixed(2)} / ${b.voltage.toStringAsFixed(2)} V',
                    Icons.compare,
                    _kTextSecondary)),
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
          Text(label,
              style: const TextStyle(fontSize: 10, color: _kTextSecondary)),
        ]),
        const SizedBox(height: 4),
        Text(value,
            style: TextStyle(
                fontSize: 13, fontWeight: FontWeight.bold, color: color),
            overflow: TextOverflow.ellipsis),
      ]),
    );
  }

  void _showSlotInfoSheet(_BattSlot slot) {
    final deviceId = slot.device?.remoteId.toString() ?? '—';
    final lastData =
        slot.lastDataAt == null ? 'No data yet' : _timeLabel(slot.lastDataAt!);
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: _kSurface,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
          borderRadius: BorderRadius.vertical(top: Radius.circular(16))),
      builder: (_) => DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.55,
        maxChildSize: 0.92,
        builder: (_, scrollController) => ListView(
          controller: scrollController,
          padding: const EdgeInsets.fromLTRB(16, 12, 16, 24),
          children: [
            Center(
                child: Container(
              width: 40,
              height: 4,
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                  color: _kTextSecondary.withValues(alpha: 0.4),
                  borderRadius: BorderRadius.circular(2)),
            )),
            _infoRow(
                'Device',
                slot.device?.platformName.isNotEmpty == true
                    ? slot.device!.platformName
                    : '—'),
            _infoRow('MAC', deviceId),
            _infoRow('Status', slot.status),
            _infoRow('Last update', lastData),
            const SizedBox(height: 16),
            Row(children: [
              const Text('BLE / Protocol Log',
                  style: TextStyle(
                      color: _kTextPrimary,
                      fontWeight: FontWeight.bold,
                      fontSize: 14)),
              const Spacer(),
              TextButton.icon(
                onPressed: _shareDebugLog,
                icon: const Icon(Icons.share, size: 14),
                label: const Text('Share', style: TextStyle(fontSize: 12)),
                style: TextButton.styleFrom(
                  foregroundColor: _kInfo,
                  padding: EdgeInsets.zero,
                ),
              ),
            ]),
            const SizedBox(height: 8),
            if (slot.logs.isEmpty)
              const Text('No log entries yet.',
                  style: TextStyle(color: _kTextSecondary, fontSize: 12))
            else
              ...slot.logs.map((line) => Padding(
                    padding: const EdgeInsets.only(bottom: 4),
                    child: Text(line,
                        style: const TextStyle(
                            color: _kTextSecondary,
                            fontSize: 11,
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
        SizedBox(
            width: 100,
            child: Text(label,
                style: const TextStyle(color: _kTextSecondary, fontSize: 13))),
        Expanded(
            child: Text(value,
                style: const TextStyle(
                    color: _kTextPrimary,
                    fontSize: 13,
                    fontFamily: 'monospace'))),
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

    const startAngle = pi * 0.75; // 135°
    const sweepFull = pi * 1.5; // 270° total arc

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
