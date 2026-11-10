# FinWallet Documentation Review

**Document Version:** 1.1  
**Review Scope:** Requirements, Use Cases, Diagrams, Database, API, Security, Testing, and Architecture.

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | Initial release | Initial document version |
| 1.1 | 2026-11-10 | Updated technology stack to Spring Boot 4.1.2 and revised technical specifications |

## 1. Review Objective

This document consolidates the approved business and technical decisions for FinWallet before backend implementation begins.

The purpose is to maintain consistency across all project documentation and prevent conflicting requirements during development.

## 2. Approved Business Rules

1. FinWallet is a simulated digital wallet application.
2. Each registered user owns exactly one wallet.
3. A newly created wallet starts with IDR 0 and ACTIVE status.
4. Registration creates the user and wallet atomically.
5. Phone number must be unique.
6. Password and transaction PIN must be securely hashed.
7. BLOCKED users cannot log in.
8. INACTIVE and BLOCKED wallets cannot perform financial transactions.
9. Only ACTIVE wallets can perform balance-changing transactions.
10. Top up is simulated and does not use a real payment gateway.
11. Top up does not require a transaction PIN.
12. Transfer is only available between registered FinWallet users.
13. Transfer requires the sender's transaction PIN.
14. Self-transfer is prohibited.
15. Payment/expense is simulated and requires a transaction PIN.
16. Financial transactions must be atomic.
17. Wallet balance must never become negative.
18. Every successful balance change must have corresponding ledger records.
19. Financial records must not be silently modified or deleted.
20. Users can only access their own wallet and transaction information.

## 3. Approved Transaction Rules

### Top Up
- Transaction type: TOP_UP.
- Entry type: CREDIT.
- Requires an ACTIVE wallet.
- Does not require PIN.
- Increases wallet balance.
- Creates one ledger record.

### Transfer
- Transaction type: TRANSFER.
- Sender entry type: DEBIT.
- Recipient entry type: CREDIT.
- Requires an ACTIVE sender and recipient wallet.
- Requires sender transaction PIN.
- Sender and recipient cannot be the same user.
- Both ledger records use the same reference number.
- Both records and balance changes must commit atomically.

### Payment / Expense
- Transaction type: PAYMENT.
- Entry type: DEBIT.
- Requires an ACTIVE wallet.
- Requires transaction PIN.
- Requires a valid active category.
- Decreases wallet balance.
- Creates one ledger record.

## 4. Approved Database Rules

Tables:
- app_users
- wallets
- transaction_categories
- wallet_transactions

Relationships:
- One user has one wallet.
- One wallet has many transactions.
- One transaction may reference a category.
- A transfer ledger may reference the related wallet.

Data rules:
- Primary keys use UUID.
- Monetary values use NUMERIC(19,2).
- Amount must be greater than zero.
- Wallet balance must be greater than or equal to zero.
- Phone number must be unique.
- Transfer reference numbers are shared between two ledger records.

The reference number is not unique per ledger row because one transfer generates two rows. The application service and automated tests must verify that both entries are created together.

## 5. Concurrency and Atomicity

All balance-changing operations must use database transactions.

Wallet rows must be locked before balance changes.

For transfers involving two wallets:
1. Determine a consistent wallet ID ordering.
2. Lock wallets in that order.
3. Recheck wallet status and available balance.
4. Update both balances.
5. Save both ledger records.
6. Commit all changes together.

If an operation fails, all changes must roll back.

## 6. Approved Authentication Rules

- Authentication uses phone number and password.
- Password and PIN are hashed using BCrypt.
- JWT access token expiration is 15 minutes.
- Protected endpoints require a valid JWT.
- BLOCKED users cannot log in.
- Wallet status does not prevent login; it prevents financial operations when the wallet is not ACTIVE.
- Logout removes the token from the client.
- Existing JWT tokens remain valid until expiration after password changes unless a revocation mechanism is implemented later.

## 7. API Rules

API base path:

`/api/v1`

Core endpoints:
- POST `/auth/register`
- POST `/auth/login`
- GET `/users/me`
- PUT `/users/me`
- PATCH `/users/me/password`
- GET `/wallets/me`
- POST `/wallets/me/top-ups`
- POST `/transfers`
- POST `/payments`
- GET `/transaction-categories`
- GET `/transactions`
- GET `/transactions/{id}`

All endpoints must enforce ownership and authorization rules.

Entities must not be exposed directly through API responses. Use DTOs.

## 8. Technical Standards

- Java 21.
- Spring Boot 4.
- PostgreSQL.
- Spring Data JPA / Hibernate.
- Spring Security.
- JWT.
- Maven.
- BigDecimal for monetary calculations.
- UUID for primary keys.
- Flyway for database migration.
- Bean Validation for request validation.
- Centralized exception handling.
- Unit and integration testing.

## 9. Required Testing

Critical test coverage:
- Registration atomicity.
- Duplicate phone number.
- Password and PIN hashing.
- JWT authentication.
- User and wallet ownership.
- Wallet status validation.
- Top up atomicity.
- Transfer debit and credit pairing.
- Insufficient balance.
- Concurrent transfer and payment.
- Payment category validation.
- Transaction history access control.
- No negative wallet balance.
- No partial financial transactions.

## 10. Known MVP Limitations

The following are intentionally excluded:
- Real bank integration.
- Real payment gateway.
- Merchant payment integration.
- Admin approval for top up.
- Salary or income tracking.
- Server-side JWT revocation.
- Administrative dashboard.

These may be considered for future versions but are not part of MVP.

## 11. Review Conclusion

The business rules and technical direction are sufficiently defined to begin backend implementation.

The implementation must follow the approved documents:
- BRD
- Use Case Specification
- Use Case Diagram
- Activity Diagrams
- ERD and Database Design
- API Specification
- Security Design
- Test Plan
- Backend Architecture
- Implementation Blueprint
- Documentation Review

Any changes to the approved rules must be reflected in the relevant documentation before implementation.
