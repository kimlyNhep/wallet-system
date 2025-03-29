package com.wlt.wallet.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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
