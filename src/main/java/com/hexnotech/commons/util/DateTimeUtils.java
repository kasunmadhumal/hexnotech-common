package com.hexnotech.commons.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

/**
 * Utility methods for common date/time operations across Hexnotech services.
 * All methods are static — no instantiation needed.
 */
public final class DateTimeUtils {

    public static final ZoneId DHAKA = ZoneId.of("Asia/Dhaka");
    public static final ZoneId UTC   = ZoneId.of("UTC");

    private static final DateTimeFormatter ISO_LOCAL  = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    private DateTimeUtils() {}

    /** Returns the current time in Dhaka timezone. */
    public static ZonedDateTime nowDhaka() {
        return ZonedDateTime.now(DHAKA);
    }

    /** Returns the current time in UTC. */
    public static ZonedDateTime nowUtc() {
        return ZonedDateTime.now(UTC);
    }

    /**
     * Converts a {@link LocalDateTime} (assumed UTC) to Dhaka time.
     *
     * @param utcTime the UTC LocalDateTime
     * @return ZonedDateTime in Asia/Dhaka
     */
    public static ZonedDateTime utcToDhaka(LocalDateTime utcTime) {
        return utcTime.atZone(UTC).withZoneSameInstant(DHAKA);
    }

    /**
     * Converts a {@link LocalDateTime} (assumed Dhaka) to UTC.
     *
     * @param dhakaTime the Dhaka LocalDateTime
     * @return ZonedDateTime in UTC
     */
    public static ZonedDateTime dhakaToUtc(LocalDateTime dhakaTime) {
        return dhakaTime.atZone(DHAKA).withZoneSameInstant(UTC);
    }

    /**
     * Formats a {@link LocalDateTime} using the display format:
     * {@code "dd MMM yyyy, hh:mm a"} (e.g. "28 Aug 2026, 08:30 AM").
     */
    public static String toDisplayString(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FMT);
    }

    /**
     * Formats a {@link LocalDateTime} as an ISO-8601 string.
     */
    public static String toIsoString(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_LOCAL);
    }

    /**
     * Parses an ISO-8601 string to {@link LocalDateTime}.
     *
     * @param isoString e.g. "2026-08-28T08:30:00"
     * @return the parsed LocalDateTime
     */
    public static LocalDateTime parseIso(String isoString) {
        return LocalDateTime.parse(isoString, ISO_LOCAL);
    }

    /**
     * Checks whether a given date range [start, end] contains {@code now}.
     */
    public static boolean isWithinRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(start) && !now.isAfter(end);
    }

    /**
     * Returns the start of the day (midnight) for a given date.
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        return date.atStartOfDay();
    }

    /**
     * Returns the end of the day (23:59:59.999999999) for a given date.
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        return date.atTime(LocalTime.MAX);
    }
}
