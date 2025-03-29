package com.wlt.payment.service;

import com.wlt.payment.dto.GenerateGiftCodeRequestDto;
import com.wlt.payment.dto.GenerateGiftCodeResponseDto;
import com.wlt.payment.dto.GiftCodeRedeemEvent;
import com.wlt.payment.dto.GiftCodeValidationResponseDto;

public interface TopUpBalanceService {
    GiftCodeValidationResponseDto validateGiftCode(String code);
    void markGiftCodeAsRedeemed(Long userId, String code);
    GenerateGiftCodeResponseDto generateGiftCode(GenerateGiftCodeRequestDto generateGiftCodeRequestDto);
    void redeemGiftCode(Long userId, GiftCodeRedeemEvent giftCodeRedeemEvent);
}
