package com.wlt.transaction.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wlt_transaction_history")
public class TransactionHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String transactionType;
    private Long crWalletId;
    private Long drWalletId;
    private Long userId;
    private String transactionRefNo;
    private String crCcy;
    private String drCcy;
    private String txnCcy;
    private BigDecimal amount;
    private BigDecimal crAmount;
    private BigDecimal drAmount;
    private float exchangeRate;
    private LocalDateTime saveTimestamp;
    private String status;
}
