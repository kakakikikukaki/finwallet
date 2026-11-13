package com.finwallet.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "app_users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Getter @Setter
    private UUID id;

    @Column(nullable = false, name = "full_name", length = 100)
    @Getter @Setter
    private String fullName;

    @Column(unique = true, nullable = false, length = 20)
    @Getter @Setter
    private String phoneNumber;

    @Column(nullable = false)
    @Getter @Setter
    private String passwordHash;

    @Column(nullable = false)
    @Getter @Setter
    private String pinHash;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Getter @Setter
    private UserStatus status;

    @CreatedDate
    @Column(nullable = false)
    @Getter @Setter
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    @Getter @Setter
    private Instant updatedAt;

    public User() {
    }
}
