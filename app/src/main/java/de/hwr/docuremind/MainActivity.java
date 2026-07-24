package de.hwr.docuremind;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;
    private Button buttonLogin;
    private Button buttonRegister;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Verbindung zwischen Java-Code und XML-Elementen
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Firebase Authentication initialisieren
        firebaseAuth = FirebaseAuth.getInstance();

        buttonLogin.setOnClickListener(view -> loginUser());

        buttonRegister.setOnClickListener(view -> registerUser());
    }

    /*
     * Diese Methode wird aufgerufen, wenn der Login-Screen sichtbar wird.
     *
     * Ist bereits ein Firebase-Nutzer angemeldet,
     * wird der Login übersprungen und direkt das Dashboard geöffnet.
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            Intent intent = new Intent(
                    MainActivity.this,
                    DashboardActivity.class
            );

            /*
             * Der Login-Screen wird aus dem bisherigen Verlauf entfernt.
             *
             * Dadurch gelangt der Nutzer über die Zurück-Taste
             * nicht wieder zum Login.
             */
            intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        }
    }
    
    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (!isInputValid(email, password)) {
            return;
        }

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(MainActivity.this, "Login erfolgreich", Toast.LENGTH_SHORT).show();
                    openDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Login fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void registerUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (!isInputValid(email, password)) {
            return;
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(MainActivity.this, "Registrierung erfolgreich", Toast.LENGTH_SHORT).show();
                    openDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Registrierung fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isInputValid(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Bitte E-Mail eingeben");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Bitte Passwort eingeben");
            return false;
        }

        if (password.length() < 6) {
            editPassword.setError("Passwort muss mindestens 6 Zeichen haben");
            return false;
        }

        return true;
    }

    private void openDashboard() {
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}