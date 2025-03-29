package com.wlt.payment.repository;

import com.wlt.payment.entity.GiftCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GiftCodeRepository extends JpaRepository<GiftCode, Long> {
    Optional<GiftCode> findByGiftCode(String code);
}
