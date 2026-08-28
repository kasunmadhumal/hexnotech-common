package com.hexnotech.commons.exception;

import lombok.Getter;

/**
 * Abstract base exception for all Hexnotech domain exceptions.
 * <p>
 * All custom exceptions should extend this class rather than
 * {@link RuntimeException} directly, so that the
 * {@link GlobalExceptionHandler} can handle them uniformly.
 *
 * <pre>{@code
 * public class PaymentFailedException extends BaseException {
 *     public PaymentFailedException(String detail) {
 *         super("PAYMENT_FAILED", detail);
 *     }
 * }
 * }</pre>
 */
@Getter
public abstract class BaseException extends RuntimeException {

    /**
     * Machine-readable error code (e.g. {@code "RESOURCE_NOT_FOUND"}).
     * Exposed in the API error response for client-side handling.
     */
    private final String errorCode;

    protected BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
