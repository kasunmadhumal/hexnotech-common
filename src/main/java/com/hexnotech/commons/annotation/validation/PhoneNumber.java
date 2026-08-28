package com.hexnotech.commons.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

/**
 * Validates that the annotated string is a valid international phone number.
 * <p>
 * Accepted formats (with optional leading {@code +} and country code):
 * <ul>
 *   <li>{@code +8801712345678}</li>
 *   <li>{@code 01712345678}</li>
 *   <li>{@code +1 (555) 123-4567}</li>
 * </ul>
 *
 * <pre>{@code
 * @PhoneNumber
 * private String phone;
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})   // delegates to @Pattern below
@Pattern(
        regexp = "^\\+?[0-9. ()\\-]{7,20}$",
        message = "{hexnotech.validation.PhoneNumber.message}"
)
public @interface PhoneNumber {

    String message() default "Invalid phone number format";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
