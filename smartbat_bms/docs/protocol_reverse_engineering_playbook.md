# Protokoll-Entschluesselung: Bewaehrte Methoden, Tools und Hilfsmittel

Stand: 2026-06-08

## Ziel
Dieses Dokument fasst bewaehrte, in der Praxis etablierte Vorgehensweisen zusammen, um unbekannte oder proprietaere Protokolle systematisch zu entschluesseln.
Der Fokus liegt auf BLE-/Mobile-Szenarien (relevant fuer SmartBat/LionCheck), ist aber auch allgemein nutzbar.

## Kernprinzipien

1. Mehrkanal-Capture statt Einzelsicht
- Immer mehrere Datenquellen parallel erfassen:
  - Host-Ebene: HCI/BTSnoop, System-Logs
  - App-Ebene: Runtime-Hooks
  - Optional Funk-Ebene: OTA-Sniffer
- Grund: Jede Quelle hat Blind Spots. Die Kombination liefert robuste Evidenz.

2. Erst Struktur, dann Bedeutung
- Reihenfolge:
  - Framing (Header, Laenge, Typ, Checksumme)
  - Endianness und Feldgrenzen
  - Semantik einzelner Felder
- Das verhindert fruehes Fehl-Mapping.

3. Kontrollierte Delta-Experimente
- Immer nur eine physische/fachliche Variable gleichzeitig aendern.
- Vorher/Nachher-Payloads vergleichen.
- So werden Feldbedeutungen belastbar statt geraten.

4. Formales Protokollmodell aufbauen
- Parser-Spezifikation frueh in ein reproduzierbares Format ueberfuehren (z. B. Kaitai oder eigener Parser).
- Vorteile: Testbarkeit, Teamfaehigkeit, geringere Regressionsgefahr.

5. Aktive Verifikation statt nur Beobachtung
- Kandidat-Felder mit gezielten Requests/Writes pruefen.
- Laengen-/Checksummenreaktionen und Sequenzverhalten verifizieren.

6. Abschluss mit Robustheitsanalyse
- Mutations-/Fuzz-Tests fuer Randfaelle (ungueltige Laengen, CRC, unbekannte Opcodes).
- Zeigt versteckte Zustandsautomaten und Fehlerbehandlung.

## Bewaehrter End-to-End-Workflow

## Phase 1: Datenerhebung
- Session reproduzierbar machen (gleicher Startzustand, gleiche Geraete).
- Parallel erfassen:
  - Android HCI Snoop Log
  - Logcat/Diagnoseausgaben
  - Optional OTA-Mitschnitt
  - Optional App-Runtime-Hooks

Ergebnis: Zeitlich korrelierbare Rohdaten ohne Interpretationsspruenge.

## Phase 2: Grobstruktur inferieren
- Pakete clustern nach:
  - Richtung
  - Laenge
  - Prefix/Opcode
  - Zyklusfrequenz
- Wiederkehrende Request/Response-Muster markieren.

Ergebnis: Erste Protokollgrammatik (ohne Feldsemantik).

## Phase 3: Feldgrenzen und Checks
- Kandidaten fuer:
  - Length-Field
  - Counter/Sequence
  - Checksumme/CRC
- Endianness-Hypothesen mit bekannten Testmustern pruefen.

Ergebnis: Stabiler Parser-Rahmen.

## Phase 4: Semantik-Mapping
- Gezielte Betriebsaenderungen (z. B. Last an/aus, Laden ein/aus, Temp-Delta).
- Payload-Differenzen auf Messwerte korrelieren.

Ergebnis: Feld-zu-Messwert-Mapping mit Confidence-Stufen.

## Phase 5: Aktive Validierung
- Einzelkommandos replayen.
- Timing variieren.
- Negativtests (ungueltige Felder/CRC) ausfuehren.

Ergebnis: Verifizierte Kommandosemantik und Timing-Anforderungen.

