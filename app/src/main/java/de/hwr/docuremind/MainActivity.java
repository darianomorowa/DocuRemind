package de.hwr.docuremind;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class MainActivity extends AppCompatActivity {

    /*
     * Eingabefelder und Buttons des Login-Screens.
     */
    private EditText editEmail;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonRegister;

    /*
     * Die ursprünglichen Button-Texte werden gespeichert,
     * damit sie nach einer fehlgeschlagenen Anfrage wiederhergestellt werden.
     */
    private CharSequence defaultLoginButtonText;
    private CharSequence defaultRegisterButtonText;

    /*
     * FirebaseAuth übernimmt Anmeldung und Registrierung.
     */
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
         * Java-Variablen mit den XML-Elementen verbinden.
         */
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        /*
         * Ursprüngliche Beschriftungen für den Ladezustand merken.
         */
        defaultLoginButtonText = buttonLogin.getText();
        defaultRegisterButtonText = buttonRegister.getText();

        /*
         * Firebase Authentication initialisieren.
         */
        firebaseAuth = FirebaseAuth.getInstance();

        /*
         * Klickaktionen für Anmeldung und Registrierung.
         */
        buttonLogin.setOnClickListener(
                view -> loginUser()
        );

        buttonRegister.setOnClickListener(
                view -> registerUser()
        );
    }

    /*
     * Ist bereits ein Nutzer angemeldet,
     * wird der Login-Screen automatisch übersprungen.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            openDashboard();
        }
    }

    /*
     * Meldet einen bestehenden Nutzer mit E-Mail und Passwort an.
     */
    private void loginUser() {
        String email =
                editEmail.getText().toString().trim();

        String password =
                editPassword.getText().toString();

        if (!isInputValid(
                email,
                password,
                false
        )) {
            return;
        }

        /*
         * Während Firebase arbeitet, werden weitere Klicks verhindert.
         */
        setAuthLoading(
                true,
                false
        );

        firebaseAuth
                .signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Login erfolgreich",
                            Toast.LENGTH_SHORT
                    ).show();

                    openDashboard();
                })
                .addOnFailureListener(exception -> {
                    setAuthLoading(
                            false,
                            false
                    );

                    Toast.makeText(
                            MainActivity.this,
                            getAuthErrorMessage(
                                    exception,
                                    false
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * Erstellt ein neues Firebase-Benutzerkonto.
     */
    private void registerUser() {
        String email =
                editEmail.getText().toString().trim();

        String password =
                editPassword.getText().toString();

        if (!isInputValid(
                email,
                password,
                true
        )) {
            return;
        }

        /*
         * Während der Registrierung sind weitere Klicks gesperrt.
         */
        setAuthLoading(
                true,
                true
        );

        firebaseAuth
                .createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(
                            MainActivity.this,
                            "Registrierung erfolgreich",
                            Toast.LENGTH_SHORT
                    ).show();

                    openDashboard();
                })
                .addOnFailureListener(exception -> {
                    setAuthLoading(
                            false,
                            true
                    );

                    Toast.makeText(
                            MainActivity.this,
                            getAuthErrorMessage(
                                    exception,
                                    true
                            ),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    /*
     * Prüft E-Mail-Adresse und Passwort vor der Firebase-Anfrage.
     *
     * Die Mindestlänge von sechs Zeichen ist nur
     * bei einer neuen Registrierung erforderlich.
     */
    private boolean isInputValid(
            String email,
            String password,
            boolean registration
    ) {
        editEmail.setError(null);
        editPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            editEmail.setError(
                    "Bitte E-Mail eingeben"
            );

            editEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()) {

            editEmail.setError(
                    "Bitte eine gültige E-Mail eingeben"
            );

            editEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError(
                    "Bitte Passwort eingeben"
            );

            editPassword.requestFocus();
            return false;
        }

        if (registration
                && password.length() < 6) {

            editPassword.setError(
                    "Passwort muss mindestens 6 Zeichen haben"
            );

            editPassword.requestFocus();
            return false;
        }

        return true;
    }

    /*
     * Aktiviert oder deaktiviert die Eingaben während einer Firebase-Anfrage.
     *
     * Dadurch kann eine Anmeldung oder Registrierung
     * nicht mehrfach gleichzeitig gestartet werden.
     */
    private void setAuthLoading(
            boolean loading,
            boolean registration
    ) {
        editEmail.setEnabled(!loading);
        editPassword.setEnabled(!loading);
        buttonLogin.setEnabled(!loading);
        buttonRegister.setEnabled(!loading);

        if (loading) {
            if (registration) {
                buttonRegister.setText(
                        "Registrierung läuft..."
                );
            } else {
                buttonLogin.setText(
                        "Anmeldung läuft..."
                );
            }

            return;
        }

        buttonLogin.setText(
                defaultLoginButtonText
        );

        buttonRegister.setText(
                defaultRegisterButtonText
        );
    }

    /*
     * Übersetzt häufige Firebase-Fehler
     * in verständliche Meldungen für den Nutzer.
     */
    private String getAuthErrorMessage(
            Exception exception,
            boolean registration
    ) {
        if (exception
                instanceof FirebaseNetworkException) {

            return "Keine Verbindung zu Firebase. Bitte Internetverbindung prüfen.";
        }

        if (exception
                instanceof FirebaseAuthUserCollisionException) {

            return "Für diese E-Mail-Adresse existiert bereits ein Konto.";
        }

        if (exception
                instanceof FirebaseAuthWeakPasswordException) {

            return "Das Passwort ist zu schwach.";
        }

        if (exception
                instanceof FirebaseAuthInvalidUserException) {

            return "Für diese E-Mail-Adresse wurde kein Konto gefunden.";
        }

        if (exception
                instanceof FirebaseAuthInvalidCredentialsException) {

            if (registration) {
                return "Die E-Mail-Adresse oder das Passwort ist ungültig.";
            }

            return "E-Mail-Adresse oder Passwort ist falsch.";
        }

        if (registration) {
            return "Registrierung fehlgeschlagen. Bitte erneut versuchen.";
        }

        return "Login fehlgeschlagen. Bitte erneut versuchen.";
    }

    /*
     * Öffnet das Dashboard und entfernt den Login-Screen
     * aus dem bisherigen Navigationsverlauf.
     */
    private void openDashboard() {
        Intent intent = new Intent(
                MainActivity.this,
                DashboardActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}