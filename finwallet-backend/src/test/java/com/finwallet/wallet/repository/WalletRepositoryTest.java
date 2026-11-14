package com.finwallet.wallet.repository;

import com.finwallet.config.JpaAuditingConfig;
import com.finwallet.user.entity.User;
import com.finwallet.user.entity.UserStatus;
import com.finwallet.user.repository.UserRepository;
import com.finwallet.wallet.entity.Wallet;
import com.finwallet.wallet.entity.WalletStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaAuditingConfig.class)
public class WalletRepositoryTest {

    @Container
    private static final PostgreSQLContainer postgre = new PostgreSQLContainer("postgres:18-alpine")
            .withDatabaseName("finwallet_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatabaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgre::getJdbcUrl);
        registry.add("spring.datasource.username", postgre::getUsername);
        registry.add("spring.datasource.password", postgre::getPassword);
    }

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    private User getUser() {

        User user = new User();

        user.setFullName("Rizki Test");
        user.setPhoneNumber("+6281234567890");
        user.setPasswordHash("dummy-password-hash");
        user.setPinHash("dummy-pin-hash");
        user.setStatus(UserStatus.ACTIVE);

        return  user;
    }


    @Test
    void shouldFindWalletByUser() {

        User user = getUser();

        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();

        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        Wallet savedWallet = walletRepository.save(wallet);

        Optional<Wallet> foundWallet = walletRepository.findWalletByUser(savedUser);

        assertTrue(foundWallet.isPresent());
        assertEquals(foundWallet.get().getId(), savedWallet.getId());
        assertEquals(foundWallet.get().getUser().getId(), savedWallet.getUser().getId());
    }

    @Test
    void shouldReturnTrueWhenWalletExists() {
        Wallet wallet = new Wallet();
        User savedUser = userRepository.save(getUser());

        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);

        walletRepository.save(wallet);

        assertTrue(walletRepository.existsByUser(savedUser));
    }

    @Test
    void shouldReturnFalseWhenWalletDoesNotExist() {
        User savedUser = userRepository.save(getUser());

        assertFalse(walletRepository.existsByUser(savedUser));
    }
}
