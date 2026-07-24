package de.hwr.docuremind;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DocuRemindPrefs";
    private static final String KEY_REMINDER_DAYS = "reminderDays";

    private RadioGroup radioReminderGroup;
    private RadioButton radio30;
    private RadioButton radio60;
    private RadioButton radio90;
    private Button buttonSaveSettings;
    private Button buttonBackSettings;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        radioReminderGroup = findViewById(R.id.radioReminderGroup);
        radio30 = findViewById(R.id.radio30);
        radio60 = findViewById(R.id.radio60);
        radio90 = findViewById(R.id.radio90);
        buttonSaveSettings = findViewById(R.id.buttonSaveSettings);
        buttonBackSettings = findViewById(R.id.buttonBackSettings);

        preferences = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        loadSettings();

        buttonSaveSettings.setOnClickListener(view -> saveSettings());

        buttonBackSettings.setOnClickListener(view -> finish());
    }

    private void saveSettings() {
        int selectedDays = 30;
        int selectedRadioButtonId =
                radioReminderGroup.getCheckedRadioButtonId();

        if (selectedRadioButtonId == R.id.radio60) {
            selectedDays = 60;
        } else if (selectedRadioButtonId == R.id.radio90) {
            selectedDays = 90;
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(KEY_REMINDER_DAYS, selectedDays);
        editor.apply();

        Toast.makeText(
                this,
                "Erinnerung auf " + selectedDays + " Tage eingestellt",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void loadSettings() {
        int savedDays = preferences.getInt(
                KEY_REMINDER_DAYS,
                30
        );

        if (savedDays == 60) {
            radio60.setChecked(true);
        } else if (savedDays == 90) {
            radio90.setChecked(true);
        } else {
            radio30.setChecked(true);
        }
    }
}