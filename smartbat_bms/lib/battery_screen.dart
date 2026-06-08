import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'battery_data.dart';
import 'bms_service.dart';

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

class BatteryScreen extends StatefulWidget {
  final BluetoothDevice device;
  final BmsService service;

  const BatteryScreen({
    super.key,
    required this.device,
    required this.service,
  });

  @override
  State<BatteryScreen> createState() => _BatteryScreenState();
}

class _BatteryScreenState extends State<BatteryScreen> {
  BatteryData? _data;
  String _status = 'Connecting…';
  StreamSubscription? _dataSub;
  StreamSubscription? _connSub;
  StreamSubscription<String>? _statusSub;
  StreamSubscription<String>? _logSub;
  final List<String> _logs = [];
  DateTime? _lastDataAt;
  bool _isRefreshing = false;

  bool get _hasReceivedData => _data != null || _lastDataAt != null;

  @override
  void initState() {
    super.initState();
    _statusSub = widget.service.statusStream.listen((status) {
      if (!mounted) return;
      setState(() {
        // After first data, do not fall back to generic connection states.
        if (_hasReceivedData) {
          final lower = status.toLowerCase();
          if (lower.contains('connecting') ||
              lower.contains('connected') ||
              lower.contains('discovering') ||
              lower.contains('waiting')) {
            return;
          }
        }
        _status = status;
      });
    });
    _logSub = widget.service.debugStream.listen((line) {
      if (!mounted) return;
      setState(() {
        _logs.insert(0, '${_timeLabel(DateTime.now())}  $line');
        if (_logs.length > 40) {
          _logs.removeRange(40, _logs.length);
        }

        // Show progress as soon as raw BLE frames arrive, even before parsing.
        if (!_hasReceivedData && line.startsWith('RX ')) {
          _status = 'Connected - receiving raw data…';
        }
      });
    });
    _connect();
  }

  Future<void> _connect() async {
    _connSub = widget.device.connectionState.listen((state) {
      if (mounted && state == BluetoothConnectionState.disconnected) {
        setState(() => _status = 'Disconnected');
      }
    });

    try {
      // Attach data listener before connect() so we do not miss the first snapshot.
      _dataSub = widget.service.dataStream.listen((data) {
        if (mounted) {
          setState(() {
            _data = data;
            _lastDataAt = DateTime.now();
            _status = 'Data received';
          });
        }
      });

      await widget.service.connect(widget.device);
      if (mounted && !_hasReceivedData) {
        setState(() => _status = 'Connected – waiting for data…');
      }
    } catch (e) {
      if (mounted) setState(() => _status = 'Connection error: $e');
    }
  }

  Future<void> _disconnect() async {
    await widget.service.disconnect();
    if (mounted) Navigator.pop(context);
  }

  Future<void> _refresh() async {
    if (_isRefreshing) return;
    setState(() => _isRefreshing = true);
    try {
      await widget.service.requestSnapshot();
    } finally {
      if (mounted) setState(() => _isRefreshing = false);
    }
  }

  String _timeLabel(DateTime value) {
    final hh = value.hour.toString().padLeft(2, '0');
    final mm = value.minute.toString().padLeft(2, '0');
    final ss = value.second.toString().padLeft(2, '0');
    return '$hh:$mm:$ss';
  }

