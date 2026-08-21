package com.biz.ontology.common.exception;

public class BusinessException extends RuntimeException {

    private final PlatformErrorCode errorCode;

    public BusinessException(PlatformErrorCode errorCode) {
        this(errorCode, errorCode.getDefaultMessage());
    }

    public BusinessException(PlatformErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public PlatformErrorCode getErrorCode() {
        return errorCode;
    }
}
