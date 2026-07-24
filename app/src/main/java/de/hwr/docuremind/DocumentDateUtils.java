package de.hwr.docuremind;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/*
 * Diese Hilfsklasse enthält alle Berechnungen rund um Ablaufdaten.
 *
 * Dadurch müssen Datumsberechnungen nicht mehrfach in verschiedenen
 * Activities programmiert werden.
 */
public final class DocumentDateUtils {

    /*
     * Einheitliches Anzeigeformat für alle Dokumentdaten.
     * Beispiel: 31.12.2026
     */
    private static final String DATE_PATTERN = "dd.MM.yyyy";

    /*
     * Anzahl der Millisekunden eines Tages.
     * Dieser Wert wird zur Berechnung der verbleibenden Tage genutzt.
     */
    private static final long MILLIS_PER_DAY =
            24L * 60L * 60L * 1000L;

    /*
     * Der private Konstruktor verhindert, dass versehentlich ein Objekt
     * dieser Hilfsklasse erstellt wird.
     *
     * Die Methoden werden direkt über DocumentDateUtils aufgerufen.
     */
    private DocumentDateUtils() {
    }

    /*
     * Wandelt einen Zeitstempel in ein lesbares deutsches Datum um.
     *
     * Beispiel:
     * 1798671600000 wird zu 31.12.2026
     */
    public static String formatDate(long dateMillis) {
        if (dateMillis <= 0) {
            return "";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        DATE_PATTERN,
                        Locale.GERMANY
                );

        return dateFormat.format(
                new Date(dateMillis)
        );
    }

    /*
     * Wandelt einen Datumstext zurück in einen Zeitstempel.
     *
     * Diese Methode dient auch als Fallback für ältere Dokumente,
     * bei denen das Datum bisher nur als Text gespeichert wurde.
     */
    public static long parseDateToMillis(String dateText) {
        if (dateText == null || dateText.trim().isEmpty()) {
            return 0L;
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        DATE_PATTERN,
                        Locale.GERMANY
                );

        /*
         * Lenient false verhindert, dass ungültige Daten wie
         * 40.15.2026 automatisch umgerechnet werden.
         */
        dateFormat.setLenient(false);

        try {
            Date parsedDate = dateFormat.parse(dateText);

            if (parsedDate == null) {
                return 0L;
            }

            return normalizeDate(
                    parsedDate.getTime()
            );

        } catch (ParseException exception) {
            /*
             * Bei einem ungültigen Datum wird 0 zurückgegeben.
             * Die App kann dadurch erkennen, dass kein gültiges Datum vorliegt.
             */
            return 0L;
        }
    }

    /*
     * Setzt die Uhrzeit eines Datums immer auf 12:00 Uhr.
     *
     * Dadurch werden Probleme durch Sommer- und Winterzeit bei der
     * Berechnung von Kalendertagen reduziert.
     */
    public static long normalizeDate(long dateMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dateMillis);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    /*
     * Berechnet, wie viele Tage zwischen heute und dem Ablaufdatum liegen.
     *
     * Positiver Wert: Dokument läuft in der Zukunft ab.
     * Wert 0: Dokument läuft heute ab.
     * Negativer Wert: Dokument ist bereits abgelaufen.
     */
    public static long getDaysUntil(long expiryDateMillis) {
        if (expiryDateMillis <= 0) {
            return 0L;
        }

        long today =
                normalizeDate(
                        System.currentTimeMillis()
                );

        long expiryDate =
                normalizeDate(expiryDateMillis);

        long difference =
                expiryDate - today;

        return Math.round(
                (double) difference / MILLIS_PER_DAY
        );
    }

    /*
     * Erzeugt einen verständlichen Statustext für das Dashboard.
     *
     * Beispiele:
     * "In 14 Tagen"
     * "Morgen fällig"
     * "Heute fällig"
     * "Seit 3 Tagen abgelaufen"
     */
    public static String getStatusText(
            long expiryDateMillis
    ) {
        if (expiryDateMillis <= 0) {
            return "Datum fehlt";
        }

        long daysUntil =
                getDaysUntil(expiryDateMillis);

        if (daysUntil < 0) {
            long overdueDays =
                    Math.abs(daysUntil);

            if (overdueDays == 1) {
                return "Seit 1 Tag abgelaufen";
            }

            return "Seit "
                    + overdueDays
                    + " Tagen abgelaufen";
        }

        if (daysUntil == 0) {
            return "Heute fällig";
        }

        if (daysUntil == 1) {
            return "Morgen fällig";
        }

        return "In "
                + daysUntil
                + " Tagen";
    }

    /*
     * Bestimmt anhand der verbleibenden Tage die Statusfarbe.
     *
     * Rot: maximal 3 Tage oder bereits abgelaufen
     * Orange: maximal 14 Tage
     * Blau: maximal 30 Tage
     * Grün: mehr als 30 Tage
     */
    public static int getStatusColorResource(
            long expiryDateMillis
    ) {
        if (expiryDateMillis <= 0) {
            return R.color.docu_text_secondary;
        }

        long daysUntil =
                getDaysUntil(expiryDateMillis);

        if (daysUntil <= 3) {
            return R.color.docu_danger;
        }

        if (daysUntil <= 14) {
            return R.color.docu_warning;
        }

        if (daysUntil <= 30) {
            return R.color.docu_primary;
        }

        return R.color.docu_success;
    }
}