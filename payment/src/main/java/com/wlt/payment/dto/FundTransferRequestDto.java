package com.wlt.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FundTransferRequestDto {
    private String paymentRefNo;
    private Long crWalletId;
    private String crCcy;
    private String drCcy;
    private Long drWalletId;
    private String ccy;
    private BigDecimal amount;
}
