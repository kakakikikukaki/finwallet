# FinWallet — Test Plan

**Version:** 1.1  
**Application:** FinWallet Backend REST API  
**Testing Type:** Functional, Validation, Security, Integration, and Concurrency Testing  
**Database:** PostgreSQL

---

## Revision History

| Version | Date | Description |
|---|---|---|
| 1.0 | Initial release | Initial document version |
| 1.1 | 2026-11-10 | Updated technology stack to Spring Boot 4.1.2 and revised technical specifications |

## 1. Testing Objectives

Pengujian bertujuan memastikan bahwa:

1. Semua fitur berjalan sesuai BRD dan Use Case Specification.
2. API menerima request valid dan menolak request tidak valid.
3. Saldo wallet selalu akurat.
4. Setiap perubahan saldo memiliki catatan transaksi.
5. Transfer dan Payment diproses secara atomik.
6. Pengguna tidak dapat mengakses data pengguna lain.
7. JWT, password, dan transaction PIN ditangani secara aman.
8. Transaksi bersamaan tidak menyebabkan saldo tidak konsisten.

## 2. Testing Scope

### 2.1 In Scope

- User registration.
- User login.
- View and update profile.
- Change password.
- View wallet balance and status.
- Top Up.
- Transfer.
- Payment / Expense.
- Transaction categories.
- Transaction history and detail.
- Authentication and authorization.
- Database transaction integrity.
- Concurrent financial transactions.

### 2.2 Out of Scope

- Real bank transfer.
- Payment gateway integration.
- Merchant payment processing.
- Admin approval.
- Refund and reversal.
- Refresh token.
- Server-side JWT revocation.
- Mobile application testing.

## 3. Test Environment

| Component | Configuration |
|---|---|
| Java | Java 21 |
| Framework | Spring Boot 4 |
| Database | PostgreSQL |
| Security | Spring Security + JWT |
| ORM | Spring Data JPA / Hibernate |
| API Testing | Postman |
| Unit Testing | JUnit 5 |
| Mocking | Mockito |
| Integration Testing | Spring Boot Test |
| Build Tool | Maven |

Testing should use a dedicated test database. Production credentials and production financial data must never be used.

## 4. Test Data

Use the following sample data in a controlled test environment.

| Data | User A | User B |
|---|---|---|
| Name | User A | User B |
| Phone | 081111111111 | 082222222222 |
| Password | TestPassword123! | TestPassword123! |
| PIN | 123456 | 654321 |
| Initial Balance | IDR 500,000 | IDR 200,000 |
| Wallet Status | ACTIVE | ACTIVE |

Additional test conditions:
- A user with an INACTIVE wallet.
- A user with a BLOCKED wallet.
- A BLOCKED user account.
- An inactive transaction category.
- A nonexistent recipient.
- An expired or invalid JWT.

These are test-only values and must not be used as production credentials.

## 5. Functional Test Cases

### 5.1 Registration

| ID | Test Scenario | Expected Result |
|---|---|---|
| REG-001 | Register with valid data | HTTP 201; user and wallet created |
| REG-002 | Register with existing phone number | HTTP 409 |
| REG-003 | Register without full name | HTTP 400 |
| REG-004 | Register without phone number | HTTP 400 |
| REG-005 | Register with invalid phone format | HTTP 400 |
| REG-006 | Register with weak password | HTTP 400 |
| REG-007 | Register with PIN containing fewer than 6 digits | HTTP 400 |
| REG-008 | Register with PIN containing non-numeric characters | HTTP 400 |
| REG-009 | Verify stored password and PIN | Both values are hashed |
| REG-010 | Simulate wallet creation failure | User creation is rolled back |

### 5.2 Login

| ID | Test Scenario | Expected Result |
|---|---|---|
| LOG-001 | Login with valid credentials | HTTP 200 and JWT returned |
| LOG-002 | Login with incorrect password | HTTP 401 |
| LOG-003 | Login with unknown phone number | HTTP 401 |
| LOG-004 | Login with missing password | HTTP 400 |
| LOG-005 | Login with BLOCKED user | Login rejected |
| LOG-006 | Login with ACTIVE user and BLOCKED wallet | Login allowed; financial operations remain restricted |
| LOG-007 | Verify JWT claims | Subject, issued time, expiration, and issuer are valid |

### 5.3 Profile

| ID | Test Scenario | Expected Result |
|---|---|---|
| PRF-001 | View own profile | HTTP 200 and own profile returned |
| PRF-002 | Update full name | HTTP 200 and profile updated |
| PRF-003 | Update with empty full name | HTTP 400 |
| PRF-004 | Attempt to update another user's profile | Request rejected |
| PRF-005 | Verify response fields | Password hash and PIN hash are not exposed |

