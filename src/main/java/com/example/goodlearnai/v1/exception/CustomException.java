package com.example.goodlearnai.v1.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Mouse
 */
@Setter
@Getter
public class CustomException extends RuntimeException {
    private String msg;

    public CustomException(String msg) {
        this.msg = msg;
    }

}