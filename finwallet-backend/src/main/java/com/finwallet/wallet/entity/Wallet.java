package com.finwallet.wallet.entity;

import com.finwallet.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@EntityListeners(AuditingEntityListener.class)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter @Setter
    private UUID id;

    @Getter @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_wallets_user"), name = "user_id",nullable = false, unique = true, updatable = false)
    private User user;

    @Column(nullable = false, precision = 19, scale = 2)
    @Getter @Setter
    private BigDecimal balance;

    @Column(nullable = false)
    @Getter @Setter
    @Enumerated(EnumType.STRING)
    private WalletStatus status;

    @Column(nullable = false)
    @Getter @Setter
    @CreatedDate
    private Instant createdAt;

    @Column(nullable = false)
    @Getter @Setter
    @LastModifiedDate
    private Instant updatedAt;

    public Wallet(User user, BigDecimal balance, WalletStatus status) {
        this.user = user;
        this.balance = balance;
        this.status = status;
    }

    public Wallet() {
    }
}

