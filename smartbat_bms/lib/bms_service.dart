import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'battery_data.dart';

class BmsService {
  static const String _svcUuid = "0000fff0-0000-1000-8000-00805f9b34fb";
  static const String _writeUuid = "0000fff6-0000-1000-8000-00805f9b34fb";
  static const String _notifyUuid = "0000fff4-0000-1000-8000-00805f9b34fb";
  static const String _echoNotifyUuid = "0000fff6-0000-1000-8000-00805f9b34fb";
  static const List<String> _initReadUuids = [
    "0000fff4-0000-1000-8000-00805f9b34fb",
    "0000fff5-0000-1000-8000-00805f9b34fb",
    "0000fff6-0000-1000-8000-00805f9b34fb",
  ];

  // Captured from LionCheck app session: 8-byte ASCII command frames.
  // Phase 1 (bootstrap): broad register sweep once after connect.
  static const List<String> _lionBootstrapCommands = [
    '+RAA1002',
    '+RAA0A03',
    '+RAA0802',
    '+RAA0C02',
    '+RAA0403',
    '+RAA3C03',
    '+RAA0603',
    '+RAA4802',
    '+RAA0202',
    '+RAA2C02', // Found via APK string analysis (was missing from previous list)
  ];

  // Phase 2 (steady core): high-frequency loop used while the detail page is open.
  static const List<String> _lionSteadyCommands = [
    '+RAA0202',
    '+RAA0A03',
    '+RAA0802',
    '+RAA2C02',
    '+RAA1002',
  ];

  // Observed in reference captures: after multiple steady loops, LionCheck injects
  // an extended sweep again (likely for less-frequent detail blocks).
  // Current (+RAA1002) and voltage (+RAA0802) are prepended so they are never
  // skipped for more than a few commands even during the extended sweep.
  static const List<String> _lionExtendedSweepCommands = [
    '+RAA1002', // current — keep fresh at start of sweep
    '+RAA0802', // voltage — keep fresh at start of sweep
    '+RAA0C02',
    '+RAA0403',
    '+RAA3C03',
    '+RAA0603',
    '+RAA4802',
  ];

  BluetoothDevice? _device;
  BluetoothCharacteristic? _writeChar;
  BluetoothCharacteristic? _notifyChar;
  BluetoothCharacteristic? _altNotifyChar;
  final Map<String, BluetoothCharacteristic> _charsByShortUuid = {};
  bool _hasValidFrame = false;
  int _echoFrameCount = 0;
  bool _useLionCommandSet = false;
  int _lionBootstrapIndex = 0;
  int _lionSteadyIndex = 0;
  int _lionExtendedSweepIndex = -1;
  int _lionSteadyLoopCount = 0;
  int _lionBurstPollsRemaining = 0;
  List<int>? _lastTxPayload;
  int _rxTotalFrames = 0;
  int _rxEchoFrames = 0;
  int _rxHeartbeatFrames = 0;
  int _rxPayloadFrames = 0;
  int _rxUnknownFrames = 0;
  int _rxAsciiFrames = 0;

  // ── Lion data accumulation (populated from +RD,XX responses) ─────────────
  int? _lionSoc; // State of charge (%)
  int? _lionVoltageMv; // Pack voltage (mV)
  int? _lionCurrentRaw; // Current raw (signed 16-bit, before ×ratio)
  int? _lionTempRaw; // Temperature raw (K×10, e.g. 2990 = 25.9 °C)
  int _lionRatio = 0; // Ratio multiplier (from +RD,0A or embedded in +RD,04)
  int? _lionRmcRaw; // Remaining capacity raw (×ratio = mAh)
  int? _lionFccRaw; // Full charge capacity raw (×ratio = mAh)
  int? _lionCycles; // Cycle count
  int? _lionAtte; // Time to empty (minutes)
  int? _lionAttf; // Time to full (minutes)
  final Map<int, int> _lionCellVoltagesMv = {};

