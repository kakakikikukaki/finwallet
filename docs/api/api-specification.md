# FinWallet — API Specification

**Version:** 1.0  
**Base URL:** `/api/v1`  
**Protocol:** HTTPS in production  
**Data Format:** JSON  
**Authentication:** JWT Bearer Token  
**Currency:** IDR

---

## 1. General API Standards

### 1.1 Request Headers

Protected endpoints require:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
Accept: application/json
```

### 1.2 Response Format

Successful response:

```json
{
  "success": true,
  "message": "Request successful",
  "data": {}
}
```

Error response:

```json
{
  "success": false,
  "message": "Request failed",
  "error": {
    "code": "VALIDATION_ERROR",
    "details": [
      {
        "field": "amount",
        "message": "Amount must be greater than zero"
      }
    ]
  }
}
```

### 1.3 HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | Request successful |
| 201 | Resource successfully created |
| 400 | Invalid request or validation error |
| 401 | Missing or invalid authentication |
| 403 | Authenticated user is not allowed to perform the action |
| 404 | Resource not found |
| 409 | Conflict, such as duplicate phone number |
| 500 | Internal server error |

### 1.4 Common Rules

- All request and response bodies use JSON.
- Monetary values use decimal numbers, not floating-point calculations.
- Currency is IDR.
- Date-time values use ISO 8601 with timezone information.
- IDs use UUID format.
- Sensitive values such as password and transaction PIN must never appear in API responses.
- Financial operations must be processed atomically.
- The API must not expose another user's private wallet or transaction data.

---

## 2. Authentication APIs

### 2.1 Register

**Endpoint:** `POST /auth/register`  
**Authentication:** Public  
**Success Status:** `201 Created`

Request:

```json
{
  "fullName": "Muhammad Rizki",
  "phoneNumber": "081234567890",
  "password": "StrongPassword123!",
  "transactionPin": "123456"
}
```

Validation:
- `fullName` is required, maximum 100 characters.
- `phoneNumber` is required and must use the accepted normalized format.
- `phoneNumber` must be unique.
- `password` is required and must satisfy the configured password policy.
- `transactionPin` is required and must contain exactly 6 digits.

Success response:

```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "userId": "UUID",
    "fullName": "Muhammad Rizki",
    "phoneNumber": "081234567890",
    "wallet": {
      "walletId": "UUID",
      "balance": 0.00,
      "currency": "IDR",
      "status": "ACTIVE"
    }
  }
}
```

Business rules:
- Create user and wallet in one database transaction.
- Hash password and PIN before storing.
- Automatically create one ACTIVE wallet with zero balance.
- Do not automatically log the user in after registration in this API version.
- Reject duplicate phone numbers.

### 2.2 Login

**Endpoint:** `POST /auth/login`  
**Authentication:** Public  
**Success Status:** `200 OK`

Request:

```json
{
  "phoneNumber": "081234567890",
  "password": "StrongPassword123!"
}
```

Success response:

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "accessToken": "<jwt-access-token>",
    "tokenType": "Bearer",
    "expiresIn": 900,
    "user": {
      "id": "UUID",
      "fullName": "Muhammad Rizki",
      "phoneNumber": "081234567890"
    }
  }
}
```

Business rules:
- Find user by phone number.
- Verify password using the configured password encoder.
- Return the same authentication error for unknown phone numbers and incorrect passwords.
- Generate JWT only after successful authentication.
- An INACTIVE or BLOCKED wallet does not automatically prevent login; it prevents financial transactions.
- A BLOCKED user account cannot log in.

---

## 3. User APIs

All endpoints require JWT authentication.

### 3.1 View Profile

**Endpoint:** `GET /users/me`  
**Success Status:** `200 OK`

Success response:

```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "data": {
    "id": "UUID",
    "fullName": "Muhammad Rizki",
    "phoneNumber": "081234567890",
    "status": "ACTIVE",
    "createdAt": "2026-11-09T10:00:00+07:00"
  }
}
```

### 3.2 Update Profile

**Endpoint:** `PUT /users/me`  
**Success Status:** `200 OK`

Request:

```json
{
  "fullName": "Muhammad Rizki Darmawan"
}
```

Business rules:
- Only the authenticated user's profile can be updated.
- Phone number cannot be changed through this endpoint.
- Validate the full name.

### 3.3 Change Password

**Endpoint:** `PATCH /users/me/password`  
**Success Status:** `200 OK`

Request:

```json
{
  "currentPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}
```

Business rules:
- Verify the current password.
- Validate the new password against the password policy.
- Hash the new password before saving.
- Do not return password hashes.
- Existing JWT tokens are not automatically revoked in this MVP. The client should require a fresh login after password change.

---

