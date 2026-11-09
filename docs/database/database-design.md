# FinWallet — Database Design

**Version:** 1.0  
**Database:** PostgreSQL  
**Currency:** IDR

## 1. Overview

Database FinWallet dirancang untuk menyimpan akun pengguna, wallet, kategori pembayaran, dan catatan transaksi keuangan.

Prinsip desain:
- Satu pengguna hanya memiliki satu wallet.
- Setiap wallet dimiliki oleh tepat satu pengguna.
- Semua nominal menggunakan tipe data `NUMERIC`, bukan floating point.
- Setiap perubahan saldo wajib menghasilkan catatan transaksi.
- Catatan transaksi keuangan tidak boleh diubah atau dihapus secara sembarangan.
- Transfer menghasilkan dua ledger entries dengan satu reference number yang sama.
- Pembuatan akun dan wallet harus dilakukan secara atomik.

## 2. Table Design

### 2.1 app_users

Menyimpan informasi akun pengguna.

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | UUID | PK | User identifier |
| full_name | VARCHAR(100) | NOT NULL | Nama pengguna |
| phone_number | VARCHAR(20) | UNIQUE, NOT NULL | Nomor telepon login |
| password_hash | VARCHAR(255) | NOT NULL | Password yang sudah di-hash |
| pin_hash | VARCHAR(255) | NOT NULL | Transaction PIN yang sudah di-hash |
| status | VARCHAR(20) | NOT NULL | ACTIVE / BLOCKED |
| created_at | TIMESTAMPTZ | NOT NULL | Waktu pembuatan |
| updated_at | TIMESTAMPTZ | NOT NULL | Waktu perubahan |

Nomor telepon harus disimpan dalam format yang konsisten agar pengecekan keunikan tidak menghasilkan nomor ganda akibat perbedaan format.

### 2.2 wallets

Menyimpan informasi wallet dan saldo pengguna.

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | UUID | PK | Wallet identifier |
| user_id | UUID | FK, UNIQUE, NOT NULL | Pemilik wallet |
| balance | NUMERIC(19,2) | NOT NULL | Saldo wallet |
| status | VARCHAR(20) | NOT NULL | ACTIVE / INACTIVE / BLOCKED |
| created_at | TIMESTAMPTZ | NOT NULL | Waktu pembuatan |
| updated_at | TIMESTAMPTZ | NOT NULL | Waktu perubahan |

Saldo awal wallet adalah IDR 0.00.

Satu user hanya boleh memiliki satu wallet. Saldo tidak boleh bernilai negatif.

### 2.3 transaction_categories

Menyimpan kategori pembayaran atau pengeluaran.

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | UUID | PK | Category identifier |
| name | VARCHAR(50) | UNIQUE, NOT NULL | Nama kategori |
| is_active | BOOLEAN | NOT NULL | Status kategori |
| created_at | TIMESTAMPTZ | NOT NULL | Waktu pembuatan |

Contoh kategori:
- Food
- Transportation
- Shopping
- Bills
- Entertainment
- Other

Kategori hanya digunakan untuk transaksi PAYMENT. TOP_UP dan TRANSFER tidak memerlukan kategori.

### 2.4 wallet_transactions

Menyimpan catatan seluruh perubahan saldo wallet.

| Column | Type | Constraint | Description |
|---|---|---|---|
| id | UUID | PK | Transaction entry identifier |
| wallet_id | UUID | FK, NOT NULL | Wallet yang mengalami perubahan saldo |
| category_id | UUID | FK, NULL | Kategori pembayaran |
| related_wallet_id | UUID | NULL | Wallet lawan transaksi untuk transfer |
| reference_no | VARCHAR(50) | NOT NULL | Referensi transaksi |
| transaction_type | VARCHAR(20) | NOT NULL | TOP_UP / TRANSFER / PAYMENT |
| entry_type | VARCHAR(10) | NOT NULL | CREDIT / DEBIT |
| amount | NUMERIC(19,2) | NOT NULL | Nominal transaksi |
| description | TEXT | NULL | Keterangan transaksi |
| created_at | TIMESTAMPTZ | NOT NULL | Waktu transaksi |

Nominal `amount` selalu disimpan sebagai angka positif. Arah perubahan saldo ditentukan oleh `entry_type`.

## 3. Transaction Recording Rules

| Transaction | Entry Type | Balance Impact |
|---|---|---|
| Top Up | CREDIT | Menambah saldo |
| Transfer - Sender | DEBIT | Mengurangi saldo |
| Transfer - Recipient | CREDIT | Menambah saldo |
| Payment | DEBIT | Mengurangi saldo |

### Transfer Example

Pengguna A mengirim IDR 100,000 kepada pengguna B.

Sistem mencatat dua baris:

| Wallet | Reference | Type | Entry | Amount |
|---|---|---|---|---:|
| Wallet A | TRF-001 | TRANSFER | DEBIT | 100,000 |
| Wallet B | TRF-001 | TRANSFER | CREDIT | 100,000 |

Kedua catatan harus tersimpan dalam satu database transaction. Jika salah satu proses gagal, seluruh perubahan harus di-rollback.

## 4. Data Integrity Rules

1. `app_users.phone_number` harus unik.
2. `wallets.user_id` harus unik.
3. `wallets.balance` tidak boleh negatif.
4. Nominal transaksi harus lebih besar dari nol.
5. `reference_no` boleh digunakan oleh dua baris untuk transfer, tetapi tidak boleh digunakan oleh transaksi berbeda.
6. Setiap transfer harus memiliki pasangan DEBIT dan CREDIT.
7. TOP_UP hanya menghasilkan CREDIT.
8. PAYMENT hanya menghasilkan DEBIT.
9. Semua transaksi yang berhasil harus memiliki catatan ledger.
10. Data transaksi tidak boleh diubah atau dihapus melalui fitur aplikasi biasa.

## 5. Transaction Processing

Semua proses yang mengubah saldo wajib menggunakan database transaction.

Untuk mencegah saldo tidak konsisten ketika beberapa permintaan berjalan bersamaan:
- Gunakan database transaction.
- Kunci baris wallet menggunakan row-level locking.
- Periksa ulang saldo setelah wallet terkunci.
- Untuk transfer, kunci kedua wallet dengan urutan ID yang konsisten.
- Simpan perubahan saldo dan ledger entries dalam satu transaksi database.

## 6. Security Considerations

- Password disimpan dalam bentuk hash menggunakan BCrypt atau password encoder yang aman.
- Transaction PIN disimpan dalam bentuk hash, bukan plaintext.
- Jangan pernah mengembalikan password hash atau PIN hash melalui API.
- Akses transaksi harus dibatasi berdasarkan wallet milik pengguna yang sedang login.
- Validasi status user dan wallet sebelum transaksi finansial.

## 7. Scope Limitations

Database MVP tidak mencakup:
- Integrasi bank atau payment gateway.
- Transaksi uang sungguhan.
- Admin approval.
- Pencatatan gaji atau pendapatan pribadi.
- Refund dan reversal transaksi.
- Multi-currency.