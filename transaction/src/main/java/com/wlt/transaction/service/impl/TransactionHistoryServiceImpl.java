package com.wlt.transaction.service.impl;

import com.wlt.transaction.dto.TransactionHistoryEvent;
import com.wlt.transaction.entity.TransactionHistory;
import com.wlt.transaction.repository.TransactionHistoryRepository;
import com.wlt.transaction.service.TransactionHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    private final TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public void saveTransactionHistory(TransactionHistoryEvent event) {
        TransactionHistory transactionHistory = new TransactionHistory();
        BeanUtils.copyProperties(event, transactionHistory);
        transactionHistoryRepository.save(transactionHistory);
    }
}
