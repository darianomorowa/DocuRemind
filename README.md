# DocuRemind

DocuRemind ist eine native Android App zur Verwaltung wichtiger Dokumente, Verträge und Fristen.

Nutzer können Dokumentinformationen erfassen, Ablaufdaten verwalten und sich rechtzeitig vor dem Ablauf eines Dokuments über Android Benachrichtigungen erinnern lassen.

Das Projekt wurde im Rahmen des Moduls Android App Development an der HWR Berlin umgesetzt.

## Funktionen

DocuRemind bietet aktuell folgende Funktionen:

* Registrierung mit E Mail Adresse und Passwort
* Login über Firebase Authentication
* Automatische Wiedererkennung einer bestehenden Anmeldung
* Dokumente anlegen
* Dokumente anzeigen
* Dokumente bearbeiten
* Dokumente löschen
* Speicherung von Dokumentname, Kategorie, Ablaufdatum und optionaler Notiz
* Nutzerbezogene Speicherung der Dokumente in Cloud Firestore
* Sortierung der Dokumente nach Ablaufdatum
* Berechnung der verbleibenden Tage bis zum Ablauf
* Farblich dargestellter Fristenstatus
* Konfigurierbare Erinnerungen
* Erinnerungszeitpunkte von 30, 60 oder 90 Tagen
* Regelmäßige Hintergrundprüfung über Android WorkManager
* Lokale Android Benachrichtigungen bei relevanten Fristen
* Export eines Dokuments beziehungsweise seiner Frist an eine installierte Kalender App
* Einstellungen werden lokal über SharedPreferences gespeichert

## Technologiestack

Die Anwendung wurde nativ für Android entwickelt.

| Bereich | Technologie |
|---|---|
| Programmiersprache | Java |
| Benutzeroberfläche | XML Views |
| UI Komponenten | Material Components |
| Benutzerkonten | Firebase Authentication |
| Cloud Datenbank | Cloud Firestore |
| Lokale Einstellungen | SharedPreferences |
| Hintergrundaufgaben | Android WorkManager |
| Benachrichtigungen | Android Notification API |
| Kalenderintegration | Intent / CalendarContract |
| Java Version | Java 11 |
| Min SDK | 24 |
| Target SDK | 36 |
| App Version | 1.0 |

Package beziehungsweise Namespace:

```text
de.hwr.docuremind
```

## Architektur

DocuRemind trennt die Benutzeroberfläche, Anwendungslogik, Datenhaltung und Android Hintergrundfunktionen.

### Activities

#### MainActivity

Verantwortlich für:

* Login
* Registrierung
* bestehende Firebase Sitzung
* Navigation zum Dashboard

#### DashboardActivity

Verantwortlich für:

* Laden der Dokumente aus Firestore
* Sortierung nach Ablaufdatum
* Darstellung der Dokumentkarten
* Fristenstatus
* Navigation zu Dokumenten, Einstellungen und neuen Einträgen

#### AddDocumentActivity

Verantwortlich für:

* Anlegen neuer Dokumente
* Bearbeiten bestehender Dokumente
* Eingabevalidierung
* Auswahl eines Ablaufdatums
* Speicherung in Firestore

#### DetailActivity

Verantwortlich für:

* Anzeige eines Dokuments
* Bearbeiten
* Löschen
* Übergabe der Frist an eine Kalender App

#### SettingsActivity

Verantwortlich für:

* Aktivieren und Deaktivieren von Erinnerungen
* Auswahl des Erinnerungszeitpunkts
* Speicherung der Einstellungen

## Hilfsklassen

### DocumentDateUtils

Enthält die zentrale Datumslogik.

Die Klasse übernimmt unter anderem:

* Formatierung von Datumswerten
* Umwandlung von Datumswerten
* Berechnung verbleibender Tage
* Bestimmung des Fristenstatus
* Zuordnung der Statusdarstellung

### ReminderPreferences

Kapselt den Zugriff auf SharedPreferences.

Gespeichert werden unter anderem:

```text
notificationsEnabled
reminderDays
```

### ReminderScheduler

Plant beziehungsweise beendet die regelmäßige Hintergrundprüfung über WorkManager.

### ReminderWorker

Wird durch WorkManager im Hintergrund ausgeführt.

Der Worker:

1. prüft, ob Erinnerungen aktiviert sind
2. ermittelt den aktuell angemeldeten Firebase Nutzer
3. lädt dessen Dokumente aus Firestore
4. berechnet die verbleibenden Tage
5. überprüft die Erinnerungsschwellen
6. löst bei Bedarf eine Android Benachrichtigung aus

### NotificationHelper

Übernimmt:

* Erstellung des Notification Channels
* Prüfung der Benachrichtigungsberechtigung
* Darstellung einer Dokumenterinnerung

## Datenhaltung

DocuRemind verwendet drei unterschiedliche Speicherbereiche.

