import 'dart:async';
import 'dart:math';
import 'battery_data.dart';

/// Realistic demo data generator for showcasing the app without BT hardware.
/// Generates smooth but varied battery telemetry with occasional events.
class DemoDataGenerator {
  DemoDataGenerator._();

  static final DemoDataGenerator instance = DemoDataGenerator._();

  // State for battery A
  double _aVoltage = 13.45;
  double _aCurrent = -6.5; // discharging
  double _aRemainingAh = 158.0;
  double _aNominalAh = 200.0;
  int _aCycles = 42;
  int _aSoc = 79;
  DateTime? _aLastChange;
  int? _aAttMinutes = 175; // ~2h 55min remaining

  // State for battery B
  double _bVoltage = 13.46;
  double _bCurrent = -7.1;
  double _bRemainingAh = 162.0;
  double _bNominalAh = 200.0;
  int _bCycles = 38;
  int _bSoc = 81;
  DateTime? _bLastChange;
  int? _bAttMinutes = 180;

  // Randomness
  final _rnd = Random();
  Timer? _timer;

  // Temperature state
  double _aTemp = 26.5;
  double _bTemp = 25.8;

  // Cell voltages (16 cells each)
  late List<double> _aCellVoltages;
  late List<double> _bCellVoltages;

  // Event scheduling
  int _tickCount = 0;
  bool _demoIsRunning = false;

  StreamController<BatteryData>? _aController;
  StreamController<BatteryData>? _bController;

  void _ensureControllers() {
    _aController ??= StreamController<BatteryData>.broadcast();
    _bController ??= StreamController<BatteryData>.broadcast();
  }

  Stream<BatteryData> get aStream {
    _ensureControllers();
    return _aController!.stream;
  }

  Stream<BatteryData> get bStream {
    _ensureControllers();
    return _bController!.stream;
  }

  bool get isRunning => _demoIsRunning;

  void start() {
    if (_demoIsRunning) return;
    _demoIsRunning = true;
    _ensureControllers();

    // Initialize cells around 3.35-3.42V with small random offsets
    _aCellVoltages = List.generate(16, (_) => 3.35 + _rnd.nextDouble() * 0.07);
    _bCellVoltages = List.generate(16, (_) => 3.35 + _rnd.nextDouble() * 0.07);

    _aLastChange = DateTime.now();
    _bLastChange = DateTime.now();

    // Immediate first emission
    _emitA();
    _emitB();

    // Then periodic updates every 1.5s for smooth but CPU-friendly demo
    _timer = Timer.periodic(const Duration(milliseconds: 1500), (_) => _tick());
  }

  void stop() {
    _demoIsRunning = false;
    _timer?.cancel();
    _timer = null;
    _aController?.close();
    _bController?.close();
    _aController = null;
    _bController = null;
  }

  void _tick() {
    _tickCount++;
    _updateA();
    _updateB();
    _emitA();
    _emitB();

    // Occasionally simulate transient alerts (rare, to avoid flashy UI)
    if (_tickCount % 40 == 0) {
      // Subtle cell delta spike
      if (_rnd.nextBool()) {
        final idx = _rnd.nextInt(16);
        _aCellVoltages[idx] = _aCellVoltages[idx] - 0.02;
      }
    }
    if (_tickCount % 55 == 0) {
      final idx = _rnd.nextInt(16);
      _bCellVoltages[idx] = _bCellVoltages[idx] + 0.015;
    }
  }

  void _updateA() {
    final now = DateTime.now();
    final dt = now.difference(_aLastChange!).inMilliseconds / 1000.0;
    _aLastChange = now;

    // Random walk with mean reversion
    _aVoltage += (_rnd.nextDouble() - 0.5) * 0.004 + (-0.0003) * dt;
    _aVoltage = _aVoltage.clamp(13.10, 13.70);

    // Current: mostly discharging
    _aCurrent += (_rnd.nextDouble() - 0.5) * 0.4;
    if (_rnd.nextDouble() < 0.005) {
      _aCurrent = _rnd.nextDouble() * 12.0; // rare charge surge
    } else {
      _aCurrent = _aCurrent.clamp(-15.0, 0.8);
    }

    // SoC and capacity based on current
    final consumedAh = _aCurrent * (dt / 3600.0);
    _aRemainingAh -= consumedAh;
    _aRemainingAh = _aRemainingAh.clamp(10.0, _aNominalAh);
    _aSoc = ((_aRemainingAh / _aNominalAh) * 100).round().clamp(0, 100);

    // Time estimate
    if (_aCurrent.abs() > 0.1) {
      final hoursLeft = _aRemainingAh / _aCurrent.abs();
      _aAttMinutes = (hoursLeft * 60).round().clamp(0, 9999);
    } else {
      _aAttMinutes = null;
    }

    // Temperature drift
    _aTemp += (_rnd.nextDouble() - 0.5) * 0.08;
    _aTemp = _aTemp.clamp(18.0, 45.0);

    // Slight cell drift
    for (var i = 0; i < _aCellVoltages.length; i++) {
      _aCellVoltages[i] += (_rnd.nextDouble() - 0.5) * 0.003;
      _aCellVoltages[i] = _aCellVoltages[i].clamp(3.0, 3.65);
    }
  }

