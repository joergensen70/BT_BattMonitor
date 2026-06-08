import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:share_plus/share_plus.dart';
import 'battery_data.dart';
import 'recording_service.dart';

// ── Design tokens (must match battery_screen.dart) ────────────────────────────
const _kBg           = Color(0xFF0A0F14);
const _kSurface      = Color(0xFF121A23);
const _kSurfaceElev  = Color(0xFF182330);
const _kTextPrimary  = Color(0xFFEAF2FF);
const _kTextSecondary = Color(0xFF9FB0C8);

// Spec §16.3 fixed color mapping
const _kColorAVolt  = Color(0xFF35D07F); // Battery A Voltage
const _kColorBVolt  = Color(0xFF4DB3FF); // Battery B Voltage
const _kColorACurr  = Color(0xFFFFB020); // Battery A Current
const _kColorBCurr  = Color(0xFFFF5D5D); // Battery B Current

// ── Point model ───────────────────────────────────────────────────────────────
class ChartPoint {
  final double tSec;      // seconds relative to session start
  final double? aVolt;
  final double? aCurr;
  final double? bVolt;
  final double? bCurr;
  const ChartPoint(this.tSec, {this.aVolt, this.aCurr, this.bVolt, this.bCurr});
}

// ── Chart data buffer (singleton, lives as long as app process) ───────────────
class ChartBuffer {
  static final ChartBuffer instance = ChartBuffer._();
  ChartBuffer._();

  static const int _maxPoints = 1800; // 30 min @ 1 Hz
  final List<ChartPoint> points = [];
  DateTime? _sessionStart;

  void addA(BatteryData d) => _add(aVolt: d.voltage, aCurr: d.current);
  void addB(BatteryData d) => _add(bVolt: d.voltage, bCurr: d.current);

  void _add({double? aVolt, double? aCurr, double? bVolt, double? bCurr}) {
    _sessionStart ??= DateTime.now();
    final t = DateTime.now().difference(_sessionStart!).inMilliseconds / 1000.0;

    // Merge into last point if within 200 ms
    if (points.isNotEmpty && (t - points.last.tSec) < 0.2) {
      final last = points.last;
      points[points.length - 1] = ChartPoint(last.tSec,
        aVolt: aVolt ?? last.aVolt,
        aCurr: aCurr ?? last.aCurr,
        bVolt: bVolt ?? last.bVolt,
        bCurr: bCurr ?? last.bCurr,
      );
    } else {
      points.add(ChartPoint(t, aVolt: aVolt, aCurr: aCurr, bVolt: bVolt, bCurr: bCurr));
      if (points.length > _maxPoints) points.removeAt(0);
    }
    _notifyListeners();
  }

  final List<VoidCallback> _listeners = [];
  void addListener(VoidCallback cb) => _listeners.add(cb);
  void removeListener(VoidCallback cb) => _listeners.remove(cb);
  void _notifyListeners() { for (final cb in _listeners) cb(); }

  void reset() { points.clear(); _sessionStart = null; }
}

// ── Chart Screen ─────────────────────────────────────────────────────────────
class ChartScreen extends StatefulWidget {
  const ChartScreen({super.key});

  @override
  State<ChartScreen> createState() => _ChartScreenState();
}

class _ChartScreenState extends State<ChartScreen> {
  final _buffer = ChartBuffer.instance;
  final _rec    = RecordingService.instance;

  // Visibility toggles
  bool _showAVolt = true;
  bool _showBVolt = true;
  bool _showACurr = true;
  bool _showBCurr = true;

  Timer? _elapsedTimer;

