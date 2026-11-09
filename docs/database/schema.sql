-- =========================================
-- FINWALLET DATABASE SCHEMA
-- Database: PostgreSQL
-- =========================================

-- 1. APP USERS
CREATE TABLE app_users (
    id UUID PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    pin_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_user_status
        CHECK (status IN ('ACTIVE', 'BLOCKED'))
);


-- 2. WALLETS
CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    balance NUMERIC(19, 2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES app_users(id),

    CONSTRAINT chk_wallet_balance
        CHECK (balance >= 0),

    CONSTRAINT chk_wallet_status
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);


-- 3. TRANSACTION CATEGORIES
CREATE TABLE transaction_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 4. WALLET TRANSACTIONS
CREATE TABLE wallet_transactions (
    id UUID PRIMARY KEY,
    wallet_id UUID NOT NULL,
    category_id UUID NULL,
    related_wallet_id UUID NULL,
    reference_no VARCHAR(50) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    entry_type VARCHAR(10) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    description TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transaction_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT fk_transaction_category
        FOREIGN KEY (category_id)
        REFERENCES transaction_categories(id),

    CONSTRAINT fk_transaction_related_wallet
        FOREIGN KEY (related_wallet_id)
        REFERENCES wallets(id),

    CONSTRAINT chk_transaction_type
        CHECK (
            transaction_type IN ('TOP_UP', 'TRANSFER', 'PAYMENT')
        ),

    CONSTRAINT chk_transaction_entry
        CHECK (entry_type IN ('CREDIT', 'DEBIT')),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transaction_rules
        CHECK (
            (transaction_type = 'TOP_UP'
                AND entry_type = 'CREDIT'
                AND category_id IS NULL
                AND related_wallet_id IS NULL)
            OR
            (transaction_type = 'TRANSFER'
                AND category_id IS NULL
                AND related_wallet_id IS NOT NULL
                AND related_wallet_id <> wallet_id)
            OR
            (transaction_type = 'PAYMENT'
                AND entry_type = 'DEBIT'
                AND category_id IS NOT NULL
                AND related_wallet_id IS NULL)
        )
);


-- =========================================
-- INDEXES
-- =========================================

CREATE INDEX idx_wallet_transactions_wallet_created
ON wallet_transactions (wallet_id, created_at DESC);

CREATE INDEX idx_wallet_transactions_reference
ON wallet_transactions (reference_no);

CREATE INDEX idx_wallet_transactions_category
ON wallet_transactions (category_id);


-- =========================================
-- INITIAL PAYMENT CATEGORIES
-- =========================================

INSERT INTO transaction_categories (id, name)
VALUES
    ('10000000-0000-0000-0000-000000000001', 'Food'),
    ('10000000-0000-0000-0000-000000000002', 'Transportation'),
    ('10000000-0000-0000-0000-000000000003', 'Shopping'),
    ('10000000-0000-0000-0000-000000000004', 'Bills'),
    ('10000000-0000-0000-0000-000000000005', 'Entertainment'),
    ('10000000-0000-0000-0000-000000000006', 'Other');