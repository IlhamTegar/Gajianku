# Catatan Teknis GajianKu

## Konsep Inti

### 1. Preset Alokasi
Preset menentukan pembagian persentase gaji ke kategori tetap.

**Preset Default 1:**
- Orangtua: 40%
- Biaya Hidup: 20%
- Tabungan Wajib: 15%
- Tabungan Darurat: 10%
- Investasi: 10%
- Gaya Hidup: 5%
  Total: 100%

**Preset Default 2:**
- Orangtua: 35%
- Tabungan Wajib: 25%
- Biaya Hidup: 25%
- Tabungan Darurat: 10%
- Investasi: 5%
  Total: 100%

**Aturan:**
- Total harus 100% — kalau tidak, tombol Simpan disabled + warning
- User bisa edit preset default, maks 5 preset
- Setiap kategori punya flag: apakah masuk "kantong tabungan" atau tidak

### 2. Kategori Tetap

| Kategori | Masuk Kantong Tabungan? | Keterangan |
|---|---|---|
| Orangtua | Tidak | Transfer rutin |
| Biaya Hidup | Tidak | Kebutuhan sehari-hari |
| Tabungan Wajib | Ya | Untuk target beli (laptop, motor, dll) |
| Tabungan Darurat | Ya | Hanya untuk bencana/kejadian tak terduga |
| Investasi | Tidak | Nilai bisa naik/turun, bukan tabungan |
| Gaya Hidup | Tidak | Jajan/nongkrong (opsional) |

### 3. Prioritas Target Tabungan
- **Tinggi** — wajib dipenuhi dulu
- **Sedang** — dipenuhi setelah Tinggi aman
- **Rendah** — sisa terakhir

**Aturan alokasi:**
- Level Tinggi diisi dulu sampai penuh, baru Sedang, baru Rendah
- Kalau uang tidak cukup di satu level, dibagi proporsional ke semua target di level itu
- Kalau level atas belum penuh, tidak "bocor" ke level bawah

### 4. Auto-Detect Saldo Masuk
- Pakai `NotificationListenerService` (native Android)
- Filter notifikasi dari app bank/e-wallet (whitelist package)
- Parse teks notifikasi dengan regex per bank
- Kalau terdeteksi "saldo masuk" / "kredit" → trigger alokasi otomatis
- Simpan raw text + hasil parse ke DB untuk audit

**Bank yang akan didukung (bertahap):**
- Fase 1: BCA, GoPay
- Fase 2: Mandiri, OVO, DANA, ShopeePay
- Fase 3: BRI, BNI, dll

## Skema Database (Rencana)
- users — profil, gaji bulanan
- presets — nama preset, is_default
- preset_items — preset_id, kategori, persentase
- categories — fixed: Orangtua, Biaya Hidup, Tab. Wajib, Tab. Darurat, Investasi, Gaya Hidup
- savings_targets — nama, target_dana, terkumpul, prioritas, kategori
- salary_allocations — bulan, gaji, preset_id, total
- allocation_items — allocation_id, kategori, nominal
- incoming_notifs — raw text, parsed nominal, sumber, timestamp, status
- expenses — pencatatan pengeluaran (kategori, nominal, bulan)
- emergency_fund — saldo darurat (single row)
- emergency_fund_history — riwayat pakai/tambah darurat


## Catatan Penting

- **Investasi BUKAN tabungan** — dipisah karena nilai fluktuatif
- **Tabungan Wajib vs Darurat** — dibedakan: Wajib untuk target, Darurat untuk bencana
- **Auto-transfer ditunda** — butuh izin payment, fokus dulu ke alokasi & tracking
