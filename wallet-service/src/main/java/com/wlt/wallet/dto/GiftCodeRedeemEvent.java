package com.wlt.wallet.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GiftCodeRedeemEvent {
    private Long userId;
    private String giftCode;
    private Long creditWalletId;
}
