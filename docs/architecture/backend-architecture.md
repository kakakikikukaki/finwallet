# FinWallet Backend Architecture

**Document Version:** 1.1  
**Project:** FinWallet  
**Backend Stack:** Java 21, Spring Boot 4, PostgreSQL, Spring Security, JWT, Spring Data JPA

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | Initial release | Initial document version |
| 1.1 | 2026-11-10 | Updated technology stack to Spring Boot 4.1.2 and revised technical specifications |

## 1. Architecture Overview

FinWallet menggunakan Layered Architecture untuk memisahkan tanggung jawab setiap komponen backend.

Request flow:

`Client → Controller → Service → Repository → Database`

Komponen pendukung:
- Security: authentication dan authorization.
- DTO: request dan response API.
- Entity: representasi tabel database.
- Exception: centralized error handling.
- Configuration: pengaturan aplikasi dan security.

## 2. Package Structure

```text
finwallet-backend/
└── src/
    ├── main/
    │   ├── java/com/finwallet/
    │   │   ├── FinWalletApplication.java
    │   │   ├── config/
    │   │   ├── controller/
    │   │   ├── dto/
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   ├── entity/
    │   │   ├── enums/
    │   │   ├── exception/
    │   │   ├── repository/
    │   │   ├── security/
    │   │   └── service/
    │   └── resources/
    │       ├── application.yml
    │       └── application-dev.yml
    └── test/
        └── java/com/finwallet/
```

## 3. Layer Responsibilities

### Controller
- Menerima HTTP request.
- Melakukan validasi request melalui DTO.
- Memanggil service.
- Mengembalikan HTTP response.

### Service
- Menjalankan business logic.
- Memvalidasi aturan bisnis.
- Mengatur transaksi database.
- Memanggil repository.

### Repository
- Mengakses database menggunakan Spring Data JPA.
- Menyediakan query untuk kebutuhan service.
- Mengelola locking untuk operasi finansial.

### Entity
- Merepresentasikan tabel database.
- Mendefinisikan primary key, field, constraint mapping, dan relasi.

### DTO
- Memisahkan data API dari entity.
- Memvalidasi input.
- Mengontrol informasi yang dikembalikan API.

### Security
- Memvalidasi JWT.
- Mengatur authentication dan authorization.
- Mengamankan endpoint.

### Exception
- Menangani exception secara terpusat.
- Menghasilkan format error API yang konsisten.

## 4. Entity Classes

| Class | Database Table | Responsibility |
|---|---|---|
| User | app_users | Data pengguna dan kredensial |
| Wallet | wallets | Saldo dan status wallet |
| TransactionCategory | transaction_categories | Kategori transaksi |
| WalletTransaction | wallet_transactions | Catatan debit dan kredit |

Entity tidak boleh langsung diekspos melalui controller. Gunakan DTO untuk seluruh response API.

## 5. Enum Classes

| Enum | Values |
|---|---|
| UserStatus | ACTIVE, BLOCKED |
| WalletStatus | ACTIVE, INACTIVE, BLOCKED |
| TransactionType | TOP_UP, TRANSFER, PAYMENT |
| EntryType | CREDIT, DEBIT |

## 6. Repository Classes

### UserRepository
- `findByPhoneNumber`
- `existsByPhoneNumber`

### WalletRepository
- `findByUserId`
- `findByIdForUpdate`

### TransactionCategoryRepository
- `findByIsActiveTrue`

### WalletTransactionRepository
- Query transaksi berdasarkan wallet.
- Query transaksi berdasarkan reference.
- Query detail transaksi berdasarkan ID dan kepemilikan wallet.
- Pagination dan filtering riwayat transaksi.

Repository harus membatasi data transaksi berdasarkan wallet milik pengguna yang sedang login.

## 7. Service Classes

| Service | Responsibility |
|---|---|
| AuthService | Register dan login |
| UserService | Profile dan password management |
| WalletService | Wallet balance dan top up |
| TransferService | Transfer antar pengguna |
| PaymentService | Simulated payment/expense |
| TransactionService | Transaction history dan detail |
| CategoryService | Active transaction categories |

