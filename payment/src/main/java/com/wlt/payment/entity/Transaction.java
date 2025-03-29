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
    private BigDecimal amount;
    private String ccy;
    private BigDecimal crAmount;
    private String crCcy;
    private BigDecimal drAmount;
    private String drCcy;
    private String description;
    private String crWalletId;
    private String drWalletId;
    private BigDecimal exchangeRate;
    private LocalDateTime saveTimestamp;
}