### 5.4 Change Password

| ID | Test Scenario | Expected Result |
|---|---|---|
| PWD-001 | Change password with correct current password | HTTP 200 |
| PWD-002 | Change password with incorrect current password | Request rejected |
| PWD-003 | Change password with invalid new password | HTTP 400 |
| PWD-004 | Login with new password | Login successful |
| PWD-005 | Login with old password | Login rejected |

### 5.5 Wallet

| ID | Test Scenario | Expected Result |
|---|---|---|
| WAL-001 | View own wallet | HTTP 200 |
| WAL-002 | View wallet balance | Correct balance returned |
| WAL-003 | View wallet with INACTIVE status | Wallet information still accessible |
| WAL-004 | View wallet with BLOCKED status | Wallet information still accessible |
| WAL-005 | Attempt to access another user's wallet | Access rejected |

### 5.6 Top Up

| ID | Test Scenario | Expected Result |
|---|---|---|
| TOP-001 | Top up with valid amount | HTTP 201; balance increases |
| TOP-002 | Top up with zero amount | HTTP 400 |
| TOP-003 | Top up with negative amount | HTTP 400 |
| TOP-004 | Top up with missing amount | HTTP 400 |
| TOP-005 | Top up an INACTIVE wallet | HTTP 403 |
| TOP-006 | Top up a BLOCKED wallet | HTTP 403 |
| TOP-007 | Verify ledger after top up | One TOP_UP / CREDIT entry exists |
| TOP-008 | Simulate ledger save failure | Balance update is rolled back |
| TOP-009 | Verify top up without PIN | Allowed according to MVP rules |

### 5.7 Transfer

| ID | Test Scenario | Expected Result |
|---|---|---|
| TRF-001 | Transfer with valid data | HTTP 201; sender debited and recipient credited |
| TRF-002 | Transfer to unknown phone number | HTTP 404 |
| TRF-003 | Transfer to own phone number | HTTP 400 |
| TRF-004 | Transfer with incorrect PIN | Request rejected; balance unchanged |
| TRF-005 | Transfer with zero amount | HTTP 400 |
| TRF-006 | Transfer with negative amount | HTTP 400 |
| TRF-007 | Transfer with insufficient balance | HTTP 400; balance unchanged |
| TRF-008 | Transfer from INACTIVE wallet | HTTP 403 |
| TRF-009 | Transfer from BLOCKED wallet | HTTP 403 |
| TRF-010 | Transfer to INACTIVE recipient wallet | HTTP 403 |
| TRF-011 | Transfer to BLOCKED recipient wallet | HTTP 403 |
| TRF-012 | Verify transfer ledger | One DEBIT and one CREDIT with the same reference |
| TRF-013 | Simulate recipient credit failure | Sender debit is rolled back |
| TRF-014 | Verify both wallet balances | Total combined balance remains unchanged |

### 5.8 Payment / Expense

| ID | Test Scenario | Expected Result |
|---|---|---|
| PAY-001 | Payment with valid data | HTTP 201; balance decreases |
| PAY-002 | Payment with incorrect PIN | Request rejected; balance unchanged |
| PAY-003 | Payment with zero amount | HTTP 400 |
| PAY-004 | Payment with negative amount | HTTP 400 |
| PAY-005 | Payment with insufficient balance | HTTP 400 |
| PAY-006 | Payment with nonexistent category | HTTP 404 |
| PAY-007 | Payment with inactive category | Request rejected |
| PAY-008 | Payment from INACTIVE wallet | HTTP 403 |
| PAY-009 | Payment from BLOCKED wallet | HTTP 403 |
| PAY-010 | Payment without description | Allowed |
| PAY-011 | Verify payment ledger | One PAYMENT / DEBIT entry exists |
| PAY-012 | Simulate ledger save failure | Balance update is rolled back |

### 5.9 Transaction Categories

| ID | Test Scenario | Expected Result |
|---|---|---|
| CAT-001 | Get active categories | HTTP 200 |
| CAT-002 | Verify inactive categories | Not included in response |
| CAT-003 | Request categories without JWT | HTTP 401 |

### 5.10 Transaction History and Detail

