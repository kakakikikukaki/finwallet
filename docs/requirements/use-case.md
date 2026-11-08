# FINWALLET
# Use Case Specification

| Document Information | Details |
|---|---|
| Project Name | FinWallet |
| Document Type | Use Case Specification |
| Version | 1.1 |
| Status | Approved |
| Reference | BRD v1.1 |

---

## 1. Introduction

### 1.1 Purpose

This document describes the interactions between users and the FinWallet system. It defines the actors, use cases, preconditions, main flows, alternative flows, and expected outcomes.

### 1.2 Actors

| Actor | Description |
|---|---|
| Guest | A visitor who has not authenticated. |
| Registered User | An authenticated FinWallet user. |
| System | Processes requests, validates business rules, and manages wallet transactions. |

---

## 2. Use Case List

| Use Case ID | Use Case Name | Primary Actor |
|---|---|---|
| UC-01 | Register Account | Guest |
| UC-02 | Login | Guest |
| UC-03 | View and Update Profile | Registered User |
| UC-04 | Change Password | Registered User |
| UC-05 | View Wallet Balance and Status | Registered User |
| UC-06 | Top Up Wallet | Registered User |
| UC-07 | Transfer Funds | Registered User |
| UC-08 | Record Payment / Expense | Registered User |
| UC-09 | View Transaction History and Detail | Registered User |

---

## 3. Use Case Specifications

### UC-01 — Register Account

**Primary Actor:** Guest

**Description:** Allows a guest to create a new FinWallet account.

**Preconditions:**
- The guest does not have an existing account using the same phone number.

**Main Flow:**
1. Guest selects Register.
2. System displays the registration form.
3. Guest enters name, phone number, password, and transaction PIN.
4. Guest submits the registration form.
5. System validates the submitted information.
6. System checks phone number uniqueness.
7. System hashes the password and transaction PIN.
8. System creates the user account.
9. System automatically creates one ACTIVE wallet with an initial balance of IDR 0.
10. System saves the user and wallet records atomically.
11. System displays successful registration.

**Alternative Flows:**
- A1: Phone number already exists. System rejects registration and displays an error.
- A2: Invalid input. System displays validation errors.
- A3: Database operation fails. System rolls back user and wallet creation.

**Postconditions:**
- A new user account exists.
- One ACTIVE wallet is associated with the user.
- Initial wallet balance is IDR 0.

---

### UC-02 — Login

**Primary Actor:** Guest

**Description:** Allows an existing user to authenticate using their phone number and password.

**Preconditions:**
- The user has registered an account.

**Main Flow:**
1. Guest selects Login.
2. System displays the login form.
3. Guest enters phone number and password.
4. Guest submits the login form.
5. System validates the credentials.
6. System verifies the password hash.
7. System generates a JWT access token.
8. System returns the token to the user.
9. User gains access to protected features.

**Alternative Flows:**
- A1: Phone number does not exist. System rejects login.
- A2: Password is incorrect. System rejects login.
- A3: Credentials are invalid. System displays an authentication error.

**Postconditions:**
- User is authenticated.
- A valid JWT access token is issued.

---

### UC-03 — View and Update Profile

**Primary Actor:** Registered User

**Description:** Allows an authenticated user to view and update their own profile.

**Preconditions:**
- User is authenticated with a valid JWT.

**Main Flow:**
1. User opens Profile.
2. System retrieves the authenticated user's profile.
3. System displays profile information.
4. User selects Edit Profile.
5. User updates permitted profile information.
6. User submits the changes.
7. System validates the submitted information.
8. System saves the changes.
9. System displays an update confirmation.

**Alternative Flows:**
- A1: Invalid profile information. System rejects the changes.
- A2: User attempts to access another user's profile. System denies access.

**Postconditions:**
- User can view their own profile.
- Valid profile changes are saved.

---

### UC-04 — Change Password

**Primary Actor:** Registered User

**Description:** Allows an authenticated user to change their account password.

**Preconditions:**
- User is authenticated.

**Main Flow:**
1. User opens Change Password.
2. System displays the password change form.
3. User enters the current password and new password.
4. User submits the form.
5. System verifies the current password.
6. System validates the new password.
7. System hashes the new password.
8. System updates the password.
9. System displays a success message.

**Alternative Flows:**
- A1: Current password is incorrect. System rejects the request.
- A2: New password does not meet validation rules. System displays an error.

**Postconditions:**
- User password is updated securely.

---

### UC-05 — View Wallet Balance and Status

**Primary Actor:** Registered User

**Description:** Allows users to view their own wallet balance and status.

**Preconditions:**
- User is authenticated.
- User has an associated wallet.

**Main Flow:**
1. User opens Wallet.
2. System retrieves the user's wallet.
3. System displays the current balance.
4. System displays the wallet status.

**Alternative Flows:**
- A1: Wallet information cannot be retrieved. System displays an error.
- A2: User attempts to access another user's wallet. System denies access.

**Postconditions:**
- User can view their own wallet balance and status.

---

### UC-06 — Top Up Wallet

**Primary Actor:** Registered User

**Description:** Allows users to increase their wallet balance through a simulated top-up transaction.

**Preconditions:**
- User is authenticated.
- Wallet status is ACTIVE.

**Main Flow:**
1. User selects Top Up.
2. System displays the top-up form.
3. User enters the top-up amount.
4. User submits the request.
5. System validates that the amount is greater than zero.
6. System validates the wallet status.
7. System generates a unique transaction reference.
8. System increases the wallet balance.
9. System records the transaction with type TOP_UP.
10. System commits the database transaction.
11. System displays the updated balance and transaction confirmation.

