package com.wlt.payment.service;

import com.wlt.payment.dto.*;

public interface TopUpBalanceService {
    GiftCodeValidationResponseDto validateGiftCode(String code);
    void markGiftCodeAsRedeemed(Long userId, String code);
    GenerateGiftCodeResponseDto generateGiftCode(GenerateGiftCodeRequestDto generateGiftCodeRequestDto);
    RedeemGiftCodeResponseDto redeemGiftCode(Long userId, GiftCodeRedeemEvent giftCodeRedeemEvent);
}
