package com.wlt.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WalletUpdateEvent {
    private String type;
    private BigDecimal amount;
    private Long walletId;
    private Long userId;
    private String ccy;
}