**Alternative Flows:**
- A1: Amount is zero or negative. System rejects the request.
- A2: Wallet is INACTIVE or BLOCKED. System rejects the request.
- A3: Database operation fails. System rolls back the balance update and transaction record.

**Postconditions:**
- Wallet balance increases by the top-up amount.
- A TOP_UP transaction record exists.
- No real payment processing is performed.

**Business Notes:**
- No transaction PIN is required.
- No admin approval is required.
- Top-up is simulated within the FinWallet system.

---

### UC-07 — Transfer Funds

**Primary Actor:** Registered User

**Description:** Allows a user to transfer funds to another registered FinWallet user.

**Preconditions:**
- Sender is authenticated.
- Sender has an associated wallet.

**Main Flow:**
1. Sender selects Transfer.
2. System displays the transfer form.
3. Sender enters the recipient's registered phone number.
4. Sender enters the transfer amount.
5. Sender enters the transaction PIN.
6. Sender submits the transfer request.
7. System identifies the recipient using the phone number.
8. System validates that the recipient exists.
9. System validates that sender and recipient are different users.
10. System validates that both wallets have ACTIVE status.
11. System validates that the transfer amount is greater than zero.
12. System verifies the sender's transaction PIN.
13. System validates that the sender has sufficient balance.
14. System generates a unique transfer reference.
15. System debits the sender's wallet.
16. System credits the recipient's wallet.
17. System records the corresponding debit and credit transaction entries using the same transfer reference.
18. System commits all changes atomically.
19. System displays the transfer confirmation.

**Alternative Flows:**
- A1: Recipient phone number does not exist. System rejects the transfer.
- A2: Sender attempts to transfer to their own wallet. System rejects the transfer.
- A3: Recipient wallet is INACTIVE or BLOCKED. System rejects the transfer.
- A4: Sender wallet is INACTIVE or BLOCKED. System rejects the transfer.
- A5: Transaction PIN is incorrect. System rejects the transfer.
- A6: Insufficient balance. System rejects the transfer.
- A7: Amount is zero or negative. System rejects the transfer.
- A8: Database operation fails. System rolls back all balance changes and transaction entries.

**Postconditions:**
- Sender balance decreases by the transfer amount.
- Recipient balance increases by the transfer amount.
- Two corresponding transaction entries exist.
- Both entries share one transfer reference.
- Failed transfers do not partially change either wallet balance.

**Business Notes:**
- Transfers are only allowed between registered FinWallet users.
- A valid transaction PIN is required.
- Both wallets must be ACTIVE.
- Debit and credit operations must be atomic.

---

### UC-08 — Record Payment / Expense

**Primary Actor:** Registered User

**Description:** Allows users to record a simulated payment or expense that deducts funds from their wallet.

**Preconditions:**
- User is authenticated.
- Wallet status is ACTIVE.

**Main Flow:**
1. User selects Payment / Expense.
2. System displays the expense form.
3. User enters the amount.
4. User selects an expense category.
5. User optionally enters a description.
6. User enters the transaction PIN.
7. User submits the request.
8. System validates the amount.
9. System validates the wallet status.
10. System verifies the transaction PIN.
11. System validates sufficient wallet balance.
12. System generates a unique transaction reference.
13. System deducts the expense amount from the wallet balance.
14. System records the transaction with type EXPENSE.
15. System commits the database transaction.
16. System displays the transaction confirmation and updated balance.

**Alternative Flows:**
- A1: Amount is zero or negative. System rejects the request.
- A2: Transaction PIN is incorrect. System rejects the request.
- A3: Insufficient balance. System rejects the request.
- A4: Wallet is INACTIVE or BLOCKED. System rejects the request.
- A5: Database operation fails. System rolls back the balance update and transaction record.

**Postconditions:**
- Wallet balance decreases by the expense amount.
- An EXPENSE transaction record exists.
- Failed requests do not change the wallet balance.

**Business Notes:**
- A valid transaction PIN is required.
- No real merchant or payment gateway is involved.
- The expense represents a simulated wallet deduction.

---

### UC-09 — View Transaction History and Detail

**Primary Actor:** Registered User

**Description:** Allows users to view their own transaction history and individual transaction details.

**Preconditions:**
- User is authenticated.

**Main Flow:**
1. User opens Transaction History.
2. System retrieves transactions associated with the authenticated user's wallet.
3. System displays the transaction list.
4. User selects a transaction.
5. System retrieves the selected transaction details.
6. System validates that the transaction belongs to the user's wallet.
7. System displays transaction reference, type, amount, status, and timestamp.
8. For transfers, the system distinguishes incoming and outgoing transactions.

**Alternative Flows:**
- A1: No transaction records exist. System displays an empty history.
- A2: Requested transaction does not exist. System displays an error.
- A3: Transaction belongs to another user's wallet. System denies access.

**Postconditions:**
- User can view only transactions associated with their own wallet.
- Transaction details are displayed accurately.

---

## 4. General Business Constraints

The following constraints apply to the relevant use cases:

1. Each registered user has exactly one wallet.
2. Only ACTIVE wallets can perform balance-changing transactions.
3. Wallet balances must never become negative.
4. All transaction amounts must be greater than zero.
5. Every successful transaction must have a unique reference.
6. All balance changes must be recorded.
7. Failed financial transactions must not partially update balances.
8. Users can only access their own wallet and transaction records.
9. Passwords and transaction PINs must be securely hashed.
10. Successful financial transaction records must not be silently modified or deleted.

---

**Document End**

*FinWallet — Use Case Specification v1.1*