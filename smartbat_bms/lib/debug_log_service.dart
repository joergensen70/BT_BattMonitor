import 'dart:async';
import 'dart:io';

import 'package:path_provider/path_provider.dart';

String _fmtDebugTs(DateTime dt) {
  final local = dt.toLocal();
  final yyyy = local.year.toString().padLeft(4, '0');
  final mm = local.month.toString().padLeft(2, '0');
  final dd = local.day.toString().padLeft(2, '0');
  final hh = local.hour.toString().padLeft(2, '0');
  final min = local.minute.toString().padLeft(2, '0');
  final ss = local.second.toString().padLeft(2, '0');
  final ms = local.millisecond.toString().padLeft(3, '0');
  return '$yyyy-$mm-$dd $hh:$min:$ss.$ms';
}

class DebugLogService {
  DebugLogService._();

  static final DebugLogService instance = DebugLogService._();

  static const int _maxLines = 180;
  final List<String> _lines = [];
  Timer? _flushTimer;
  bool _flushInFlight = false;

  List<String> get lines => List.unmodifiable(_lines);

  void add(String message) {
    final line = '${_fmtDebugTs(DateTime.now())}  $message';
    _lines.add(line);
    if (_lines.length > _maxLines) {
      _lines.removeRange(0, _lines.length - _maxLines);
    }
    _scheduleFlush();
  }

  Future<String> exportSnapshot() async {
    final dir = await getApplicationDocumentsDirectory();
    final stamp = DateTime.now().toUtc().millisecondsSinceEpoch;
    final file = File('${dir.path}/smartbat_debug_log_$stamp.txt');
    await file.writeAsString(_buildText(), flush: true);
    return file.path;
  }

  void _scheduleFlush() {
    _flushTimer?.cancel();
    _flushTimer = Timer(const Duration(milliseconds: 600), _flushSnapshot);
  }

  Future<void> _flushSnapshot() async {
    if (_flushInFlight) return;
    _flushInFlight = true;
    try {
      final dir = await getTemporaryDirectory();
      final file = File('${dir.path}/smartbat_debug_ring.txt');
      await file.writeAsString(_buildText(), flush: true);
    } catch (_) {
      // Best-effort background diagnostics only.
    } finally {
      _flushInFlight = false;
    }
  }

  String _buildText() {
    final buf = StringBuffer()
      ..writeln('SmartBat debug ring buffer')
      ..writeln('lines=${_lines.length}')
      ..writeln('');
    for (final line in _lines) {
      buf.writeln(line);
    }
    return buf.toString();
  }
}