## Phase 6: Handover in Produktivcode
- Parser in App-Code uebernehmen.
- Golden-Sample-Tests aus Captures anlegen.
- Regressionstests fuer neue Firmwarestaende vorsehen.

Ergebnis: Wartbare, reproduzierbare Implementierung.

## Tool-Stack (Praxiserprobt)

### Paketanalyse und Capture
- Wireshark/TShark
  - Standard fuer zeitliche Korrelation, Filter, Dissektion, Export.
- Android HCI Snoop + btsnooz.py
  - Sehr stark fuer BLE auf Android-Hosts.
- btmon (BlueZ)
  - CLI-Monitoring und BTSnoop Read/Write unter Linux.

### Reverse Engineering (App/Binary)
- Frida
  - Dynamische Instrumentierung, Hooking von BLE- und Crypto-Pfaden.
- JADX
  - Java/Kotlin-Analyse von APK/DEX, schnelle Codepfad-Suche.
- Ghidra
  - Native Libraries, Decompilation, Graphing, Scripting.

### Protokoll-Inferenz und Modellierung
- Netzob
  - Message- und Zustandsinferenz, Traffic-Generierung, Fuzzing.
- Kaitai Struct
  - Formale, deklarative Spezifikation binaerer Formate/Frames.

### Aktive Tests/Fuzzing
- Scapy
  - Paketbau, Mutation, Replay.
- boofuzz
  - Struktur- und zustandsorientiertes Fuzzing.

### Optional: Funkebene
- nRF Sniffer
  - BLE-On-Air Sicht in Echtzeit (nuetzlich bei Timing-/Air-Issues).
- Ubertooth
  - Open-Source BLE-Sniffing-Hardware, insbesondere fuer passive Analysen.

## SmartBat/LionCheck: Konkrete Empfehlung

1. Primarkette
- Android HCI Snoop + Logcat + Wireshark als Hauptquelle.

2. Entscheidender Hebel bei proprietaeren Befehlen
- Frida-Hooks auf:
  - BLE writeCharacteristic-Aufrufe
  - String-/Byte-Transformation
  - relevante Crypto-APIs
- Ziel: Klartext vor und nach App-seitiger Verarbeitung sehen.

3. Replay-Matrix
- Kommandos einzeln senden (isolierte Zyklen).
- Nur eine Betriebsgroesse je Testlauf aendern.
- Payload-Delta gegen Messwerte mappen.

4. Parser-Hardening
- Feld- und Checksum-Validierung strikt machen.
- Unbekannte Opcodes protokollieren statt verwerfen.

## Typische Fehlerquellen
- Nur OTA sniffen und Host/App ignorieren.
- Semantik raten, bevor Framing stabil ist.
- Mehrere Betriebsvariablen gleichzeitig aendern.
- Keine Golden-Sample-Regressionen pflegen.
- Timingabhaengigkeiten beim Replay ignorieren.

## 2-Tage-Quickstart (operativ)

Tag 1
1. Reproduzierbare Session aufzeichnen (HCI, Logcat, optional OTA).
2. Request/Response-Cluster bilden.
3. Length/Checksum/Endianness-Hypothesen testen.

Tag 2
1. Frida-Hooks fuer BLE/Crypto-Pfad aktivieren.
2. Einzelkommando-Replays + Delta-Tests fahren.
3. Vorlaeufigen Parser erstellen und gegen Captures validieren.

## Referenzkategorien (extern)
- Wireshark Bluetooth und Capture-Setup
- Android Bluetooth Debugging/HCI Snoop
- Frida Dokumentation (Android + API)
- JADX/Ghidra Projekt- und Nutzungsdokumentation
- Netzob (Protocol RE, Inference, Fuzzing)
- Kaitai Struct User Guide
- Scapy und boofuzz Dokumentation
- BlueZ btmon Manpage
- OWASP MASTG (Reverse Engineering, Dynamic Analysis, Proxying, Tooling)
- Mitmproxy/Charles Dokumentation (fuer HTTP/HTTPS-seitige Nebenkanaele)
