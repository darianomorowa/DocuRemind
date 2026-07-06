package de.hwr.docuremind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailActivity extends AppCompatActivity {

    private TextView textDocumentName;
    private TextView textDocumentCategory;
    private TextView textDocumentDate;
    private TextView textDocumentNote;
    private Button buttonEdit;
    private Button buttonDelete;
    private Button buttonBackDetail;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private String documentId;
    private String name;
    private String category;
    private String expiryDate;
    private String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textDocumentName = findViewById(R.id.textDocumentName);
        textDocumentCategory = findViewById(R.id.textDocumentCategory);
        textDocumentDate = findViewById(R.id.textDocumentDate);
        textDocumentNote = findViewById(R.id.textDocumentNote);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBackDetail = findViewById(R.id.buttonBackDetail);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        readIntentData();
        showDocumentData();

        buttonEdit.setOnClickListener(view -> openEditScreen());

        buttonDelete.setOnClickListener(view -> deleteDocument());

        buttonBackDetail.setOnClickListener(view -> finish());
    }

    private void readIntentData() {
        documentId = getIntent().getStringExtra("documentId");
        name = getIntent().getStringExtra("documentName");
        category = getIntent().getStringExtra("documentCategory");
        expiryDate = getIntent().getStringExtra("documentDate");
        note = getIntent().getStringExtra("documentNote");
    }

    private void showDocumentData() {
        textDocumentName.setText("Name: " + name);
        textDocumentCategory.setText("Kategorie: " + category);
        textDocumentDate.setText("Ablaufdatum: " + expiryDate);
        textDocumentNote.setText("Notiz: " + note);
    }

    private void openEditScreen() {
        Intent intent = new Intent(DetailActivity.this, AddDocumentActivity.class);

        intent.putExtra("documentId", documentId);
        intent.putExtra("documentName", name);
        intent.putExtra("documentCategory", category);
        intent.putExtra("documentDate", expiryDate);
        intent.putExtra("documentNote", note);

        startActivity(intent);
        finish();
    }

    private void deleteDocument() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kein Nutzer eingeloggt", Toast.LENGTH_SHORT).show();
            return;
        }

        if (documentId == null) {
            Toast.makeText(this, "Dokument-ID fehlt", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .document(documentId)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(DetailActivity.this, "Dokument gelöscht", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            DetailActivity.this,
                            "Fehler beim Löschen: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}