  // ── XOR encoding: derived from EncryptUtils.java in LionCheck APK ──────────
  // Key = sum of encryptByte[nibble] for each nibble of hex(numeric suffix) + offset
  // Device name format: [A|B]<decimal_id>  e.g. "SmartBat-A19681"
  //   type A → offset +5,  type B → offset +8
  // For SmartBat-A19681: hex(19681)='4CE1', nibbles [4,12,14,1]
  //   sum = encryptByte[4..] = 1+5+9+5 = 20, key = 20+5 = 25 = 0x19
  // Fallback: 0x19 (SmartBat-A type default)
  static const List<int> _encryptByteTable = [
    2,
    5,
    4,
    3,
    1,
    4,
    1,
    6,
    8,
    3,
    7,
    2,
    5,
    8,
    9,
    3
  ];

  /// Computes the XOR key from the BLE device name using the LionCheck algorithm.
  /// Returns 0x19 as fallback for unrecognised name formats.
  static int calcLionXorKey(String deviceName) {
    // Find the type prefix A or B followed by a decimal number
    final match = RegExp(r'[Aa](\d+)|[Bb](\d+)').firstMatch(deviceName);
    if (match == null) return 0x19;
    final isTypeA = match.group(1) != null;
    final numStr = match.group(1) ?? match.group(2)!;
    final numVal = int.tryParse(numStr);
    if (numVal == null) return 0x19;
    // Convert number to hex string (no prefix, lowercase)
    final hexStr = numVal.toRadixString(16);
    int sum = 0;
    for (int i = 0; i < hexStr.length; i++) {
      final nibble = int.parse(hexStr[i], radix: 16);
      sum += _encryptByteTable[nibble & 15];
    }
    return sum + (isTypeA ? 5 : 8);
  }

  // Instance XOR key — set during connect() from the device name.
  int _lionXorKey = 0x19;
  String _overrideName =
      ''; // set before connect() for auto-reconnect with no platformName

  /// Call before connect() when restoring from saved state (platformName may be empty).
  void overrideDeviceName(String name) => _overrideName = name;

  static const int _lionFastBurstPolls = 180;
  static const int _rxSummaryEvery = 40;
  static const Duration _lionFastPollInterval = Duration(milliseconds: 60);
  static const Duration _lionSteadyPollInterval = Duration(milliseconds: 100);

  final _dataController = StreamController<BatteryData>.broadcast();
  final _statusController = StreamController<String>.broadcast();
  final _debugController = StreamController<String>.broadcast();
  final _buf = <int>[];
  Timer? _pollTimer;
  Duration _pollInterval = const Duration(seconds: 5);
  final List<StreamSubscription<List<int>>> _notifySubs = [];
  BatteryData? _lastBasic;

  Stream<BatteryData> get dataStream => _dataController.stream;
  Stream<String> get statusStream => _statusController.stream;
  Stream<String> get debugStream => _debugController.stream;

  void _restartPollTimer(Duration interval) {
    _pollInterval = interval;
    _pollTimer?.cancel();
    _pollTimer = Timer.periodic(_pollInterval, (_) => _poll());
    _log('Polling interval set to ${_pollInterval.inMilliseconds} ms');
  }

