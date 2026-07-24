package de.hwr.docuremind;

import android.content.Context;
import android.content.SharedPreferences;

/*
 * Diese Hilfsklasse verwaltet alle Einstellungen
 * des Erinnerungssystems an einer zentralen Stelle.
 *
 * Dadurch verwenden SettingsActivity und später ReminderWorker
 * garantiert dieselben Namen und Standardwerte.
 */
public final class ReminderPreferences {

    /*
     * Name der lokalen SharedPreferences-Datei.
     */
    private static final String PREFS_NAME =
            "DocuRemindPrefs";

    /*
     * Schlüssel für die beiden gespeicherten Einstellungen.
     */
    private static final String KEY_NOTIFICATIONS_ENABLED =
            "notificationsEnabled";

    private static final String KEY_REMINDER_DAYS =
            "reminderDays";

    /*
     * Standardwerte für neue Nutzer:
     * Erinnerungen sind aktiviert und starten 30 Tage vorher.
     */
    private static final boolean DEFAULT_NOTIFICATIONS_ENABLED =
            true;

    private static final int DEFAULT_REMINDER_DAYS =
            30;

    /*
     * Ein privater Konstruktor verhindert, dass versehentlich
     * ein Objekt dieser Hilfsklasse erstellt wird.
     */
    private ReminderPreferences() {
    }

    /*
     * Öffnet die gemeinsame Einstellungsdatei der App.
     *
     * MODE_PRIVATE bedeutet, dass nur DocuRemind
     * auf diese Werte zugreifen kann.
     */
    private static SharedPreferences getPreferences(
            Context context
    ) {
        return context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );
    }

    /*
     * Speichert den Hauptschalter für Erinnerungen.
     */
    public static void setNotificationsEnabled(
            Context context,
            boolean enabled
    ) {
        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        enabled
                )
                .apply();
    }

    /*
     * Lädt den gespeicherten Zustand des Hauptschalters.
     */
    public static boolean areNotificationsEnabled(
            Context context
    ) {
        return getPreferences(context)
                .getBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        DEFAULT_NOTIFICATIONS_ENABLED
                );
    }

    /*
     * Speichert den gewählten ersten Erinnerungszeitpunkt.
     *
     * Erlaubte Werte sind 30, 60 oder 90 Tage.
     */
    public static void setReminderDays(
            Context context,
            int reminderDays
    ) {
        getPreferences(context)
                .edit()
                .putInt(
                        KEY_REMINDER_DAYS,
                        reminderDays
                )
                .apply();
    }

    /*
     * Lädt den gespeicherten ersten Erinnerungszeitpunkt.
     */
    public static int getReminderDays(
            Context context
    ) {
        return getPreferences(context)
                .getInt(
                        KEY_REMINDER_DAYS,
                        DEFAULT_REMINDER_DAYS
                );
    }

    /*
     * Speichert beide Einstellungen in einem gemeinsamen Vorgang.
     *
     * Diese Methode wird vom Speichern-Button der SettingsActivity genutzt.
     */
    public static void saveSettings(
            Context context,
            boolean notificationsEnabled,
            int reminderDays
    ) {
        getPreferences(context)
                .edit()
                .putBoolean(
                        KEY_NOTIFICATIONS_ENABLED,
                        notificationsEnabled
                )
                .putInt(
                        KEY_REMINDER_DAYS,
                        reminderDays
                )
                .apply();
    }
}