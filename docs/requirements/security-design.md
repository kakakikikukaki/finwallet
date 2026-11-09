# FinWallet — Security Design

**Version:** 1.0  
**Backend:** Java 21 + Spring Boot 3  
**Security Framework:** Spring Security  
**Authentication:** JWT  
**Password Hashing:** BCrypt  
**Database:** PostgreSQL

---

## 1. Security Objectives

Security design FinWallet bertujuan untuk:

1. Melindungi akun dan informasi pengguna.
2. Memastikan hanya pengguna terautentikasi yang dapat mengakses endpoint privat.
3. Membatasi akses data berdasarkan pemilik wallet.
4. Melindungi password dan transaction PIN.
5. Mencegah transaksi finansial tanpa otorisasi.
6. Menjaga integritas saldo dan catatan transaksi.
7. Mengurangi risiko penyalahgunaan API.

## 2. Authentication

### 2.1 Login Authentication

Pengguna melakukan login menggunakan:
- Phone number.
- Password.

Authentication flow:
1. User mengirim phone number dan password.
2. Backend mencari akun berdasarkan phone number.
3. Backend memeriksa status akun.
4. Backend memverifikasi password menggunakan PasswordEncoder.
5. Jika valid, backend membuat JWT access token.
6. Backend mengembalikan token kepada client.

Ketentuan:
- Phone number harus unik.
- Password tidak boleh disimpan dalam plaintext.
- Pesan kesalahan login tidak boleh membedakan akun yang tidak ditemukan dengan password yang salah.
- Akun BLOCKED tidak dapat login.
- Wallet INACTIVE atau BLOCKED tidak otomatis menghalangi login.

### 2.2 JWT Access Token

JWT digunakan untuk mengautentikasi request setelah login.

Token claims yang disarankan:

| Claim | Description |
|---|---|
| sub | User ID |
| iat | Token issued time |
| exp | Token expiration time |
| iss | Token issuer |

Ketentuan:
- Access token berlaku selama 15 menit.
- Gunakan algoritma signature yang aman dan diverifikasi secara eksplisit.
- Secret key harus berasal dari environment variable atau secret manager, bukan hardcoded.
- Backend harus memvalidasi signature, expiration, dan issuer.
- Jangan menyimpan password atau transaction PIN di JWT.
- JWT tidak boleh diterima jika sudah expired atau signature tidak valid.

Refresh token tidak termasuk scope MVP.

### 2.3 JWT Request Validation

Setiap request ke protected endpoint harus membawa:

`Authorization: Bearer <access_token>`

JWT authentication filter bertugas:
1. Membaca Authorization header.
2. Mengambil token setelah prefix Bearer.
3. Memvalidasi token.
4. Mengambil subject atau user ID.
5. Memuat identitas pengguna.
6. Membuat authentication object pada SecurityContext.
7. Meneruskan request ke proses berikutnya.

Jika token tidak valid atau expired, backend mengembalikan HTTP 401.

## 3. Password Security

Password disimpan dalam bentuk hash menggunakan BCrypt.

Ketentuan:
- Gunakan Spring Security `PasswordEncoder`.
- Password tidak boleh disimpan dalam bentuk plaintext.
- Jangan membuat algoritma hashing sendiri.
- Gunakan BCrypt dengan work factor yang sesuai kemampuan server; nilai awal 12 dapat digunakan lalu diukur.
- Verifikasi password menggunakan `matches()`, bukan membandingkan hash secara manual.
- Password tidak boleh ditampilkan kembali melalui API atau log.
- Password harus memenuhi kebijakan panjang dan kompleksitas yang ditetapkan aplikasi.

## 4. Transaction PIN Security

Transaction PIN digunakan untuk mengotorisasi:
- Transfer.
- Payment / Expense.

Ketentuan:
- PIN terdiri dari tepat 6 digit.
- PIN disimpan dalam bentuk hash menggunakan PasswordEncoder.
- PIN tidak boleh disimpan dalam plaintext.
- PIN tidak boleh dikirim kembali dalam response.
- Backend memverifikasi PIN menggunakan `PasswordEncoder.matches()`.
- Top Up tidak memerlukan PIN sesuai keputusan scope MVP.
- PIN yang salah menyebabkan transaksi ditolak.

### PIN Attempt Protection

Untuk mengurangi percobaan PIN berulang:
- Batasi jumlah percobaan PIN yang salah.
- Gunakan mekanisme rate limiting atau temporary lock.
- Jangan mengungkapkan PIN hash atau informasi sensitif pada response.
- Catat kejadian percobaan gagal secara aman.

Kebijakan jumlah percobaan dan durasi temporary lock akan ditetapkan sebelum implementasi.

## 5. Authorization

Authorization memastikan pengguna hanya dapat mengakses data yang menjadi haknya.

Ketentuan:
- User ID diperoleh dari SecurityContext berdasarkan JWT.
- Jangan mempercayai user ID atau wallet ID dari request body untuk menentukan pemilik transaksi.
- Setiap query profile, wallet, dan transaction harus dibatasi berdasarkan pengguna yang login.
- Pengguna tidak boleh membaca transaksi wallet pengguna lain.
- Jika transaksi tidak ditemukan atau bukan milik pengguna, API mengembalikan 404 agar keberadaan transaksi pengguna lain tidak terungkap.

### Wallet Status Authorization