  Future<void> connect(BluetoothDevice device) async {
    _device = device;
    // Compute XOR key from device name (EncryptUtils algorithm).
    // Prefer override (auto-reconnect) → platformName → advName.
    final name = _overrideName.isNotEmpty
        ? _overrideName
        : (device.platformName.isNotEmpty
            ? device.platformName
            : device.advName);
    _lionXorKey = calcLionXorKey(name);
    _setStatus('Connecting');
    _log(
        'Connecting to $name  [XOR key=0x${_lionXorKey.toRadixString(16).padLeft(2, '0').toUpperCase()}]');
    try {
      await device.connect(autoConnect: false);
    } catch (e) {
      final message = e.toString().toLowerCase();
      if (!message.contains('already connected')) {
        _setStatus('Connection failed');
        _log('Connect error: $e');
        rethrow;
      }
      _log('Device was already connected');
    }
    _setStatus('Discovering services');

    final services = await device.discoverServices();
    _log('Discovered ${services.length} services');
    BluetoothService? svc;
    for (final s in services) {
      _log(
          'Service ${s.serviceUuid} with ${s.characteristics.length} characteristics');
      if (_uuidMatches(s.serviceUuid.toString(), _svcUuid)) {
        svc = s;
        break;
      }
    }

    if (svc == null) {
      _setStatus('BMS service not found');
      throw Exception('BMS service FFF0 not found');
    }

    _charsByShortUuid.clear();
    for (final c in svc.characteristics) {
      final short = _shortUuid(c.characteristicUuid.toString());
      _charsByShortUuid[short] = c;
      _log(
        'Characteristic ${c.characteristicUuid} '
        'R:${c.properties.read} '
        'W:${c.properties.write} '
        'WNR:${c.properties.writeWithoutResponse} '
        'N:${c.properties.notify}',
      );
    }

    _writeChar = _charsByShortUuid[_shortUuid(_writeUuid)];
    _notifyChar = _charsByShortUuid[_shortUuid(_notifyUuid)];
    _altNotifyChar = _charsByShortUuid[_shortUuid(_echoNotifyUuid)];

    if (_writeChar == null || _notifyChar == null) {
      _setStatus('Missing BMS characteristics');
      throw Exception('Required BMS characteristics not found');
    }

    if (!(_writeChar!.properties.writeWithoutResponse ||
        _writeChar!.properties.write)) {
      _setStatus('BMS write characteristic unavailable');
      throw Exception('FFF6 is not writable');
    }

    Future<void> enableNotify(BluetoothCharacteristic? c) async {
      if (c == null || !c.properties.notify) return;
      await c.setNotifyValue(true);
      _notifySubs.add(c.onValueReceived.listen(_onData));
      _log('Notifications enabled on ${c.characteristicUuid}');
    }

    await enableNotify(_notifyChar);
    if (_altNotifyChar != _notifyChar) {
      await enableNotify(_altNotifyChar);
    }

    for (final uuid in _initReadUuids) {
      final c = _charsByShortUuid[_shortUuid(uuid)];
      if (c == null || !c.properties.read) continue;
      final short = _shortUuid(c.characteristicUuid.toString());
      try {
        final v = await c.read();
        _log('INIT READ $short (${v.length}b): ${_hex(v)}');
        if (v.isNotEmpty) _onData(v);
      } catch (e) {
        _log('INIT READ $short failed: $e');
      }
    }

    _switchToLion('SmartBat LionCheck protocol');
    _setStatus('Connected');
    await requestSnapshot();
  }

  void _onData(List<int> chunk) {
    _log('RX chunk ${_hex(chunk)}');

    // XOR-decode received bytes (LionCheck applies same key to TX and RX).
    final decoded = chunk.map((b) => b ^ _lionXorKey).toList();

    // Echo check: compare against the *encoded* last TX payload (device echoes
    // back the raw bytes it received, not the decoded form).
    if (_lastTxPayload != null &&
        chunk.length == _lastTxPayload!.length &&
        _listEquals(chunk, _lastTxPayload!)) {
      _echoFrameCount++;
      _rxEchoFrames++;
      _rxTotalFrames++;
      _log('Ignoring command echo: ${_hex(chunk)}');
      if (!_hasValidFrame && _echoFrameCount == 12) {
        _setStatus('Connected - echo only (protocol mismatch?)');
      }
      _logRxSummary();
      return;
    }

    if (_isLikelyAscii(decoded)) {
      _rxAsciiFrames++;
      _log('RX ascii decoded: ${ascii.decode(decoded)}');
    }

    _buf.addAll(decoded);
    _drain();
  }

  void _drain() {
    while (true) {
      final crIdx = _buf.indexOf(0x0D);
      if (crIdx < 0) break;
      final line = _buf.sublist(0, crIdx);
      final skip = (crIdx + 1 < _buf.length && _buf[crIdx + 1] == 0x0A)
          ? crIdx + 2
          : crIdx + 1;
      _buf.removeRange(0, skip);
      if (line.isEmpty) continue;
      _parseLionLine(line);
      _logRxSummary();
    }
  }

  void _logRxSummary({bool force = false}) {
    if (!force &&
        (_rxTotalFrames == 0 || _rxTotalFrames % _rxSummaryEvery != 0)) {
      return;
    }
    _log(
      'RX summary total=$_rxTotalFrames echo=$_rxEchoFrames '
      'heartbeat=$_rxHeartbeatFrames payload=$_rxPayloadFrames '
      'unknown=$_rxUnknownFrames ascii=$_rxAsciiFrames',
    );
  }

