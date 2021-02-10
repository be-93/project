package com.backend.project.exception;

public class HomeTaxException extends CustomException{

    public HomeTaxException(ErrorCode errorCode) {
        super(errorCode);
    }

    public HomeTaxException(ErrorCode errorCode, String message) {
        super(errorCode , message);
    }

}