| ID | Test Scenario | Expected Result |
|---|---|---|
| HIS-001 | Get own transaction history | HTTP 200 |
| HIS-002 | Get history with pagination | Correct page and size |
| HIS-003 | Filter by transaction type | Only matching records returned |
| HIS-004 | Filter by entry type | Only CREDIT or DEBIT records returned |
| HIS-005 | Filter by date range | Only matching records returned |
| HIS-006 | Get own transaction detail | HTTP 200 |
| HIS-007 | Get nonexistent transaction detail | HTTP 404 |
| HIS-008 | Access another user's transaction detail | HTTP 404 |
| HIS-009 | Verify transaction ordering | Newest transaction first |

## 6. Security Test Cases

| ID | Test Scenario | Expected Result |
|---|---|---|
| SEC-001 | Access protected endpoint without JWT | HTTP 401 |
| SEC-002 | Access protected endpoint with invalid JWT | HTTP 401 |
| SEC-003 | Access protected endpoint with expired JWT | HTTP 401 |
| SEC-004 | Access another user's wallet | Request rejected |
| SEC-005 | Access another user's transaction | HTTP 404 |
| SEC-006 | Inspect registration response | No password or PIN hash |
| SEC-007 | Inspect login response | No password or PIN hash |
| SEC-008 | Inspect application logs | No plaintext password, PIN, or complete JWT |
| SEC-009 | Submit invalid request values | Request validation rejects invalid input |
| SEC-010 | Attempt financial operation with non-ACTIVE wallet | HTTP 403 |

## 7. Transaction Integrity Test Cases

These tests verify that financial data remains consistent even when a transaction fails.

| ID | Test Scenario | Expected Result |
|---|---|---|
| INT-001 | Successful Top Up | Balance and CREDIT ledger entry both saved |
| INT-002 | Successful Transfer | Both wallet balances and both ledger entries saved |
| INT-003 | Successful Payment | Balance and DEBIT ledger entry both saved |
| INT-004 | Failure while saving transaction ledger | Balance update rolled back |
| INT-005 | Failure while updating recipient balance | Entire transfer rolled back |
| INT-006 | Transfer between two wallets | Combined balance remains unchanged |
| INT-007 | Top Up | Balance increases by exactly the top up amount |
| INT-008 | Payment | Balance decreases by exactly the payment amount |
| INT-009 | Attempt to create negative balance | Database rejects the update |
| INT-010 | Verify transaction reference | Each financial operation has a valid reference |

## 8. Concurrency Test Cases

Concurrency tests must use multiple independent requests or threads.

| ID | Test Scenario | Expected Result |
|---|---|---|
| CON-001 | Two simultaneous transfers from the same sender | No overspending |
| CON-002 | Two simultaneous payments from the same wallet | No negative balance |
| CON-003 | Two simultaneous top ups | Both valid top ups are reflected exactly once |
| CON-004 | Two transfers involving the same pair of wallets | No lost balance update |
| CON-005 | Concurrent transfers in opposite directions | No inconsistent balance or unresolved deadlock |
| CON-006 | Concurrent requests exceed available balance in total | Only affordable transactions succeed |

Example:

A wallet has IDR 100,000. Two requests simultaneously attempt to transfer IDR 80,000.

Expected result:
- Only one transfer succeeds.
- The other transfer is rejected for insufficient balance.
- Final balance is IDR 20,000.
- No partial ledger entries exist.

## 9. Test Execution and Reporting

Each test case should be recorded with:

| Field | Description |
|---|---|
| Test Case ID | Unique test identifier |
| Execution Date | Date the test was executed |
| Actual Result | Actual system response |
| Status | PASS / FAIL / BLOCKED |
| Evidence | Screenshot, API response, or test report |
| Remarks | Additional notes |

### Test Result Definitions

- **PASS:** Actual result matches expected result.
- **FAIL:** Actual result differs from expected result.
- **BLOCKED:** Test cannot be executed due to an unresolved dependency or issue.
- **NOT RUN:** Test has not been executed.

## 10. Testing Completion Criteria

Testing is considered complete when:

1. All critical functional test cases pass.
2. All financial transaction integrity tests pass.
3. No unauthorized access to another user's wallet or transaction is possible.
4. No transaction produces a negative wallet balance.
5. Concurrent transactions do not cause overspending or lost updates.
6. Failed transactions leave no partial balance or ledger changes.
7. All critical defects have been resolved and retested.
8. Test execution results are documented.

## 11. Automation Strategy

Recommended automated testing:

- Unit tests for service business logic.
- Mockito tests for isolated service dependencies.
- Spring Boot integration tests for API and security behavior.
- PostgreSQL-backed integration tests for transaction and locking behavior.
- Postman collection for manual API verification.
- Concurrent integration tests for balance integrity.

Use JUnit 5 and Testcontainers with PostgreSQL where practical, so database-specific behavior is tested against PostgreSQL rather than relying only on an in-memory database.