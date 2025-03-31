package com.wlt.user.constants;

import lombok.Getter;

@Getter
public enum MessageError {
    EMAIL_ALREADY_TAKEN("ERR-001", "Email already taken"),
    SUPER_ADMIN_NOT_FOUND("ERR-002", "Super admin role not found"),
    USER_NOT_FOUND("ERR-003", "User not found"),
    ROLE_NOT_FOUND("ERR-004", "Role not found"),
    SESSION_NOT_FOUND("ERR-005", "Session not found"),
    EMAIL_CANNOT_BE_NULL("ERR-006", "Email cannot be null"),
    EMAIL_INVALID("ERR-007", "Email is invalid"),;

    private final String code;
    private final String message;

    MessageError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(String code) {
        MessageError messageEnum = MessageError.valueOf(code);
        return messageEnum.getMessage();
    }
}
