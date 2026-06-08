# LionCheck / SmartBat Protokoll-Mapping Matrix

Stand: 2026-06-07

## Ziel
Dieses Dokument fasst den aktuellen Reverse-Engineering-Stand fuer das SmartBat/LionCheck BLE-Protokoll zusammen.
Es kombiniert:
- interne Capture-Evidenz aus diesem Workspace
- externe Protokollquellen (JBD/Xiaoxiang/JK)
- eine erste Mapping-Matrix fuer +RAA-Kommandos

## Kurzfazit
- Das beobachtete Verhalten passt teilweise zu JBD/Xiaoxiang (Datenmodell, typische Telemetrie-Felder).
- Die konkrete LionCheck-Befehlsschicht mit +RAAxxxx ist oeffentlich nicht dokumentiert.
- writeLlsAlertLevel + +RAAxxxx ist sehr wahrscheinlich eine proprietaere App-Schicht.

## Interne Evidenz (Workspace)

### 1) LionCheck aktiv und FFF4-Notify gesetzt
- com.gddai.lioncheck aktiv:
  - smartbat_bms/captures/capture_20260607_143350/logcat_threadtime.txt:53
- setCharacteristicNotification auf FFF4:
  - smartbat_bms/captures/capture_20260607_143350/logcat_threadtime.txt:57
  - smartbat_bms/captures/capture_20260607_143350/logcat_threadtime.txt:59

### 2) writeLlsAlertLevel und +RAA-Kommandos
- writeLlsAlertLevel Eintraege vorhanden (Session): 681
- Beispiele:
  - +RAA1002 bei dumpstate Zeile 142694
  - +RAA0A03 bei dumpstate Zeile 143158
  - +RAA0802 bei dumpstate Zeile 143309
  - +RAA0C02 bei dumpstate Zeile 143684
  - +RAA0403 bei dumpstate Zeile 143786
  - +RAA3C03 bei dumpstate Zeile 143881
- Datei:
  - smartbat_bms/captures/capture_20260607_143350/dumpstate-2026-06-07-14-35-33.txt

### 3) Beobachtete Verteilung +RAA (aus Extraktion)
- 0202: 78
- 0403: 43
- 0603: 41
- 0802: 79
- 0A03: 79
- 0C02: 42
- 1002: 78
- 1802: 41
- 1A02: 41
- 2802: 40
- 2C02: 37
- 3C03: 42
- 4802: 40

Interpretation:
- Es gibt ein stabiles, zyklisches Polling-Set.
- 0x2C erscheint nur im Steady-Betrieb und ist wahrscheinlich ein detailreicher Poll fuer laufende Anzeige.

## Externe Vergleichsquellen

### A) JBD/Xiaoxiang Standardprotokoll (gut dokumentiert)
Referenzen:
- https://blog.ja-ke.tech/2020/02/07/ltt-power-bms-chinese-protocol.html
- https://gitlab.com/bms-tools/bms-tools/-/blob/master/JBD_REGISTER_MAP.md
- https://github.com/syssi/esphome-jbd-bms
- https://github.com/sshoecraft/jbdtool/blob/main/jbd.c

Gemeinsame Kernaussagen:
- Frame: DD ... 77
- Read-Kommando: A5, Write-Kommando: 5A
- Typische Abfragen: Register 0x03 (Basic), 0x04 (Cell Voltages)
- Checksumme: 0x10000 minus Summenbytes (payloadbezogen)

### B) Xiaoxiang BLE Open Source Beispiel
Referenz:
- https://github.com/neilsheps/overkill-xiaoxiang-jbd-bms-ble-reader

Wichtige Indizien:
- BLE Service/Chars FF00/FF01/FF02 (anbieterabhaengige BLE-UART-Topologie)
- DD A5 03/04 Polling fuer Telemetrie

### C) JBD-UP / Eco-Worthy Variante
Referenz:
- https://gist.github.com/PhracturedBlue/7ef619594eaa4c27f4ff068b461865b8

Kernaussage:
- Neben klassischem JBD existieren Modbus-aehnliche Varianten (z. B. 0x78/0x79).
- Unterstreicht, dass herstellerspezifische Schichten ueber gleichem Datenmodell ueblich sind.

### D) LionCheck App Quelle
Referenz:
- https://play.google.com/store/apps/details?id=com.gddai.lioncheck&hl=en

Kernaussage:
- App bestaetigt BLE + kompatible Modelle/Protokolle, aber keine technische Kommando-Doku.

## Vergleich: Extern vs. SmartBat/LionCheck

### Uebereinstimmungen
- Ziel-Telemetrie (SOC, Spannung, Strom, Temperatur) ist identisch zur JBD/Xiaoxiang-Welt.
- Interne App-Parserlogik fuer Register 03/04 ist fachlich plausibel.

