package com.wlt.payment.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "wlt_transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String paymentRefNo;
    private BigDecimal amount;
    private String ccy;
    private String crCcy;
    private String drCcy;
    private String description;
    private Long crWalletId;
    private Long drWalletId;
    private BigDecimal exchangeRate;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
