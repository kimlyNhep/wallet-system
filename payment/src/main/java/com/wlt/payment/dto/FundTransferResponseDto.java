package com.wlt.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FundTransferResponseDto {
    private String paymentRefNo;
    private Long crWalletId;
    private BigDecimal crAmount;
    private String crCcy;
    private BigDecimal drAmount;
    private String drCcy;
    private Long drWalletId;
    private String ccy;
}
