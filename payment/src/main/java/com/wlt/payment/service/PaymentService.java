package com.wlt.payment.service;

import com.wlt.payment.dto.*;

public interface PaymentService {
    GiftCodeValidationResponseDto validateGiftCode(String code);
    void markGiftCodeAsRedeemed(Long userId, String code);
    GenerateGiftCodeResponseDto generateGiftCode(GenerateGiftCodeRequestDto generateGiftCodeRequestDto);
    GetExchangeRateAmountDto getExchangeRateAmount(GetExchangeRateAmountRequestDto getExchangeRateAmountRequestDto);
    FundTransferResponseDto fundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto);
}
