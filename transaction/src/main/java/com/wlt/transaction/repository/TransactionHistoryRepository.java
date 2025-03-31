package com.wlt.transaction.repository;

import com.wlt.transaction.entity.TransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUserId(Long userId);
    List<TransactionHistory> findByUserIdAndSaveTimestampAfterAndSaveTimestampBefore(Long userId, LocalDateTime saveTimestamp, LocalDateTime saveTimestampAfter);
}
