package com.wlt.transaction.repository;

import com.wlt.transaction.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
}
