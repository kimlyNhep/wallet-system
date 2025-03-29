package com.wlt.transaction.service;

import com.wlt.transaction.dto.TransactionHistoryEvent;
import com.wlt.transaction.entity.TransactionHistory;

public interface TransactionHistoryService {
    void saveTransactionHistory(TransactionHistoryEvent event);
}
