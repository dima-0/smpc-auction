# Beschreibung

Das Projekt enthält eine prototypische Implementierung der Online-Auktion-App, welche im Rahmen der Bachelorarbeit _Realisierung von Secure Multiparty Computation (Android Plattform)_ konzipiert wurde.

# Voraussetzungen

* Alle Teilprojekte erfordern Java 8 oder höher.
* Ausführung des App-Clients erfordert ein Gerät mit Android 8 (API-Level 26) oder höher.
* Zur Ausführung und Konfiguration des Android-Emulators empfiehlt es sich, die neueste Version der IDE [Android Studio](https://developer.android.com/studio/) zu verwenden.

# Getting Started

Repository lokal klonen: `git clone https://github.com/dima-0/smpc-auction.git`

## Ausführung der JUnit-Tests (ohne IDE, mit cmd)

1. Teilprojekte bauen:
Das Bauen von auction-client erfordert die Datei **local.properties** (im Verzeichnis `auction-client/`), welche unter der Variable `sdk.dir` den Pfad der Android-SDK beinhaltet. Die Datei wird beim Öffnen des Projektes von Android Studio automatisch erzeugt. Es ist auch möglich, die Datei manuell zu erstellen und den Pfad der SDK zu spezifizieren.
```
cd smpc-auction\fresco-auction && gradlew --build-cache assemble
cd ..\auction-platform && gradlew --build-cache assemble
cd ..\auction-client && gradlew assembleDebug
```

2. JUnit-Tests ausführen (im jeweiligen Projektverzeichnis):
```
gradlew check
```
## Ausführung der Auktionsplattform (auction-platform)

Um eine ausführbare JAR-Datei der Auktionsplattform zu bauen, muss im Verzeichnis `auction-platform/` folgender Gradle-Befehl ausgeführt werden: 
```
gradlew createJarWithDependencies
```
Dieser erzeugt im Verzeichnis `auction-platform/build/libs` eine JAR-Datei. Diese lässt sich über die Kommandozeile auf folgende Weise ausführen: 
```
java -jar auction-platform-1.0-SNAPSHOT <config_file>
```
Als Parameter **<config_file>** muss der Pfad zur JSON-Konfigurationsdatei angegeben werden. Der Aufbau der Datei kann der Klasse `auctionplatform.worker.ServerConfiguration` entnommem werden.

## Ausführung der Online-Auktion-App (auction-client)

Der App-Client kann über Android-Studio sowohl auf dem Android-Emulator als auch auch auf einem physischen Android-Gerät (falls mit der Host-Maschine über USB verbunden) ausgeführt werden. 

Die App kann über die Datei **client_config.json** konfiguriert werden (Aufbau in Klasse `com.db.auctionclient.model.worker.ClientConfiguration`). Außerdem können in **auction.json** in Form eines Arrays die Auktionen (Aufbau in Klasse `com.db.auctionclient.model.entities.Auction`) spezifiziert werden, die beim Start der App in der lokalen Datenbank abgelegt werden. Die genannten Dateien müssen im Verzeichnis `/storage/emulated/0/Online-Auktion-App` platziert werden.
