# FINWALLET
# Business Requirements Document (BRD)

| Document Information | Details |
|---|---|
| Project Name | FinWallet |
| Document Type | Business Requirements Document |
| Version | 1.1 |
| Status | Approved |
| Project Type | Portfolio Project |
| Currency | Indonesian Rupiah (IDR) |

---

## 1. Project Overview

### 1.1 Background

FinWallet is a simulated digital wallet application designed to allow registered users to manage wallet balances and perform financial transactions within the FinWallet ecosystem.

The application provides basic digital wallet functionalities, including simulated top-ups, transfers between registered users, payment and expense recording, and transaction history.

This project is developed for educational and portfolio purposes to demonstrate backend and full-stack application development.

### 1.2 Project Objectives

- Provide a digital wallet system for registered users.
- Allow users to manage and monitor their wallet balance.
- Support simulated wallet top-up transactions.
- Enable transfers between FinWallet users.
- Allow users to record payments and expenses.
- Maintain accurate and traceable transaction records.
- Implement secure authentication and transaction validation.

---

## 2. Project Scope

### 2.1 In Scope

The following functionalities are included in the MVP:

1. User Registration and Authentication
2. User Profile Management
3. Wallet Management
4. Wallet Balance Inquiry
5. Simulated Wallet Top Up
6. Wallet-to-Wallet Transfer
7. Payment and Expense Recording
8. Transaction History and Detail
9. Security and Transaction Validation

### 2.2 Out of Scope

The following functionalities are excluded from the MVP:

- Real bank account integration.
- Payment gateway integration.
- Real interbank transfers.
- Admin approval for top-up transactions.
- Salary, bonus, or other income tracking.
- Scheduled transactions.
- Investment management.
- Budget management.
- Push notifications.

---

## 3. User Roles

### 3.1 Guest

A guest is a user who has not authenticated.

Available functionalities:
- Register a new account.
- Log in to an existing account.

### 3.2 Registered User

A registered user is an authenticated user with an active FinWallet account.

Available functionalities:
- View and update profile.
- Change password.
- View wallet balance and status.
- Top up wallet.
- Transfer funds to another FinWallet user.
- Record payments and expenses.
- View transaction history and details.

### 3.3 System

The system is responsible for:
- Validating user credentials.
- Managing wallet records.
- Validating transaction requests.
- Processing balance changes.
- Maintaining transaction records.
- Enforcing security and business rules.

---

## 4. Functional Requirements

### 4.1 User Management

| ID | Requirement |
|---|---|
| FR-001 | The system shall allow guests to register using their name, phone number, password, and transaction PIN. |
| FR-002 | The system shall ensure that each phone number is unique. |
| FR-003 | The system shall securely hash user passwords and transaction PINs. |
| FR-004 | The system shall automatically create one wallet when a user successfully registers. |
| FR-005 | The system shall allow authenticated users to view and update their profile. |
| FR-006 | The system shall allow authenticated users to change their passwords. |

### 4.2 Authentication

| ID | Requirement |
|---|---|
| FR-007 | The system shall authenticate users using their phone number and password. |
| FR-008 | The system shall issue a JWT access token after successful authentication. |
| FR-009 | The system shall validate JWT tokens for protected endpoints. |
| FR-010 | The system shall reject requests with invalid or expired tokens. |

### 4.3 Wallet Management

| ID | Requirement |
|---|---|
| FR-011 | The system shall maintain exactly one wallet for each registered user. |
| FR-012 | The system shall allow users to view their wallet balance. |
| FR-013 | The system shall display the wallet status. |
| FR-014 | The system shall prevent balance-changing transactions when the wallet status is INACTIVE or BLOCKED. |

Wallet statuses:

- ACTIVE
- INACTIVE
- BLOCKED

### 4.4 Wallet Top Up

| ID | Requirement |
|---|---|
| FR-015 | The system shall allow authenticated users to perform simulated wallet top-ups. |
| FR-016 | The system shall validate that the top-up amount is greater than zero. |
| FR-017 | The system shall increase the wallet balance immediately after successful validation and processing. |
| FR-018 | The system shall record every successful top-up transaction with the transaction type TOP_UP. |
| FR-019 | The system shall generate a unique transaction reference for each successful top-up. |

Top-up transactions do not require a transaction PIN or admin approval.

### 4.5 Wallet Transfer

| ID | Requirement |
|---|---|
| FR-020 | The system shall allow users to transfer funds to other registered FinWallet users. |
| FR-021 | The recipient shall be identified using their registered phone number. |
| FR-022 | The system shall validate recipient existence and wallet status. |
| FR-023 | The system shall validate sender wallet status, transaction PIN, and available balance. |
| FR-024 | The system shall debit the sender's wallet and credit the recipient's wallet atomically. |
| FR-025 | The system shall record corresponding debit and credit transaction entries. |
| FR-026 | The system shall use a unique transfer reference to associate both transaction entries. |

