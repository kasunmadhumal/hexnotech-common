package com.hexnotech.commons.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DateTimeUtilsTest {

    @Test
    void startOfDay_shouldReturnMidnight() {
        LocalDate date = LocalDate.of(2026, 8, 28);
        LocalDateTime start = DateTimeUtils.startOfDay(date);
        assertEquals(0, start.getHour());
        assertEquals(0, start.getMinute());
    }

    @Test
    void endOfDay_shouldReturn235959() {
        LocalDate date = LocalDate.of(2026, 8, 28);
        LocalDateTime end = DateTimeUtils.endOfDay(date);
        assertEquals(23, end.getHour());
        assertEquals(59, end.getMinute());
    }

    @Test
    void toDisplayString_shouldFormatCorrectly() {
        LocalDateTime dt = LocalDateTime.of(2026, 8, 28, 8, 30);
        String result = DateTimeUtils.toDisplayString(dt);
        assertTrue(result.contains("28 Aug 2026"));
    }

    @Test
    void toDisplayString_nullShouldReturnEmpty() {
        assertEquals("", DateTimeUtils.toDisplayString(null));
    }
}
