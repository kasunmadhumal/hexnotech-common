package com.hexnotech.commons.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void isBlank_nullAndEmptyAreBlank() {
        assertTrue(StringUtils.isBlank(null));
        assertTrue(StringUtils.isBlank(""));
        assertTrue(StringUtils.isBlank("   "));
        assertFalse(StringUtils.isBlank("a"));
    }

    @Test
    void toSlug_convertsSpacesAndSpecialChars() {
        assertEquals("hello-world", StringUtils.toSlug("Hello World!"));
        assertEquals("cafe-au-lait", StringUtils.toSlug("Café au lait"));
    }

    @Test
    void mask_masksMiddleChars() {
        // "01712345678" = 11 chars, keep first 3 + last 2 → 6 masked chars
        assertEquals("017******78", StringUtils.mask("01712345678", 3, 2, '*'));
    }

    @Test
    void toCamelCase_fromSnakeCase() {
        assertEquals("createdAt", StringUtils.toCamelCase("created_at"));
        assertEquals("firstName", StringUtils.toCamelCase("first-name"));
    }

    @Test
    void truncate_shorterThanMaxReturnsOriginal() {
        assertEquals("hello", StringUtils.truncate("hello", 10));
    }

    @Test
    void truncate_longerThanMaxAppendsDots() {
        assertTrue(StringUtils.truncate("Hello World!", 8).endsWith("..."));
    }
}