### Firebase Authentication

Firebase Authentication verwaltet:

* Benutzerkonten
* Anmeldung
* aktuelle Sitzung
* eindeutige User ID

Die User ID dient gleichzeitig zur Trennung der Dokumentdaten verschiedener Nutzer.

### Cloud Firestore

Dokumente werden nutzerbezogen gespeichert.

Struktur:

```text
users
└── {userId}
    └── documents
        └── {documentId}
            ├── name
            ├── category
            ├── expiryDate
            ├── expiryDateMillis
            ├── note
            ├── createdAt
            └── updatedAt
```

Beispielpfad:

```text
users/{uid}/documents/{documentId}
```

`expiryDate` enthält das Ablaufdatum als lesbaren Text.

`expiryDateMillis` enthält dasselbe Datum als Millisekundenwert und wird für Sortierung, Berechnungen, Erinnerungen und die Kalenderintegration verwendet.

### SharedPreferences

Die Erinnerungseinstellungen werden lokal auf dem Android Gerät gespeichert.

Dazu gehören insbesondere:

```text
notificationsEnabled
reminderDays
```

## Typischer Ablauf

Ein typischer Nutzerfluss sieht folgendermaßen aus:

```text
App starten
↓
Login oder bestehende Sitzung
↓
Dashboard
↓
Dokument anlegen
↓
Eingaben validieren
↓
Firebase Nutzer bestimmen
↓
Dokument in Firestore speichern
↓
Dashboard lädt Dokumente
↓
Ablaufdatum und Status berechnen
↓
Dokument anzeigen
```

Der Erinnerungsablauf erfolgt unabhängig davon im Hintergrund:

```text
Erinnerungen aktivieren
↓
Einstellungen in SharedPreferences speichern
↓
WorkManager plant Hintergrundprüfung
↓
ReminderWorker wird ausgeführt
↓
Dokumente aus Firestore laden
↓
Resttage berechnen
↓
Erinnerungsschwelle prüfen
↓
Android Notification anzeigen
```

## Benachrichtigungen

DocuRemind verwendet lokale Android Benachrichtigungen.

Ab Android 13 wird zusätzlich die Systemberechtigung für Benachrichtigungen benötigt.

WorkManager führt die Fristenprüfung regelmäßig im Hintergrund durch. Die Ausführung erfolgt dabei nicht sekundengenau, da Android den tatsächlichen Ausführungszeitpunkt abhängig von Systemressourcen, Netzwerk und Energieverwaltung bestimmen kann.

## Kalenderintegration

In der Detailansicht kann die Frist eines Dokuments an eine Kalender App übergeben werden.

DocuRemind erstellt den Kalendereintrag nicht selbst.

Stattdessen wird über einen Android Intent eine installierte Kalender App mit vorausgefüllten Informationen geöffnet. Der Nutzer kann den Termin dort anschließend bestätigen und speichern.

## Projekt erstellen

Voraussetzung ist eine funktionsfähige Android Entwicklungsumgebung mit Android Studio und der zum Projekt passenden Java und Android SDK Konfiguration.

Das Projekt verwendet außerdem Firebase Authentication und Cloud Firestore. Die zugehörige Firebase Konfiguration muss deshalb im Projekt vorhanden sein.

## APK erstellen

Die Debug APK kann über Android Studio erstellt werden.

Alternativ kann im Stammverzeichnis des Projekts der Gradle Wrapper verwendet werden.

Mac oder Linux:

```bash
./gradlew assembleDebug
```

Windows:

```text
gradlew.bat assembleDebug
```

Nach einem erfolgreichen Build befindet sich die APK normalerweise unter:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Bekannte Grenzen

DocuRemind ist als Hochschul MVP umgesetzt und keine produktionsreife Dokumentenplattform.

Aktuell gelten insbesondere folgende Einschränkungen:

* Es werden Dokumentinformationen beziehungsweise Metadaten gespeichert, jedoch keine PDF Dateien oder Scans.
* Es gibt keinen Dateiupload.
* Firebase Storage wird nicht verwendet.
* Es gibt keine OCR Erkennung.
* Dokumente werden nicht automatisch analysiert.
* Erinnerungen werden lokal durch WorkManager ausgelöst und nicht serverseitig versendet.
* WorkManager garantiert keine sekundengenaue Ausführung.
* Die Erinnerungsprüfung basiert auf dem aktuell auf dem Gerät angemeldeten Firebase Nutzer.
* Der Kalenderexport speichert keinen Termin automatisch, sondern öffnet eine Kalender App mit vorausgefüllten Daten.

## Projektstatus

Version:

```text
1.0
```

Status:

```text
Hochschulprojekt / MVP
```

Die zentrale Zielsetzung von DocuRemind besteht darin, Dokumentfristen übersichtlich zu verwalten und Nutzer rechtzeitig vor wichtigen Ablaufdaten zu informieren.