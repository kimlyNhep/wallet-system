package com.wlt.transaction.service;

import com.wlt.transaction.dto.TransactionHistoryResponseDto;

import java.util.List;

public interface TransactionService {
    List<TransactionHistoryResponseDto> getTransactionHistory(Long userId);
}
