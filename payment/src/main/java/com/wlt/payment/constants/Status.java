package com.wlt.payment.constants;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Status {
    public static final String INIT = "INT";
    public static final String PENDING = "PND";
    public static final String SUCCESS = "SUC";
}
