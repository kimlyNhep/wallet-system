package com.wlt.transaction.service.impl;

import com.wlt.transaction.dto.TransactionHistoryResponseDto;
import com.wlt.transaction.entity.TransactionHistory;
import com.wlt.transaction.repository.TransactionHistoryRepository;
import com.wlt.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionHistoryListingServiceImpl implements TransactionService {

    private final TransactionHistoryRepository transactionHistoryRepository;
    @Override
    public List<TransactionHistoryResponseDto> getTransactionHistory(Long userId) {
        List<TransactionHistory> transactionHistories = transactionHistoryRepository.findByUserId(userId);
        return  transactionHistories.stream().map(transactionHistory -> {
            TransactionHistoryResponseDto transactionHistoryResponseDto = new TransactionHistoryResponseDto();
            BeanUtils.copyProperties(transactionHistory, transactionHistoryResponseDto);
            return transactionHistoryResponseDto;
        }).toList();
    }
}
