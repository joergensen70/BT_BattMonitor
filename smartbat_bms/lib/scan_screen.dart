import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter_blue_plus/flutter_blue_plus.dart';
import 'package:permission_handler/permission_handler.dart';
import 'battery_screen.dart';
import 'bms_service.dart';

class ScanScreen extends StatefulWidget {
  const ScanScreen({super.key});

  @override
  State<ScanScreen> createState() => _ScanScreenState();
}

class _ScanScreenState extends State<ScanScreen> {
  final List<ScanResult> _results = [];
  bool _isScanning = false;
  String? _error;
  StreamSubscription? _scanSub;
  StreamSubscription? _stateSub;

  @override
  void initState() {
    super.initState();
    _stateSub = FlutterBluePlus.adapterState.listen((_) {
      if (mounted) setState(() {});
    });
  }

  @override
  void dispose() {
    _scanSub?.cancel();
    _stateSub?.cancel();
    FlutterBluePlus.stopScan();
    super.dispose();
  }

  Future<void> _requestPermissions() async {
    await [
      Permission.bluetoothScan,
      Permission.bluetoothConnect,
      Permission.location,
    ].request();
  }

  Future<void> _startScan() async {
    final adapterState = await FlutterBluePlus.adapterState.first;
    if (adapterState != BluetoothAdapterState.on) {
      setState(() {
        _error = 'Bluetooth ist nicht eingeschaltet.';
      });
      return;
    }

    await _requestPermissions();
    setState(() {
      _error = null;
      _results.clear();
      _isScanning = true;
    });

    _scanSub?.cancel();
    _scanSub = FlutterBluePlus.scanResults.listen((results) {
      if (!mounted) return;
      setState(() {
        for (final r in results) {
          final idx = _results.indexWhere(
            (e) => e.device.remoteId == r.device.remoteId,
          );
          if (idx < 0) {
            _results.add(r);
          } else {
            _results[idx] = r;
          }
        }
        _results.sort((a, b) => b.rssi.compareTo(a.rssi));
      });
    });

    try {
      await FlutterBluePlus.startScan(timeout: const Duration(seconds: 15));
      await FlutterBluePlus.isScanning.where((s) => !s).first;
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = 'Scan fehlgeschlagen: $e';
        });
      }
    } finally {
      if (mounted) setState(() => _isScanning = false);
    }
  }

  Future<void> _connect(BluetoothDevice device) async {
    await FlutterBluePlus.stopScan();
    if (!mounted) return;
    Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => BatteryScreen(device: device, service: BmsService()),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Row(
          children: [
            Icon(Icons.battery_charging_full, color: Color(0xFF00C853)),
            SizedBox(width: 8),
            Text('SmartBat BMS'),
          ],
        ),
        backgroundColor: const Color(0xFF161B22),
      ),
      body: Column(
        children: [
          _buildStatusBanner(),
          _buildScanButton(),
          Expanded(child: _buildDeviceList()),
        ],
      ),
    );
  }

  Widget _buildStatusBanner() {
    return StreamBuilder<BluetoothAdapterState>(
      stream: FlutterBluePlus.adapterState,
      initialData: FlutterBluePlus.adapterStateNow,
      builder: (context, snapshot) {
        final state = snapshot.data ?? BluetoothAdapterState.unknown;
        final isOn = state == BluetoothAdapterState.on;
        final text = _error ?? (isOn
            ? 'Bereit: Scan starten, Geraet waehlen, dann Verbindungstest.'
            : 'Bluetooth ist derzeit nicht aktiv.');
        return Container(
          width: double.infinity,
          margin: const EdgeInsets.fromLTRB(16, 16, 16, 0),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: _error != null
                ? const Color(0xFF3D0000)
                : isOn
                    ? const Color(0xFF10261A)
                    : const Color(0xFF3A2A00),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Icon(
                _error != null
                    ? Icons.error_outline
                    : isOn
                        ? Icons.bluetooth_searching
                        : Icons.bluetooth_disabled,
                color: _error != null
                    ? Colors.redAccent
                    : isOn
                        ? const Color(0xFF00C853)
                        : Colors.orangeAccent,
              ),
              const SizedBox(width: 10),
              Expanded(
                child: Text(
                  text,
                  style: const TextStyle(color: Colors.white70, fontSize: 13),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildScanButton() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: ElevatedButton.icon(
        onPressed: _isScanning ? null : _startScan,
        icon: _isScanning
            ? const SizedBox(
                width: 18,
                height: 18,
                child: CircularProgressIndicator(strokeWidth: 2),
              )
            : const Icon(Icons.search),
        label: Text(_isScanning ? 'Scanning…' : 'Scan for Batteries'),
        style: ElevatedButton.styleFrom(
          minimumSize: const Size.fromHeight(48),
          backgroundColor: const Color(0xFF00C853),
          foregroundColor: Colors.black,
          textStyle: const TextStyle(
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
      ),
    );
  }

  Widget _buildDeviceList() {
    if (_results.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Icon(Icons.bluetooth_searching, size: 72, color: Colors.white24),
            const SizedBox(height: 16),
            Text(
              _isScanning ? 'Suche nach BLE-Geraeten…' : 'Mit Scan suchst du nach deinem BMS oder Testgeraet',
              style: const TextStyle(color: Colors.white54, fontSize: 15),
            ),
          ],
        ),
      );
    }
    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      itemCount: _results.length,
      itemBuilder: (_, i) => _buildDeviceTile(_results[i]),
    );
  }

  Widget _buildDeviceTile(ScanResult result) {
    final name = result.device.platformName.isNotEmpty
        ? result.device.platformName
        : result.advertisementData.advName.isNotEmpty
            ? result.advertisementData.advName
            : result.device.remoteId.toString();
    final rssi = result.rssi;

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      color: const Color(0xFF161B22),
      child: ListTile(
        leading: const CircleAvatar(
          backgroundColor: Color(0xFF1F2937),
          child: Icon(Icons.battery_full, color: Color(0xFF00C853)),
        ),
        title: Text(name, style: const TextStyle(fontWeight: FontWeight.bold)),
        subtitle: Text(
          result.device.remoteId.toString(),
          style: const TextStyle(fontSize: 11, color: Colors.white54),
        ),
        trailing: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Icon(
              rssi > -60
                  ? Icons.signal_wifi_4_bar
                  : rssi > -80
                      ? Icons.network_wifi_3_bar
                      : Icons.network_wifi_1_bar,
              size: 16,
              color: rssi > -60
                  ? Colors.green
                  : rssi > -80
                      ? Colors.orange
                      : Colors.red,
            ),
            Text(
              '$rssi dBm',
              style: const TextStyle(fontSize: 11, color: Colors.white54),
            ),
          ],
        ),
        onTap: () => _connect(result.device),
      ),
    );
  }
}
