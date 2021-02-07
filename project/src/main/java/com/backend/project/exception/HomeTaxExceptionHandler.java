package com.backend.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice
@Slf4j
public class HomeTaxExceptionHandler {

    @ExceptionHandler(BadPaddingException.class)
    @ResponseBody
    public void passWordException(HttpServletResponse response) throws IOException {
        String message = "패스워드가 정확하지 않습니다.";
//        alertCall(response, message);
        log.info("### HOME_TAX_PASSWORD_ERROR ###");
    }

    @ExceptionHandler(HomeTaxException.class)
    @ResponseBody
    public void HomeTaxException(HttpServletResponse response , String message) throws IOException {
        alertCall(response, message);
        log.info("### HOME_TAX_LOGIN_ERROR ###");
    }


    private void alertCall(HttpServletResponse response, String message) throws IOException {
        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script> alert('" + message + "') </script>");
        out.flush();
    }

}
