package com.wlt.user.constants;

import lombok.Getter;

@Getter
public enum MessageEnum {
    EMAIL_ALREADY_TAKEN("ERR-001", "Email already taken");

    private final String code;
    private final String message;

    MessageEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(String code) {
        MessageEnum messageEnum = MessageEnum.valueOf(code);
        return messageEnum.getMessage();
    }
}