### Abweichungen
- +RAAxxxx und writeLlsAlertLevel sind in oeffentlichen JBD/Xiaoxiang/JK-Dokumenten nicht gefunden.
- Daraus folgt: sehr wahrscheinlich proprietaere LionCheck/SmartBat-Schicht ueber Vendor-Characteristic(s).

## Aktueller Codebezug (im Projekt)
- JBD-Kommandos DD A5 03/04 sind im Code vorhanden:
  - smartbat_bms/lib/bms_service.dart:14
- Lion-Bootstrap + Steady Kommandos sind abgebildet:
  - smartbat_bms/lib/bms_service.dart:22
  - smartbat_bms/lib/bms_service.dart:38
- FFF3 wird als bevorzugter TX-Kanal fuer Lion-Kommandos behandelt:
  - smartbat_bms/lib/bms_service.dart:538

## Mapping-Matrix (+RAA)

Hinweis: Feldzuordnung ist vorlaeufig und muss ueber kontrollierte Aenderungstests bestaetigt werden.

| Kommando | Beobachtung | Vermutete Funktion | Confidence |
|---|---|---|---|
| +RAA0202 | sehr haeufig, steady-loop | schneller Status/Heartbeat oder SOC-Block | Mittel |
| +RAA0A03 | sehr haeufig | Telemetrieblock (moeglich Strom/Leistung) | Niedrig-Mittel |
| +RAA0802 | sehr haeufig | Telemetrieblock (moeglich Spannung/SOC) | Niedrig-Mittel |
| +RAA1002 | sehr haeufig | Telemetrie-/Statusblock | Niedrig-Mittel |
| +RAA2C02 | steady-loop only | Detail-/Erweiterungsdaten im Live-Screen | Mittel |
| +RAA2802 | bootstrap + wiederkehrend | erweitertes Statusfeld | Niedrig |
| +RAA4802 | bootstrap + wiederkehrend | erweitertes Statusfeld | Niedrig |
| +RAA3C03 | bootstrap + periodisch | moeglicher Temperatur-/Alarmblock | Niedrig |
| +RAA0403 | bootstrap + periodisch | moeglicher Kapazitaets-/Counterblock | Niedrig |
| +RAA0603 | bootstrap + periodisch | moeglicher Temperatur-/Alarmblock | Niedrig |
| +RAA0C02 | bootstrap + periodisch | moeglicher FET/Protection-Block | Niedrig |
| +RAA1802 | bootstrap + periodisch | moeglicher Cell-Meta-Block | Niedrig |
| +RAA1A02 | bootstrap + periodisch | moeglicher Cell-Meta-Block | Niedrig |

## Was noch fehlt (Blocker)
- Es fehlt ein vollstaendiger, dekodierbarer Request/Response-Verlauf auf HCI-Ebene pro Characteristic.
- Ohne diesen Verlauf ist keine sichere Byte-zu-Feld Zuordnung moeglich.

## Konkreter naechster Schritt
1. Vollstaendigen BLE-HCI-Snoop einer erfolgreichen LionCheck-Session aufzeichnen.
2. Reihenfolge exakt replayen (gleiche Characteristic, gleiche Timing-Abstaende).
3. Pro +RAA-Kommando nur eine UI-Veraenderung provozieren und Bytes korrelieren.

## Testprotokoll-Vorlage (fuer kommende Session)
- Schritt 1: Nur +RAA0202 senden, 30 Zyklen loggen.
- Schritt 2: Nur +RAA0A03 senden, 30 Zyklen loggen.
- Schritt 3: Nur +RAA0802 senden, 30 Zyklen loggen.
- Schritt 4: Nur +RAA1002 senden, 30 Zyklen loggen.
- Schritt 5: Last/Charge gezielt aendern, dann Delta-Analyse der Payload-Bytes.

Erwartung:
- Ein Teil der Kommandos wird direkt mit Spannung/Strom/SOC korrelieren.
- Danach kann eine stabile Parser-Mapping-Tabelle aufgebaut werden.

## Session-Update (Abend 2026-06-07)

- Original-Referenzrunde `capture_20260607_151017` bestaetigt App-Muster:
  - ausschliesslich 8-Byte Writes
  - kurzer Initial-Burst, danach etwa 5 Writes pro Sekunde
- In `lib/bms_service.dart` wurde umgesetzt:
  - frueher Wechsel auf Lion-Modus bei erkannten SmartBat-native Frames
  - Burst/Steady Scheduler (60 ms, danach 200 ms)
  - Schutz gegen versehentlichen Rueckfall auf 1000 ms Poll-Intervall
- Verifikation `capture_20260607_152104`:
  - Scheduler-Fix greift (schnelle Lion-Startfolge sichtbar)
  - weiterhin keine decodierte Telemetrieframe-Ausgabe

### Startpunkt fuer morgen
1. 45-60s Capture mit aktueller App auf derselben Batterie aufnehmen.
2. RX-Frames char-/source-aware auswerten und Parser auf reales Lion-Responseformat anpassen.