## 4. Wallet APIs

### 4.1 View Wallet Balance and Status

**Endpoint:** `GET /wallets/me`  
**Success Status:** `200 OK`

Success response:

```json
{
  "success": true,
  "message": "Wallet retrieved successfully",
  "data": {
    "walletId": "UUID",
    "balance": 500000.00,
    "currency": "IDR",
    "status": "ACTIVE"
  }
}
```

Business rules:
- Retrieve the wallet belonging to the authenticated user.
- Return the current balance and wallet status.
- This endpoint is read-only.
- A wallet with INACTIVE or BLOCKED status may still be viewed by its owner.

### 4.2 Top Up Wallet

**Endpoint:** `POST /wallets/me/top-ups`  
**Success Status:** `201 Created`

Request:

```json
{
  "amount": 100000.00,
  "description": "Simulated wallet top up"
}
```

Success response:

```json
{
  "success": true,
  "message": "Top up successful",
  "data": {
    "transactionId": "UUID",
    "referenceNo": "TOP-20261109-001",
    "transactionType": "TOP_UP",
    "entryType": "CREDIT",
    "amount": 100000.00,
    "balanceBefore": 500000.00,
    "balanceAfter": 600000.00,
    "currency": "IDR",
    "createdAt": "2026-11-09T10:30:00+07:00"
  }
}
```

Business rules:
- Amount must be greater than zero.
- Wallet must have ACTIVE status.
- Top up is simulated; no payment gateway or bank integration.
- No transaction PIN is required.
- Increase balance and save the TOP_UP ledger entry atomically.
- Generate a unique transaction reference.

---

## 5. Transfer API

### 5.1 Transfer Funds

**Endpoint:** `POST /transfers`  
**Success Status:** `201 Created`

Request:

```json
{
  "recipientPhoneNumber": "081298765432",
  "amount": 50000.00,
  "transactionPin": "123456",
  "description": "Transfer to friend"
}
```

Success response:

```json
{
  "success": true,
  "message": "Transfer successful",
  "data": {
    "referenceNo": "TRF-20261109-001",
    "transactionType": "TRANSFER",
    "amount": 50000.00,
    "currency": "IDR",
    "sender": {
      "walletId": "UUID",
      "balanceAfter": 550000.00
    },
    "recipient": {
      "walletId": "UUID"
    },
    "createdAt": "2026-11-09T11:00:00+07:00"
  }
}
```

Business rules:
- Recipient must be a registered FinWallet user.
- Sender cannot transfer to their own wallet.
- Sender and recipient wallets must both be ACTIVE.
- Sender's transaction PIN must be correct.
- Amount must be greater than zero.
- Sender must have sufficient balance.
- Lock both wallets and re-check the sender's balance before updating.
- Lock wallet rows in a consistent order to reduce deadlocks.
- Debit sender and credit recipient in one database transaction.
- Save two ledger entries using the same reference number:
  - Sender: TRANSFER / DEBIT.
  - Recipient: TRANSFER / CREDIT.
- If any operation fails, roll back the complete transfer.

---

## 6. Payment API

### 6.1 Record Payment / Expense

**Endpoint:** `POST /payments`  
**Success Status:** `201 Created`

Request:

```json
{
  "categoryId": "UUID",
  "amount": 25000.00,
  "transactionPin": "123456",
  "description": "Lunch"
}
```

Success response:

```json
{
  "success": true,
  "message": "Payment recorded successfully",
  "data": {
    "transactionId": "UUID",
    "referenceNo": "PAY-20261109-001",
    "transactionType": "PAYMENT",
    "entryType": "DEBIT",
    "amount": 25000.00,
    "balanceBefore": 550000.00,
    "balanceAfter": 525000.00,
    "currency": "IDR",
    "createdAt": "2026-11-09T12:00:00+07:00"
  }
}
```

Business rules:
- Category must exist and be active.
- Amount must be greater than zero.
- Transaction PIN must be correct.
- Wallet must have ACTIVE status.
- Wallet must have sufficient balance.
- Deduct balance and save PAYMENT ledger entry atomically.
- Payment is a simulated wallet deduction, not a real merchant or payment gateway transaction.
- Description is optional.

---

## 7. Transaction Category API

### 7.1 Get Active Categories

**Endpoint:** `GET /transaction-categories`  
**Success Status:** `200 OK`

Success response:

```json
{
  "success": true,
  "message": "Categories retrieved successfully",
  "data": [
    {
      "id": "UUID",
      "name": "Food"
    },
    {
      "id": "UUID",
      "name": "Transportation"
    },
    {
      "id": "UUID",
      "name": "Shopping"
    }
  ]
}
```

