package com.hexnotech.commons.constants;

public enum Status {

    /** The operation completed successfully. */
    OK,

    /** The caller does not have permission to execute the specified operation. */
    PERMISSION_DENIED,

    /** The caller is not authenticated. */
    UNAUTHENTICATED,

    /** Some requested entity was not found. */
    NOT_FOUND,

    /** The entity that the client attempted to create already exists. */
    ALREADY_EXISTS,

    /** Client specified an invalid argument. */
    INVALID_ARGUMENT,

    /** Some invariant expected by the underlying system has been broken. */
    INTERNAL,

    /** The operation was rejected because the system is not in a state required to execute. */
    FAILED_PRECONDITION,

    /** The requested feature is disabled or not available. */
    UNAVAILABLE,

    /** The operation is not implemented or is not supported. */
    UNIMPLEMENTED,

    /** The operation has expired before completion. */
    DEADLINE_EXCEEDED,

    /** The request does not have valid authentication credentials. */
    RESOURCE_EXHAUSTED;

    public boolean isOk() {
        return this == OK;
    }

    public boolean isError() {
        return this != OK;
    }
}
