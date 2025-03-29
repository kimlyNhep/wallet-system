package com.wlt.payment.repository;

import com.wlt.payment.entity.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, Long> {
    Optional<ExchangeRate> findByCrCcyAndDrCcy(String crCcy, String drCcy);
}