| Wallet Status | View Wallet | Top Up | Transfer | Payment |
|---|---|---|---|---|
| ACTIVE | Allowed | Allowed | Allowed | Allowed |
| INACTIVE | Allowed | Denied | Denied | Denied |
| BLOCKED | Allowed | Denied | Denied | Denied |

User account dengan status BLOCKED tidak dapat login.

## 6. Financial Transaction Security

Semua operasi perubahan saldo harus diproses secara atomik.

Ketentuan:
1. Gunakan database transaction melalui Spring `@Transactional`.
2. Kunci baris wallet ketika melakukan perubahan saldo.
3. Lakukan validasi ulang saldo setelah lock diperoleh.
4. Pastikan wallet berstatus ACTIVE.
5. Pastikan nominal transaksi lebih besar dari nol.
6. Simpan perubahan saldo dan ledger entry dalam satu transaksi database.
7. Jika salah satu langkah gagal, lakukan rollback.
8. Gunakan `BigDecimal` untuk perhitungan nominal.
9. Jangan menerima saldo akhir dari client sebagai input.
10. Jangan menyediakan endpoint untuk mengubah atau menghapus ledger entry.

### Transfer Security

- Verifikasi identitas pengirim dari JWT.
- Cari penerima berdasarkan nomor telepon.
- Tolak transfer ke wallet sendiri.
- Verifikasi PIN pengirim.
- Pastikan kedua wallet ACTIVE.
- Lock kedua wallet dalam urutan ID yang konsisten.
- Periksa kembali saldo pengirim setelah lock.
- Debit dan credit harus tersimpan secara atomik.
- Kedua ledger entries harus menggunakan reference number yang sama.

## 7. API Security

### 7.1 Public Endpoints

Endpoint yang dapat diakses tanpa JWT:

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`

### 7.2 Protected Endpoints

Semua endpoint lainnya memerlukan JWT yang valid.

### 7.3 CORS

- Izinkan origin frontend yang dikonfigurasi.
- Jangan menggunakan wildcard origin bersama credentials.
- Batasi HTTP methods sesuai kebutuhan API.
- Batasi request headers yang diperbolehkan.
- Jangan menganggap CORS sebagai pengganti authentication atau authorization.

### 7.4 Input Validation

Gunakan Jakarta Bean Validation untuk memvalidasi request DTO.

Contoh:
- `@NotBlank` untuk nama dan nomor telepon.
- `@Size` untuk batas panjang input.
- `@NotNull` untuk nominal.
- `@DecimalMin` untuk nominal minimum.
- Validasi PIN dengan pola enam digit.
- Validasi kategori harus aktif.
- Validasi panjang description.

Semua input harus divalidasi sebelum diproses service.

### 7.5 Error Handling

- Gunakan response error yang konsisten.
- Jangan mengembalikan stack trace kepada client.
- Jangan mengungkapkan SQL query atau struktur internal database.
- Jangan mencatat password, PIN, atau JWT utuh ke log.
- Gunakan HTTP status yang sesuai.

## 8. Sensitive Configuration

Konfigurasi sensitif harus dikelola di luar source code.

Contoh environment variables:

| Variable | Purpose |
|---|---|
| DB_URL | PostgreSQL connection URL |
| DB_USERNAME | Database username |
| DB_PASSWORD | Database password |
| JWT_SECRET | JWT signing secret |
| JWT_EXPIRATION | Access token lifetime |

Ketentuan:
- Jangan commit file `.env` berisi secret.
- Sediakan `.env.example` tanpa nilai rahasia.
- Tambahkan secret file ke `.gitignore`.
- Jangan mengunggah credential database atau JWT secret ke GitHub.
- Gunakan HTTPS untuk deployment production.

## 9. Logging and Monitoring

Catat kejadian penting seperti:
- Login berhasil dan gagal.
- Perubahan profile.
- Perubahan password.
- Top Up berhasil atau gagal.
- Transfer berhasil atau gagal.
- Payment berhasil atau gagal.
- Penolakan akses.

Ketentuan:
- Jangan mencatat password, PIN, atau token.
- Gunakan user ID dan reference number untuk membantu penelusuran.
- Jangan mengubah ledger hanya untuk keperluan logging.
- Log tidak menggantikan catatan transaksi pada database.

## 10. Security Scope Limitations

Fitur berikut tidak termasuk MVP:
- Refresh token.
- Server-side JWT revocation atau blacklist.
- Forgot password.
- Change transaction PIN.
- Multi-factor authentication.
- Device management.
- Admin account management.
- Fraud detection.
- Real payment gateway security.

## 11. Security Acceptance Criteria

Security dianggap memenuhi kebutuhan MVP apabila:

1. Password dan PIN tersimpan sebagai hash.
2. Login menghasilkan JWT hanya jika kredensial valid.
3. Endpoint protected menolak request tanpa JWT.
4. JWT expired atau tidak valid ditolak.
5. Pengguna tidak dapat membaca wallet pengguna lain.
6. Pengguna tidak dapat membaca transaksi pengguna lain.
7. Wallet non-ACTIVE tidak dapat melakukan perubahan saldo.
8. Transfer dan Payment memerlukan PIN yang benar.
9. Saldo tidak dapat menjadi negatif.
10. Kegagalan transaksi tidak meninggalkan perubahan saldo parsial.
11. Secret tidak tersimpan di repository.
12. Informasi sensitif tidak muncul dalam response atau application log.