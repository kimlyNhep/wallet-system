package com.wlt.payment.repository;

import com.wlt.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByPaymentRefNoAndStatus(String paymentRefNo, String status);
}
