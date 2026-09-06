package com.hexnotech.commons.util;

import com.hexnotech.commons.exception.HexnotechValidationException;
import org.springframework.util.CollectionUtils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Collectors;

public class ValidatorUtil {

    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();


    public static <E> void validate(E object) {
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<Object>> validations = validator.validate(object);
        if (!CollectionUtils.isEmpty(validations)) {
            String errorMessage = validations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw HexnotechValidationException.by(errorMessage);
        }
    }
}

