package com.hexnotech.commons.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Standard API response envelope used across all Hexnotech services.
 * <p>
 * Every REST endpoint should wrap its return value in an {@code ApiResponse}:
 *
 * <pre>{@code
 * // Success
 * return ResponseEntity.ok(ApiResponse.success("User created", userDto));
 *
 * // Error (typically from GlobalExceptionHandler)
 * return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
 * }</pre>
 *
 * @param <T> the type of the response payload
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** Whether the request was processed successfully. */
    private final boolean success;

    /** Human-readable message describing the result. */
    private final String message;

    /** The actual response payload — {@code null} on error responses. */
    private final T data;

    /** ISO-8601 timestamp of when the response was generated. */
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();

    // ─── Factory helpers ──────────────────────────────────────────────────────

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success("Success", data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
