package com.wlt.wallet.service;

import com.wlt.wallet.dto.InitFundTransferRequestDto;
import com.wlt.wallet.dto.FundTransferResponseDto;

public interface FundTransferService {
    FundTransferResponseDto initFundTransfer(Long userId, InitFundTransferRequestDto fundTransferRequestDto);
}