Business rules:
- Return active categories only.
- Categories are managed through initial database seed data in the MVP.
- No category creation or deletion endpoint is exposed to regular users.

---

## 8. Transaction History APIs

### 8.1 Get Transaction History

**Endpoint:** `GET /transactions`  
**Success Status:** `200 OK`

Query parameters:

| Parameter | Type | Required | Description |
|---|---|---|---|
| page | Integer | No | Page number, default 0 |
| size | Integer | No | Page size, default 10 |
| type | String | No | TOP_UP / TRANSFER / PAYMENT |
| entryType | String | No | CREDIT / DEBIT |
| startDate | Date | No | Start date filter |
| endDate | Date | No | End date filter |

Example:

```http
GET /api/v1/transactions?page=0&size=10&type=TRANSFER
```

Success response:

```json
{
  "success": true,
  "message": "Transactions retrieved successfully",
  "data": {
    "content": [
      {
        "id": "UUID",
        "referenceNo": "TRF-20261109-001",
        "transactionType": "TRANSFER",
        "entryType": "DEBIT",
        "amount": 50000.00,
        "description": "Transfer to friend",
        "createdAt": "2026-11-09T11:00:00+07:00"
      }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 1,
    "totalPages": 1
  }
}
```

Business rules:
- Return only transactions belonging to the authenticated user's wallet.
- Sort newest transactions first.
- Apply pagination.
- Validate date range and filter values.
- For transfers, show DEBIT for the sender and CREDIT for the recipient.

### 8.2 Get Transaction Detail

**Endpoint:** `GET /transactions/{id}`  
**Success Status:** `200 OK`

Example:

```http
GET /api/v1/transactions/UUID
```

Success response:

```json
{
  "success": true,
  "message": "Transaction detail retrieved successfully",
  "data": {
    "id": "UUID",
    "referenceNo": "TRF-20261109-001",
    "transactionType": "TRANSFER",
    "entryType": "DEBIT",
    "amount": 50000.00,
    "currency": "IDR",
    "description": "Transfer to friend",
    "relatedWalletId": "UUID",
    "createdAt": "2026-11-09T11:00:00+07:00"
  }
}
```

Business rules:
- Retrieve only a transaction belonging to the authenticated user's wallet.
- Return `404 NOT_FOUND` when the transaction does not exist or does not belong to the user.
- Do not expose another user's private information.

---

## 9. Error Codes

| Error Code | HTTP Status | Description |
|---|---:|---|
| VALIDATION_ERROR | 400 | Request validation failed |
| INVALID_CREDENTIALS | 401 | Phone number or password is incorrect |
| INVALID_TOKEN | 401 | JWT is invalid or expired |
| ACCESS_DENIED | 403 | User is not allowed to perform the action |
| USER_BLOCKED | 403 | User account is blocked |
| PHONE_ALREADY_REGISTERED | 409 | Phone number already exists |
| USER_NOT_FOUND | 404 | User does not exist |
| WALLET_NOT_FOUND | 404 | Wallet does not exist |
| WALLET_NOT_ACTIVE | 403 | Wallet cannot perform financial transactions |
| INVALID_TRANSACTION_PIN | 400 | Transaction PIN is incorrect |
| INVALID_AMOUNT | 400 | Amount is invalid |
| INSUFFICIENT_BALANCE | 400 | Wallet balance is insufficient |
| RECIPIENT_NOT_FOUND | 404 | Recipient does not exist |
| SELF_TRANSFER_NOT_ALLOWED | 400 | Sender cannot transfer to themselves |
| CATEGORY_NOT_FOUND | 404 | Category does not exist |
| INTERNAL_SERVER_ERROR | 500 | Unexpected server error |

Error details must not expose stack traces, SQL statements, password hashes, PIN hashes, or other sensitive information.

---

## 10. Authentication and Authorization Rules

1. Register and login are public endpoints.
2. All other endpoints require a valid JWT access token.
3. The authenticated user's identity must be obtained from the JWT security context.
4. Users may only access their own profile, wallet, and transaction records.
5. Financial transactions require an ACTIVE wallet.
6. Transfer and Payment require transaction PIN verification.
7. Top Up does not require transaction PIN verification in the MVP.
8. A BLOCKED user cannot log in.
9. A BLOCKED or INACTIVE wallet cannot perform financial transactions.
10. The JWT access token is stateless in this MVP. Logout is performed by deleting the token on the client; server-side token revocation is not included.

---

## 11. API Scope Limitations

The MVP does not include:
- Refresh token.
- Forgot/reset password.
- Change transaction PIN.
- Server-side JWT blacklist or token revocation.
- Real bank or payment gateway integration.
- Merchant payment processing.
- Refund and reversal.
- Admin APIs.
- Real-time notification.