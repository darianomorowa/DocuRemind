package de.hwr.docuremind;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    private Button buttonAddDocument;
    private Button buttonSettings;
    private TextView textExampleDocument;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        buttonAddDocument = findViewById(R.id.buttonAddDocument);
        buttonSettings = findViewById(R.id.buttonSettings);
        textExampleDocument = findViewById(R.id.textExampleDocument);

        buttonAddDocument.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, AddDocumentActivity.class);
            startActivity(intent);
        });

        buttonSettings.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        textExampleDocument.setOnClickListener(view -> {
            Intent intent = new Intent(DashboardActivity.this, DetailActivity.class);
            intent.putExtra("documentName", "Reisepass");
            intent.putExtra("documentCategory", "Ausweis");
            intent.putExtra("documentDate", "31.12.2026");
            startActivity(intent);
        });
    }
}