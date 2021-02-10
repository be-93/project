package com.backend.project.exception;

import com.backend.project.entity.ErrorLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {

//    @PersistenceContext
//    private EntityManager em;

    //CustomException을 상속받은 클래스가 예외를 발생 시킬 시, Catch하여 ErrorResponse를 반환한다.
    @ExceptionHandler(CustomException.class)
    @Transactional
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("## CustomException", e);

        ErrorCode errorCode = e.getErrorCode();

        ErrorResponse response
                = ErrorResponse
                .create()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(e.getMessage());

//        createErrorLog(errorCode.getStatus(),errorCode.getCode(),e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.resolve(errorCode.getStatus()));
    }

    //모든 예외를 핸들링하여 ErrorResponse 형식으로 반환한다.
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("## Exception ", e);

        ErrorResponse response
                = ErrorResponse
                .create()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage());

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    protected void createErrorLog( int states, String code, String message) {
//        ErrorLog errorLog = new ErrorLog(states, code, message);
//        em.persist(errorLog);
//    }
}
