package de.hwr.docuremind;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDocumentActivity extends AppCompatActivity {

    private Button buttonSaveDocument;
    private Button buttonBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        buttonSaveDocument = findViewById(R.id.buttonSaveDocument);
        buttonBack = findViewById(R.id.buttonBack);

        buttonSaveDocument.setOnClickListener(view -> {
            Toast.makeText(AddDocumentActivity.this, "Speichern folgt mit Firebase", Toast.LENGTH_SHORT).show();
        });

        buttonBack.setOnClickListener(view -> {
            finish();
        });
    }
}