package com.wlt.wallet.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DebitAccountBalanceRequestDto {
    private Long debitWalletId;
    private BigDecimal debitBalance;
    private String ccy;
}
