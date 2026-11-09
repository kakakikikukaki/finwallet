# FinWallet Implementation Blueprint

**Document Version:** 1.0  
**Project:** FinWallet  
**Backend:** Java 21, Spring Boot 3, PostgreSQL  
**Architecture:** Layered Architecture

## 1. Implementation Objective

Dokumen ini menjadi panduan implementasi backend FinWallet berdasarkan BRD, Use Case Specification, ERD, API Specification, Security Design, Test Plan, dan Backend Architecture.

Implementasi dilakukan secara bertahap. Setiap tahap harus selesai dan diuji sebelum melanjutkan ke tahap berikutnya.

## 2. Project Configuration

| Configuration | Value |
|---|---|
| Build Tool | Maven |
| Language | Java |
| Java Version | 21 |
| Spring Boot | 3.x |
| Group | com.finwallet |
| Artifact | finwallet-backend |
| Package | com.finwallet |
| Packaging | Jar |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Authentication | Spring Security + JWT |

## 3. Dependencies

| Dependency | Purpose |
|---|---|
| spring-boot-starter-web | REST API |
| spring-boot-starter-data-jpa | Database persistence |
| postgresql | PostgreSQL driver |
| spring-boot-starter-security | Authentication and authorization |
| spring-boot-starter-validation | DTO validation |
| jjwt-api | JWT API |
| jjwt-impl | JWT implementation |
| jjwt-jackson | JWT JSON processing |
| lombok | Reduce boilerplate |
| flyway-core | Database migration |
| flyway-database-postgresql | PostgreSQL migration support |
| spring-boot-starter-test | Unit and integration testing |
| spring-security-test | Security testing |
| testcontainers-postgresql | PostgreSQL integration testing |

Use a consistent JJWT version for all three JJWT modules. Use the dependency versions managed by the selected Spring Boot 3 release where available.

## 4. Application Configuration

Configuration files:

- `application.yml`: common application settings.
- `application-dev.yml`: local development settings.
- Environment variables: database credentials and JWT secret.

Required environment variables:

- DB_URL
- DB_USERNAME
- DB_PASSWORD
- JWT_SECRET

Do not commit actual credentials or secrets.

Use `spring.jpa.hibernate.ddl-auto=validate` and Flyway for schema migration.

## 5. Database Migration

Migration directory:

`src/main/resources/db/migration/`

Initial migration:

`V1__init_schema.sql`

Use `docs/database/schema.sql` as the initial schema reference.

Flyway manages subsequent database changes through new versioned migration files. Do not edit an already-applied migration in an existing environment; create a new migration instead.

## 6. Implementation Sequence

### Phase 1: Project Initialization
- Generate Spring Boot project.
- Configure Maven dependencies.
- Create package structure.
- Configure PostgreSQL connection.
- Configure application profiles.

Acceptance criteria:
- Application starts successfully.
- Database connection succeeds.
- No secrets are committed.

### Phase 2: Database and Entity
- Create enum classes.
- Create User entity.
- Create Wallet entity.
- Create TransactionCategory entity.
- Create WalletTransaction entity.
- Create initial Flyway migration.
- Create repository classes.

Acceptance criteria:
- Database schema is created.
- Entity mapping is valid.
- Repository basic operations work.

### Phase 3: Authentication and Security
- Implement registration.
- Hash password and PIN using BCrypt.
- Implement login.
- Implement JWT generation and validation.
- Configure endpoint authorization.

Acceptance criteria:
- Duplicate phone number is rejected.
- Password and PIN are never stored as plaintext.
- Valid JWT can access protected endpoints.
- Invalid and expired JWT are rejected.

### Phase 4: User and Wallet
- Implement profile retrieval.
- Implement profile update.
- Implement password change.
- Implement wallet balance and status retrieval.

Acceptance criteria:
- Users can only access their own data.
- Sensitive hashes are not exposed.

### Phase 5: Top Up
- Validate amount.
- Validate wallet status.
- Lock wallet during balance update.
- Increase wallet balance.
- Create TOP_UP CREDIT ledger record.

Acceptance criteria:
- Amount must be greater than zero.
- Only ACTIVE wallets can perform top up.
- Balance and ledger updates are atomic.

### Phase 6: Transfer
- Find recipient by phone number.
- Validate transaction PIN.
- Validate wallet statuses.
- Lock sender and recipient wallets in consistent ID order.
- Recheck balance after acquiring locks.
- Debit sender.
- Credit recipient.
- Create two ledger records with the same reference number.

Acceptance criteria:
- Self-transfer is rejected.
- Insufficient balance is rejected.
- Balance cannot become negative.
- Both ledger entries are created together.
- Failed transfer does not partially update balances.

### Phase 7: Payment and Expense
- Retrieve active categories.
- Validate amount and transaction PIN.
- Lock wallet.
- Recheck available balance.
- Deduct wallet balance.
- Create PAYMENT DEBIT ledger record.

Acceptance criteria:
- Category must be valid and active.
- Only ACTIVE wallets can make payments.
- Balance and ledger update atomically.

### Phase 8: Transaction History
- Implement transaction history.
- Implement pagination.
- Implement type and date filters.
- Implement transaction detail.

Acceptance criteria:
- Users only access their own transactions.
- Results are ordered newest first.
- Filters and pagination work correctly.

### Phase 9: Final Testing
- Unit testing.
- Integration testing.
- Security testing.
- Financial integrity testing.
- Concurrency testing.

Acceptance criteria:
- Critical test cases pass.
- No unauthorized data access.
- No negative balance.
- No partial financial transactions.

## 7. Class Creation Order

1. FinWalletApplication
2. Enum classes
3. Entity classes
4. Flyway migration
5. Repository classes
6. Request and response DTOs
7. Exception classes
8. PasswordConfig
9. AuthService and AuthController
10. JWT and Security classes
11. UserService and UserController
12. WalletService and WalletController
13. TransferService and TransferController
14. PaymentService and PaymentController
15. TransactionService and TransactionController
16. CategoryService and CategoryController
17. Unit and integration tests

## 8. Coding Rules

- Use UUID for primary keys.
- Use BigDecimal for monetary values.
- Use enums for fixed status and transaction types.
- Use DTOs for API request and response.
- Use Bean Validation for request validation.
- Use @Transactional for financial operations.
- Use pessimistic locking for wallet balance changes.
- Recheck balance after locking.
- Lock multiple wallets in consistent ID order.
- Use centralized exception handling.
- Never expose password or PIN hashes.
- Never expose another user's wallet or transaction information.
- Do not silently modify or delete financial ledger records.

## 9. Definition of Done

A feature is considered complete only when:

1. Its business rules are implemented.
2. Its API response matches the API specification.
3. Validation and error handling work.
4. Relevant automated tests pass.
5. Security and ownership rules are verified.
6. Documentation is updated if the design changes.
7. Changes are committed and pushed to GitHub.

## 10. Implementation Workflow

For each feature:

1. Review the requirement.
2. Identify affected entity, DTO, repository, service, and controller.
3. Implement the feature.
4. Explain the classes, annotations, methods, and dependencies.
5. Run the application.
6. Test using Postman or automated tests.
7. Review the implementation.
8. Commit and push the completed feature.

The implementation must follow the approved requirements and architecture. Any scope or design change must be documented before implementation.
