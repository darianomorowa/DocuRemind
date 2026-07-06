package de.hwr.docuremind;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class DashboardActivity extends AppCompatActivity {

    private Button buttonAddDocument;
    private Button buttonSettings;
    private Button buttonLogout;
    private LinearLayout layoutDocumentList;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buttonAddDocument = findViewById(R.id.buttonAddDocument);
        buttonSettings = findViewById(R.id.buttonSettings);
        buttonLogout = findViewById(R.id.buttonLogout);
        layoutDocumentList = findViewById(R.id.layoutDocumentList);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        buttonAddDocument.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, AddDocumentActivity.class);
            startActivity(intent);
        });

        buttonSettings.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        buttonLogout.setOnClickListener(view -> {
            firebaseAuth.signOut();
            Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        loadDocuments();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void loadDocuments() {
        layoutDocumentList.removeAllViews();

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Kein Nutzer eingeloggt", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("users")
                .document(userId)
                .collection("documents")
                .orderBy("createdAt")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        showEmptyMessage();
                        return;
                    }

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String documentId = documentSnapshot.getId();
                        String name = documentSnapshot.getString("name");
                        String category = documentSnapshot.getString("category");
                        String expiryDate = documentSnapshot.getString("expiryDate");
                        String note = documentSnapshot.getString("note");

                        addDocumentView(documentId, name, category, expiryDate, note);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            DashboardActivity.this,
                            "Fehler beim Laden: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private void showEmptyMessage() {
        TextView emptyText = new TextView(this);
        emptyText.setText("Noch keine Dokumente gespeichert.");
        emptyText.setTextSize(16);
        emptyText.setPadding(16, 16, 16, 16);

        layoutDocumentList.addView(emptyText);
    }

    private void addDocumentView(
            String documentId,
            String name,
            String category,
            String expiryDate,
            String note
    ) {
        TextView documentView = new TextView(this);

        documentView.setText(
                "Name: " + name + "\n" +
                        "Kategorie: " + category + "\n" +
                        "Ablaufdatum: " + expiryDate
        );

        documentView.setTextSize(18);
        documentView.setPadding(24, 24, 24, 24);
        documentView.setBackgroundColor(Color.rgb(238, 238, 238));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        layoutParams.setMargins(0, 0, 0, 16);
        documentView.setLayoutParams(layoutParams);

        documentView.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, DetailActivity.class);
            intent.putExtra("documentId", documentId);
            intent.putExtra("documentName", name);
            intent.putExtra("documentCategory", category);
            intent.putExtra("documentDate", expiryDate);
            intent.putExtra("documentNote", note);
            startActivity(intent);
        });

        layoutDocumentList.addView(documentView);
    }
}