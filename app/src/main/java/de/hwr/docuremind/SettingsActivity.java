package de.hwr.docuremind;

import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioButton radio30;
    private Button buttonSaveSettings;
    private Button buttonBackSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        radio30 = findViewById(R.id.radio30);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
        buttonBackSettings = findViewById(R.id.buttonBackSettings);

        radio30.setChecked(true);

        buttonSaveSettings.setOnClickListener(view -> {
            Toast.makeText(SettingsActivity.this, "Einstellungen werden später gespeichert", Toast.LENGTH_SHORT).show();
        });

        buttonBackSettings.setOnClickListener(view -> {
            finish();
        });
    }
}