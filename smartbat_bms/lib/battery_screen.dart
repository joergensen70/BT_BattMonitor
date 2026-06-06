import 'dart:async';
import 'dart:math';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'battery_data.dart';
import 'bms_service.dart';

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

  @override
  void initState() {
    super.initState();
    _connect();
  }

  Future<void> _connect() async {
    _connSub = widget.device.connectionState.listen((state) {
      if (mounted && state == BluetoothConnectionState.disconnected) {
        setState(() => _status = 'Disconnected');
      }
    });

    try {
      await widget.service.connect(widget.device);
      if (mounted) setState(() => _status = 'Connected – waiting for data…');
      _dataSub = widget.service.dataStream.listen((data) {
        if (mounted) setState(() { _data = data; _status = ''; });
      });
    } catch (e) {
      if (mounted) setState(() => _status = 'Connection error: $e');
    }
  }

  Future<void> _disconnect() async {
    await widget.service.disconnect();
    if (mounted) Navigator.pop(context);
  }

  @override
  void dispose() {
    _dataSub?.cancel();
    _connSub?.cancel();
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
        backgroundColor: const Color(0xFF161B22),
        actions: [
          IconButton(
            icon: const Icon(Icons.bluetooth_disabled),
            tooltip: 'Disconnect',
            onPressed: _disconnect,
          ),
        ],
      ),
      body: _data == null ? _buildLoading() : _buildDashboard(_data!),
    );
  }

  Widget _buildLoading() {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          const CircularProgressIndicator(color: Color(0xFF00C853)),
          const SizedBox(height: 20),
          Text(_status, style: const TextStyle(color: Colors.white70, fontSize: 15)),
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

  Widget _buildSocGauge(BatteryData d) {
    final Color socColor = d.soc > 60
        ? const Color(0xFF00C853)
        : d.soc > 25
            ? Colors.orange
            : Colors.redAccent;

    return Card(
      color: const Color(0xFF161B22),
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
                          color: Colors.white60,
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
              style: const TextStyle(color: Colors.white54, fontSize: 13),
            ),
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
          '${d.voltage.toStringAsFixed(2)} V',
          Icons.flash_on,
          Colors.yellow,
        ),
        _statCard(
          'Current',
          '${d.current >= 0 ? '+' : ''}${d.current.toStringAsFixed(2)} A',
          Icons.compare_arrows,
          d.isCharging ? Colors.greenAccent : Colors.lightBlueAccent,
        ),
        _statCard(
          'Power',
          '${d.power.toStringAsFixed(1)} W',
          Icons.bolt,
          Colors.orange,
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
      color: const Color(0xFF161B22),
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
                  style: const TextStyle(fontSize: 12, color: Colors.white54),
                ),
              ],
            ),
            Text(
              value,
              style: const TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTempCard(BatteryData d) {
    return Card(
      color: const Color(0xFF161B22),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.thermostat, color: Colors.redAccent, size: 18),
                SizedBox(width: 6),
                Text(
                  'Temperature',
                  style: TextStyle(fontWeight: FontWeight.bold, fontSize: 15),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Wrap(
              spacing: 10,
              runSpacing: 6,
              children: d.temperatures.asMap().entries.map((e) {
                final t = e.value;
                final color = t > 45
                    ? Colors.red
                    : t > 35
                        ? Colors.orange
                        : Colors.greenAccent;
                return Chip(
                  backgroundColor: const Color(0xFF1F2937),
                  label: Text(
                    'NTC${e.key + 1}: ${t.toStringAsFixed(1)} °C',
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
      color: const Color(0xFF161B22),
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
                    Icon(Icons.grid_view, color: Color(0xFF00C853), size: 18),
                    SizedBox(width: 6),
                    Text(
                      'Cell Voltages',
                      style: TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 15),
                    ),
                  ],
                ),
                Text(
                  'Δ $deltaMs mV',
                  style: TextStyle(
                    fontSize: 12,
                    color: deltaMs > 50 ? Colors.orange : Colors.white54,
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
                    color: const Color(0xFF1F2937),
                    borderRadius: BorderRadius.circular(6),
                    border: Border.all(
                      color: isMin
                          ? Colors.redAccent
                          : isMax
                              ? Colors.greenAccent
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
                            fontSize: 10, color: Colors.white54),
                      ),
                      Text(
                        '${(v * 1000).round()}',
                        style: const TextStyle(
                            fontSize: 13, fontWeight: FontWeight.bold),
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 6),
            const Text(
              'mV per cell  •  green = max  •  red = min',
              style: TextStyle(fontSize: 11, color: Colors.white38),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildAlertsCard(BatteryData d) {
    return Card(
      color: const Color(0xFF3D0000),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Row(
              children: [
                Icon(Icons.warning_amber, color: Colors.redAccent, size: 20),
                SizedBox(width: 6),
                Text(
                  'Active Alerts',
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    fontSize: 15,
                    color: Colors.redAccent,
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
                    const Icon(Icons.circle, size: 6, color: Colors.redAccent),
                    const SizedBox(width: 8),
                    Text(p, style: const TextStyle(color: Colors.redAccent)),
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
