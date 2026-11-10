# Catatan Teknis GajianKu

## Konsep Inti

### 1. Onboarding
- User input **nama** dan **bank/e-wallet** penerima gaji (BCA, GoPay, OVO, DANA, dll).
- Gaji **tidak diinput manual** — dideteksi otomatis dari notifikasi.
- Setelah onboarding, user memilih preset alokasi.

### 2. Preset Alokasi

**Preset Default 1 (Standar):**
- Orangtua: 40%
- Biaya Hidup: 20%
- Tabungan Wajib: 15%
- Tabungan Darurat: 10%
- Investasi: 10%
- Gaya Hidup: 5%
- **Total: 100%**

**Preset Default 2 (Hemat):**
- Orangtua: 35%
- Tabungan Wajib: 25%
- Biaya Hidup: 25%
- Tabungan Darurat: 10%
- Investasi: 5%
- **Total: 100%**

**Aturan:**
- Total harus 100% — kalau tidak, tombol Simpan disabled + warning
- User bisa edit preset default, maks 5 preset
- Setiap kategori punya flag: apakah masuk "kantong tabungan" atau tidak

### 3. Kategori Tetap

| Kategori | Masuk Kantong Tabungan? | Keterangan |
|---|---|---|
| Orangtua | Tidak | Transfer rutin |
| Biaya Hidup | Tidak | Kebutuhan sehari-hari |
| Tabungan Wajib | Ya | Untuk target beli (laptop, motor, dll) |
| Tabungan Darurat | Ya | Hanya untuk bencana/kejadian tak terduga |
| Investasi | Tidak | Nilai bisa naik/turun, bukan tabungan |
| Gaya Hidup | Tidak | Jajan/nongkrong (opsional) |

### 4. Prioritas Target Tabungan

- **Tinggi** — wajib dipenuhi dulu
- **Sedang** — dipenuhi setelah Tinggi aman
- **Rendah** — sisa terakhir

**Aturan alokasi:**
- Level Tinggi diisi dulu sampai penuh, baru Sedang, baru Rendah
- Kalau uang tidak cukup di satu level, dibagi proporsional ke semua target di level itu
- Kalau level atas belum penuh, tidak "bocor" ke level bawah
- Urutan default: berdasarkan waktu dibuat
- User bisa drag-and-drop untuk mengubah urutan manual

### 5. Auto-Detect Saldo Masuk

- Pakai `NotificationListenerService` (native Android)
- Filter notifikasi dari app bank/e-wallet (whitelist package)
- Parse teks notifikasi dengan regex per bank
- Kalau terdeteksi "saldo masuk" / "kredit" / "menerima" → trigger alokasi otomatis
- Simpan raw text + hasil parse ke DB untuk audit

**Bank yang didukung saat ini:**
- BCA (`com.bca`)
- GoPay (`com.gojek.gopay`)
- BRI (`id.co.bri.brimo`)
- Mandiri (`com.bankmandiri.mandiri`)
- DANA (`id.dana`)
- Simulasi ADB (`com.android.shell`) — untuk testing

**Aturan deteksi gaji:**
- Notifikasi masuk tanggal **1–10** → dianggap **gaji** → dialokasikan ke preset
- Notifikasi masuk tanggal **> 10** → dianggap **pemasukan lain** → dibagi 50% ke Tabungan Darurat, 50% ke Tabungan Wajib (target)

### 6. Distribusi Tabungan Wajib ke Target

- Setelah alokasi gaji, Tabungan Wajib (misal 15% dari gaji) didistribusikan ke target aktif.
- Menggunakan `SavingsDistributor` dengan prioritas T/S/R.
- Target yang sudah penuh otomatis berubah status ke `SIAP_DIBELI`.

### 7. Tabungan Darurat

- Saldo terpisah dari Tabungan Wajib.
- Disembunyikan di menu Profile (agar tidak tergoda).
- User bisa "Pakai Dana Darurat" dengan mencatat nominal + keterangan.
- History pemakaian tersimpan.

## Skema Database

- `users` — profil, nama, bank, gaji bulanan
- `presets` — nama preset, is_default
- `preset_items` — preset_id, kategori, persentase
- `categories` — fixed: Orangtua, Biaya Hidup, Tab. Wajib, Tab. Darurat, Investasi, Gaya Hidup
- `savings_targets` — nama, target_dana, terkumpul, prioritas, status, sort_order
- `salary_allocations` — bulan, gaji, preset_id, total, deficit_paid
- `allocation_items` — allocation_id, kategori, nominal
- `incoming_notifs` — raw text, parsed nominal, sumber, timestamp, status
- `expenses` — pencatatan pengeluaran (kategori, nominal, bulan)
- `emergency_fund` — saldo darurat (single row)
- `emergency_fund_history` — riwayat pakai/tambah darurat
- `global_deficit` — total minus global (single row)
- `deficit_history` — riwayat tambah/bayar minus
- `app_notifications` — log notifikasi yang dikirim ke user

## Catatan Penting

- **Investasi BUKAN tabungan** — dipisah karena nilai fluktuatif
- **Tabungan Wajib vs Darurat** — dibedakan: Wajib untuk target, Darurat untuk bencana
- **Auto-transfer ditunda** — butuh izin payment, fokus dulu ke alokasi & tracking
- **Bug yang diketahui** — lihat BUGS.md