package de.hwr.docuremind;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class DetailActivity extends AppCompatActivity {

    private TextView textDocumentName;
    private TextView textDocumentCategory;
    private TextView textDocumentDate;
    private Button buttonEdit;
    private Button buttonDelete;
    private Button buttonBackDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        textDocumentName = findViewById(R.id.textDocumentName);
        textDocumentCategory = findViewById(R.id.textDocumentCategory);
        textDocumentDate = findViewById(R.id.textDocumentDate);
        buttonEdit = findViewById(R.id.buttonEdit);
        buttonDelete = findViewById(R.id.buttonDelete);
        buttonBackDetail = findViewById(R.id.buttonBackDetail);

        String name = getIntent().getStringExtra("documentName");
        String category = getIntent().getStringExtra("documentCategory");
        String date = getIntent().getStringExtra("documentDate");

        textDocumentName.setText("Name: " + name);
        textDocumentCategory.setText("Kategorie: " + category);
        textDocumentDate.setText("Ablaufdatum: " + date);

        buttonEdit.setOnClickListener(view -> {
            Toast.makeText(DetailActivity.this, "Bearbeiten folgt später", Toast.LENGTH_SHORT).show();
        });

        buttonDelete.setOnClickListener(view -> {
            Toast.makeText(DetailActivity.this, "Löschen folgt später", Toast.LENGTH_SHORT).show();
        });

        buttonBackDetail.setOnClickListener(view -> {
            finish();
        });
    }
}