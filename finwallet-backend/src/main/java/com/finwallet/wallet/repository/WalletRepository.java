package com.finwallet.wallet.repository;

import com.finwallet.user.entity.User;
import com.finwallet.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findWalletByUser(User user);
    boolean existsByUser(User user);
}