  void _updateB() {
    final now = DateTime.now();
    final dt = now.difference(_bLastChange!).inMilliseconds / 1000.0;
    _bLastChange = DateTime.now();

    _bVoltage += (_rnd.nextDouble() - 0.5) * 0.004 + (-0.0002) * dt;
    _bVoltage = _bVoltage.clamp(13.10, 13.70);

    _bCurrent += (_rnd.nextDouble() - 0.5) * 0.5;
    if (_rnd.nextDouble() < 0.005) {
      _bCurrent = _rnd.nextDouble() * 12.0;
    } else {
      _bCurrent = _bCurrent.clamp(-15.0, 0.8);
    }

    final consumedAh = _bCurrent * (dt / 3600.0);
    _bRemainingAh -= consumedAh;
    _bRemainingAh = _bRemainingAh.clamp(10.0, _bNominalAh);
    _bSoc = ((_bRemainingAh / _bNominalAh) * 100).round().clamp(0, 100);

    if (_bCurrent.abs() > 0.1) {
      final hoursLeft = _bRemainingAh / _bCurrent.abs();
      _bAttMinutes = (hoursLeft * 60).round().clamp(0, 9999);
    } else {
      _bAttMinutes = null;
    }

    _bTemp += (_rnd.nextDouble() - 0.5) * 0.07;
    _bTemp = _bTemp.clamp(18.0, 45.0);

    for (var i = 0; i < _bCellVoltages.length; i++) {
      _bCellVoltages[i] += (_rnd.nextDouble() - 0.5) * 0.003;
      _bCellVoltages[i] = _bCellVoltages[i].clamp(3.0, 3.65);
    }
  }

  void _emitA() {
    if (!_demoIsRunning || _aController == null || _aController!.isClosed) return;
    final temps = _aTemp > 30 ? [_aTemp] : [_aTemp, _aTemp + _rnd.nextDouble()];
    _aController!.add(BatteryData(
      voltage: _aVoltage,
      current: _aCurrent,
      remainingAh: _aRemainingAh,
      nominalAh: _aNominalAh,
      cycles: _aCycles,
      soc: _aSoc,
      temperatures: temps.map((t) => double.parse(t.toStringAsFixed(1))).toList(),
      cellVoltages: _aCellVoltages.map((v) => double.parse(v.toStringAsFixed(3))).toList(),
      cellCount: 16,
      chargeFet: _aCurrent > 0.1,
      dischargeFet: _aCurrent <= 0.1,
      protectionStatus: 0,
      atteMin: _aAttMinutes,
      attfMin: null,
    ));
  }

  void _emitB() {
    if (!_demoIsRunning || _bController == null || _bController!.isClosed) return;
    final temps = _bTemp > 30 ? [_bTemp] : [_bTemp, _bTemp + _rnd.nextDouble() * 0.5];
    _bController!.add(BatteryData(
      voltage: _bVoltage,
      current: _bCurrent,
      remainingAh: _bRemainingAh,
      nominalAh: _bNominalAh,
      cycles: _bCycles,
      soc: _bSoc,
      temperatures: temps.map((t) => double.parse(t.toStringAsFixed(1))).toList(),
      cellVoltages: _bCellVoltages.map((v) => double.parse(v.toStringAsFixed(3))).toList(),
      cellCount: 16,
      chargeFet: _bCurrent > 0.1,
      dischargeFet: _bCurrent <= 0.1,
      protectionStatus: 0,
      atteMin: _bAttMinutes,
      attfMin: null,
    ));
  }
}