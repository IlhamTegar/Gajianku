# GajianKu

Aplikasi Android untuk mengelola alokasi gaji secara otomatis dengan sistem preset persentase, target tabungan berprioritas, dan deteksi saldo masuk dari notifikasi bank/e-wallet.

## Latar Belakang

Banyak orang kesulitan mengatur gaji begitu masuk — sering habis tanpa sadar untuk hal yang tidak prioritas. GajianKu membantu dengan:
- Bantu Membagi gaji otomatis berdasarkan preset persentase
- Memisahkan tabungan wajib (untuk target) dan tabungan darurat (untuk bencana)
- Mendeteksi saldo masuk dari notifikasi bank & langsung mengalokasikannya

## Fitur Utama

- **Preset Alokasi** — 2 preset default + custom (maks 5), dengan validasi total 100%
- **Kategori Tetap** — Orangtua, Biaya Hidup, Tabungan Wajib, Tabungan Darurat, Investasi, Gaya Hidup
- **Target Tabungan Berprioritas** — Tinggi / Sedang / Rendah
- **Auto-Detect Saldo Masuk** — via NotificationListenerService (parsing notifikasi bank/e-wallet)
- **Riwayat Alokasi** — per bulan, dengan breakdown

## Tech Stack

- **Bahasa:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room
- **Arsitektur:** MVVM + Repository
- **Async:** Coroutines + Flow
- **DI:** Hilt

## Status

🚧 Dalam pengembangan — fase awal (setup & dokumentasi)

## Roadmap

Lihat [ROADMAP.md](ROADMAP.md)

## Catatan Teknis

Lihat [NOTES.md](NOTES.md)

## Lisensi

MIT License