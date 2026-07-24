package de.hwr.docuremind;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/*
 * Diese Hilfsklasse plant und beendet den täglichen ReminderWorker.
 *
 * Ein eindeutiger Name verhindert mehrere parallele Worker.
 */
public final class ReminderScheduler {

    /*
     * Eindeutiger Name des täglichen Hintergrundauftrags.
     */
    private static final String WORK_NAME =
            "docuremind_daily_reminder_check";

    /*
     * Diese Hilfsklasse soll nicht als Objekt erstellt werden.
     */
    private ReminderScheduler() {
    }

    /*
     * Startet oder beendet den Worker passend zur Nutzereinstellung.
     */
    public static void updateSchedule(
            Context context
    ) {
        if (ReminderPreferences
                .areNotificationsEnabled(context)) {

            scheduleDailyCheck(context);

        } else {
            cancelDailyCheck(context);
        }
    }

    /*
     * Plant ungefähr alle 24 Stunden eine Fristenprüfung.
     */
    private static void scheduleDailyCheck(
            Context context
    ) {
        /*
         * Firestore benötigt eine Internetverbindung.
         */
        Constraints constraints =
                new Constraints.Builder()
                        .setRequiredNetworkType(
                                NetworkType.CONNECTED
                        )
                        .build();

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(
                        ReminderWorker.class,
                        24,
                        TimeUnit.HOURS
                )
                        .setConstraints(constraints)
                        .build();

        /*
         * KEEP verhindert, dass derselbe tägliche Worker
         * mehrfach angelegt wird.
         */
        WorkManager.getInstance(
                        context.getApplicationContext()
                )
                .enqueueUniquePeriodicWork(
                        WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        request
                );
    }

    /*
     * Entfernt den täglichen Worker,
     * wenn Erinnerungen deaktiviert wurden.
     */
    private static void cancelDailyCheck(
            Context context
    ) {
        WorkManager.getInstance(
                        context.getApplicationContext()
                )
                .cancelUniqueWork(WORK_NAME);
    }
}