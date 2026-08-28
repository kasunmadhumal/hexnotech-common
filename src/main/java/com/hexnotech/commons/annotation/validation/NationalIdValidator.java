package com.hexnotech.commons.annotation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validates Bangladeshi National ID numbers.
 * Accepts 10-digit, 13-digit, or 17-digit purely numeric strings.
 */
public class NationalIdValidator implements ConstraintValidator<NationalId, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // use @NotBlank / @NotNull separately
        }
        String digits = value.trim();
        if (!digits.matches("\\d+")) {
            return false;
        }
        int len = digits.length();
        return len == 10 || len == 13 || len == 17;
    }
}
