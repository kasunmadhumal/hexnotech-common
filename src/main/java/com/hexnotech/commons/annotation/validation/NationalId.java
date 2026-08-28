package com.hexnotech.commons.annotation.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates that the annotated string is a valid Bangladeshi National ID (NID).
 * <p>
 * Bangladesh NIDs are either:
 * <ul>
 *   <li>10 digits (smart card format)</li>
 *   <li>13 digits (old format)</li>
 *   <li>17 digits (computerised format)</li>
 * </ul>
 *
 * <pre>{@code
 * @NationalId
 * private String nid;
 * }</pre>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = NationalIdValidator.class)
public @interface NationalId {

    String message() default "Invalid National ID — must be 10, 13, or 17 digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