  /// Parse one decoded ASCII line from the Lion/SmartBat device.
  /// Format confirmed via BlueDataUtils.java decompiled from LionCheck APK.
  /// str[6:8]  = first data byte (hex, little-endian low)
  /// str[8:10] = second data byte (hex, little-endian high)
  /// str[10:12]= third data byte (ratio / extra)
  /// 16-bit LE value = parseInt(str[8:10]+str[6:8], 16)
  void _parseLionLine(List<int> lineBytes) {
    _rxTotalFrames++;
    final str = String.fromCharCodes(lineBytes);
    _log('Lion line: $str');

    if (str.length < 8 || !str.startsWith('+RD,')) {
      // May be an ERR response or garbage — ignore silently unless debug needed.
      if (!str.toUpperCase().contains('ERR')) {
        _rxUnknownFrames++;
        _log(
            'Lion: unrecognised line (${lineBytes.length}b): ${_hex(lineBytes)}');
      }
      return;
    }

    // Helper to read a little-endian 16-bit value from str positions 6-9.
    int le16() {
      if (str.length < 10) return 0;
      return int.parse(str.substring(8, 10) + str.substring(6, 8), radix: 16);
    }

    int byte1() => int.parse(str.substring(6, 8), radix: 16);
    int byte3() =>
        str.length >= 12 ? int.parse(str.substring(10, 12), radix: 16) : 0;

    final type = str.substring(0, 6);
    _rxPayloadFrames++;
    _hasValidFrame = true;

    switch (type) {
      case '+RD,02': // SOC (%)
        _lionSoc = byte1();
        _log('Lion SOC: $_lionSoc%');

      case '+RD,04': // RMC — remaining capacity raw; byte3 updates ratio
        final r = byte3();
        if (r != 0) _lionRatio = r;
        _lionRmcRaw = le16();
        _log('Lion RMC raw: $_lionRmcRaw  ratio: $_lionRatio');

      case '+RD,06': // FCC — full charge capacity raw
        _lionFccRaw = le16();
        _log('Lion FCC raw: $_lionFccRaw');

      case '+RD,08': // Pack voltage (mV, little-endian 16-bit)
        _lionVoltageMv = le16();
        _log('Lion Voltage: ${_lionVoltageMv}mV');

      case '+RD,0A': // Ratio factor (third byte only)
        final r = byte3();
        if (r != 0) _lionRatio = r;
        _log('Lion ratio: $_lionRatio');

      case '+RD,0C': // Temperature (K×10 raw, little-endian 16-bit)
        _lionTempRaw = le16();
        final degC = (_lionTempRaw! - 2731) / 10.0;
        _log('Lion Temp: ${degC.toStringAsFixed(1)}°C (raw $_lionTempRaw)');

      case '+RD,10': // Current (signed 16-bit × ratio = mA)
        int raw = le16();
        if (raw > 32768) raw -= 65535;
        _lionCurrentRaw = raw;
        _log('Lion Current raw: $_lionCurrentRaw × ratio=$_lionRatio');

      case '+RD,2C': // Cycle count
        _lionCycles = le16();
        _log('Lion Cycles: $_lionCycles');

      case '+RD,3C': // DCAP — design capacity
        final r = byte3();
        if (r != 0) _lionRatio = r;
        _log('Lion DCAP raw: ${le16()}  ratio: $_lionRatio');

      case '+RD,48': // Manufacturing date
        _log('Lion Date raw: ${le16()}');

      default:
        // Cell voltage registers: +RD,3X where X encodes cell index
        if (str.length >= 6 && str[4] == '3') {
          final cell = 16 - int.parse(str.substring(5, 6), radix: 16);
          final mv = le16();
          _lionCellVoltagesMv[cell] = mv;
          _log('Lion Cell[$cell]: $mv mV');
        } else {
          _rxUnknownFrames++;
          _log('Lion: unknown register $type');
        }
    }

    _tryEmitLionData();
  }

