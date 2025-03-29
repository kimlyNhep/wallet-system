package com.wlt.user.exception;

import com.wlt.user.constants.MessageEnum;

public class CustomException extends RuntimeException {
    private String code;

    public CustomException(MessageEnum code) {
        super(MessageEnum.getMessage(code.getCode()));
    }
}
