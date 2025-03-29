package com.wlt.wallet.repository;

import com.wlt.wallet.entity.WalletAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletAccountRepository extends JpaRepository<WalletAccount, Long> {
    Optional<WalletAccount> findByUserId(Long userId);

    Optional<WalletAccount> findByIdAndStatus(Long id, String status);
}
