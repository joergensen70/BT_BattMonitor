import 'dart:async';
import 'dart:io';
import 'package:path_provider/path_provider.dart';
import 'battery_data.dart';

String _fmtDateTime(DateTime dt) {
  final d = dt.toLocal();
  return '${d.year.toString().padLeft(4,'0')}'
      '${d.month.toString().padLeft(2,'0')}'
      '${d.day.toString().padLeft(2,'0')}_'
      '${d.hour.toString().padLeft(2,'0')}'
      '${d.minute.toString().padLeft(2,'0')}'
      '${d.second.toString().padLeft(2,'0')}';
}

String _fmtSessionId(DateTime utc) =>
    '${utc.year.toString().padLeft(4,'0')}'
    '${utc.month.toString().padLeft(2,'0')}'
    '${utc.day.toString().padLeft(2,'0')}'
    '${utc.hour.toString().padLeft(2,'0')}'
    '${utc.minute.toString().padLeft(2,'0')}'
    '${utc.second.toString().padLeft(2,'0')}'
    '_${utc.millisecond.toString().padLeft(3,'0')}';

// ── Row model ─────────────────────────────────────────────────────────────────
class RecordingRow {
  final int timestampUtcMs;
  final int tRelMs;
  final double? aVolt;
  final double? aCurr;
  final double? bVolt;
  final double? bCurr;
  final bool aConnected;
  final bool bConnected;
  final String qualityFlag; // 'ok' | 'partial' | 'stale'

  const RecordingRow({
    required this.timestampUtcMs,
    required this.tRelMs,
    this.aVolt,
    this.aCurr,
    this.bVolt,
    this.bCurr,
    required this.aConnected,
    required this.bConnected,
    required this.qualityFlag,
  });

  String toCsvRow() {
    String f(double? v) => v == null ? '' : v.toStringAsFixed(3);
    return '$timestampUtcMs,$tRelMs,${f(aVolt)},${f(aCurr)},${f(bVolt)},${f(bCurr)},$aConnected,$bConnected,$qualityFlag';
  }
}

// ── Session model ─────────────────────────────────────────────────────────────
class RecordingSession {
  final String sessionId;
  final DateTime startedAtUtc;
  DateTime? stoppedAtUtc;
  final String batteryADeviceId;
  final String batteryBDeviceId;
  final List<RecordingRow> rows = [];
  bool interrupted = false;

  RecordingSession({
    required this.sessionId,
    required this.startedAtUtc,
    required this.batteryADeviceId,
    required this.batteryBDeviceId,
  });

  Duration get elapsed =>
      (stoppedAtUtc ?? DateTime.now().toUtc()).difference(startedAtUtc);

  bool get isRunning => stoppedAtUtc == null;
}

// ── Recording Service (singleton) ─────────────────────────────────────────────
class RecordingService {
  static final RecordingService instance = RecordingService._();
  RecordingService._();

  static const String _csvHeader =
      'timestamp_utc_ms,t_rel_ms,battery_a_voltage_v,battery_a_current_a,'
      'battery_b_voltage_v,battery_b_current_a,battery_a_connected,'
      'battery_b_connected,quality_flag';

  RecordingSession? _active;
  final List<RecordingSession> _completed = [];

  RecordingSession? get activeSession => _active;
  List<RecordingSession> get completedSessions => List.unmodifiable(_completed);
  bool get isRecording => _active != null;

  // Notifiers
  final List<VoidCallback> _listeners = [];
  void addListener(VoidCallback cb) => _listeners.add(cb);
  void removeListener(VoidCallback cb) => _listeners.remove(cb);
  void _notify() { for (final cb in _listeners) cb(); }

  // ── Control ────────────────────────────────────────────────────────────────

  /// Returns null on success, error message on failure.
  String? startRecording({String aDeviceId = '', String bDeviceId = ''}) {
    if (_active != null) return 'Recording already active';
    final now = DateTime.now().toUtc();
    final id = _makeSessionId(now);
    _active = RecordingSession(
      sessionId: id,
      startedAtUtc: now,
      batteryADeviceId: aDeviceId,
      batteryBDeviceId: bDeviceId,
    );
    _notify();
    return null;
  }

  void stopRecording() {
    if (_active == null) return;
    _active!.stoppedAtUtc = DateTime.now().toUtc();
    _completed.add(_active!);
    _active = null;
    _notify();
  }

  // ── Data ingestion (merge A + B within 200 ms into one row) ──────────────

  BatteryData? _pendingA;
  BatteryData? _pendingB;
  int _pendingTsMs = 0;
  Timer? _flushTimer;

  void addA(BatteryData d) => _pending(aData: d);
  void addB(BatteryData d) => _pending(bData: d);

  void _pending({BatteryData? aData, BatteryData? bData}) {
    if (_active == null) return;
    final nowMs = DateTime.now().millisecondsSinceEpoch;
    if (_pendingTsMs == 0 || (nowMs - _pendingTsMs) >= 200) {
      if (_pendingTsMs != 0) _flushPending();
      _pendingTsMs = nowMs;
    }
    if (aData != null) _pendingA = aData;
    if (bData != null) _pendingB = bData;
    if (_pendingA != null && _pendingB != null) {
      _flushTimer?.cancel();
      _flushPending();
    } else {
      _flushTimer?.cancel();
      _flushTimer = Timer(const Duration(milliseconds: 200), _flushPending);
    }
  }

  void _flushPending() {
    if (_active == null || _pendingTsMs == 0) return;
    final a = _pendingA;
    final b = _pendingB;
    final relMs = _pendingTsMs - _active!.startedAtUtc.millisecondsSinceEpoch;
    final quality = (a != null && b != null)
        ? 'ok'
        : (a == null && b == null) ? 'stale' : 'partial';
    _active!.rows.add(RecordingRow(
      timestampUtcMs: _pendingTsMs,
      tRelMs: relMs,
      aVolt: a?.voltage, aCurr: a?.current,
      bVolt: b?.voltage, bCurr: b?.current,
      aConnected: a != null,
      bConnected: b != null,
      qualityFlag: quality,
    ));
    _pendingA = null;
    _pendingB = null;
    _pendingTsMs = 0;
  }

  // ── CSV Export ─────────────────────────────────────────────────────────────

  /// Exports [session] to the app documents directory.
  /// Returns the file path on success, throws on failure.
  Future<String> exportCsv(RecordingSession session) async {
    final dir = await getApplicationDocumentsDirectory();
    final dtStr = _fmtDateTime(session.startedAtUtc);
    final filename = 'smartbat_recording_${session.sessionId}_$dtStr.csv';
    final file = File('${dir.path}/$filename');

    final buf = StringBuffer()..writeln(_csvHeader);
    for (final row in session.rows) {
      buf.writeln(row.toCsvRow());
    }
    await file.writeAsString(buf.toString(), flush: true);
    return file.path;
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  String _makeSessionId(DateTime utc) => _fmtSessionId(utc);
}
