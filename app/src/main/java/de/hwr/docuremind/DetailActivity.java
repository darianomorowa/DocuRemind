package de.hwr.docuremind;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {

    /*
     * Textfelder aus der XML-Datei.
     *
     * Sie zeigen die Dokumentdaten und den berechneten Fristenstatus an.
     */
    private TextView textDocumentName;
    private TextView textDocumentCategory;
    private TextView textDocumentDate;
    private TextView textDocumentNote;
    private TextView textDetailDays;
    private TextView textDetailStatus;

    /*
     * Buttons für die Aktionen der Detailansicht.
     */
    private Button buttonEdit;
    private Button buttonCalendar;
    private Button buttonDelete;
    private Button buttonBackDetail;

    /*
     * FirebaseAuth liefert den aktuell angemeldeten Nutzer.
     * Firestore wird zum Löschen des Dokuments benötigt.
     */
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    /*
     * Dokumentdaten, die vom Dashboard über den Intent übergeben werden.
     */
    private String documentId;
    private String name;
    private String category;
    private String expiryDate;
    private String note;

    /*
     * Das Ablaufdatum als Zeitstempel.
     *
     * Dieser Wert wird für Statusberechnung, Bearbeitung
     * und Kalenderexport verwendet.
     */
    private long expiryDateMillis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        /*
         * XML-Elemente mit den Java-Variablen verbinden.
         */
        textDocumentName =
                findViewById(R.id.textDocumentName);

        textDocumentCategory =
                findViewById(R.id.textDocumentCategory);

        textDocumentDate =
                findViewById(R.id.textDocumentDate);

        textDocumentNote =
                findViewById(R.id.textDocumentNote);

        textDetailDays =
                findViewById(R.id.textDetailDays);

        textDetailStatus =
                findViewById(R.id.textDetailStatus);

        buttonEdit =
                findViewById(R.id.buttonEdit);

        buttonCalendar =
                findViewById(R.id.buttonCalendar);

        buttonDelete =
                findViewById(R.id.buttonDelete);

        buttonBackDetail =
                findViewById(R.id.buttonBackDetail);

        /*
         * Firebase-Dienste initialisieren.
         */
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        /*
         * Übergebene Dokumentdaten einlesen und anzeigen.
         */
        readIntentData();
        showDocumentData();
        showExpiryStatus();

        /*
         * Klickaktionen der Detailseite festlegen.
         */
        buttonEdit.setOnClickListener(
                view -> openEditScreen()
        );

        buttonCalendar.setOnClickListener(
                view -> addDocumentToCalendar()
        );

        buttonDelete.setOnClickListener(
                view -> showDeleteConfirmation()
        );

        buttonBackDetail.setOnClickListener(
                view -> finish()
        );
    }

    /*
     * Liest alle Daten aus dem Intent,
     * mit dem das Dashboard diese Activity geöffnet hat.
     */
    private void readIntentData() {
        documentId =
                getIntent().getStringExtra(
                        "documentId"
                );

        name =
                getIntent().getStringExtra(
                        "documentName"
                );

        category =
                getIntent().getStringExtra(
                        "documentCategory"
                );

        expiryDate =
                getIntent().getStringExtra(
                        "documentDate"
                );

        note =
                getIntent().getStringExtra(
                        "documentNote"
                );

        expiryDateMillis =
                getIntent().getLongExtra(
                        "documentDateMillis",
                        0L
                );

        /*
         * Fallback für ältere Dokumente:
         * Falls noch kein Zeitstempel gespeichert wurde,
         * wird das lesbare Textdatum umgewandelt.
         */
        if (expiryDateMillis <= 0) {
            expiryDateMillis =
                    DocumentDateUtils.parseDateToMillis(
                            expiryDate
                    );
        }
    }

    /*
     * Zeigt die Dokumentinformationen in den vorgesehenen Textfeldern an.
     *
     * Leere Werte werden durch verständliche Ersatztexte dargestellt.
     */
    private void showDocumentData() {
        if (TextUtils.isEmpty(name)) {
            name = "Unbenanntes Dokument";
        }

        if (TextUtils.isEmpty(category)) {
            category = "Ohne Kategorie";
        }

        if (TextUtils.isEmpty(expiryDate)
                && expiryDateMillis > 0) {

            expiryDate =
                    DocumentDateUtils.formatDate(
                            expiryDateMillis
                    );
        }

        if (TextUtils.isEmpty(expiryDate)) {
            expiryDate = "Kein Ablaufdatum";
        }

        if (TextUtils.isEmpty(note)) {
            note = "Keine Notiz hinterlegt.";
        }

        textDocumentName.setText(name);
        textDocumentCategory.setText(category);
        textDocumentDate.setText(expiryDate);
        textDocumentNote.setText(note);
    }

    /*
     * Berechnet den sichtbaren Fristenstatus
     * und färbt das Statusfeld passend zur Dringlichkeit.
     */
    private void showExpiryStatus() {
        String detailedStatus =
                DocumentDateUtils.getStatusText(
                        expiryDateMillis
                );

        String shortStatus =
                getShortStatusText(
                        expiryDateMillis
                );

        textDetailDays.setText(detailedStatus);
        textDetailStatus.setText(shortStatus);

        int statusColorResource =
                DocumentDateUtils
                        .getStatusColorResource(
                                expiryDateMillis
                        );

        int statusColor =
                ContextCompat.getColor(
                        this,
                        statusColorResource
                );

        /*
         * Der vorhandene runde Hintergrund bleibt erhalten,
         * erhält aber die zur Frist passende Farbe.
         */
        Drawable statusBackground =
                textDetailStatus.getBackground();

        if (statusBackground != null) {
            statusBackground
                    .mutate()
                    .setTint(statusColor);
        }
    }

    /*
     * Erzeugt einen kurzen Status für das farbige Statusfeld.
     *
     * Die ausführliche Restzeit wird daneben separat angezeigt.
     */
    private String getShortStatusText(
            long dateMillis
    ) {
        if (dateMillis <= 0) {
            return "Datum fehlt";
        }

        long daysUntil =
                DocumentDateUtils.getDaysUntil(
                        dateMillis
                );

        if (daysUntil < 0) {
            return "Abgelaufen";
        }

        if (daysUntil == 0) {
            return "Heute";
        }

        if (daysUntil <= 3) {
            return "Dringend";
        }

        if (daysUntil <= 14) {
            return "Bald fällig";
        }

        if (daysUntil <= 30) {
            return "Im Blick";
        }

        return "Aktiv";
    }

    /*
     * Öffnet das Dokumentformular im Bearbeitungsmodus.
     *
     * Alle vorhandenen Werte werden als Intent-Extras
     * an AddDocumentActivity übergeben.
     */
    private void openEditScreen() {
        Intent intent = new Intent(
                DetailActivity.this,
                AddDocumentActivity.class
        );

        intent.putExtra(
                "documentId",
                documentId
        );

        intent.putExtra(
                "documentName",
                name
        );

        intent.putExtra(
                "documentCategory",
                category
        );

        intent.putExtra(
                "documentDate",
                expiryDate
        );

        intent.putExtra(
                "documentDateMillis",
                expiryDateMillis
        );

        intent.putExtra(
                "documentNote",
                note
        );

        startActivity(intent);

        /*
         * Die alte Detailansicht wird geschlossen.
         *
         * Nach dem Speichern gelangt der Nutzer dadurch
         * direkt zurück zum aktualisierten Dashboard.
         */
        finish();
    }

    /*
     * Öffnet einen Bestätigungsdialog,
     * bevor ein Dokument endgültig gelöscht wird.
     *
     * Dadurch kann ein versehentlicher Klick abgebrochen werden.
     */
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Dokument löschen?")
                .setMessage(
                        "Möchtest du „"
                                + name
                                + "“ wirklich löschen?"
                )
                .setNegativeButton(
                        "Abbrechen",
                        null
                )
                .setPositiveButton(
                        "Löschen",
                        (dialog, which) -> deleteDocument()
                )
                .show();
    }

    /*
     * Löscht das Dokument aus dem Firestore-Bereich
     * des aktuell angemeldeten Nutzers.
     */
    private void deleteDocument() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(
                    this,
                    "Kein Nutzer eingeloggt",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (TextUtils.isEmpty(documentId)) {
            Toast.makeText(
                    this,
                    "Dokument-ID fehlt",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            DetailActivity.this,
                            "Dokument gelöscht",
                            Toast.LENGTH_SHORT
                    ).show();

                    /*
                     * Nach dem Löschen wird die Detailseite beendet.
                     * Das Dashboard lädt die Dokumentliste automatisch neu.
                     */
                    finish();
                })
                .addOnFailureListener(exception -> {

                    Toast.makeText(
                            DetailActivity.this,
                            "Fehler beim Löschen: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * Öffnet eine installierte Kalender-App mit vorausgefüllten Daten.
     *
     * Der Nutzer bestätigt dort selbst, ob der Termin
     * tatsächlich gespeichert werden soll.
     */
    private void addDocumentToCalendar() {
        if (expiryDateMillis <= 0) {
            Toast.makeText(
                    this,
                    "Kein gültiges Ablaufdatum vorhanden",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        /*
         * Impliziter Intent:
         * Android sucht eine geeignete Kalender-App.
         */
        Intent calendarIntent =
                new Intent(Intent.ACTION_INSERT);

        calendarIntent.setData(
                CalendarContract.Events.CONTENT_URI
        );

        /*
         * Vorausgefüllte Daten für den Kalendertermin.
         */
        calendarIntent.putExtra(
                CalendarContract.Events.TITLE,
                "DocuRemind: " + name
        );

        calendarIntent.putExtra(
                CalendarContract.Events.DESCRIPTION,
                createCalendarDescription()
        );

        calendarIntent.putExtra(
                CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                expiryDateMillis
        );

        calendarIntent.putExtra(
                CalendarContract.EXTRA_EVENT_END_TIME,
                expiryDateMillis
                        + 24L * 60L * 60L * 1000L
        );

        calendarIntent.putExtra(
                CalendarContract.EXTRA_EVENT_ALL_DAY,
                true
        );

        /*
         * Vor dem Start wird geprüft, ob das Gerät
         * eine passende Kalender-App besitzt.
         */
        if (calendarIntent.resolveActivity(
                getPackageManager()
        ) != null) {

            startActivity(calendarIntent);

        } else {
            Toast.makeText(
                    this,
                    "Keine Kalender-App gefunden",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
     * Erstellt einen verständlichen Beschreibungstext
     * für den Kalendertermin.
     */
    private String createCalendarDescription() {
        String description =
                "Kategorie: " + category;

        if (!TextUtils.isEmpty(note)
                && !note.equals(
                "Keine Notiz hinterlegt."
        )) {

            description += "\nNotiz: " + note;
        }

        return description;
    }
}