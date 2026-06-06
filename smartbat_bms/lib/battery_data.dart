class BatteryData {
  final double voltage;
  final double current;
  final double remainingAh;
  final double nominalAh;
  final int cycles;
  final int soc;
  final List<double> temperatures;
  final List<double> cellVoltages;
  final int cellCount;
  final bool chargeFet;
  final bool dischargeFet;
  final int protectionStatus;

  const BatteryData({
    required this.voltage,
    required this.current,
    required this.remainingAh,
    required this.nominalAh,
    required this.cycles,
    required this.soc,
    required this.temperatures,
    required this.cellVoltages,
    required this.cellCount,
    required this.chargeFet,
    required this.dischargeFet,
    required this.protectionStatus,
  });

  double get power => voltage * current.abs();
  bool get isCharging => current > 0.1;
  bool get isDischarging => current < -0.1;

  BatteryData copyWith({List<double>? cellVoltages}) {
    return BatteryData(
      voltage: voltage,
      current: current,
      remainingAh: remainingAh,
      nominalAh: nominalAh,
      cycles: cycles,
      soc: soc,
      temperatures: temperatures,
      cellVoltages: cellVoltages ?? this.cellVoltages,
      cellCount: cellCount,
      chargeFet: chargeFet,
      dischargeFet: dischargeFet,
      protectionStatus: protectionStatus,
    );
  }

  static const Map<int, String> _protectionBits = {
    0: 'Cell Overvoltage',
    1: 'Cell Undervoltage',
    2: 'Pack Overvoltage',
    3: 'Pack Undervoltage',
    4: 'Charge Overtemp',
    5: 'Discharge Overtemp',
    6: 'Charge Overcurrent',
    7: 'Discharge Overcurrent',
    8: 'Short Circuit',
    9: 'IC Error',
    10: 'Mosfet Lock',
  };

  List<String> get activeProtections {
    final result = <String>[];
    for (final entry in _protectionBits.entries) {
      if ((protectionStatus >> entry.key) & 1 == 1) {
        result.add(entry.value);
      }
    }
    return result;
  }
}