  /// Emit a BatteryData update whenever we have the minimum required fields.
  void _tryEmitLionData() {
    // Require at least voltage and SOC.
    if (_lionVoltageMv == null || _lionSoc == null) return;

    final voltage = _lionVoltageMv! / 1000.0;

    // current = signed_raw × ratio; ratio and raw are each in 10-mA-per-unit
    // → (raw × ratio) mA → / 1000 = A
    final current = (_lionRatio != 0 && _lionCurrentRaw != null)
        ? (_lionCurrentRaw! * _lionRatio) / 1000.0
        : 0.0;

    final tempC = _lionTempRaw != null ? (_lionTempRaw! - 2731) / 10.0 : 25.0;

    // capacity = (raw × ratio) mAh → / 1000 = Ah
    final remainAh = (_lionRmcRaw != null && _lionRatio != 0)
        ? (_lionRmcRaw! * _lionRatio) / 1000.0 / 1000.0
        : 0.0;
    final nominalAh = (_lionFccRaw != null && _lionRatio != 0)
        ? (_lionFccRaw! * _lionRatio) / 1000.0 / 1000.0
        : 0.0;
    final cellVoltages = _currentLionCellVoltages();

    _lastBasic = BatteryData(
      voltage: voltage,
      current: current,
      remainingAh: remainAh,
      nominalAh: nominalAh,
      cycles: _lionCycles ?? (_lastBasic?.cycles ?? 0),
      soc: _lionSoc!.clamp(0, 100),
      temperatures: [tempC],
      cellVoltages: cellVoltages.isNotEmpty
          ? cellVoltages
          : (_lastBasic?.cellVoltages ?? []),
      cellCount: cellVoltages.isNotEmpty
          ? cellVoltages.length
          : (_lastBasic?.cellCount ?? 0),
      chargeFet: true,
      dischargeFet: true,
      protectionStatus: 0,
      atteMin: _lionAtte,
      attfMin: _lionAttf,
    );
    _log(
      'Emit: ${voltage.toStringAsFixed(3)} V  '
      '${current.toStringAsFixed(3)} A  '
      '${_lionSoc}% SoC  '
      '${tempC.toStringAsFixed(1)}°C',
    );
    _setStatus('Data received');
    _emit(_lastBasic!);
  }

  List<double> _currentLionCellVoltages() {
    if (_lionCellVoltagesMv.isEmpty) return const [];
    final indices = _lionCellVoltagesMv.keys.toList()..sort();
    return [for (final index in indices) _lionCellVoltagesMv[index]! / 1000.0];
  }

  void _emit(BatteryData data) {
    if (!_dataController.isClosed) _dataController.add(data);
  }

  String _nextLionAsciiCommand() {
    if (_lionBootstrapIndex < _lionBootstrapCommands.length) {
      final cmd = _lionBootstrapCommands[_lionBootstrapIndex++];
      return cmd;
    }

    if (_lionExtendedSweepIndex >= 0) {
      final cmd = _lionExtendedSweepCommands[_lionExtendedSweepIndex++];
      if (_lionExtendedSweepIndex >= _lionExtendedSweepCommands.length) {
        _lionExtendedSweepIndex = -1;
      }
      return cmd;
    }

    final cmd =
        _lionSteadyCommands[_lionSteadyIndex % _lionSteadyCommands.length];
    _lionSteadyIndex = (_lionSteadyIndex + 1) % _lionSteadyCommands.length;

    // Every 8 steady loops, run one extended sweep before returning to steady core.
    if (_lionSteadyIndex == 0) {
      _lionSteadyLoopCount++;
      if (_lionSteadyLoopCount % 8 == 0) {
        _lionExtendedSweepIndex = 0;
      }
    }

    return cmd;
  }

  bool _isLikelyAscii(List<int> bytes) {
    if (bytes.isEmpty) return false;
    for (final b in bytes) {
      if (b < 0x20 || b > 0x7E) return false;
    }
    return true;
  }

  BluetoothCharacteristic? _lionPreferredWrite() => _writeChar;

  Future<void> requestSnapshot() async {
    await _poll();
  }

  void _switchToLion(String reason) {
    if (_useLionCommandSet) return;
    _useLionCommandSet = true;
    _lionBurstPollsRemaining = _lionFastBurstPolls;
    _restartPollTimer(_lionFastPollInterval);
    _setStatus('Connected');
    _log('Switching to LionCheck command set ($reason)');
  }

