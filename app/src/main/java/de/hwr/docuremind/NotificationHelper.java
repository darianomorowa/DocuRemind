package de.hwr.docuremind;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

/*
 * Diese Hilfsklasse bündelt die gesamte Benachrichtigungslogik.
 *
 * Sie erstellt den Android-Benachrichtigungskanal,
 * fordert bei Bedarf die Nutzerberechtigung an
 * und zeigt Fristenerinnerungen an.
 */
public final class NotificationHelper {

    /*
     * Alle Fristenerinnerungen werden diesem Kanal zugeordnet.
     *
     * Nutzer können die Einstellungen dieses Kanals später
     * auch direkt in den Android-Systemeinstellungen ändern.
     */
    public static final String CHANNEL_ID =
            "document_reminders";

    /*
     * Diese Nummer identifiziert unsere Berechtigungsanfrage.
     */
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE =
            1001;

    /*
     * Der private Konstruktor verhindert,
     * dass unnötig ein Objekt der Hilfsklasse erstellt wird.
     */
    private NotificationHelper() {
    }

    /*
     * Erstellt den Benachrichtigungskanal für Android 8 oder neuer.
     *
     * Die Methode darf mehrfach aufgerufen werden.
     * Android erstellt einen bereits vorhandenen Kanal nicht erneut.
     */
    public static void createNotificationChannel(
            Context context
    ) {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.O) {

            return;
        }

        String channelName =
                "Dokumentenfristen";

        String channelDescription =
                "Erinnerungen an bevorstehende und abgelaufene Dokumente";

        /*
         * IMPORTANCE_DEFAULT zeigt eine normale Benachrichtigung
         * mit Ton gemäß den Systemeinstellungen des Nutzers.
         */
        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        channelName,
                        NotificationManager.IMPORTANCE_DEFAULT
                );

        channel.setDescription(
                channelDescription
        );

        NotificationManager notificationManager =
                context.getSystemService(
                        NotificationManager.class
                );

        if (notificationManager != null) {
            notificationManager
                    .createNotificationChannel(channel);
        }
    }

    /*
     * Fordert ab Android 13 die Benachrichtigungsberechtigung an.
     *
     * Auf älteren Android-Versionen ist diese Laufzeitabfrage
     * nicht erforderlich.
     */
    public static void requestPermissionIfNeeded(
            Activity activity
    ) {
        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.TIRAMISU) {

            return;
        }

        boolean permissionGranted =
                ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED;

        if (!permissionGranted) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{
                            Manifest.permission.POST_NOTIFICATIONS
                    },
                    NOTIFICATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /*
     * Prüft, ob DocuRemind aktuell Benachrichtigungen anzeigen darf.
     *
     * Neben der Android-13-Berechtigung wird auch berücksichtigt,
     * ob der Nutzer Benachrichtigungen systemweit deaktiviert hat.
     */
    public static boolean canSendNotifications(
            Context context
    ) {
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.TIRAMISU) {

            boolean permissionGranted =
                    ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED;

            if (!permissionGranted) {
                return false;
            }
        }

        return NotificationManagerCompat
                .from(context)
                .areNotificationsEnabled();
    }

    /*
     * Erstellt und zeigt eine Erinnerung für ein Dokument.
     *
     * documentId und daysUntil erzeugen gemeinsam eine stabile ID.
     * Dadurch wird dieselbe Erinnerung bei einem erneuten Worker-Lauf
     * aktualisiert, statt mehrfach nebeneinander angezeigt zu werden.
     */
    public static void showDocumentReminder(
            Context context,
            String documentId,
            String documentName,
            long daysUntil
    ) {
        /*
         * Der Nutzer kann Erinnerungen innerhalb von DocuRemind
         * vollständig deaktivieren.
         */
        if (!ReminderPreferences
                .areNotificationsEnabled(context)) {

            return;
        }

        /*
         * Ohne Android-Berechtigung darf keine Notification erscheinen.
         */
        if (!canSendNotifications(context)) {
            return;
        }

        createNotificationChannel(context);

        /*
         * Beim Antippen der Benachrichtigung
         * wird das Dashboard von DocuRemind geöffnet.
         */
        Intent openAppIntent =
                new Intent(
                        context,
                        DashboardActivity.class
                );

        openAppIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        int requestCode =
                documentId.hashCode()
                        & 0x7fffffff;

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        context,
                        requestCode,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        String title =
                createNotificationTitle(daysUntil);

        String message =
                createNotificationMessage(
                        documentName,
                        daysUntil
                );

        /*
         * NotificationCompat sorgt dafür,
         * dass die Benachrichtigung auf verschiedenen Android-Versionen
         * möglichst einheitlich funktioniert.
         */
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                R.drawable.ic_notification_document
                        )
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(message)
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_DEFAULT
                        )
                        .setCategory(
                                NotificationCompat.CATEGORY_REMINDER
                        )
                        .setVisibility(
                                NotificationCompat.VISIBILITY_PRIVATE
                        )
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        /*
         * Gleiche Kombination aus Dokument und Fristenstufe
         * erhält immer dieselbe Notification-ID.
         */
        int notificationId =
                (documentId + ":" + daysUntil)
                        .hashCode()
                        & 0x7fffffff;

        NotificationManagerCompat
                .from(context)
                .notify(
                        notificationId,
                        builder.build()
                );
    }

    /*
     * Erstellt die Überschrift abhängig von der Dringlichkeit.
     */
    private static String createNotificationTitle(
            long daysUntil
    ) {
        if (daysUntil < 0) {
            return "Dokument abgelaufen";
        }

        if (daysUntil == 0) {
            return "Dokument läuft heute ab";
        }

        if (daysUntil == 1) {
            return "Dokument läuft morgen ab";
        }

        if (daysUntil <= 3) {
            return "Dringende Dokumentenfrist";
        }

        return "Dokumentenfrist nähert sich";
    }

    /*
     * Erstellt den ausführlichen Text der Erinnerung.
     */
    private static String createNotificationMessage(
            String documentName,
            long daysUntil
    ) {
        String safeName = documentName;

        if (safeName == null
                || safeName.trim().isEmpty()) {

            safeName = "Ein Dokument";
        }

        if (daysUntil < 0) {
            long overdueDays =
                    Math.abs(daysUntil);

            if (overdueDays == 1) {
                return safeName
                        + " ist seit einem Tag abgelaufen.";
            }

            return safeName
                    + " ist seit "
                    + overdueDays
                    + " Tagen abgelaufen.";
        }

        if (daysUntil == 0) {
            return safeName
                    + " läuft heute ab.";
        }

        if (daysUntil == 1) {
            return safeName
                    + " läuft morgen ab.";
        }

        return safeName
                + " läuft in "
                + daysUntil
                + " Tagen ab.";
    }
}