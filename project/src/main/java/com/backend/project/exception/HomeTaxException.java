package com.backend.project.exception;

public class HomeTaxException extends Exception{

    private static final long serialVersionUID = 1L;

    private ErrorCode errorCode;

    public HomeTaxException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public HomeTaxException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

}
