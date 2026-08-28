package com.hexnotech.commons.annotation.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NationalIdValidatorTest {

    private final NationalIdValidator validator = new NationalIdValidator();
    private final ConstraintValidatorContext ctx = mock(ConstraintValidatorContext.class);

    @Test
    void nullIsValid() {
        assertTrue(validator.isValid(null, ctx));
    }

    @Test
    void tenDigitIsValid() {
        assertTrue(validator.isValid("1234567890", ctx));
    }

    @Test
    void thirteenDigitIsValid() {
        assertTrue(validator.isValid("1234567890123", ctx));
    }

    @Test
    void seventeenDigitIsValid() {
        assertTrue(validator.isValid("12345678901234567", ctx));
    }

    @Test
    void elevenDigitIsInvalid() {
        assertFalse(validator.isValid("12345678901", ctx));
    }

    @Test
    void nonNumericIsInvalid() {
        assertFalse(validator.isValid("123-456-7890", ctx));
    }
}