  Future<void> _poll() async {
    if (_writeChar == null) return;
    try {
      if (_lionBurstPollsRemaining > 0) {
        _lionBurstPollsRemaining--;
        if (_lionBurstPollsRemaining == 0 &&
            _pollInterval != _lionSteadyPollInterval) {
          _restartPollTimer(_lionSteadyPollInterval);
          _log('Lion scheduler switched to steady cadence (5 writes/sec)');
        }
      }

      final asciiCommand = _nextLionAsciiCommand();
      final rawBytes = ascii.encode(asciiCommand);
      final payload =
          Uint8List.fromList(rawBytes.map((b) => b ^ _lionXorKey).toList());
      final lionPhase = _lionBootstrapIndex < _lionBootstrapCommands.length
          ? 'bootstrap'
          : 'steady';
      _log(
        'TX Lion ASCII $asciiCommand (${payload.length}b) '
        'phase=$lionPhase [XOR 0x${_lionXorKey.toRadixString(16).padLeft(2, '0').toUpperCase()}]',
      );
      await _writeLionPayload(payload);
    } catch (e) {
      _log('Polling failed: $e');
    }
  }

  Future<void> _writeLionPayload(List<int> payload) async {
    final c = _lionPreferredWrite();
    if (c == null) {
      throw Exception('BMS write characteristic is not ready');
    }
    final withoutResponse = c.properties.writeWithoutResponse;
    _log(
      'TX ${_hex(payload)} -> ${c.characteristicUuid} '
      '(${withoutResponse ? "withoutResponse" : "withResponse"})',
    );
    _lastTxPayload = List<int>.from(payload);
    await c.write(payload, withoutResponse: withoutResponse);
  }

  bool _listEquals(List<int> a, List<int> b) {
    if (a.length != b.length) return false;
    for (int i = 0; i < a.length; i++) {
      if (a[i] != b[i]) return false;
    }
    return true;
  }

  String _hex(List<int> bytes) => bytes
      .map((b) => b.toRadixString(16).padLeft(2, '0').toUpperCase())
      .join(' ');

  String _normalizeUuid(String value) {
    return value.toLowerCase().replaceAll(RegExp(r'[^0-9a-f]'), '');
  }

  bool _uuidMatches(String actual, String expectedCanonical) {
    final actualNorm = _normalizeUuid(actual);
    final expectedNorm = _normalizeUuid(expectedCanonical);
    if (actualNorm == expectedNorm) return true;

    // Compare by 16-bit short UUID (e.g. fff0/fff1/fff2).
    final shortExpected =
        expectedNorm.length >= 8 ? expectedNorm.substring(4, 8) : expectedNorm;
    return actualNorm.endsWith(shortExpected) || actualNorm == shortExpected;
  }

  String _shortUuid(String value) {
    final norm = _normalizeUuid(value);
    if (norm.length >= 8) return norm.substring(4, 8);
    return norm;
  }

  void _setStatus(String status) {
    if (!_statusController.isClosed) {
      _statusController.add(status);
    }
  }

  void _log(String message) {
    if (!_debugController.isClosed) {
      _debugController.add(message);
    }
  }

  Future<void> disconnect() async {
    _setStatus('Disconnecting');
    _pollTimer?.cancel();
    for (final sub in _notifySubs) {
      await sub.cancel();
    }
    _notifySubs.clear();
    _pollTimer = null;
    try {
      await _device?.disconnect();
    } catch (_) {}
    _log('Disconnected from device');
    _setStatus('Disconnected');
    _writeChar = _notifyChar = _device = _lastBasic = null;
    _altNotifyChar = null;
    _hasValidFrame = false;
    _echoFrameCount = 0;
    _useLionCommandSet = false;
    _lionBootstrapIndex = 0;
    _lionSteadyIndex = 0;
    _lionExtendedSweepIndex = -1;
    _lionSteadyLoopCount = 0;
    _lionBurstPollsRemaining = 0;
    _lionCellVoltagesMv.clear();
    _lastTxPayload = null;
    _logRxSummary(force: true);
    _rxTotalFrames = 0;
    _rxEchoFrames = 0;
    _rxHeartbeatFrames = 0;
    _rxPayloadFrames = 0;
    _rxUnknownFrames = 0;
    _rxAsciiFrames = 0;
    _pollInterval = const Duration(seconds: 5);
    _buf.clear();
  }

  void dispose() {
    disconnect();
    _dataController.close();
    _statusController.close();
    _debugController.close();
  }
}
