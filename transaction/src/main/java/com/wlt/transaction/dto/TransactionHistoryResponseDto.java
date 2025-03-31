package com.wlt.transaction.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionHistoryResponseDto {
    private String transactionType;
    private Long crWalletId;
    private Long drWalletId;
    private Long userId;
    private String transactionRefNo;
    private String crCcy;
    private String drCcy;
    private String txnCcy;
    private BigDecimal amount;
    private BigDecimal crAmount;
    private BigDecimal drAmount;
    private float exchangeRate;
    private LocalDateTime saveTimestamp;
    private String status;
}