MVP menggunakan satu service class per fitur tanpa pemisahan interface dan implementation. Interface dapat ditambahkan jika kebutuhan implementasi berkembang.

## 8. Controller Classes

| Controller | Endpoint Responsibility |
|---|---|
| AuthController | Register dan login |
| UserController | Profile dan password |
| WalletController | Balance dan top up |
| TransferController | Transfer |
| PaymentController | Payment/expense |
| TransactionController | History dan detail |
| CategoryController | Transaction categories |

Controller tidak boleh memuat business logic transaksi finansial.

## 9. Security Components

| Class | Responsibility |
|---|---|
| SecurityConfig | Security filter chain dan endpoint authorization |
| PasswordConfig | BCrypt PasswordEncoder |
| JwtService | JWT generation dan validation |
| JwtAuthenticationFilter | JWT request filtering |
| CustomUserDetailsService | Load user untuk authentication |
| GlobalExceptionHandler | Centralized API exception handling |

JWT access token memiliki masa berlaku 15 menit sesuai security design. Password dan PIN disimpan dalam bentuk hash.

## 10. Financial Transaction Integrity

Seluruh operasi yang mengubah saldo wajib menggunakan database transaction.

### Top Up
1. Validate amount and wallet status.
2. Lock wallet.
3. Increase wallet balance.
4. Create TOP_UP CREDIT transaction.
5. Commit.

### Transfer
1. Validate recipient, PIN, amount, and wallet status.
2. Lock sender and recipient wallets in consistent ID order.
3. Recheck balance and wallet status.
4. Debit sender wallet.
5. Credit recipient wallet.
6. Create two ledger records with the same reference number.
7. Commit.

### Payment
1. Validate amount, category, PIN, and wallet status.
2. Lock wallet.
3. Recheck available balance.
4. Debit wallet.
5. Create PAYMENT DEBIT transaction.
6. Commit.

If any operation fails, all database changes must be rolled back.

Use `BigDecimal` for money calculations and PostgreSQL `NUMERIC(19,2)` for monetary values.

## 11. DTO Design

Request DTO examples:
- RegisterRequest
- LoginRequest
- UpdateProfileRequest
- ChangePasswordRequest
- TopUpRequest
- TransferRequest
- PaymentRequest
- TransactionFilterRequest

Response DTO examples:
- RegisterResponse
- LoginResponse
- UserResponse
- WalletResponse
- TransactionResponse
- TransferResponse
- PageResponse
- MessageResponse

Never expose password hash, PIN hash, JWT signing secret, or unnecessary internal identifiers.

## 12. Request Processing Flow

Example: Transfer API

1. Client sends `POST /api/v1/transfers`.
2. JwtAuthenticationFilter validates the access token.
3. TransferController receives TransferRequest.
4. TransferService validates business rules and transaction PIN.
5. WalletRepository locks both wallets.
6. TransferService updates balances and creates ledger entries.
7. Database commits the operation.
8. Controller returns TransferResponse.

## 13. Coding Standards

- Use UUID for primary keys.
- Use BigDecimal for monetary calculations.
- Use enums for fixed status and transaction values.
- Use DTOs for API input and output.
- Use Bean Validation for request validation.
- Use `@Transactional` for financial operations.
- Use pagination for transaction history.
- Use centralized exception handling.
- Avoid exposing JPA entities through REST APIs.
- Keep credentials and secrets outside source control.
- Follow consistent naming conventions.
- Write unit and integration tests for business logic.

## 14. Architecture Acceptance Criteria

- Every layer has a clearly defined responsibility.
- Controllers do not contain business logic.
- Services do not expose database entities directly through API.
- Repositories handle database access only.
- All financial operations are atomic.
- Concurrent transactions cannot produce negative balances.
- Users can only access their own wallet and transactions.
- Sensitive credentials are never exposed in API responses or logs.
- The backend remains modular and testable.
