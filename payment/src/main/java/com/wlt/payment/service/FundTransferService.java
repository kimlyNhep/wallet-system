package com.wlt.payment.service;

import com.wlt.payment.dto.*;

public interface FundTransferService {
    FundTransferResponseDto initFundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto);

    void fundTransfer(Long userId, FundTransferRequestDto fundTransferRequestDto);

    TransferAcknowledgementResponseDto makeAcknowledgement(TransferAcknowledgementRequestDto transferAcknowledgementRequestDto);
    TransactionStatusResponseDto transactionStatus(TransferAcknowledgementRequestDto transferAcknowledgementRequestDto);
}
