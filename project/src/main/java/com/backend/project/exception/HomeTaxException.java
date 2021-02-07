package com.backend.project.exception;


public class HomeTaxException extends Exception{
    public HomeTaxException() {
        super();
    }
    public HomeTaxException(String message) {
        super(message);
    }

    public HomeTaxException(String message, Throwable cause) {
        super(message, cause);
    }

    public HomeTaxException(Throwable cause) {
        super(cause);
    }

    public HomeTaxException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
