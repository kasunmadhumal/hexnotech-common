package com.hexnotech.commons.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility methods for common String operations across Hexnotech services.
 * All methods are null-safe unless otherwise noted.
 */
public final class StringUtils {

    private static final Pattern SLUG_DISALLOWED = Pattern.compile("[^a-z0-9\\-]");
    private static final Pattern WHITESPACE       = Pattern.compile("[\\s]+");

    private StringUtils() {}

    /** Returns {@code true} if the string is null or blank. */
    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** Returns the string trimmed, or an empty string if null. */
    public static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    /** Returns the string trimmed, or {@code null} if the value is null. */
    public static String trimOrNull(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * Masks a string, leaving only the first {@code visibleStart} and last
     * {@code visibleEnd} characters visible.
     *
     * <pre>{@code
     * mask("01712345678", 3, 2, '*') → "017*******78"
     * }</pre>
     *
     * @param value        the original string
     * @param visibleStart number of leading characters to keep
     * @param visibleEnd   number of trailing characters to keep
     * @param maskChar     the masking character (e.g. '*')
     * @return masked string
     */
    public static String mask(String value, int visibleStart, int visibleEnd, char maskChar) {
        if (isBlank(value)) return value;
        int len = value.length();
        if (visibleStart + visibleEnd >= len) return value;
        String masked = String.valueOf(maskChar).repeat(len - visibleStart - visibleEnd);
        return value.substring(0, visibleStart) + masked + value.substring(len - visibleEnd);
    }

    /**
     * Converts a string to a URL-friendly slug.
     *
     * <pre>{@code
     * toSlug("Hello World!") → "hello-world"
     * toSlug("Café au lait") → "cafe-au-lait"
     * }</pre>
     */
    public static String toSlug(String input) {
        if (isBlank(input)) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return SLUG_DISALLOWED
                .matcher(WHITESPACE.matcher(normalized.toLowerCase(Locale.ROOT)).replaceAll("-"))
                .replaceAll("")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Converts a snake_case or kebab-case string to camelCase.
     *
     * <pre>{@code
     * toCamelCase("created_at")   → "createdAt"
     * toCamelCase("first-name")   → "firstName"
     * }</pre>
     */
    public static String toCamelCase(String input) {
        if (isBlank(input)) return trimOrEmpty(input);
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        for (char c : input.toCharArray()) {
            if (c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Capitalises only the first letter of the given string.
     *
     * <pre>{@code
     * capitalize("hello") → "Hello"
     * }</pre>
     */
    public static String capitalize(String s) {
        if (isBlank(s)) return trimOrEmpty(s);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /**
     * Truncates a string to {@code maxLength} characters, appending "..." if truncated.
     */
    public static String truncate(String s, int maxLength) {
        if (s == null || s.length() <= maxLength) return s;
        return s.substring(0, maxLength - 3) + "...";
    }
}
