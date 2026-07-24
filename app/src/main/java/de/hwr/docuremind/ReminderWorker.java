package de.hwr.docuremind;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/*
 * Dieser Worker prüft regelmäßig die gespeicherten Dokumentfristen.
 *
 * WorkManager startet ihn im Hintergrund ungefähr einmal täglich.
 * Eine sekundengenaue Ausführung ist für DocuRemind nicht notwendig.
 */
public class ReminderWorker extends Worker {

    /*
     * Dieser Konstruktor wird von WorkManager benötigt.
     */
    public ReminderWorker(
            @NonNull Context context,
            @NonNull WorkerParameters workerParameters
    ) {
        super(context, workerParameters);
    }

    /*
     * Diese Methode enthält die eigentliche Hintergrundaufgabe.
     */
    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

        /*
         * Bei deaktivierten Erinnerungen muss nichts geprüft werden.
         */
        if (!ReminderPreferences.areNotificationsEnabled(context)) {
            return Result.success();
        }

        /*
         * Dokumente gehören immer zum aktuell angemeldeten Nutzer.
         */
        FirebaseUser currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            return Result.success();
        }

        try {
            /*
             * Alle Dokumente des Nutzers aus Firestore laden.
             */
            QuerySnapshot result = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(currentUser.getUid())
                            .collection("documents")
                            .get()
            );

            int firstReminderDays =
                    ReminderPreferences.getReminderDays(context);

            /*
             * Jedes Dokument einzeln auf seine Frist prüfen.
             */
            for (DocumentSnapshot document : result.getDocuments()) {
                checkDocument(
                        context,
                        document,
                        firstReminderDays
                );
            }

            return Result.success();

        } catch (Exception exception) {
            /*
             * Bei einem vorübergehenden Netzwerkfehler
             * versucht WorkManager die Aufgabe später erneut.
             */
            return Result.retry();
        }
    }

    /*
     * Prüft die Frist eines einzelnen Dokuments.
     */
    private void checkDocument(
            Context context,
            DocumentSnapshot document,
            int firstReminderDays
    ) {
        long expiryDateMillis =
                readExpiryDateMillis(document);

        if (expiryDateMillis <= 0) {
            return;
        }

        long daysUntil =
                DocumentDateUtils.getDaysUntil(
                        expiryDateMillis
                );

        if (!shouldSendReminder(
                daysUntil,
                firstReminderDays
        )) {
            return;
        }

        String documentName =
                document.getString("name");

        NotificationHelper.showDocumentReminder(
                context,
                document.getId(),
                documentName,
                daysUntil
        );
    }

    /*
     * Legt fest, an welchen Fristen eine Benachrichtigung erscheint.
     */
    private boolean shouldSendReminder(
            long daysUntil,
            int firstReminderDays
    ) {
        /*
         * Vom Nutzer ausgewählte erste Erinnerung:
         * 30, 60 oder 90 Tage vorher.
         */
        if (daysUntil == firstReminderDays) {
            return true;
        }

        /*
         * Feste Folgeerinnerungen vor dem Ablaufdatum.
         */
        if (daysUntil == 14
                || daysUntil == 7
                || daysUntil == 3
                || daysUntil == 1
                || daysUntil == 0) {

            return true;
        }

        /*
         * Abgelaufene Dokumente werden alle sieben Tage gemeldet.
         */
        if (daysUntil < 0) {
            long overdueDays = Math.abs(daysUntil);

            return overdueDays % 7 == 0;
        }

        return false;
    }

    /*
     * Liest den Ablaufzeitpunkt aus Firestore.
     *
     * Für ältere Dokumente kann weiterhin das Textdatum
     * in einen Zeitstempel umgewandelt werden.
     */
    private long readExpiryDateMillis(
            DocumentSnapshot document
    ) {
        Object storedMillis =
                document.get("expiryDateMillis");

        if (storedMillis instanceof Number) {
            return ((Number) storedMillis).longValue();
        }

        return DocumentDateUtils.parseDateToMillis(
                document.getString("expiryDate")
        );
    }
}