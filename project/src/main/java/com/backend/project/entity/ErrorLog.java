package com.backend.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Getter @Setter
@RequiredArgsConstructor
@Table(name = "error_log")
public class ErrorLog {

    @Id @GeneratedValue
    private Long id;
    private int states;
    private String code;
    private String message;
    private String createTime;

    public ErrorLog(int states, String code, String message) {
        this.states = states;
        this.code = code;
        this.message = message;
        this.createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
