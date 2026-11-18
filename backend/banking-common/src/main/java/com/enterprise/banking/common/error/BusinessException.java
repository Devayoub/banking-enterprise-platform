package com.enterprise.banking.common.error;

/** Expected, domain-level failure that maps to a well-defined {@link ErrorCode} and HTTP status. */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, errorCode.defaultMessage());
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode errorCode() {
        return errorCode;
    }

    public static BusinessException notFound(String resource) {
        return new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, resource + " not found.");
    }
}
