package de.hwr.docuremind;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    /*
     * Bedienelemente und Anzeigeflächen des Dashboards.
     */
    private Button buttonAddDocument;
    private Button buttonSettings;
    private Button buttonLogout;

    private TextView textDocumentCount;
    private TextView textEmptyState;
    private ProgressBar progressDocuments;
    private LinearLayout layoutDocumentList;

    /*
     * FirebaseAuth liefert den aktuellen Nutzer.
     * Firestore lädt seine gespeicherten Dokumente.
     */
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        /*
         * XML-Elemente mit dem Java-Code verbinden.
         */
        buttonAddDocument =
                findViewById(R.id.buttonAddDocument);

        buttonSettings =
                findViewById(R.id.buttonSettings);

        buttonLogout =
                findViewById(R.id.buttonLogout);

        textDocumentCount =
                findViewById(R.id.textDocumentCount);

        textEmptyState =
                findViewById(R.id.textEmptyState);

        progressDocuments =
                findViewById(R.id.progressDocuments);

        layoutDocumentList =
                findViewById(R.id.layoutDocumentList);

        /*
         * Firebase-Dienste initialisieren.
         */
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        /*
         * Den Benachrichtigungskanal für DocuRemind vorbereiten.
         */
        NotificationHelper.createNotificationChannel(this);

        /*
         * Bei aktivierten Erinnerungen wird bei Bedarf
         * die Android-Berechtigung angefragt.
         */
        if (ReminderPreferences
                .areNotificationsEnabled(this)) {

            NotificationHelper
                    .requestPermissionIfNeeded(this);
        }

        /*
         * WorkManager passend zur aktuellen Einstellung
         * starten oder beenden.
         */
        ReminderScheduler.updateSchedule(this);

        /*
         * Navigation zum Dokumentformular.
         */
        buttonAddDocument.setOnClickListener(view -> {
            Intent intent = new Intent(
                    DashboardActivity.this,
                    AddDocumentActivity.class
            );

            startActivity(intent);
        });

        /*
         * Navigation zu den Einstellungen.
         */
        buttonSettings.setOnClickListener(view -> {
            Intent intent = new Intent(
                    DashboardActivity.this,
                    SettingsActivity.class
            );

            startActivity(intent);
        });

        /*
         * Aktuellen Nutzer abmelden.
         */
        buttonLogout.setOnClickListener(
                view -> logoutUser()
        );
    }

    /*
     * Prüft beim Öffnen des Dashboards,
     * ob weiterhin ein Nutzer angemeldet ist.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() == null) {
            openLoginScreen();
        }
    }

    /*
     * onResume wird jedes Mal ausgeführt,
     * wenn das Dashboard wieder sichtbar wird.
     *
     * Dadurch wird die Dokumentliste nach dem Hinzufügen,
     * Bearbeiten oder Löschen automatisch neu geladen.
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (firebaseAuth.getCurrentUser() != null) {
            loadDocuments();
        }
    }

    /*
     * Meldet den Nutzer bei Firebase ab
     * und öffnet anschließend den Login-Screen.
     */
    private void logoutUser() {
        firebaseAuth.signOut();
        openLoginScreen();
    }

    /*
     * Öffnet den Login-Screen und entfernt alle bisherigen
     * Activities aus dem Navigationsverlauf.
     */
    private void openLoginScreen() {
        Intent intent = new Intent(
                DashboardActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    /*
     * Lädt alle Dokumente des aktuell angemeldeten Nutzers
     * aus Firestore.
     */
    private void loadDocuments() {

        /*
         * Alte Karten entfernen und Ladeanzeige starten.
         */
        layoutDocumentList.removeAllViews();
        textEmptyState.setVisibility(View.GONE);
        progressDocuments.setVisibility(View.VISIBLE);

        /*
         * Ohne angemeldeten Nutzer darf keine Firestore-Abfrage erfolgen.
         */
        if (firebaseAuth.getCurrentUser() == null) {
            progressDocuments.setVisibility(View.GONE);

            Toast.makeText(
                    this,
                    "Kein Nutzer eingeloggt",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId =
                firebaseAuth
                        .getCurrentUser()
                        .getUid();

        /*
         * Dokumente aus der persönlichen Unterkollektion laden.
         */
        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .get()
                .addOnSuccessListener(querySnapshots -> {

                    progressDocuments.setVisibility(
                            View.GONE
                    );

                    /*
                     * Firestore-Ergebnisse in eine bearbeitbare Liste übernehmen.
                     */
                    List<QueryDocumentSnapshot> documents =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot snapshot
                            : querySnapshots) {

                        documents.add(snapshot);
                    }

                    /*
                     * Dokumente nach ihrem Ablaufdatum sortieren.
                     * Die früheste Frist erscheint zuerst.
                     */
                    documents.sort((first, second) -> {

                        long firstDate =
                                getSortValue(first);

                        long secondDate =
                                getSortValue(second);

                        return Long.compare(
                                firstDate,
                                secondDate
                        );
                    });

                    showDocuments(documents);
                })
                .addOnFailureListener(exception -> {

                    progressDocuments.setVisibility(
                            View.GONE
                    );

                    textEmptyState.setText(
                            "Dokumente konnten nicht geladen werden."
                    );

                    textEmptyState.setVisibility(
                            View.VISIBLE
                    );

                    Toast.makeText(
                            DashboardActivity.this,
                            "Fehler beim Laden: "
                                    + exception.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * Aktualisiert die Dokumentanzahl und erzeugt
     * für jedes Dokument eine eigene Karte.
     */
    private void showDocuments(
            List<QueryDocumentSnapshot> documents
    ) {
        int documentCount = documents.size();

        if (documentCount == 1) {
            textDocumentCount.setText(
                    "1 Dokument"
            );
        } else {
            textDocumentCount.setText(
                    documentCount + " Dokumente"
            );
        }

        /*
         * Bei einer leeren Liste wird ein Hinweis angezeigt.
         */
        if (documents.isEmpty()) {
            textEmptyState.setText(
                    "Noch keine Dokumente gespeichert."
            );

            textEmptyState.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        /*
         * Für jedes Firestore-Dokument wird eine Karte erzeugt.
         */
        for (QueryDocumentSnapshot snapshot
                : documents) {

            addDocumentCard(snapshot);
        }
    }

    /*
     * Erstellt eine sichtbare Dokumentkarte
     * aus item_document.xml.
     */
    private void addDocumentCard(
            QueryDocumentSnapshot snapshot
    ) {
        View itemView =
                getLayoutInflater().inflate(
                        R.layout.item_document,
                        layoutDocumentList,
                        false
                );

        MaterialCardView cardDocument =
                itemView.findViewById(
                        R.id.cardDocument
                );

        TextView textItemName =
                itemView.findViewById(
                        R.id.textItemName
                );

        TextView textItemStatus =
                itemView.findViewById(
                        R.id.textItemStatus
                );

        TextView textItemCategory =
                itemView.findViewById(
                        R.id.textItemCategory
                );

        TextView textItemDate =
                itemView.findViewById(
                        R.id.textItemDate
                );

        /*
         * Werte aus dem Firestore-Dokument auslesen.
         */
        String documentId =
                snapshot.getId();

        String name =
                snapshot.getString("name");

        String category =
                snapshot.getString("category");

        String expiryDate =
                snapshot.getString("expiryDate");

        String note =
                snapshot.getString("note");

        long expiryDateMillis =
                readExpiryDateMillis(snapshot);

        /*
         * Fallback-Werte verhindern leere Karten.
         */
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
            expiryDate = "Kein Datum";
        }

        if (TextUtils.isEmpty(note)) {
            note = "";
        }

        /*
         * Status und Farbe anhand des Ablaufdatums berechnen.
         */
        String statusText =
                DocumentDateUtils.getStatusText(
                        expiryDateMillis
                );

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
         * Dokumentinformationen in der Karte anzeigen.
         */
        textItemName.setText(name);
        textItemStatus.setText(statusText);

        textItemCategory.setText(
                "Kategorie: " + category
        );

        textItemDate.setText(
                "Ablaufdatum: " + expiryDate
        );

        /*
         * Statusfeld entsprechend der Dringlichkeit färben.
         */
        Drawable statusBackground =
                textItemStatus.getBackground();

        if (statusBackground != null) {
            statusBackground
                    .mutate()
                    .setTint(statusColor);
        }

        /*
         * Auch der Kartenrand erhält die Statusfarbe.
         */
        cardDocument.setStrokeColor(
                statusColor
        );

        /*
         * Finale Variablen werden für den Klick-Listener benötigt.
         */
        String finalName = name;
        String finalCategory = category;
        String finalExpiryDate = expiryDate;
        String finalNote = note;

        long finalExpiryDateMillis =
                expiryDateMillis;

        /*
         * Beim Klick öffnet sich die Detailansicht.
         * Die Dokumentdaten werden als Intent-Extras übergeben.
         */
        cardDocument.setOnClickListener(view -> {
            Intent intent = new Intent(
                    DashboardActivity.this,
                    DetailActivity.class
            );

            intent.putExtra(
                    "documentId",
                    documentId
            );

            intent.putExtra(
                    "documentName",
                    finalName
            );

            intent.putExtra(
                    "documentCategory",
                    finalCategory
            );

            intent.putExtra(
                    "documentDate",
                    finalExpiryDate
            );

            intent.putExtra(
                    "documentDateMillis",
                    finalExpiryDateMillis
            );

            intent.putExtra(
                    "documentNote",
                    finalNote
            );

            startActivity(intent);
        });

        /*
         * Die fertige Karte wird an die Dokumentliste angehängt.
         */
        layoutDocumentList.addView(itemView);
    }

    /*
     * Liest den neuen Zeitstempel aus Firestore.
     *
     * Bei älteren Dokumenten ohne Zeitstempel wird
     * das gespeicherte Textdatum umgewandelt.
     */
    private long readExpiryDateMillis(
            QueryDocumentSnapshot snapshot
    ) {
        Object storedMillis =
                snapshot.get("expiryDateMillis");

        if (storedMillis instanceof Number) {
            return ((Number) storedMillis)
                    .longValue();
        }

        return DocumentDateUtils.parseDateToMillis(
                snapshot.getString("expiryDate")
        );
    }

    /*
     * Liefert den Wert für die Sortierung.
     *
     * Dokumente ohne gültiges Datum erscheinen am Ende.
     */
    private long getSortValue(
            QueryDocumentSnapshot snapshot
    ) {
        long expiryDateMillis =
                readExpiryDateMillis(snapshot);

        if (expiryDateMillis <= 0) {
            return Long.MAX_VALUE;
        }

        return expiryDateMillis;
    }
}