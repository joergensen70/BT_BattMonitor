# SmartBat Protocol Gap And Execution Plan

Stand: 2026-06-08

## Ziel
Schnellster Weg zu belastbarer Protokollentschluesselung ohne Raten.

## Was noch fehlt

1. Ground truth aus der Original-App
- Uns fehlt der nachweisbare Klartextpfad vor und nach App-seitiger Transformation.
- Ohne diesen Schritt bleiben +RAA-Interpretationen indirekt.

2. Char-/Source-aware RX-Korrelation
- Aktuell sehen wir TX-/Echo-/Heartbeat-Muster, aber keine stabile Zuordnung pro Characteristic und Event-Typ.
- Wir brauchen je Frame: Quelle (notify/read), Characteristic, Richtung, Zeitdelta zum letzten TX.

3. Reproduzierbare Delta-Matrix
- Es fehlen standardisierte Einzelkommando-Laeufe mit genau einer physischen Aenderung je Run.
- Ohne diese Matrix bleibt Feldsemantik unsicher.

4. Formale Parser-Gates
- Es fehlen harte Akzeptanzkriterien fuer Parser-Fortschritt.
- Noetig: Golden Samples + Must-Pass Checks.

## Schnellster Weg (empfohlen)

1. Original-App instrumentieren (Frida)
- Hook BLE write/read/notify inkl. Bytearrays, UUIDs, Timestamp.
- Hook Crypto-/Transformationsfunktionen (Cipher, MessageDigest, Mac, Base64), um eventuelle Vor-/Nachbearbeitung zu sehen.

2. Parallel-Capture fahren
- Gleichzeitig HCI-Snoop + Logcat + Frida-Log.
- Ziel: lueckenlose Korrelation zwischen App-Aufruf und BLE-Frame.

3. 60-Minuten Delta-Matrix
- 5 isolierte Kommandos aus Steady-Set einzeln fahren (z. B. 0202, 0A03, 0802, 1002, 2C02).
- Pro Lauf nur eine physische Variable aendern (Last an/aus, Laden an/aus, Temperaturanstieg simuliert).

4. Parser nur mit Evidenz erweitern
- Feldmapping nur ueber dokumentierte Delta-Beweise.
- Unbekannte Frames protokollieren, niemals still verwerfen.

## Abbruchkriterien fuer Guessing

- Kein Feldmapping ohne:
  - mindestens 3 reproduzierte Delta-Treffer,
  - gleiche Richtung/Quelle,
  - konsistente Endianness ueber mehrere Runs.

## Done-Definition

- Mindestens 3 Kernfelder (Spannung, Strom, SOC) mit Confidence Hoch.
- Parser besteht Golden-Sample-Tests fuer mindestens 3 Captures.
- Replay erzeugt erwartete Antworttypen bei stabiler Timing-Strategie.

## Morgen als erstes

1. Frida-Hook starten (Script unter tools/frida).
2. 45-60s Referenzsession aufnehmen mit Original-App.
3. Direkt danach eine 45-60s Session mit unserer App.
4. Delta-Tabelle fuellen und nur daraus Parser-Updates ableiten.

## Aktueller Ausfuehrungsstatus

- Hook-Skript bereit: tools/frida/lioncheck_ble_crypto_hook.js
- Frida-Runbook bereit: tools/frida/README.md
- Host-Check am 2026-06-08: Frida CLI installiert und funktionsfaehig.
- Device-Check am 2026-06-08: USB-Device erreichbar, aber Original-App ist auf jailed Android nicht direkt attachbar.
- Frida-Fehlerbild:
  - Spawn: verlangt Gadget auf Host (nachgeruestet)
  - Attach by PID: "unable to connect to remote frida-server: closed"

## Harte Evidenz ohne Guessing (bereits extrahiert)

- Statischer String-Scan aus `analysis/lioncheck_apk/base.apk` bestaetigt:
  - komplette +RAA-Befehlsfamilie (`+RAA0202`, `+RAA0A03`, `+RAA0802`, `+RAA1002`, `+RAA2C02`, `+RAA0C02`, `+RAA0403`, `+RAA3C03`, `+RAA0603`, `+RAA1802`, `+RAA1A02`, `+RAA2802`, `+RAA4802`)
  - Crypto-/Integrity-Indizien: `AES`, `Checksum`, `CRC`, `MessageDigest`, `Mac`
  - BLE-Indizien: `writeCharacteristic`, `BluetoothGatt`, `FFF1/FFF4/FFF5/FFF6/FFF8`
- Schlussfolgerung: +RAA und Integritaetslogik sind real in der Original-App vorhanden, nicht nur Capture-Artefakte.

## Sofortbefehle fuer den Start (morgen)

1. Parallel Capture starten:
  - powershell -File tools/start_smartbat_capture.ps1
2. 45-60s reproduzierbarer Lauf.
3. Capture stoppen:
  - powershell -File tools/stop_smartbat_capture.ps1
4. Danach Parser nur auf evidenzbasierte Deltas erweitern.

## Wenn Live-Hooking auf Original-App noetig ist

1. Option A: Rooted Testgeraet mit stabil laufendem frida-server.
2. Option B: Gepatchte LionCheck-Testkopie mit Frida Gadget (nicht Production-App).
3. Ohne A/B: weiter mit statischer Analyse + kontrollierten Delta-Captures.
