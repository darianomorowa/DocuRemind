package de.hwr.docuremind;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddDocumentActivity extends AppCompatActivity {

    /*
     * Verbindungen zu den sichtbaren Elementen aus der XML-Datei.
     */
    private TextView textAddDocumentTitle;
    private EditText editDocumentName;
    private EditText editCategory;
    private EditText editExpiryDate;
    private EditText editNote;
    private Button buttonSaveDocument;
    private Button buttonBack;

    /*
     * FirebaseAuth liefert den aktuell angemeldeten Nutzer.
     * FirebaseFirestore speichert die Dokumentdaten.
     */
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    /*
     * documentId ist nur im Bearbeitungsmodus gefüllt.
     * isEditMode unterscheidet zwischen neuem und bestehendem Dokument.
     */
    private String documentId;
    private boolean isEditMode = false;

    /*
     * Das ausgewählte Datum wird intern als Zeitstempel gespeichert.
     */
    private long selectedExpiryDateMillis = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        /*
         * XML-Elemente mit dem Java-Code verbinden.
         */
        textAddDocumentTitle =
                findViewById(R.id.textAddDocumentTitle);

        editDocumentName =
                findViewById(R.id.editDocumentName);

        editCategory =
                findViewById(R.id.editCategory);

        editExpiryDate =
                findViewById(R.id.editExpiryDate);

        editNote =
                findViewById(R.id.editNote);

        buttonSaveDocument =
                findViewById(R.id.buttonSaveDocument);

        buttonBack =
                findViewById(R.id.buttonBack);

        /*
         * Firebase-Dienste für diese Activity initialisieren.
         */
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        /*
         * Prüfen, ob ein bestehendes Dokument bearbeitet wird.
         */
        checkEditMode();

        /*
         * Beim Antippen des Datumsfeldes wird ein DatePicker geöffnet.
         */
        editExpiryDate.setOnClickListener(
                view -> showDatePicker()
        );

        /*
         * Beim Speichern werden die Eingaben geprüft und anschließend
         * neu erstellt oder aktualisiert.
         */
        buttonSaveDocument.setOnClickListener(
                view -> saveDocument()
        );

        /*
         * finish beendet die aktuelle Activity und kehrt zurück.
         */
        buttonBack.setOnClickListener(
                view -> finish()
        );
    }

    /*
     * Prüft, ob die Activity über den Bearbeiten-Button geöffnet wurde.
     *
     * Wenn eine documentId vorhanden ist, werden die bisherigen Daten
     * in das Formular eingetragen.
     */
    private void checkEditMode() {
        documentId =
                getIntent().getStringExtra("documentId");

        if (documentId == null) {
            return;
        }

        isEditMode = true;

        String name =
                getIntent().getStringExtra(
                        "documentName"
                );

        String category =
                getIntent().getStringExtra(
                        "documentCategory"
                );

        String expiryDate =
                getIntent().getStringExtra(
                        "documentDate"
                );

        String note =
                getIntent().getStringExtra(
                        "documentNote"
                );

        selectedExpiryDateMillis =
                getIntent().getLongExtra(
                        "documentDateMillis",
                        0L
                );

        /*
         * Ältere Dokumente besitzen möglicherweise noch keinen
         * Zeitstempel. Dann wird das Textdatum umgewandelt.
         */
        if (selectedExpiryDateMillis <= 0) {
            selectedExpiryDateMillis =
                    DocumentDateUtils.parseDateToMillis(
                            expiryDate
                    );
        }

        /*
         * Vorhandene Daten in die Eingabefelder schreiben.
         */
        editDocumentName.setText(name);
        editCategory.setText(category);

        editExpiryDate.setText(
                DocumentDateUtils.formatDate(
                        selectedExpiryDateMillis
                )
        );

        editNote.setText(note);

        /*
         * Überschrift und Button an den Bearbeitungsmodus anpassen.
         */
        textAddDocumentTitle.setText(
                "Dokument bearbeiten"
        );

        buttonSaveDocument.setText(
                "Änderungen speichern"
        );
    }

    /*
     * Öffnet einen Android-DatePicker.
     *
     * Nach der Auswahl wird das Datum sowohl als Zeitstempel gespeichert
     * als auch lesbar im Eingabefeld angezeigt.
     */
    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();

        /*
         * Im Bearbeitungsmodus soll der bisherige Wert vorausgewählt sein.
         */
        if (selectedExpiryDateMillis > 0) {
            calendar.setTimeInMillis(
                    selectedExpiryDateMillis
            );
        }

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {

                            calendar.set(
                                    Calendar.YEAR,
                                    year
                            );

                            calendar.set(
                                    Calendar.MONTH,
                                    month
                            );

                            calendar.set(
                                    Calendar.DAY_OF_MONTH,
                                    dayOfMonth
                            );

                            selectedExpiryDateMillis =
                                    DocumentDateUtils.normalizeDate(
                                            calendar.getTimeInMillis()
                                    );

                            editExpiryDate.setText(
                                    DocumentDateUtils.formatDate(
                                            selectedExpiryDateMillis
                                    )
                            );

                            editExpiryDate.setError(null);
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                );

        datePickerDialog.show();
    }

    /*
     * Liest alle Eingaben aus und entscheidet anschließend,
     * ob ein neues Dokument erstellt oder ein bestehendes aktualisiert wird.
     */
    private void saveDocument() {
        String name =
                editDocumentName
                        .getText()
                        .toString()
                        .trim();

        String category =
                editCategory
                        .getText()
                        .toString()
                        .trim();

        String note =
                editNote
                        .getText()
                        .toString()
                        .trim();

        /*
         * Ungültige Eingaben stoppen den Speichervorgang.
         */
        if (!isInputValid(name, category)) {
            return;
        }

        /*
         * Ohne eingeloggten Nutzer dürfen keine Daten gespeichert werden.
         */
        if (firebaseAuth.getCurrentUser() == null) {
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

        String expiryDateText =
                DocumentDateUtils.formatDate(
                        selectedExpiryDateMillis
                );

        /*
         * Eine Map fasst alle Dokumentdaten für Firestore zusammen.
         */
        Map<String, Object> document =
                new HashMap<>();

        document.put("name", name);
        document.put("category", category);
        document.put("expiryDate", expiryDateText);

        document.put(
                "expiryDateMillis",
                selectedExpiryDateMillis
        );

        document.put("note", note);

        document.put(
                "updatedAt",
                System.currentTimeMillis()
        );

        /*
         * Abhängig vom Modus wird create oder update verwendet.
         */
        if (isEditMode) {
            updateDocument(userId, document);
        } else {
            createDocument(userId, document);
        }
    }

    /*
     * Speichert ein komplett neues Dokument in Firestore.
     * Firestore erzeugt automatisch eine neue Dokument-ID.
     */
    private void createDocument(
            String userId,
            Map<String, Object> document
    ) {
        document.put(
                "createdAt",
                System.currentTimeMillis()
        );

        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .add(document)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    AddDocumentActivity.this,
                                    "Dokument gespeichert",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            Toast.makeText(
                                    AddDocumentActivity.this,
                                    "Fehler beim Speichern: "
                                            + exception.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    /*
     * Aktualisiert ein bereits bestehendes Firestore-Dokument.
     * Dazu wird die vorhandene documentId verwendet.
     */
    private void updateDocument(
            String userId,
            Map<String, Object> document
    ) {
        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .document(documentId)
                .update(document)
                .addOnSuccessListener(
                        unused -> {

                            Toast.makeText(
                                    AddDocumentActivity.this,
                                    "Dokument aktualisiert",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(
                        exception -> {

                            Toast.makeText(
                                    AddDocumentActivity.this,
                                    "Fehler beim Aktualisieren: "
                                            + exception.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                );
    }

    /*
     * Prüft die Pflichtfelder des Formulars.
     *
     * Bei einem Fehler wird direkt am betroffenen Feld eine Meldung gezeigt.
     */
    private boolean isInputValid(
            String name,
            String category
    ) {
        if (TextUtils.isEmpty(name)) {
            editDocumentName.setError(
                    "Bitte Dokumentname eingeben"
            );

            return false;
        }

        if (TextUtils.isEmpty(category)) {
            editCategory.setError(
                    "Bitte Kategorie eingeben"
            );

            return false;
        }

        if (selectedExpiryDateMillis <= 0) {
            editExpiryDate.setError(
                    "Bitte Ablaufdatum auswählen"
            );

            return false;
        }

        return true;
    }
}