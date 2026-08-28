package com.hexnotech.commons.exception;

/**
 * Thrown when a business rule or invariant is violated (HTTP 422).
 * <p>
 * Use this for domain-level validations that cannot be expressed as bean
 * validation constraints — e.g. "A booking cannot be cancelled after check-in".
 *
 * <pre>{@code
 * if (booking.isCheckedIn()) {
 *     throw new BusinessException("Cannot cancel a booking after check-in");
 * }
 * }</pre>
 */
public class BusinessException extends BaseException {

    private static final String ERROR_CODE = "BUSINESS_RULE_VIOLATION";

    public BusinessException(String message) {
        super(ERROR_CODE, message);
    }

    public BusinessException(String errorCode, String message) {
        super(errorCode, message);
    }
}
