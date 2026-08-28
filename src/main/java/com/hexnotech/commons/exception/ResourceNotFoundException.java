package com.hexnotech.commons.exception;

/**
 * Thrown when a requested resource cannot be found (HTTP 404).
 *
 * <pre>{@code
 * User user = userRepository.findById(id)
 *     .orElseThrow(() -> new ResourceNotFoundException("User", id));
 * }</pre>
 */
public class ResourceNotFoundException extends BaseException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String resourceName, Object identifier) {
        super(ERROR_CODE, resourceName + " not found with identifier: " + identifier);
    }

    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }
}
