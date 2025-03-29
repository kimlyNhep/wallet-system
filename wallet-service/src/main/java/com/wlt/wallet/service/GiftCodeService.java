package com.wlt.wallet.service;

import com.wlt.wallet.dto.RedeemGiftCodeRequestDto;
import com.wlt.wallet.dto.RedeemGiftCodeResponseDto;

public interface GiftCodeService {
    RedeemGiftCodeResponseDto redeemGiftCode(Long userId, RedeemGiftCodeRequestDto requestDto);
}
