/*
 *
 *  * Copyright(C) 2025 ACCELaero
 *  * All rights reserved.
 *  * THIS IS UNPUBLISHED PROPRIETARY SOURCE CODE OF
 *  * Information Systems Associates (pvt) Ltd.
 *  *
 *  * This copy of the Source Code is intended for Information Systems Associates (pvt) Ltd's internal
 *  * use only and is intended for view by persons duly authorized by the management of
 *  * Information Systems Associates (pvt) Ltd. No part of this file may be reproduced or distributed
 *  * in any form or by any means without the written approval of the Management of
 *  * Information Systems Associates (pvt) Ltd.
 *
 */

package com.hexnotech.commons.type.generic;

import com.hexnotech.commons.exception.HexnotechValidationException;
import com.hexnotech.commons.util.HexnotechUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class HexnotechValidator {
    
    private final List<Supplier<String>> validationErrorSuppliers = new ArrayList<>();
    
    public static final String REQUIRED_FIELD_ERROR = " required";
    public static final String CHARACTERS = " characters";
    public static final String MUST_BE = " must be ";

    public static final String SHOULD_NOT_BE_EMPTY = " should not be empty";
    public static final String UNPARSEABLE_DATE = "Unparseable date: ";

    public static HexnotechValidator of() {
        return new HexnotechValidator();
    }

    public HexnotechValidator addValidationError(Throwable throwable) {
        if (throwable != null) {
            validationErrorSuppliers.add(throwable::getMessage);
        }
        return this;
    }

    public HexnotechValidator addValidationError(Supplier<String> validationErrorSupplier) {
        if (validationErrorSupplier != null) {
            validationErrorSuppliers.add(validationErrorSupplier);
        }
        return this;
    }

    public HexnotechValidator addValidationError(Supplier<Boolean> validation, Supplier<String> errorSupplier) {
        validationErrorSuppliers.add(() -> validation.get() ? errorSupplier.get() : "");
        return this;
    }

    public HexnotechValidator addValidationError(boolean condition, Supplier<String> errorSupplier) {
        if (condition) {
            validationErrorSuppliers.add(errorSupplier);
        }
        return this;
    }

    public HexnotechValidator validateNotNull(Object object, String fieldName) {
        if (HexnotechUtil.isEmpty(object)) {
            validationErrorSuppliers.add(() -> fieldName + REQUIRED_FIELD_ERROR);
        }
        return this;
    }
    // ----------------- Start String Validations -----------------
    public HexnotechValidator validateStringField(String field, String fieldName) {
        if (HexnotechUtil.isTrimmedEmpty(field)) {
            validationErrorSuppliers.add(() -> fieldName + REQUIRED_FIELD_ERROR);
        }
        return this;
    }
    public HexnotechValidator validateLength(String field, String fieldName, Integer length) {
      return validateLength(field, fieldName, length, length);
    }

    public HexnotechValidator validateMaxLength(String field, String fieldName, Integer maxLength) {
        return validateLength(field, fieldName, 0, maxLength);
    }

    public HexnotechValidator validateMinLength(String field, String fieldName, Integer minLength) {
        return validateLength(field, fieldName, minLength, 0);
    }

    public HexnotechValidator validateLength(String field, String fieldName, int min, int max) {
        if (!HexnotechUtil.isTrimmedEmpty(field) && !HexnotechUtil.withinCharacterLimit(field, min, max)) {
                validationErrorSuppliers.add(() -> buildCharLimitError(fieldName, min, max));
            }
        return this;
    }

    private String buildCharLimitError(String fieldName, int min, int max) {
        if (min != 0 && max != 0 && min == max) {
            return fieldName + MUST_BE + "exactly " + min + CHARACTERS;
        }
        if (min != 0 && max == 0) {
            return fieldName + MUST_BE + "at least " + min + CHARACTERS;
        }
        if (min == 0 && max != 0) {
        return fieldName + MUST_BE + "at most " + max + CHARACTERS;
        }
        return fieldName + MUST_BE + "between " + min + " and " + max + CHARACTERS;
    }
    // ----------------- End String Validations -----------------

    public HexnotechValidator validateForNumber(String value, String fieldName) {
        if (!HexnotechUtil.isTrimmedEmpty(value) && !HexnotechUtil.isNumber(value)) {
            validationErrorSuppliers.add(() -> fieldName + " must be a valid number");
        }
        return this;
    }

    public HexnotechValidator validatePositive(Number value, String fieldName) {
        if (!HexnotechUtil.isPositiveOrZero(value)) {
            validationErrorSuppliers.add(() -> fieldName + " should be positive ");
        }
        return this;
    }
    
    public <T extends Comparable<T>> HexnotechValidator validateRange(T min, T max, String fieldName) {
        if (!HexnotechUtil.isEmpty(min) && !HexnotechUtil.isEmpty(max) && min.compareTo(max) >= 0) {
                validationErrorSuppliers.add(() ->
                        "Invalid range for " + fieldName + ": min (" + min + ") must be less than max (" + max + ")");
            }
        return this;
    }
    
    public <T extends Comparable<T>> HexnotechValidator validateRange(T value, T min, T max, String fieldName) {
        if (!HexnotechUtil.isEmpty(value) && !HexnotechUtil.withinRange(value, min, max)) {
                validationErrorSuppliers.add(() ->
                        fieldName + " should be between " + min + " and " + max);
            }
        return this;
    }
    

    public HexnotechValidator validateList(List<String> field, String fieldName) {
        if (HexnotechUtil.isEmpty(field)) {
            validationErrorSuppliers.add(() -> fieldName + REQUIRED_FIELD_ERROR);
        }
        return this;
    }

    public HexnotechValidator validateTypeList(Collection<?> field, String fieldName) {
        if (HexnotechUtil.isEmpty(field)) {
            validationErrorSuppliers.add(() -> fieldName + REQUIRED_FIELD_ERROR);
        }
        return this;
    }

    public HexnotechValidator validateNonEmptyList(Collection<?> list, String fieldName) {
        return validateNonEmptyList(list, () -> fieldName + SHOULD_NOT_BE_EMPTY);
    }

    public HexnotechValidator validateNonEmptyList(Collection<?> list, Supplier<String> errorMessage) {
        if (HexnotechUtil.isNotEmpty(list)) {
            validationErrorSuppliers.add(errorMessage);
        }
        return this;
    }

    public static void validate(boolean condition, Supplier<String> errorSupplier) {
        HexnotechValidator.of().addValidationError(() -> condition, errorSupplier).validate();
    }
    
    public void validate() {
        String errorMessage = validationErrorSuppliers.stream()
                .map(Supplier::get)
                .filter(HexnotechUtil::isNotEmpty)
                .collect(Collectors.joining(", "));
        if (HexnotechUtil.isNotEmpty(errorMessage)) {
            throw new HexnotechValidationException(errorMessage);
        }
    }

}