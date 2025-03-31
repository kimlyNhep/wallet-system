package com.wlt.wallet.constants;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;

@Getter
public enum MessageError {
    SUC("SUC-001", "Success"),
    ERR_001_INSUFFICIENT_AMOUNT("ERR-001", "Insufficient amount"),
    ERR_002_WALLET_NOT_ENOUGH("ERR-002", "Wallet not enough"),
    ERR_003_INVALID_GIFT_CODE("ERR-003", "Invalid gift code"),
    ERR_004_CANNOT_QUERY_OTHER_WALLET("ERR-004", "Can not query other's wallet"),
    ERR_005_ERROR_UNBLOCK_WALLET("ERR-005", "Error unblock wallet"),;

    private final String code;
    private final String message;

    MessageError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static String getMessage(String code) {
        return Objects.requireNonNull(Arrays.stream(MessageError.values()).filter(e -> e.code.equals(code))
                .findFirst().orElse(null)).getMessage();
    }
}