  @override
  void dispose() {
    _dataSub?.cancel();
    _connSub?.cancel();
    _statusSub?.cancel();
    _logSub?.cancel();
    widget.service.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final name = widget.device.platformName.isNotEmpty
        ? widget.device.platformName
        : 'Battery';
    return Scaffold(
      appBar: AppBar(
        title: Text(name),
        backgroundColor: _kSurface,
        foregroundColor: _kTextPrimary,
        actions: [
          IconButton(
            icon: _isRefreshing
                ? const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  )
                : const Icon(Icons.refresh),
            tooltip: 'Request data',
            onPressed: _isRefreshing ? null : _refresh,
          ),
          IconButton(
            icon: const Icon(Icons.info_outline),
            tooltip: 'Connection info',
            onPressed: () => _showInfoSheet(context),
          ),
          IconButton(
            icon: const Icon(Icons.bluetooth_disabled),
            tooltip: 'Disconnect',
            onPressed: _disconnect,
          ),
        ],
      ),
      backgroundColor: _kBg,
      body: _data == null ? _buildLoading() : _buildDashboard(_data!),
    );
  }

  void _showInfoSheet(BuildContext context) {
    final deviceId = widget.device.remoteId.toString();
    final lastData = _lastDataAt == null ? 'No data yet' : _timeLabel(_lastDataAt!);
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: _kSurface,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
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
                width: 40, height: 4,
                margin: const EdgeInsets.only(bottom: 16),
                decoration: BoxDecoration(
                  color: _kTextSecondary.withOpacity(0.4),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
            Row(
              children: [
                const Icon(Icons.bluetooth_connected, color: _kAccent, size: 18),
                const SizedBox(width: 8),
                Text('Connection', style: TextStyle(
                  color: _kTextPrimary, fontWeight: FontWeight.bold, fontSize: 16)),
              ],
            ),
            const SizedBox(height: 14),
            _infoRow('Device', widget.device.platformName.isNotEmpty
                ? widget.device.platformName : '—'),
            _infoRow('MAC', deviceId),
            _infoRow('Status', _status),
            _infoRow('Last update', lastData),
            const SizedBox(height: 20),
            Row(
              children: [
                const Icon(Icons.receipt_long, color: _kInfo, size: 18),
                const SizedBox(width: 8),
                Text('BLE / Protocol Log', style: TextStyle(
                  color: _kTextPrimary, fontWeight: FontWeight.bold, fontSize: 16)),
              ],
            ),
            const SizedBox(height: 10),
            if (_logs.isEmpty)
              Text('No log entries yet.',
                  style: const TextStyle(color: _kTextSecondary, fontSize: 12))
            else
              ..._logs.map((line) => Padding(
                padding: const EdgeInsets.only(bottom: 5),
                child: Text(line,
                    style: const TextStyle(
                      color: _kTextSecondary,
                      fontSize: 11,
                      fontFamily: 'monospace',
                    )),
              )),
          ],
        ),
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(label,
                style: const TextStyle(color: _kTextSecondary, fontSize: 13)),
          ),
          Expanded(
            child: Text(value,
                style: const TextStyle(
                    color: _kTextPrimary, fontSize: 13,
                    fontFamily: 'monospace')),
          ),
        ],
      ),
    );
  }

  Widget _buildLoading() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          Card(
            color: _kSurface,
            child: Padding(
              padding: const EdgeInsets.all(18),
              child: Column(
                children: [
                  const CircularProgressIndicator(color: _kAccent),
                  const SizedBox(height: 20),
                  Text(_status, style: const TextStyle(color: _kTextSecondary, fontSize: 15)),
                  const SizedBox(height: 10),
                  Text(
                    widget.device.remoteId.toString(),
                    style: const TextStyle(color: _kTextSecondary, fontSize: 12),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDashboard(BatteryData d) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        children: [
          _buildSocGauge(d),
          const SizedBox(height: 12),
          _buildStatsGrid(d),
          if (d.temperatures.isNotEmpty) ...[
            const SizedBox(height: 12),
            _buildTempCard(d),
          ],
          if (d.cellVoltages.isNotEmpty) ...[
            const SizedBox(height: 12),
            _buildCellsCard(d),
          ],
          if (d.activeProtections.isNotEmpty) ...[
            const SizedBox(height: 12),
            _buildAlertsCard(d),
          ],
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  // ConnectionCard removed from dashboard — available via AppBar ⓘ button.

  // _buildLogCard removed from dashboard — shown inside the info bottomsheet.

  Widget _buildSocGauge(BatteryData d) {
    final Color socColor = d.soc > 60
        ? _kAccent
        : d.soc > 25
            ? _kWarning
            : _kCritical;

    // ATTE / ATTF display — 65535 is the BMS "no value" sentinel, ignore it.
    String? timeHint;
    if (d.isCharging && (d.attfMin ?? 0) > 0 && d.attfMin != 65535) {
      final h = d.attfMin! ~/ 60;
      final m = d.attfMin! % 60;
      timeHint = h > 0 ? '⚡ ${h}h ${m}min to full' : '⚡ ${m}min to full';
    } else if (d.isDischarging && (d.atteMin ?? 0) > 0 && d.atteMin != 65535) {
      final h = d.atteMin! ~/ 60;
      final m = d.atteMin! % 60;
      timeHint = h > 0 ? '⏱ ${h}h ${m}min to empty' : '⏱ ${m}min to empty';
    }

    return Card(
      color: _kSurface,
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            SizedBox(
              width: 190,
              height: 190,
              child: CustomPaint(
                painter: _GaugePainter(d.soc / 100.0, socColor),
                child: Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        '${d.soc}%',
                        style: TextStyle(
                          fontSize: 44,
                          fontWeight: FontWeight.bold,
                          color: socColor,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        d.isCharging
                            ? '⚡ Charging'
                            : d.isDischarging
                                ? '🔋 Discharging'
                                : '— Idle',
                        style: const TextStyle(
                          fontSize: 13,
                          color: _kTextSecondary,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '${d.remainingAh.toStringAsFixed(1)} Ah  /  ${d.nominalAh.toStringAsFixed(1)} Ah',
              style: const TextStyle(color: _kTextSecondary, fontSize: 13),
            ),
            if (timeHint != null) ...[              const SizedBox(height: 4),
              Text(timeHint,
                  style: const TextStyle(color: _kTextSecondary, fontSize: 12)),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildStatsGrid(BatteryData d) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 8,
      crossAxisSpacing: 8,
      childAspectRatio: 1.6,
      children: [
        _statCard(
          'Voltage',
          '${d.voltage.toStringAsFixed(3)} V',
          Icons.flash_on,
          Colors.yellow,
        ),
        _statCard(
          'Current',
          '${d.current >= 0 ? '+' : ''}${d.current.toStringAsFixed(3)} A',
          Icons.compare_arrows,
          d.isCharging ? _kAccent : _kInfo,
        ),
        _statCard(
          'Power',
          '${d.power.toStringAsFixed(1)} W',
          Icons.bolt,
          _kWarning,
        ),
        _statCard(
          'Cycles',
          '${d.cycles}',
          Icons.loop,
          Colors.purpleAccent,
        ),
      ],
    );
  }

  Widget _statCard(String label, String value, IconData icon, Color color) {
    return Card(
      color: _kSurface,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Icon(icon, size: 16, color: color),
                const SizedBox(width: 5),
                Text(
                  label,
                  style: const TextStyle(fontSize: 12, color: _kTextSecondary),
                ),
              ],
            ),
            Text(
              value,
              style: const TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
                color: _kTextPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTempCard(BatteryData d) {
    return Card(
      color: _kSurface,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.thermostat, color: _kCritical, size: 18),
                SizedBox(width: 6),
                Text(
                  'Temperature',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15,
                      color: _kTextPrimary),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 10,
              runSpacing: 6,
              children: d.temperatures.asMap().entries.map((e) {
                final degC = e.value;
                final degF = ((degC * 1.8 + 32.0) * 10).round() / 10.0;
                final color = degC > 45
                    ? _kCritical
                    : degC > 35
                        ? _kWarning
                        : _kAccent;
                return Chip(
                  backgroundColor: _kSurfaceElev,
                  label: Text(
                    'NTC${e.key + 1}: ${degC.toStringAsFixed(1)}°C / ${degF.toStringAsFixed(1)}°F',
                    style: TextStyle(color: color, fontSize: 13),
                  ),
                );
              }).toList(),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildCellsCard(BatteryData d) {
    final voltages = d.cellVoltages;
    final minV = voltages.reduce(min);
    final maxV = voltages.reduce(max);
    final deltaMs = ((maxV - minV) * 1000).round();

    return Card(
      color: _kSurface,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                const Row(
                  children: [
                    Icon(Icons.grid_view, color: _kAccent, size: 18),
                    SizedBox(width: 6),
                    Text(
                      'Cell Voltages',
                      style: TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15,
                          color: _kTextPrimary),
                    ),
                  ],
                ),
                Text(
                  'Δ $deltaMs mV',
                  style: TextStyle(
                    fontSize: 12,
                    color: deltaMs > 50 ? _kWarning : _kTextSecondary,
                    fontWeight: deltaMs > 50 ? FontWeight.bold : FontWeight.normal,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            GridView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                crossAxisCount: 4,
                childAspectRatio: 1.7,
                mainAxisSpacing: 6,
                crossAxisSpacing: 6,
              ),
              itemCount: voltages.length,
              itemBuilder: (_, i) {
                final v = voltages[i];
                final isMin = voltages.length > 1 && v == minV;
                final isMax = voltages.length > 1 && v == maxV;
                return Container(
                  decoration: BoxDecoration(
                    color: _kSurfaceElev,
                    borderRadius: BorderRadius.circular(6),
                    border: Border.all(
                      color: isMin
                          ? _kCritical
                          : isMax
                              ? _kAccent
                              : Colors.transparent,
                      width: 1.5,
                    ),
                  ),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        'C${i + 1}',
                        style: const TextStyle(
                            fontSize: 10, color: _kTextSecondary),
                      ),
                      Text(
                        '${(v * 1000).round()}',
                        style: const TextStyle(
                            fontSize: 13, fontWeight: FontWeight.bold,
                            color: _kTextPrimary),
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 6),
            const Text(
              'mV per cell  •  green = max  •  red = min',
              style: TextStyle(fontSize: 11, color: _kTextSecondary),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAlertsCard(BatteryData d) {
    return Card(
      color: const Color(0xFF2D0808),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.warning_amber, color: _kCritical, size: 20),
                SizedBox(width: 6),
                Text(
                  'Active Alerts',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                    color: _kCritical,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            ...d.activeProtections.map(
              (p) => Padding(
                padding: const EdgeInsets.only(bottom: 4),
                child: Row(
                  children: [
                    const Icon(Icons.circle, size: 6, color: _kCritical),
                    const SizedBox(width: 8),
                    Text(p, style: const TextStyle(color: _kCritical)),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
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