  @override
  void initState() {
    super.initState();
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.landscapeLeft,
      DeviceOrientation.landscapeRight,
    ]);
    _buffer.addListener(_onData);
    _rec.addListener(_onRecState);
    _startElapsedTick();
  }

  @override
  void dispose() {
    _buffer.removeListener(_onData);
    _rec.removeListener(_onRecState);
    _elapsedTimer?.cancel();
    SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);
    super.dispose();
  }

  void _onData() { if (mounted) setState(() {}); }
  void _onRecState() { if (mounted) setState(() {}); }

  void _startElapsedTick() {
    _elapsedTimer?.cancel();
    _elapsedTimer = Timer.periodic(const Duration(seconds: 1), (_) {
      if (mounted && _rec.isRecording) setState(() {});
    });
  }

  // ── Build ──────────────────────────────────────────────────────────────────
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _kBg,
      appBar: AppBar(
        backgroundColor: _kSurface,
        foregroundColor: _kTextPrimary,
        title: _buildTitle(),
        actions: [
          _buildRecordingActions(),
          IconButton(
            icon: const Icon(Icons.refresh),
            tooltip: 'Reset timeline',
            onPressed: () => setState(() => _buffer.reset()),
          ),
        ],
      ),
      body: Column(children: [
        _buildLegend(),
        Expanded(child: _buildChart()),
        if (_rec.completedSessions.isNotEmpty) _buildExportBar(),
      ]),
    );
  }

  Widget _buildTitle() {
    if (_rec.isRecording) {
      final elapsed = _rec.activeSession!.elapsed;
      final mm = elapsed.inMinutes.toString().padLeft(2, '0');
      final ss = (elapsed.inSeconds % 60).toString().padLeft(2, '0');
      return Row(mainAxisSize: MainAxisSize.min, children: [
        const Icon(Icons.fiber_manual_record, color: Color(0xFFFF5D5D), size: 14),
        const SizedBox(width: 6),
        Text('REC  $mm:$ss',
            style: const TextStyle(fontSize: 15, fontWeight: FontWeight.bold,
                color: Color(0xFFFF5D5D))),
      ]);
    }
    return const Text('Chart', style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold));
  }

  Widget _buildRecordingActions() {
    if (_rec.isRecording) {
      return IconButton(
        icon: const Icon(Icons.stop_circle_outlined),
        tooltip: 'Stop recording',
        color: const Color(0xFFFF5D5D),
        onPressed: () => _rec.stopRecording(),
      );
    }
    return IconButton(
      icon: const Icon(Icons.fiber_manual_record),
      tooltip: 'Start recording',
      color: const Color(0xFFFF5D5D),
      onPressed: () {
        final err = _rec.startRecording();
        if (err != null && mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text(err), backgroundColor: const Color(0xFFFF5D5D)));
        }
      },
    );
  }

  Widget _buildExportBar() {
    final sessions = _rec.completedSessions;
    return Container(
      color: _kSurface,
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Row(children: [
        const Icon(Icons.save_alt, size: 14, color: _kTextSecondary),
        const SizedBox(width: 6),
        Text('${sessions.length} session${sessions.length == 1 ? '' : 's'} recorded',
            style: const TextStyle(color: _kTextSecondary, fontSize: 11)),
        const Spacer(),
        TextButton.icon(
          icon: const Icon(Icons.share, size: 14),
          label: const Text('Export last', style: TextStyle(fontSize: 12)),
          style: TextButton.styleFrom(foregroundColor: _kInfo, padding: EdgeInsets.zero),
          onPressed: () => _exportLast(),
        ),
      ]),
    );
  }

  Future<void> _exportLast() async {
    final sessions = _rec.completedSessions;
    if (sessions.isEmpty) return;
    try {
      final path = await _rec.exportCsv(sessions.last);
      if (!mounted) return;
      await SharePlus.instance.share(ShareParams(
        files: [XFile(path)],
        subject: 'SmartBat Recording',
      ));
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Export failed: $e'),
            backgroundColor: const Color(0xFFFF5D5D)));
    }
  }

  Widget _buildLegend() {
    return Container(
      color: _kSurface,
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
      child: Wrap(spacing: 16, runSpacing: 4, children: [
        _legendItem('A Voltage', _kColorAVolt, _showAVolt, (v) => setState(() => _showAVolt = v)),
        _legendItem('B Voltage', _kColorBVolt, _showBVolt, (v) => setState(() => _showBVolt = v)),
        _legendItem('A Current', _kColorACurr, _showACurr, (v) => setState(() => _showACurr = v)),
        _legendItem('B Current', _kColorBCurr, _showBCurr, (v) => setState(() => _showBCurr = v)),
      ]),
    );
  }

  Widget _legendItem(String label, Color color, bool visible, ValueChanged<bool> onToggle) {
    return GestureDetector(
      onTap: () => onToggle(!visible),
      child: Row(mainAxisSize: MainAxisSize.min, children: [
        Container(
          width: 16, height: 3,
          color: visible ? color : color.withOpacity(0.25),
        ),
        const SizedBox(width: 6),
        Text(label, style: TextStyle(
          fontSize: 12,
          color: visible ? _kTextPrimary : _kTextSecondary.withOpacity(0.5),
        )),
      ]),
    );
  }

  Widget _buildChart() {
    final pts = _buffer.points;
    if (pts.isEmpty) {
      return const Center(
        child: Text('No data yet — connect a battery to see live chart',
            style: TextStyle(color: _kTextSecondary, fontSize: 13)),
      );
    }

    final minT = pts.first.tSec;
    final maxT = pts.last.tSec;
    final tRange = (maxT - minT).clamp(10.0, double.infinity);

    // Collect visible voltage and current ranges
    final voltValues = <double>[];
    final currValues = <double>[];
    for (final p in pts) {
      if (_showAVolt && p.aVolt != null) voltValues.add(p.aVolt!);
      if (_showBVolt && p.bVolt != null) voltValues.add(p.bVolt!);
      if (_showACurr && p.aCurr != null) currValues.add(p.aCurr!);
      if (_showBCurr && p.bCurr != null) currValues.add(p.bCurr!);
    }

    double voltMin = voltValues.isEmpty ? 10 : voltValues.reduce((a, b) => a < b ? a : b);
    double voltMax = voltValues.isEmpty ? 15 : voltValues.reduce((a, b) => a > b ? a : b);
    double currMin = currValues.isEmpty ? -10 : currValues.reduce((a, b) => a < b ? a : b);
    double currMax = currValues.isEmpty ? 10 : currValues.reduce((a, b) => a > b ? a : b);

    // Add padding
    final vPad = ((voltMax - voltMin) * 0.1).clamp(0.1, double.infinity);
    final cPad = ((currMax - currMin) * 0.1).clamp(0.5, double.infinity);
    voltMin -= vPad; voltMax += vPad;
    currMin -= cPad; currMax += cPad;

    // Nice axis interval (~5 ticks)
    double niceInterval(double rangeVal) {
      if (rangeVal <= 0) return 1.0;
      final raw = rangeVal / 5;
      final magnitude = pow(10, (log(raw) / ln10).floor()).toDouble();
      final n = raw / magnitude;
      return (n < 1.5 ? 1 : n < 3 ? 2 : n < 7 ? 5 : 10) * magnitude;
    }
    final vInterval = niceInterval(voltMax - voltMin);
    final tInterval = (tRange / 6).clamp(1.0, double.infinity);

    // Scale current to voltage axis for dual-axis simulation
    double scaleC(double c) {
      if (currMax == currMin) return (voltMin + voltMax) / 2;
      return voltMin + (c - currMin) / (currMax - currMin) * (voltMax - voltMin);
    }

    List<FlSpot> makeSpots(Iterable<double?> Function(ChartPoint) getter) =>
        pts.where((p) => getter(p).first != null)
           .map((p) => FlSpot(p.tSec - minT, getter(p).first!))
           .toList();

    List<FlSpot> makeCurrSpots(Iterable<double?> Function(ChartPoint) getter) =>
        pts.where((p) => getter(p).first != null)
           .map((p) => FlSpot(p.tSec - minT, scaleC(getter(p).first!)))
           .toList();

    final lines = <LineChartBarData>[
      if (_showAVolt) LineChartBarData(
        spots: makeSpots((p) => [p.aVolt]),
        color: _kColorAVolt, barWidth: 1.5,
        dotData: const FlDotData(show: false),
        isCurved: false,
      ),
      if (_showBVolt) LineChartBarData(
        spots: makeSpots((p) => [p.bVolt]),
        color: _kColorBVolt, barWidth: 1.5,
        dotData: const FlDotData(show: false),
        isCurved: false,
      ),
      if (_showACurr) LineChartBarData(
        spots: makeCurrSpots((p) => [p.aCurr]),
        color: _kColorACurr, barWidth: 1.5,
        dotData: const FlDotData(show: false),
        isCurved: false,
        dashArray: [4, 4],
      ),
      if (_showBCurr) LineChartBarData(
        spots: makeCurrSpots((p) => [p.bCurr]),
        color: _kColorBCurr, barWidth: 1.5,
        dotData: const FlDotData(show: false),
        isCurved: false,
        dashArray: [4, 4],
      ),
    ];

    return Padding(
      padding: const EdgeInsets.fromLTRB(8, 12, 16, 8),
      child: LineChart(
        LineChartData(
          minX: 0, maxX: tRange,
          minY: voltMin, maxY: voltMax,
          clipData: const FlClipData.all(),
          backgroundColor: _kBg,
          gridData: FlGridData(
            show: true,
            getDrawingHorizontalLine: (_) =>
                FlLine(color: _kSurfaceElev, strokeWidth: 1),
            getDrawingVerticalLine: (_) =>
                FlLine(color: _kSurfaceElev, strokeWidth: 1),
          ),
          borderData: FlBorderData(show: false),
          titlesData: FlTitlesData(
            leftTitles: AxisTitles(
              axisNameWidget: const Text('V', style: TextStyle(color: _kTextSecondary, fontSize: 10)),
              sideTitles: SideTitles(
                showTitles: true, reservedSize: 36,
                interval: vInterval,
                getTitlesWidget: (v, _) => Text(v.toStringAsFixed(1),
                    style: const TextStyle(color: _kTextSecondary, fontSize: 9)),
              ),
            ),
            rightTitles: AxisTitles(
              axisNameWidget: const Text('A', style: TextStyle(color: _kTextSecondary, fontSize: 10)),
              sideTitles: SideTitles(
                showTitles: true, reservedSize: 36,
                interval: vInterval,
                getTitlesWidget: (v, _) {
                  if (currMax == currMin) return const Text('');
                  final realA = currMin + (v - voltMin) / (voltMax - voltMin) * (currMax - currMin);
                  return Text(realA.toStringAsFixed(1),
                      style: const TextStyle(color: _kTextSecondary, fontSize: 9));
                },
              ),
            ),
            bottomTitles: AxisTitles(
              sideTitles: SideTitles(
                showTitles: true, reservedSize: 22,
                interval: tInterval,
                getTitlesWidget: (v, _) {
                  final s = v.round();
                  return Text(s >= 60 ? '${s ~/ 60}m' : '${s}s',
                      style: const TextStyle(color: _kTextSecondary, fontSize: 9));
                },
              ),
            ),
            topTitles: const AxisTitles(sideTitles: SideTitles(showTitles: false)),
          ),
          lineBarsData: lines,
          lineTouchData: LineTouchData(
            touchTooltipData: LineTouchTooltipData(
              getTooltipColor: (_) => _kSurfaceElev,
              getTooltipItems: (spots) => spots.map((s) {
                final colors = [_kColorAVolt, _kColorBVolt, _kColorACurr, _kColorBCurr];
                final i = s.barIndex.clamp(0, colors.length - 1);
                return LineTooltipItem(
                  s.y.toStringAsFixed(2),
                  TextStyle(color: colors[i], fontSize: 11),
                );
              }).toList(),
            ),
          ),
        ),
        duration: const Duration(milliseconds: 0),
      ),
    );
  }
}
