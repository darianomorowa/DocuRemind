package de.hwr.docuremind;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    /*
     * Sichtbare Bedienelemente aus activity_settings.xml.
     */
    private SwitchMaterial switchNotifications;
    private RadioGroup radioReminderGroup;
    private RadioButton radio30;
    private RadioButton radio60;
    private RadioButton radio90;
    private Button buttonSaveSettings;
    private Button buttonBackSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        /*
         * XML-Elemente mit dem Java-Code verbinden.
         */
        switchNotifications =
                findViewById(R.id.switchNotifications);

        radioReminderGroup =
                findViewById(R.id.radioReminderGroup);

        radio30 =
                findViewById(R.id.radio30);

        radio60 =
                findViewById(R.id.radio60);

        radio90 =
                findViewById(R.id.radio90);

        buttonSaveSettings =
                findViewById(R.id.buttonSaveSettings);

        buttonBackSettings =
                findViewById(R.id.buttonBackSettings);

        /*
         * Bereits gespeicherte Einstellungen laden
         * und auf dem Screen anzeigen.
         */
        loadSettings();

        /*
         * Wenn Erinnerungen ausgeschaltet werden,
         * sind die 30-/60-/90-Tage-Optionen nicht mehr bedienbar.
         */
        switchNotifications.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        updateReminderOptionsState(isChecked)
        );

        /*
         * Aktuelle Auswahl dauerhaft speichern.
         */
        buttonSaveSettings.setOnClickListener(
                view -> saveSettings()
        );

        /*
         * Aktuelle Activity schließen und zum Dashboard zurückkehren.
         */
        buttonBackSettings.setOnClickListener(
                view -> finish()
        );
    }

    /*
     * Lädt den Hauptschalter und den Erinnerungszeitpunkt
     * aus der zentralen ReminderPreferences-Klasse.
     */
    private void loadSettings() {
        boolean notificationsEnabled =
                ReminderPreferences.areNotificationsEnabled(
                        this
                );

        int reminderDays =
                ReminderPreferences.getReminderDays(
                        this
                );

        switchNotifications.setChecked(
                notificationsEnabled
        );

        /*
         * Gespeicherten Zahlenwert wieder der passenden
         * RadioButton-Auswahl zuordnen.
         */
        if (reminderDays == 60) {
            radio60.setChecked(true);
        } else if (reminderDays == 90) {
            radio90.setChecked(true);
        } else {
            radio30.setChecked(true);
        }

        updateReminderOptionsState(
                notificationsEnabled
        );
    }

    /*
     * Liest die aktuelle Auswahl aus der Oberfläche
     * und speichert beide Einstellungswerte gemeinsam.
     */
    private void saveSettings() {
        boolean notificationsEnabled =
                switchNotifications.isChecked();

        int reminderDays =
                getSelectedReminderDays();

        /*
         * Hauptschalter und Erinnerungszeitpunkt
         * dauerhaft in SharedPreferences speichern.
         */
        ReminderPreferences.saveSettings(
                this,
                notificationsEnabled,
                reminderDays
        );

        /*
         * WorkManager passend zur neuen Einstellung aktualisieren.
         *
         * Aktiviert:
         * Der tägliche Fristencheck wird geplant.
         *
         * Deaktiviert:
         * Der tägliche Fristencheck wird beendet.
         */
        ReminderScheduler.updateSchedule(this);

        /*
         * Bei aktivierten Erinnerungen wird der
         * Benachrichtigungskanal vorbereitet und bei Bedarf
         * die Android-Berechtigung angefragt.
         */
        if (notificationsEnabled) {
            NotificationHelper.createNotificationChannel(this);
            NotificationHelper.requestPermissionIfNeeded(this);
        }

        /*
         * Dem Nutzer eine verständliche Bestätigung anzeigen.
         */
        if (notificationsEnabled) {
            Toast.makeText(
                    this,
                    "Erinnerungen starten "
                            + reminderDays
                            + " Tage vor Ablauf",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Erinnerungen wurden deaktiviert",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

        /*
         * Nach dem Speichern wird WorkManager
         * passend zur neuen Einstellung aktualisiert.
         */
        ReminderScheduler.updateSchedule(this);

        /*
         * Wird der Schalter aktiviert, fordert DocuRemind
         * bei Bedarf die Android-Benachrichtigungsberechtigung an.
         */
        if (notificationsEnabled) {
            NotificationHelper
                    .createNotificationChannel(this);

            NotificationHelper
                    .requestPermissionIfNeeded(this);
        }
        /*
         * Der Bestätigungstext unterscheidet zwischen
         * aktivierten und deaktivierten Erinnerungen.
         */
        if (notificationsEnabled) {
            Toast.makeText(
                    this,
                    "Erinnerungen starten "
                            + reminderDays
                            + " Tage vor Ablauf",
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Toast.makeText(
                    this,
                    "Erinnerungen wurden deaktiviert",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /*
     * Übersetzt den ausgewählten RadioButton
     * in den Zahlenwert 30, 60 oder 90.
     */
    private int getSelectedReminderDays() {
        int selectedId =
                radioReminderGroup
                        .getCheckedRadioButtonId();

        if (selectedId == R.id.radio60) {
            return 60;
        }

        if (selectedId == R.id.radio90) {
            return 90;
        }

        return 30;
    }

    /*
     * Aktiviert oder deaktiviert alle RadioButtons.
     *
     * Zusätzlich wird die Gruppe optisch ausgegraut,
     * wenn der Hauptschalter ausgeschaltet ist.
     */
    private void updateReminderOptionsState(
            boolean enabled
    ) {
        setRadioGroupEnabled(
                radioReminderGroup,
                enabled
        );

        if (enabled) {
            radioReminderGroup.setAlpha(1.0f);
        } else {
            radioReminderGroup.setAlpha(0.45f);
        }
    }

    /*
     * Durchläuft alle Elemente innerhalb der RadioGroup
     * und setzt ihren aktivierten Zustand.
     */
    private void setRadioGroupEnabled(
            RadioGroup radioGroup,
            boolean enabled
    ) {
        for (int index = 0;
             index < radioGroup.getChildCount();
             index++) {

            View child =
                    radioGroup.getChildAt(index);

            child.setEnabled(enabled);
        }
    }
}