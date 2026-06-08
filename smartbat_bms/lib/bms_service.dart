import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'battery_data.dart';

class BmsService {
  static const String _svcUuid  = "0000fff0-0000-1000-8000-00805f9b34fb";
  static const String _fff1Uuid = "0000fff1-0000-1000-8000-00805f9b34fb";
  static const String _fff2Uuid = "0000fff2-0000-1000-8000-00805f9b34fb";

  // JBD-compatible protocol commands
  // Format: DD A5 <register> 00 <checksum_hi> <checksum_lo> 77
  // Checksum = 0x10000 - register (for 0-length payload)
  static const _cmdBasicInfo    = [0xDD, 0xA5, 0x03, 0x00, 0xFF, 0xFD, 0x77];
  static const _cmdCellVoltages = [0xDD, 0xA5, 0x04, 0x00, 0xFF, 0xFC, 0x77];
  // Some clones use swapped checksum byte order for command frames.
  static const _cmdBasicInfoAltEndian    = [0xDD, 0xA5, 0x03, 0x00, 0xFD, 0xFF, 0x77];
  static const _cmdCellVoltagesAltEndian = [0xDD, 0xA5, 0x04, 0x00, 0xFC, 0xFF, 0x77];

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
    '+RAA1802',
    '+RAA1A02',
    '+RAA2802',
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
    '+RAA1802',
    '+RAA1A02',
    '+RAA2802',
    '+RAA4802',
  ];

  // Deterministic replay mode for protocol comparison runs.
  // Keeps command order fixed across runs to make raw-byte diffs reliable.
  static const bool _lionFixedReplayMode = false; // disabled: using XOR-encoded writes
  static const bool _lionSingleCommandMode = false; // disabled: using full auto-discover
  static const String _lionSingleCommand = '+RAA0A03';
  static const bool _lionAutoDiscoverMode = true;
  static const Duration _lionAutoDiscoverHold = Duration(seconds: 15);
  // Full bootstrap sweep – all 12 known commands, one per hold period.
  static const List<String> _lionAutoDiscoverCommands = [
    '+RAA1002',
    '+RAA0A03',
    '+RAA0802',
    '+RAA0C02',
    '+RAA0403',
    '+RAA3C03',
    '+RAA0603',
    '+RAA1802',
    '+RAA1A02',
    '+RAA2802',
    '+RAA4802',
    '+RAA0202',
  ];
  static const List<String> _lionFixedReplayCycle = [
    '+RAA1002',
    '+RAA0A03',
    '+RAA0802',
    '+RAA0C02',
    '+RAA0403',
    '+RAA3C03',
    '+RAA0603',
    '+RAA1802',
    '+RAA1A02',
    '+RAA2802',
    '+RAA4802',
    '+RAA0202',
    '+RAA0202',
    '+RAA0A03',
    '+RAA0802',
    '+RAA2C02',
    '+RAA1002',
  ];

  BluetoothDevice? _device;
  BluetoothCharacteristic? _writeChar;
  BluetoothCharacteristic? _notifyChar;
  BluetoothCharacteristic? _altWriteChar;
  BluetoothCharacteristic? _altNotifyChar;
  final List<BluetoothCharacteristic> _writeCandidates = [];
  final List<BluetoothCharacteristic> _notifyCandidates = [];
  final List<BluetoothCharacteristic> _readCandidates = [];
  int _writeCandidateIndex = 0;
  bool _hasValidFrame = false;
  int _echoFrameCount = 0;
  int _commandProfileIndex = 0;
  int _lastAnnouncedProfileIndex = -1;
  bool _preferWriteWithoutResponse = true;
  bool _useLionCommandSet = false;
  bool _smartBatNativeSeen = false;
  int _lionBootstrapIndex = 0;
  int _lionSteadyIndex = 0;
  int _lionExtendedSweepIndex = -1;
  int _lionSteadyLoopCount = 0;
  int _lionFixedReplayIndex = 0;
  String _activeLionSingleCommand = _lionSingleCommand;
  int _lionAutoDiscoverIndex = 0;
  DateTime? _lionAutoDiscoverStartedAt;
  bool _lionAutoDiscoverTransition = false;
  bool _lionAutoDiscoverDoneLogged = false;
  int _lionPollCount = 0;
  int _lionBurstPollsRemaining = 0;
  bool _fff3RejectsLongWrites = false;
  List<int>? _lastTxPayload;
  int _pollAttemptsWithoutValidFrame = 0;
  bool _reportedProtocolFailure = false;
  int _rxTotalFrames = 0;
  int _rxEchoFrames = 0;
  int _rxHeartbeatFrames = 0;
  int _rxPayloadFrames = 0;
  int _rxUnknownFrames = 0;
  int _rxAsciiFrames = 0;

  // ── Lion data accumulation (populated from +RD,XX responses) ─────────────
  int? _lionSoc;          // State of charge (%)
  int? _lionVoltageMv;    // Pack voltage (mV)
  int? _lionCurrentRaw;   // Current raw (signed 16-bit, before ×ratio)
  int? _lionTempRaw;      // Temperature raw (K×10, e.g. 2990 = 25.9 °C)
  int  _lionRatio = 0;    // Ratio multiplier (from +RD,0A or embedded in +RD,04)
  int? _lionRmcRaw;       // Remaining capacity raw (×ratio = mAh)
  int? _lionFccRaw;       // Full charge capacity raw (×ratio = mAh)
  int? _lionCycles;       // Cycle count
  int? _lionAtte;         // Time to empty (minutes)
  int? _lionAttf;         // Time to full (minutes)

  // ── Gateway autonomous test matrix ──────────────────────────────────────────
  // Iterates ALL (command × characteristic × writeMode) permutations, logs each
  // result as GATEWAY_RESULT: {...} for PC-side analysis via gateway_monitor.ps1.
  // ── XOR encoding: derived from EncryptUtils.java in LionCheck APK ──────────
  // Key = sum of encryptByte[nibble] for each nibble of hex(numeric suffix) + offset
  // Device name format: [A|B]<decimal_id>  e.g. "SmartBat-A19681"
  //   type A → offset +5,  type B → offset +8
  // For SmartBat-A19681: hex(19681)='4CE1', nibbles [4,12,14,1]
  //   sum = encryptByte[4..] = 1+5+9+5 = 20, key = 20+5 = 25 = 0x19
  // Fallback: 0x19 (SmartBat-A type default)
  static const List<int> _encryptByteTable = [2,5,4,3,1,4,1,6,8,3,7,2,5,8,9,3];

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

  static const bool _gatewayMode = false; // Disabled: protocol cracked via JADX
  static const int _gwWritesPerPerm = 100; // ~6 s at 60 ms burst
  List<_GwPerm>? _gwPerms;
  int _gwIndex = 0;
  int _gwWriteCount = 0;
  bool _gwDone = false;
  _GwResult? _gwCurrentResult;

  static const int _maxJbdProbePolls = 4;
  static const int _maxTotalProbePolls = 16;
  static const int _lionFastBurstPolls = 180;
  static const int _rxSummaryEvery = 40;
  static const Duration _lionFastPollInterval   = Duration(milliseconds: 60);
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
    // Compute XOR key from device name (EncryptUtils algorithm)
    final name = device.platformName.isNotEmpty
        ? device.platformName
        : device.advName;
    _lionXorKey = calcLionXorKey(name);
    _setStatus('Connecting');
    _log('Connecting to $name  [XOR key=0x${_lionXorKey.toRadixString(16).padLeft(2, '0').toUpperCase()}]');
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
    final allChars = <BluetoothCharacteristic>[];
    for (final s in services) {
      _log('Service ${s.serviceUuid} with ${s.characteristics.length} characteristics');
      allChars.addAll(s.characteristics);
      if (_uuidMatches(s.serviceUuid.toString(), _svcUuid)) {
        svc = s;
        break;
      }
    }

    // Fallback: some devices do not expose/format FFF0 consistently,
    // but still provide the expected FFF1/FFF2 characteristics.
    if (svc == null) {
      for (final s in services) {
        final hasFff1 = s.characteristics.any(
          (c) => _uuidMatches(c.characteristicUuid.toString(), _fff1Uuid),
        );
        final hasFff2 = s.characteristics.any(
          (c) => _uuidMatches(c.characteristicUuid.toString(), _fff2Uuid),
        );
        if (hasFff1 || hasFff2) {
          svc = s;
          _log('Using fallback service ${s.serviceUuid} (contains FFF1/FFF2)');
          break;
        }
      }
    }

    if (svc == null) {
      _log('BMS service FFF0 not found, falling back to global characteristic discovery');
    }

    BluetoothCharacteristic? c1, c2;
    final selectedChars = <BluetoothCharacteristic>[];
    if (svc != null) {
      selectedChars.addAll(svc.characteristics);
      for (final c in svc.characteristics) {
        final id = c.characteristicUuid.toString().toLowerCase();
        _log(
          'Characteristic ${c.characteristicUuid} '
          'R:${c.properties.read} '
          'W:${c.properties.write} '
          'WNR:${c.properties.writeWithoutResponse} '
          'N:${c.properties.notify}',
        );
        if (_uuidMatches(id, _fff1Uuid)) c1 = c;
        if (_uuidMatches(id, _fff2Uuid)) c2 = c;
      }
    }

    // Some firmwares expose FFF1/FFF2 in another service.
    // If one or both are missing, search globally across discovered services.
    if (c1 == null || c2 == null) {
      for (final s in services) {
        for (final c in s.characteristics) {
          final id = c.characteristicUuid.toString().toLowerCase();
          if (c1 == null && _uuidMatches(id, _fff1Uuid)) {
            c1 = c;
            _log('Found fallback FFF1 in service ${s.serviceUuid}');
          }
          if (c2 == null && _uuidMatches(id, _fff2Uuid)) {
            c2 = c;
            _log('Found fallback FFF2 in service ${s.serviceUuid}');
          }
        }
      }
    }

    // Last resort: choose likely vendor write/notify pair from one service.
    if (c1 == null || c2 == null) {
      for (final s in services) {
        final vendorish = s.characteristics.where((c) {
          final short = _shortUuid(c.characteristicUuid.toString());
          return short.startsWith('ff') || short.startsWith('f');
        }).toList();
        if (vendorish.isEmpty) continue;

        final writable = vendorish.where(
          (c) => c.properties.write || c.properties.writeWithoutResponse,
        );
        final notifiable = vendorish.where((c) => c.properties.notify);

        if ((c1 == null || c2 == null) && writable.isNotEmpty && notifiable.isNotEmpty) {
          c1 ??= writable.first;
          c2 ??= notifiable.first;
          _log(
            'Using heuristic vendor chars from ${s.serviceUuid}: '
            'write=${c1.characteristicUuid}, notify=${c2.characteristicUuid}',
          );
          break;
        }
      }
    }

    // Final fallback: pick any write + notify pair from discovered characteristics.
    if (c1 == null || c2 == null) {
      final writable = allChars.where(
        (c) => c.properties.write || c.properties.writeWithoutResponse,
      ).toList();
      final notifiable = allChars.where((c) => c.properties.notify).toList();

      if (writable.isNotEmpty && notifiable.isNotEmpty) {
        c1 ??= writable.first;
        c2 ??= notifiable.first;
        _log(
          'Using generic fallback chars: '
          'write=${c1.characteristicUuid}, notify=${c2.characteristicUuid}',
        );
      }
    }

    if (selectedChars.isEmpty) {
      selectedChars.addAll(allChars);
    }

    _writeCandidates
      ..clear()
      ..addAll(
        selectedChars.where(
          (c) => c.properties.write || c.properties.writeWithoutResponse,
        ),
      );

    _notifyCandidates
      ..clear()
      ..addAll(selectedChars.where((c) => c.properties.notify));

    _readCandidates
      ..clear()
      ..addAll(selectedChars.where((c) => c.properties.read));

    int writePriority(BluetoothCharacteristic c) {
      final short = _shortUuid(c.characteristicUuid.toString());
      if (short == 'fff3') return 100;
      if (short == 'fff6') return 90;
      if (short == 'fff4') return 80;
      if (short == 'fff1') return 70;
      return 10;
    }

    int notifyPriority(BluetoothCharacteristic c) {
      final short = _shortUuid(c.characteristicUuid.toString());
      if (short == 'fff4') return 100;
      if (short == 'fff6') return 90;
      if (short == 'fff2') return 80;
      if (short == 'fff1') return 70;
      return 10;
    }

    _writeCandidates.sort((a, b) => writePriority(b).compareTo(writePriority(a)));
    _notifyCandidates.sort((a, b) => notifyPriority(b).compareTo(notifyPriority(a)));

    // fff3 = 1-byte ATT max (excluded from 8-byte commands but kept for completeness).
    // fff1 = candidate TX channel (withResponse). fff4/fff6 = echo/loopback.
    // Gateway mode needs all of fff1/fff4/fff6; fff3 stays for fallback logging.
    final preferredWrites = _writeCandidates
        .where((c) {
          final short = _shortUuid(c.characteristicUuid.toString());
          return short == 'fff1' || short == 'fff3' || short == 'fff6' || short == 'fff4';
        })
        .toList();
    if (preferredWrites.isNotEmpty) {
      _writeCandidates
        ..clear()
        ..addAll(preferredWrites);
    }

    _writeChar = _writeCandidates.isNotEmpty ? _writeCandidates.first : _writeChar;
    _altWriteChar = _writeCandidates.length > 1 ? _writeCandidates[1] : _altWriteChar;
    _notifyChar = _notifyCandidates.isNotEmpty ? _notifyCandidates.first : _notifyChar;
    _altNotifyChar = _notifyCandidates.length > 1 ? _notifyCandidates[1] : _altNotifyChar;

    _log(
      'Write candidates: ${_writeCandidates.map((c) => c.characteristicUuid).join(', ')}',
    );
    _log(
      'Notify candidates: ${_notifyCandidates.map((c) => c.characteristicUuid).join(', ')}',
    );
    _log(
      'Read candidates: ${_readCandidates.map((c) => c.characteristicUuid).join(', ')}',
    );

    // Keep candidate-based selection; do not override with legacy FFF1/FFF2-only logic.

    _log(
      'Selected chars: write=${_writeChar?.characteristicUuid}, '
      'notify=${_notifyChar?.characteristicUuid}, '
      'altWrite=${_altWriteChar?.characteristicUuid}',
    );

    if (_writeChar == null || _notifyChar == null) {
      _setStatus('Missing BMS characteristics');
      throw Exception('Required BMS characteristics not found');
    }

    _altNotifyChar ??= (_notifyCandidates.isNotEmpty ? _notifyCandidates.first : null);

    Future<void> enableNotify(BluetoothCharacteristic? c) async {
      if (c == null || !c.properties.notify) return;
      await c.setNotifyValue(true);
      _notifySubs.add(c.onValueReceived.listen(_onData));
      _log('Notifications enabled on ${c.characteristicUuid}');
    }

    await enableNotify(_notifyChar);
    await enableNotify(_altNotifyChar);
    for (final c in _notifyCandidates) {
      if (c != _notifyChar && c != _altNotifyChar) {
        await enableNotify(c);
      }
    }

    // Read all readable characteristics immediately to discover data channels.
    // fff2/fff5 are read-only and may contain battery data without any write trigger.
    for (final c in _readCandidates) {
      final short = _shortUuid(c.characteristicUuid.toString());
      try {
        final v = await c.read();
        _log('INIT READ $short (${v.length}b): ${_hex(v)}');
        // Feed fff4/fff5 data into the parser immediately
        if (short == 'fff4' || short == 'fff5') _onData(v);
      } catch (e) {
        _log('INIT READ $short failed: $e');
      }
    }

    if (_smartBatNativeSeen && !_useLionCommandSet) {
      _switchToLion('SmartBat-native frames detected during init reads');
    }

    _setStatus('Connected');
    await requestSnapshot();
    if (!_useLionCommandSet) {
      _restartPollTimer(const Duration(milliseconds: 1000));
    }
  }

  void _onData(List<int> chunk) {
    _log('RX chunk ${_hex(chunk)}');

    // XOR-decode received bytes (LionCheck applies same key to TX and RX).
    final decoded = _useLionCommandSet
        ? chunk.map((b) => b ^ _lionXorKey).toList()
        : chunk;

    // Echo check: compare against the *encoded* last TX payload (device echoes
    // back the raw bytes it received, not the decoded form).
    if (_lastTxPayload != null &&
        chunk.length == _lastTxPayload!.length &&
        _listEquals(chunk, _lastTxPayload!)) {
      _echoFrameCount++;
      _rxEchoFrames++;
      _rxTotalFrames++;
      _gwCurrentResult?.echoes++;
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
    // Lion mode: responses are ASCII lines terminated with \r\n.
    if (_useLionCommandSet) {
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
      return;
    }

    while (_buf.length >= 4) {
      final start = _buf.indexOf(0xDD);
      if (start < 0) {
        // No JBD frame in buffer - log as SmartBat native / Lion response
        final frame = _buf.toList();
        _log('RX SmartBat-native (${frame.length}b): ${_hex(frame)}');
        _tryParseSmartBatFrame(frame);
        _buf.clear();
        _logRxSummary();
        return;
      }
      if (start > 0)  _buf.removeRange(0, start);
      if (_buf.length < 4) return;

      final reg    = _buf[1];
      final status = _buf[2];
      final len    = _buf[3];
      final total  = 4 + len + 3; // header(4) + data(len) + checksum(2) + end(1)
      if (_buf.length < total) return;
      if (_buf[total - 1] != 0x77) { _buf.removeAt(0); continue; }

      final frame = _buf.sublist(0, total);
      if (!_hasValidChecksum(frame)) {
        if (_looksLikeEchoFrame(frame)) {
          _echoFrameCount++;
          _rxEchoFrames++;
          _rxTotalFrames++;
          if (!_hasValidFrame && _echoFrameCount == 8) {
            _setStatus('Connected - echo only (protocol mismatch?)');
          }
          _log('Ignoring echo frame: ${_hex(frame)}');
          _buf.removeRange(0, total);
          _logRxSummary();
          continue;
        }
        _rxUnknownFrames++;
        _rxTotalFrames++;
        _log('Discarding frame with invalid checksum: ${_hex(frame)}');
        _buf.removeAt(0);
        _logRxSummary();
        continue;
      }

      _log('RX frame ${_hex(frame)}');
      _rxPayloadFrames++;
      _rxTotalFrames++;

      if (status == 0x00) {
        _echoFrameCount = 0;
        final data = _buf.sublist(4, 4 + len);
        if (reg == 0x03) _parseBasic(data);
        if (reg == 0x04) _parseCells(data);
      } else {
        _log('Device replied with status 0x${status.toRadixString(16).padLeft(2, '0')}');
      }
      _buf.removeRange(0, total);
      _logRxSummary();
    }
  }

  /// Attempt to decode SmartBat/LionCheck native response frames.
  /// Format observed: [type(1)] [reg_or_id(1)] [??(1)] [??(1)] [??(1)] [data...]
  /// e.g. 01 02 03 04 05 00... = periodic heartbeat / idle status
  void _tryParseSmartBatFrame(List<int> d) {
    if (d.isEmpty) return;
    _smartBatNativeSeen = true;
    _rxTotalFrames++;
    // Detect periodic heartbeat: 01 02 03 04 05 [zeros]
    if (d.length >= 5 && d[0] == 0x01 && d[1] == 0x02 && d[2] == 0x03 &&
        d[3] == 0x04 && d[4] == 0x05) {
      _rxHeartbeatFrames++;
      _gwCurrentResult?.heartbeats++;
      _log('SmartBat heartbeat frame – bytes 5+: ${_hex(d.sublist(5))}');
      return;
    }
    // Potential Lion register response: [0x01] [reg] [len] [data...]
    if (d.length >= 3 && d[0] == 0x01) {
      final reg = d[1];
      final len = d[2];
      if (d.length >= 3 + len) {
        final payload = d.sublist(3, 3 + len);
        _rxPayloadFrames++;
        _gwCurrentResult?.payloads++;
        _log(
          'SmartBat reg=0x${reg.toRadixString(16).padLeft(2, '0')} '
          'len=$len data=${_hex(payload)}',
        );
        // Legacy binary Lion format — not used when _useLionCommandSet is true
        // (that path returns early in _drain). Log only.
        _log('SmartBat binary Lion reg=0x${reg.toRadixString(16).padLeft(2, '0')} payload=${_hex(payload)}');
        return;
      }
    }
    _rxUnknownFrames++;
    _gwCurrentResult?.unknowns++;
    _gwCurrentResult?.noteFrame(_hex(d));
    _log('SmartBat unknown frame: ${_hex(d)}');
  }

  void _logRxSummary({bool force = false}) {
    if (!force && (_rxTotalFrames == 0 || _rxTotalFrames % _rxSummaryEvery != 0)) {
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
    _smartBatNativeSeen = true;

    final str = String.fromCharCodes(lineBytes);
    _log('Lion line: $str');

    if (str.length < 8 || !str.startsWith('+RD,')) {
      // May be an ERR response or garbage — ignore silently unless debug needed.
      if (!str.toUpperCase().contains('ERR')) {
        _rxUnknownFrames++;
        _log('Lion: unrecognised line (${lineBytes.length}b): ${_hex(lineBytes)}');
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
    _pollAttemptsWithoutValidFrame = 0;
    _reportedProtocolFailure = false;

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

      case '+RD,18': // ATTE — minutes to empty
        _lionAtte = le16();
        _log('Lion ATTE: $_lionAtte min');

      case '+RD,1A': // ATTF — minutes to full
        _lionAttf = le16();
        _log('Lion ATTF: $_lionAttf min');

      case '+RD,28': // Serial number
        _log('Lion serial: ${le16()}');

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
          _log('Lion Cell[$cell]: ${le16()} mV');
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

    final tempC = _lionTempRaw != null
        ? (_lionTempRaw! - 2731) / 10.0
        : 25.0;

    // capacity = (raw × ratio) mAh → / 1000 = Ah
    final remainAh = (_lionRmcRaw != null && _lionRatio != 0)
        ? (_lionRmcRaw! * _lionRatio) / 1000.0 / 1000.0
        : 0.0;
    final nominalAh = (_lionFccRaw != null && _lionRatio != 0)
        ? (_lionFccRaw! * _lionRatio) / 1000.0 / 1000.0
        : 0.0;

    _lastBasic = BatteryData(
      voltage:          voltage,
      current:          current,
      remainingAh:      remainAh,
      nominalAh:        nominalAh,
      cycles:           _lionCycles ?? (_lastBasic?.cycles ?? 0),
      soc:              _lionSoc!.clamp(0, 100),
      temperatures:     [tempC],
      cellVoltages:     _lastBasic?.cellVoltages ?? [],
      cellCount:        _lastBasic?.cellCount ?? 0,
      chargeFet:        true,
      dischargeFet:     true,
      protectionStatus: 0,
      atteMin:          _lionAtte,
      attfMin:          _lionAttf,
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

  void _parseBasic(List<int> d) {
    if (d.length < 23) return;
    _hasValidFrame = true;
    _pollAttemptsWithoutValidFrame = 0;
    _reportedProtocolFailure = false;
    final ntcCount = d[22];
    final temps = <double>[];
    for (int i = 0; i < ntcCount && (23 + i * 2 + 1) < d.length; i++) {
      // Temperature stored as Kelvin * 10 (e.g. 2981 = 25.0 °C)
      temps.add((_u16(d, 23 + i * 2) - 2731) / 10.0);
    }
    _lastBasic = BatteryData(
      voltage:          _u16(d, 0)  / 100.0,   // 10 mV units → V
      current:          _s16(d, 2)  / 100.0,   // 10 mA units → A (neg = discharge)
      remainingAh:      _u16(d, 4)  / 100.0,   // 10 mAh units → Ah
      nominalAh:        _u16(d, 6)  / 100.0,
      cycles:           _u16(d, 8),
      soc:              d[19].clamp(0, 100),
      temperatures:     temps,
      cellVoltages:     _lastBasic?.cellVoltages ?? [],
      cellCount:        d[21],
      chargeFet:        (d[20] & 0x01) != 0,
      dischargeFet:     (d[20] & 0x02) != 0,
      protectionStatus: _u16(d, 16),
    );
    _setStatus('Basic data received');
    _log(
      'Parsed basic data: ${_lastBasic!.voltage.toStringAsFixed(2)} V, '
      '${_lastBasic!.current.toStringAsFixed(2)} A, ${_lastBasic!.soc}% SoC',
    );
    _emit(_lastBasic!);
  }

  void _parseCells(List<int> d) {
    _hasValidFrame = true;
    _pollAttemptsWithoutValidFrame = 0;
    _reportedProtocolFailure = false;
    final volts = [
      for (int i = 0; i + 1 < d.length; i += 2) _u16(d, i) / 1000.0,
    ];
    if (_lastBasic != null) {
      _lastBasic = _lastBasic!.copyWith(cellVoltages: volts);
      _setStatus('Cell data received');
      _log('Parsed ${volts.length} cell voltages');
      _emit(_lastBasic!);
    }
  }

  void _emit(BatteryData data) {
    if (!_dataController.isClosed) _dataController.add(data);
  }

  ({List<int> basic, List<int> cells, String name}) _activeCommandProfile() {
    switch (_commandProfileIndex % 2) {
      case 1:
        return (
          basic: _cmdBasicInfoAltEndian,
          cells: _cmdCellVoltagesAltEndian,
          name: 'JBD-alt-endian',
        );
      default:
        return (
          basic: _cmdBasicInfo,
          cells: _cmdCellVoltages,
          name: 'JBD-classic',
        );
    }
  }

  String _nextLionAsciiCommand() {
    if (_lionSingleCommandMode) {
      return _activeLionSingleCommand;
    }

    if (_lionFixedReplayMode) {
      final cmd = _lionFixedReplayCycle[_lionFixedReplayIndex % _lionFixedReplayCycle.length];
      _lionFixedReplayIndex = (_lionFixedReplayIndex + 1) % _lionFixedReplayCycle.length;
      return cmd;
    }

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

    final cmd = _lionSteadyCommands[_lionSteadyIndex % _lionSteadyCommands.length];
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

  BluetoothCharacteristic? _lionPreferredWrite() {
    // Gateway mode: use exactly the characteristic from the current permutation.
    if (_gatewayMode && _gwPerms != null && !_gwDone) {
      final targetId = _gwPerms![_gwIndex].charId;
      for (final c in _writeCandidates) {
        if (_shortUuid(c.characteristicUuid.toString()) == targetId) return c;
      }
      // Permutation's char not in writeCandidates — fall through to default.
    }
    // Protocol confirmed via JADX: LionCheck writes to fff6 (CHA_UUID2) only.
    // fff4 is notify-only (CHA_UUID1). fff1/fff3 reject 8-byte payloads.
    for (final c in _writeCandidates) {
      if (_shortUuid(c.characteristicUuid.toString()) == 'fff6') return c;
    }
    for (final c in _writeCandidates) {
      if (_shortUuid(c.characteristicUuid.toString()) == 'fff4') return c;
    }
    return _writeChar;
  }

  Future<void> requestSnapshot() async {
    await _poll();
  }

  void _switchToLion(String reason) {
    if (_useLionCommandSet) return;
    _useLionCommandSet = true;
    // Gateway: build the permutation list once; _gwIndex persists across reconnects.
    // Ordered so char changes are minimal (only 2 reconnects total over full run).
    if (_gatewayMode && _gwPerms == null) {
      _gwPerms = [
        for (final ch in ['fff1', 'fff4', 'fff6'])
          for (final cmd in _lionBootstrapCommands)
            for (final wr in [false, true])
              _GwPerm(cmd, ch, withResponse: wr),
      ];
      _gwCurrentResult = _GwResult();
      _gwDone = false;
      _log(
        'GATEWAY_INIT: ${_gwPerms!.length} permutations total, '
        'resuming at index $_gwIndex',
      );
    }
    if (_lionSingleCommandMode) {
      if (_lionAutoDiscoverMode) {
        if (_lionAutoDiscoverStartedAt == null) {
          _lionAutoDiscoverIndex = 0;
          _activeLionSingleCommand = _lionAutoDiscoverCommands.first;
          _lionAutoDiscoverStartedAt = DateTime.now();
        }
      } else {
        _activeLionSingleCommand = _lionSingleCommand;
        _lionAutoDiscoverStartedAt = DateTime.now();
      }
      _lionAutoDiscoverDoneLogged = false;
    }
    _lionFixedReplayIndex = 0;
    _lionPollCount = 0;
    _lionBurstPollsRemaining = _lionFastBurstPolls;
    // LionCheck uses writeWithResponse (ATT_WRITE_REQ). Force the same mode.
    _preferWriteWithoutResponse = false;
    _restartPollTimer(_lionFastPollInterval);
    if (_lionSingleCommandMode) {
      _setStatus('Trying Lion single command replay...');
    } else if (_lionFixedReplayMode) {
      _setStatus('Trying Lion fixed replay...');
    } else {
      _setStatus('Trying LionCheck command set...');
    }
    _log(
      _lionSingleCommandMode
          ? 'Switching to Lion single command replay ($_activeLionSingleCommand) ($reason)'
          : _lionFixedReplayMode
          ? 'Switching to Lion fixed replay command set ($reason)'
          : 'Switching to LionCheck command set ($reason)',
    );
  }

  void _maybeAutoDiscoverRotate() {
    if (!_lionSingleCommandMode || !_lionAutoDiscoverMode) return;
    if (_lionAutoDiscoverTransition) return;
    final started = _lionAutoDiscoverStartedAt;
    if (started == null) return;
    if (DateTime.now().difference(started) < _lionAutoDiscoverHold) return;

    final isLast = _lionAutoDiscoverIndex >= _lionAutoDiscoverCommands.length - 1;
    if (isLast) {
      if (!_lionAutoDiscoverDoneLogged) {
        _lionAutoDiscoverDoneLogged = true;
        _log('Auto-discover completed at command $_activeLionSingleCommand');
      }
      return;
    }

    final nextIndex = _lionAutoDiscoverIndex + 1;
    final nextCommand = _lionAutoDiscoverCommands[nextIndex];
    final device = _device;
    if (device == null) return;

    _lionAutoDiscoverTransition = true;
    _log('Auto-discover rotate: $_activeLionSingleCommand -> $nextCommand (reconnect)');
    unawaited(_rotateAutoDiscoverCommand(device, nextIndex, nextCommand));
  }

  Future<void> _rotateAutoDiscoverCommand(
    BluetoothDevice device,
    int nextIndex,
    String nextCommand,
  ) async {
    try {
      await disconnect(keepGateway: true);
      _lionAutoDiscoverIndex = nextIndex;
      _activeLionSingleCommand = nextCommand;
      _lionAutoDiscoverStartedAt = DateTime.now();
      await Future.delayed(const Duration(milliseconds: 500));
      await connect(device);
    } catch (e) {
      _log('Auto-discover reconnect failed: $e');
    } finally {
      _lionAutoDiscoverTransition = false;
    }
  }

  // ── Gateway helpers ──────────────────────────────────────────────────────────

  void _gwMaybeAdvance() {
    if (_gwPerms == null || _gwDone) return;
    final result = _gwCurrentResult ?? _GwResult();
    final perm = _gwPerms![_gwIndex];
    _log(result.toLogLine(_gwIndex, perm));
    _gwIndex++;
    _gwWriteCount = 0;
    if (_gwIndex >= _gwPerms!.length) {
      _gwDone = true;
      _setStatus('Gateway DONE – ${_gwPerms!.length} permutations complete');
      _log('GATEWAY_MATRIX_DONE: all ${_gwPerms!.length} permutations complete');
      return;
    }
    _gwCurrentResult = _GwResult();
    final nextPerm = _gwPerms![_gwIndex];
    _setStatus('Gateway: ${nextPerm.label} [$_gwIndex/${_gwPerms!.length}]');
    _log('Gateway → permutation $_gwIndex: ${nextPerm.label}');
    if (nextPerm.charId != perm.charId) {
      final device = _device;
      if (device != null) {
        _lionAutoDiscoverTransition = true;
        unawaited(_gwReconnect(device));
      }
    }
  }

  Future<void> _gwReconnect(BluetoothDevice device) async {
    try {
      await disconnect(keepGateway: true);
      await Future.delayed(const Duration(milliseconds: 500));
      await connect(device);
    } catch (e) {
      _log('Gateway reconnect failed: $e');
    } finally {
      _lionAutoDiscoverTransition = false;
    }
  }

  Future<void> _poll() async {
    if (_writeChar == null) return;
    try {
      if (!_hasValidFrame) {
        _pollAttemptsWithoutValidFrame++;
      }

      if (!_hasValidFrame && !_useLionCommandSet && _pollAttemptsWithoutValidFrame >= _maxJbdProbePolls) {
        _switchToLion(
          'after $_pollAttemptsWithoutValidFrame JBD probe polls, echoCount=$_echoFrameCount',
        );
      }

      // Keep Lion mode running even when no frame is decoded yet.
      // SmartBat devices can be notify-driven and may need longer observation windows.
      if (!_hasValidFrame && !_useLionCommandSet && _pollAttemptsWithoutValidFrame >= _maxTotalProbePolls) {
        if (!_reportedProtocolFailure) {
          _reportedProtocolFailure = true;
          _setStatus('Error: unsupported BMS protocol (only echo responses)');
          _log(
            'Protocol probe failed after $_pollAttemptsWithoutValidFrame polls '
            '(lionMode=$_useLionCommandSet, echoCount=$_echoFrameCount)',
          );
        }
        return;
      }

      if (_useLionCommandSet) {
        if (_lionAutoDiscoverTransition) {
          return;
        }

        // ── Gateway mode: overrides single/auto-discover ──────────────────
        if (_gatewayMode && _gwPerms != null && !_gwDone) {
          final perm = _gwPerms![_gwIndex];
          _preferWriteWithoutResponse = !perm.withResponse;
          final payload = ascii.encode(perm.command);
          final preferred = _lionPreferredWrite();
          _log(
            'TX Lion ASCII ${perm.command} (${payload.length}b) '
            'phase=gateway[$_gwIndex/${_gwPerms!.length - 1}]'
            ' ${perm.charId}/${perm.withResponse ? "REQ" : "CMD"}',
          );
          try {
            await _writeOnCandidates(payload, preferred: preferred, gatewayOnly: true);
            _gwCurrentResult!.writesOk++;
          } catch (_) {
            _gwCurrentResult!.writesErr++;
          }
          _gwWriteCount++;
          if (_gwWriteCount >= _gwWritesPerPerm) _gwMaybeAdvance();
          return;
        }

        _lionPollCount++;
        if (_lionBurstPollsRemaining > 0) {
          _lionBurstPollsRemaining--;
          if (_lionBurstPollsRemaining == 0 && _pollInterval != _lionSteadyPollInterval) {
            _restartPollTimer(_lionSteadyPollInterval);
            _log('Lion scheduler switched to steady cadence (5 writes/sec)');
          }
        }

        final asciiCommand = _nextLionAsciiCommand();
        final rawBytes = ascii.encode(asciiCommand);
        // XOR-encode: LionCheck applies byte ^ resouce before writeCharacteristic.
        // Key 0x19 derived from device name 'SmartBat-A19681' via EncryptUtils.java.
        final payload = Uint8List.fromList(rawBytes.map((b) => b ^ _lionXorKey).toList());
        final preferred = _lionPreferredWrite();
        final lionPhase = _lionSingleCommandMode
          ? 'single-command'
          : _lionFixedReplayMode
            ? 'fixed-replay'
            : (_lionBootstrapIndex < _lionBootstrapCommands.length ? 'bootstrap' : 'steady');
        _log(
          'TX Lion ASCII $asciiCommand (${payload.length}b) '
          'phase=$lionPhase [XOR 0x${_lionXorKey.toRadixString(16).padLeft(2, '0').toUpperCase()}]',
        );
        await _writeOnCandidates(payload, preferred: preferred);

        _maybeAutoDiscoverRotate();

        // The original app is notify-driven; keep reads as sparse fallback probes.
        if (!_hasValidFrame && _lionPollCount % 20 == 0) {
          await _readFromCandidates();
        }
        return;
      }

      if (!_hasValidFrame) {
        _commandProfileIndex = (_commandProfileIndex + 1) % 2;
        _preferWriteWithoutResponse = !_preferWriteWithoutResponse;
      }
      final profile = _activeCommandProfile();
      if (!_hasValidFrame && _lastAnnouncedProfileIndex != _commandProfileIndex) {
        _lastAnnouncedProfileIndex = _commandProfileIndex;
        _log('Trying protocol profile: ${profile.name}');
      }
      if (!_hasValidFrame) {
        _log('Trying write mode: ${_preferWriteWithoutResponse ? 'without-response-first' : 'with-response-first'}');
      }

      // Until we decode at least one valid frame, rotate write candidates.
      // Some BMS firmwares accept writes on multiple chars but respond on only one.
      BluetoothCharacteristic? preferred;
      if (_writeCandidates.isNotEmpty && !_hasValidFrame) {
        preferred = _writeCandidates[_writeCandidateIndex % _writeCandidates.length];
        _writeCandidateIndex = (_writeCandidateIndex + 1) % _writeCandidates.length;
      }

      final usedForBasic = await _writeOnCandidates(profile.basic, preferred: preferred);
      await _readFromCandidates();
      await Future.delayed(const Duration(milliseconds: 300));
      await _writeOnCandidates(profile.cells, preferred: usedForBasic);
      await _readFromCandidates();
    } catch (_) {
      // Silently ignore poll errors (device may be temporarily busy)
      _log('Polling failed');
    }
  }

  Future<void> _readFromCandidates() async {
    final prioritizedReads = _readCandidates
        .where((c) {
          final short = _shortUuid(c.characteristicUuid.toString());
          return short == 'fff6' || short == 'fff4';
        })
        .toList();
    final activeReads = prioritizedReads.isNotEmpty ? prioritizedReads : _readCandidates;

    for (final c in activeReads) {
      try {
        final value = await c.read();
        if (value.isNotEmpty) {
          _log('RX read ${c.characteristicUuid}: ${_hex(value)}');
          _onData(value);
        }
      } catch (e) {
        _log('Read failed on ${c.characteristicUuid}: $e');
      }
    }
  }

  Future<BluetoothCharacteristic> _writeOnCandidates(
    List<int> payload, {
    BluetoothCharacteristic? preferred,
    bool gatewayOnly = false, // When true, only try `preferred` (no fallback).
  }) async {
    final primary = preferred ?? _writeChar;
    final candidateOrder = <BluetoothCharacteristic>[];
    if (primary != null) candidateOrder.add(primary);
    if (!gatewayOnly) {
      if (_altWriteChar != null && _altWriteChar != primary) {
        candidateOrder.add(_altWriteChar!);
      }
      for (final c in _writeCandidates) {
        if (!candidateOrder.contains(c)) {
          candidateOrder.add(c);
        }
      }
    }

    Future<void> send(BluetoothCharacteristic c) async {
      final canWriteWithResponse = c.properties.write;
      final canWriteWithoutResponse = c.properties.writeWithoutResponse;
      if (!canWriteWithResponse && !canWriteWithoutResponse) {
        throw Exception('Characteristic ${c.characteristicUuid} is not writable');
      }

      final writeModes = <bool>[]; // true => withoutResponse
      if (_preferWriteWithoutResponse) {
        if (canWriteWithoutResponse) writeModes.add(true);
        if (canWriteWithResponse) writeModes.add(false);
      } else {
        if (canWriteWithResponse) writeModes.add(false);
        if (canWriteWithoutResponse) writeModes.add(true);
      }

      Object? lastError;
      for (final withoutResponse in writeModes) {
        try {
          final modeText = withoutResponse ? 'withoutResponse' : 'withResponse';
          _log('TX ${_hex(payload)} -> ${c.characteristicUuid} ($modeText)');
          _lastTxPayload = List<int>.from(payload);
          await c.write(payload, withoutResponse: withoutResponse);
          return;
        } catch (e) {
          lastError = e;
          final short = _shortUuid(c.characteristicUuid.toString());
          final msg = e.toString();
          if (short == 'fff3' && payload.length > 1 && msg.contains('android-code: 13')) {
            _fff3RejectsLongWrites = true;
            _log('Marking fff3 as unsuitable for long writes after invalid-length error');
          }
          _log('Write mode failed on ${c.characteristicUuid}: $e');
        }
      }

      throw Exception(lastError?.toString() ?? 'Write failed for all supported modes');
    }

    BluetoothCharacteristic? firstSuccess;

    for (final candidate in candidateOrder) {
      try {
        await send(candidate);
        firstSuccess ??= candidate;
        break;
      } catch (e) {
        _log('Write failed on ${candidate.characteristicUuid}: $e');
      }
    }

    if (firstSuccess != null) {
      return firstSuccess;
    }
    throw Exception('No writable characteristic accepted payload');
  }

  bool _hasValidChecksum(List<int> frame) {
    if (frame.length < 7) return false;
    final expected = ((frame[frame.length - 3] & 0xFF) << 8) | (frame[frame.length - 2] & 0xFF);
    int sum = 0;
    for (int i = 1; i < frame.length - 3; i++) {
      sum = (sum + frame[i]) & 0xFFFF;
    }
    final actual = (0x10000 - sum) & 0xFFFF;
    return expected == actual;
  }

  bool _looksLikeEchoFrame(List<int> frame) {
    if (frame.length != 7) return false;
    return frame[0] == 0xDD && frame[1] == 0xA5 && frame[3] == 0x00 && frame[6] == 0x77;
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
    final shortExpected = expectedNorm.length >= 8
        ? expectedNorm.substring(4, 8)
        : expectedNorm;
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

  int _u16(List<int> d, int i) => ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
  int _s16(List<int> d, int i) {
    final v = _u16(d, i);
    return v >= 0x8000 ? v - 0x10000 : v;
  }

  Future<void> disconnect({bool keepGateway = false}) async {
    _setStatus('Disconnecting');
    _pollTimer?.cancel();
    for (final sub in _notifySubs) {
      await sub.cancel();
    }
    _notifySubs.clear();
    _pollTimer = null;
    try { await _device?.disconnect(); } catch (_) {}
    _log('Disconnected from device');
    _setStatus('Disconnected');
    _writeChar = _notifyChar = _device = _lastBasic = null;
    _altWriteChar = null;
    _altNotifyChar = null;
    _writeCandidateIndex = 0;
    _hasValidFrame = false;
    _echoFrameCount = 0;
    _commandProfileIndex = 0;
    _lastAnnouncedProfileIndex = -1;
    _preferWriteWithoutResponse = true;
    _useLionCommandSet = false;
    _smartBatNativeSeen = false;
    _lionBootstrapIndex = 0;
    _lionSteadyIndex = 0;
    _lionExtendedSweepIndex = -1;
    _lionSteadyLoopCount = 0;
    _activeLionSingleCommand = _lionSingleCommand;
    _lionAutoDiscoverIndex = 0;
    _lionAutoDiscoverStartedAt = null;
    _lionAutoDiscoverTransition = false;
    _lionAutoDiscoverDoneLogged = false;
    _lionPollCount = 0;
    _lionBurstPollsRemaining = 0;
    _fff3RejectsLongWrites = false;
    _lastTxPayload = null;
    _pollAttemptsWithoutValidFrame = 0;
    _reportedProtocolFailure = false;
    _logRxSummary(force: true);
    _rxTotalFrames = 0;
    _rxEchoFrames = 0;
    _rxHeartbeatFrames = 0;
    _rxPayloadFrames = 0;
    _rxUnknownFrames = 0;
    _rxAsciiFrames = 0;
    _pollInterval = const Duration(seconds: 5);
    _buf.clear();
    if (!keepGateway) {
      _gwPerms = null;
      _gwIndex = 0;
      _gwWriteCount = 0;
      _gwDone = false;
      _gwCurrentResult = null;
    }
  }

  void dispose() {
    disconnect();
    _dataController.close();
    _statusController.close();
    _debugController.close();
  }
}

// ── Gateway data classes ──────────────────────────────────────────────────────

/// One test permutation: (command, BLE characteristic, write mode).
class _GwPerm {
  final String command;    // ASCII command e.g. '+RAA0802'
  final String charId;     // Short UUID e.g. 'fff1', 'fff4', 'fff6'
  final bool withResponse; // true = ATT_WRITE_REQ, false = ATT_WRITE_CMD

  const _GwPerm(this.command, this.charId, {required this.withResponse});

  String get label => '$command/$charId/${withResponse ? "REQ" : "CMD"}';
}

/// Accumulated RX counters for one permutation.
class _GwResult {
  int writesOk = 0;
  int writesErr = 0;
  int heartbeats = 0;
  int echoes = 0;
  int payloads = 0;
  int unknowns = 0;
  final List<String> frames = []; // first 3 unique non-HB/non-echo frames

  void noteFrame(String hex) {
    if (frames.length < 3 && !frames.contains(hex)) frames.add(hex);
  }

  String toLogLine(int idx, _GwPerm p) {
    final fStr = frames.map((f) => '"$f"').join(',');
    return 'GATEWAY_RESULT: {"i":$idx,"cmd":"${p.command}",'
        '"char":"${p.charId}","wr":${p.withResponse},'
        '"wok":$writesOk,"werr":$writesErr,'
        '"hb":$heartbeats,"echo":$echoes,'
        '"payload":$payloads,"unk":$unknowns,'
        '"frames":[$fStr]}';
  }
}
