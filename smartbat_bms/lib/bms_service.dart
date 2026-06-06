import 'dart:async';
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

  BluetoothDevice? _device;
  BluetoothCharacteristic? _writeChar;
  BluetoothCharacteristic? _notifyChar;

  final _dataController = StreamController<BatteryData>.broadcast();
  final _buf = <int>[];
  Timer? _pollTimer;
  StreamSubscription<List<int>>? _notifySub;
  BatteryData? _lastBasic;

  Stream<BatteryData> get dataStream => _dataController.stream;

  Future<void> connect(BluetoothDevice device) async {
    _device = device;
    await device.connect(autoConnect: false);

    final services = await device.discoverServices();
    BluetoothService? svc;
    for (final s in services) {
      if (s.serviceUuid.toString().toLowerCase() == _svcUuid) {
        svc = s;
        break;
      }
    }
    if (svc == null) throw Exception('BMS service (0xFFF0) not found');

    BluetoothCharacteristic? c1, c2;
    for (final c in svc.characteristics) {
      final id = c.characteristicUuid.toString().toLowerCase();
      if (id == _fff1Uuid) c1 = c;
      if (id == _fff2Uuid) c2 = c;
    }

    // Determine write vs. notify char by their declared properties.
    // Observed: fff1=Read/Write, fff2=Read/Notify (most JBD-style)
    // Fallback: fff1=Notify, fff2=Write (some DALY variants)
    if (c2 != null && c2.properties.notify) {
      _notifyChar = c2;
      _writeChar  = c1;
    } else if (c1 != null && c1.properties.notify) {
      _notifyChar = c1;
      _writeChar  = c2;
    } else {
      // Last resort: assign by UUID order
      _writeChar  = c1;
      _notifyChar = c2;
    }

    if (_notifyChar != null) {
      await _notifyChar!.setNotifyValue(true);
      _notifySub = _notifyChar!.onValueReceived.listen(_onData);
    }

    _poll();
    _pollTimer = Timer.periodic(const Duration(seconds: 5), (_) => _poll());
  }

  void _onData(List<int> chunk) {
    _buf.addAll(chunk);
    _drain();
  }

  void _drain() {
    while (_buf.length >= 7) {
      final start = _buf.indexOf(0xDD);
      if (start < 0) { _buf.clear(); return; }
      if (start > 0)  _buf.removeRange(0, start);
      if (_buf.length < 4) return;

      final reg    = _buf[1];
      final status = _buf[2];
      final len    = _buf[3];
      final total  = 4 + len + 3; // header(4) + data(len) + checksum(2) + end(1)
      if (_buf.length < total) return;
      if (_buf[total - 1] != 0x77) { _buf.removeAt(0); continue; }

      if (status == 0x00) {
        final data = _buf.sublist(4, 4 + len);
        if (reg == 0x03) _parseBasic(data);
        if (reg == 0x04) _parseCells(data);
      }
      _buf.removeRange(0, total);
    }
  }

  void _parseBasic(List<int> d) {
    if (d.length < 23) return;
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
    _emit(_lastBasic!);
  }

  void _parseCells(List<int> d) {
    final volts = [
      for (int i = 0; i + 1 < d.length; i += 2) _u16(d, i) / 1000.0,
    ];
    if (_lastBasic != null) {
      _lastBasic = _lastBasic!.copyWith(cellVoltages: volts);
      _emit(_lastBasic!);
    }
  }

  void _emit(BatteryData data) {
    if (!_dataController.isClosed) _dataController.add(data);
  }

  Future<void> _poll() async {
    if (_writeChar == null) return;
    try {
      final useResponse = _writeChar!.properties.write;
      await _writeChar!.write(_cmdBasicInfo,    withoutResponse: !useResponse);
      await Future.delayed(const Duration(milliseconds: 300));
      await _writeChar!.write(_cmdCellVoltages, withoutResponse: !useResponse);
    } catch (_) {
      // Silently ignore poll errors (device may be temporarily busy)
    }
  }

  int _u16(List<int> d, int i) => ((d[i] & 0xFF) << 8) | (d[i + 1] & 0xFF);
  int _s16(List<int> d, int i) {
    final v = _u16(d, i);
    return v >= 0x8000 ? v - 0x10000 : v;
  }

  Future<void> disconnect() async {
    _pollTimer?.cancel();
    _notifySub?.cancel();
    _pollTimer = null;
    _notifySub = null;
    try { await _device?.disconnect(); } catch (_) {}
    _writeChar = _notifyChar = _device = _lastBasic = null;
    _buf.clear();
  }

  void dispose() {
    disconnect();
    _dataController.close();
  }
}
