package com.finwallet.user.repository;

import com.finwallet.config.JpaAuditingConfig;
import com.finwallet.user.entity.User;
import com.finwallet.user.entity.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import(JpaAuditingConfig.class)
public class UserRepositoryTest {

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
    private UserRepository userRepository;

    @Test
    void shouldFindUserByPhoneNumber() {
        User user = new User();

        user.setFullName("Rizki Test");
        user.setPhoneNumber("+6281234567890");
        user.setPasswordHash("dummy-password-hash");
        user.setPinHash("dummy-pin-hash");
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByPhoneNumber("+6281234567890");

        assertTrue(foundUser.isPresent());
        assertEquals("+6281234567890", foundUser.get().getPhoneNumber());
    }

    @Test
    void shouldReturnEmptyWhenPhoneNumberNotFound() {
        Optional<User> foundUser = userRepository.findByPhoneNumber("+628127376233");

        assertTrue(foundUser.isEmpty());
    }

    @Test
    void shouldReturnTrueWhenPhoneNumberExists() {
        User user = new User();

        user.setFullName("Rizki Test");
        user.setPhoneNumber("+6281234567890");
        user.setPasswordHash("dummy-password-hash");
        user.setPinHash("dummy-pin-hash");
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        boolean phoneNumberExists = userRepository.existsByPhoneNumber("+6281234567890");
        assertTrue(phoneNumberExists);
    }

    @Test
    void shouldReturnFalseWhenPhoneNumberDoesNotExist() {
        assertFalse(userRepository.existsByPhoneNumber("+628127376233"));
    }
}
