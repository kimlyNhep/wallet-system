package com.wlt.user.exception;


import com.wlt.user.constants.MessageError;

public class CustomException extends RuntimeException {
    private final MessageError errorCode;

    public CustomException(MessageError errorCode) {
        super(errorCode.getMessage()); // Optional: Pass the message to the superclass
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode.getCode();
    }

    public String getErrorMessage() {
        return errorCode.getMessage();
    }
}