### 4.6 Payment and Expense

| ID | Requirement |
|---|---|
| FR-027 | The system shall allow authenticated users to record payments or expenses. |
| FR-028 | The system shall require an amount, category, and optional description. |
| FR-029 | The system shall validate the transaction PIN and available wallet balance. |
| FR-030 | The system shall deduct the expense amount from the user's wallet balance. |
| FR-031 | The system shall record successful transactions with the transaction type EXPENSE. |

Payment and expense recording represents a simulated wallet deduction. It does not involve an actual merchant or payment gateway.

### 4.7 Transaction History

| ID | Requirement |
|---|---|
| FR-032 | The system shall allow users to view their transaction history. |
| FR-033 | The system shall display transaction reference, type, amount, status, and timestamp. |
| FR-034 | The system shall distinguish between incoming and outgoing transfers. |
| FR-035 | The system shall restrict transaction history access to the authenticated user's own wallet. |
| FR-036 | The system shall allow users to view transaction details. |

---

## 5. Business Rules

| ID | Business Rule |
|---|---|
| BR-001 | Each registered user must have exactly one wallet. |
| BR-002 | Each phone number must be unique. |
| BR-003 | Passwords and transaction PINs must be stored as secure hashes. |
| BR-004 | Wallet statuses consist of ACTIVE, INACTIVE, and BLOCKED. |
| BR-005 | Only wallets with ACTIVE status can perform balance-changing transactions. |
| BR-006 | Wallet balances must never become negative. |
| BR-007 | Every successful transaction must have a unique reference. |
| BR-008 | Top-up transactions are simulated and do not involve real payment processing. |
| BR-009 | Top-up transactions do not require admin approval. |
| BR-010 | Transfers are only allowed between registered FinWallet users. |
| BR-011 | The sender must have sufficient available balance before transferring funds. |
| BR-012 | A valid transaction PIN is required for transfers. |
| BR-013 | A valid transaction PIN is required for payments and expenses. |
| BR-014 | All transaction amounts must be greater than zero. |
| BR-015 | Failed transactions must not cause partial balance changes. |
| BR-016 | Users can only access their own wallet and transaction records. |
| BR-017 | Successful financial transaction records must not be silently modified or deleted. |
| BR-018 | Every wallet balance change must have a corresponding transaction record. |

---

## 6. Non-Functional Requirements

### 6.1 Security

- Passwords and transaction PINs must be securely hashed.
- Protected APIs must require valid JWT authentication.
- Users must not be able to access another user's wallet information.
- Transaction PIN validation must be performed before processing debit transactions.

### 6.2 Data Integrity

- Wallet balances must remain consistent with successful transactions.
- Transfers must process debit and credit operations atomically.
- Failed transactions must not partially update wallet balances.
- Financial transaction records must be traceable.

### 6.3 Reliability

- The system must handle transaction failures safely.
- Database transactions must ensure consistency.
- The system must prevent concurrent transactions from causing incorrect wallet balances.

### 6.4 Performance

- The system should provide responsive API operations.
- Database queries should be optimized for common wallet and transaction operations.

### 6.5 Maintainability

- The backend should follow a layered architecture.
- Business logic should be separated from controllers and repositories.
- The API should be documented using OpenAPI/Swagger.
- The system should include automated tests.

---

## 7. Project Success Criteria

The MVP will be considered successful when:

1. Users can register and log in successfully.
2. Each registered user automatically receives one wallet.
3. Users can view their wallet balance and status.
4. Users can perform simulated top-up transactions.
5. Users can transfer funds to other FinWallet users.
6. Users can record payments and expenses.
7. Debit transactions require valid transaction PINs.
8. Wallet balances cannot become negative.
9. Failed transactions do not cause partial balance changes.
10. Users can access their own transaction history and details.
11. The system maintains accurate transaction records.
12. Automated tests verify the main business rules.
13. API documentation is available.

---

## 8. Project Constraints and Assumptions

- All transactions are simulated for educational and portfolio purposes.
- No real money is transferred or stored.
- Each user owns exactly one FinWallet wallet.
- The application uses Indonesian Rupiah (IDR).
- PostgreSQL is used as the primary database.
- The backend is developed using Java 21 and Spring Boot 3.
- The frontend is developed using Angular.
- The system initially targets an MVP implementation.

---

**Document End**

*FinWallet — Business Requirements Document v1.1*