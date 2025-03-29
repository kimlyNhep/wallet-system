package com.wlt.payment.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ConfirmFundTransferResponseDto {
    private String paymentRefNo;
    private String transactionRefNo;
    private String status;
}
