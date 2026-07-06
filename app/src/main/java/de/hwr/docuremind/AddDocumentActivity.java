package de.hwr.docuremind;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddDocumentActivity extends AppCompatActivity {

    private EditText editDocumentName;
    private EditText editCategory;
    private EditText editExpiryDate;
    private EditText editNote;
    private Button buttonSaveDocument;
    private Button buttonBack;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        editDocumentName = findViewById(R.id.editDocumentName);
        editCategory = findViewById(R.id.editCategory);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        editNote = findViewById(R.id.editNote);
        buttonSaveDocument = findViewById(R.id.buttonSaveDocument);
        buttonBack = findViewById(R.id.buttonBack);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        buttonSaveDocument.setOnClickListener(view -> saveDocument());

        buttonBack.setOnClickListener(view -> finish());
    }

    private void saveDocument() {
        String name = editDocumentName.getText().toString().trim();
        String category = editCategory.getText().toString().trim();
        String expiryDate = editExpiryDate.getText().toString().trim();
        String note = editNote.getText().toString().trim();

        if (!isInputValid(name, category, expiryDate)) {
            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kein Nutzer eingeloggt", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        Map<String, Object> document = new HashMap<>();
        document.put("name", name);
        document.put("category", category);
        document.put("expiryDate", expiryDate);
        document.put("note", note);
        document.put("createdAt", System.currentTimeMillis());

        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .add(document)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddDocumentActivity.this, "Dokument gespeichert", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddDocumentActivity.this, "Fehler beim Speichern: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isInputValid(String name, String category, String expiryDate) {
        if (TextUtils.isEmpty(name)) {
            editDocumentName.setError("Bitte Dokumentname eingeben");
            return false;
        }

        if (TextUtils.isEmpty(category)) {
            editCategory.setError("Bitte Kategorie eingeben");
            return false;
        }

        if (TextUtils.isEmpty(expiryDate)) {
            editExpiryDate.setError("Bitte Ablaufdatum eingeben");
            return false;
        }

        return true;
    }
}