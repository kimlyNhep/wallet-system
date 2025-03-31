package com.wlt.transaction.dto;

import lombok.Data;

@Data
public class SuccessResponse <T> {
    private String code;
    private String message;
    private T data;
}
