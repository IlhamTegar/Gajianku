# Roadmap GajianKu

## Fase 1 — Fondasi ✅ SELESAI

### Setup
- [x] Install Android Studio
- [x] Buat project Kotlin + Compose
- [x] Setup repo GitHub
- [x] Dokumentasi (README, NOTES, ROADMAP)

### Database & Logic
- [x] Skema Room (entity, DAO, database)
- [x] Logic validasi preset (total = 100%)
- [x] Logic alokasi gaji (gaji + preset → breakdown)
- [x] Logic bagi kantong tabungan (prioritas T/S/R)
- [x] Unit test untuk logic inti
- [x] Repository layer
- [x] Expense tracking

## Fase 2 — UI Dasar ✅ SELESAI

- [x] Onboarding (input nama & pilih bank)
- [x] Home (tampilkan alokasi bulan ini + grafik donat)
- [x] Preset management (pilih preset)
- [x] Target tabungan (list, tambah, prioritas, drag-and-drop)
- [x] Catatan pengeluaran (mutasi harian)
- [x] Profile (info user, tabungan darurat)
- [x] Hapus catatan & target
- [x] Total dana terkumpul (Target)
- [x] Sisa biaya hidup & total investasi (Catatan)

## Fase 3 — Auto-Detect ✅ SELESAI

- [x] NotificationListenerService setup
- [x] Izin notifikasi ke user
- [x] Whitelist package bank/e-wallet
- [x] Regex parser BCA
- [x] Trigger alokasi otomatis
- [x] Distribusi otomatis ke target
- [x] Test dengan notifikasi ADB
- [ ] Regex parser GoPay (belum diuji)
- [ ] Regex parser bank lain (Mandiri, OVO, DANA)

## Fase 4 — Perbaikan Bug & Polish 🚧 SEDANG BERJALAN

- [ ] Perbaiki bug alokasi dobel (sebagian sudah)
- [ ] Perbaiki bug gaji tersimpan 8jt bukan 80jt
- [ ] Saldo Catatan refresh otomatis (tanpa tutup-buka app)
- [ ] Home refresh otomatis setelah notifikasi
- [ ] Simpan presetId pilihan user (bukan default)
- [ ] Test ulang drag-and-drop target
- [ ] UI Profile menjadi menu (Pengaturan, Ubah Bank, Tanggal Gajian)
- [ ] Halaman "Siap Dibeli" untuk target yang sudah penuh
- [ ] Global Deficit UI

## Fase 5 — Portfolio & Publikasi

- [ ] Screenshot semua screen
- [ ] Demo video (1-2 menit)
- [ ] README final dengan screenshot
- [ ] Publish ke Play Store (internal testing)

## Fase 6 — Fitur Lanjutan (Opsional)

- [ ] Notifikasi reminder alokasi
- [ ] Statistik & chart tambahan
- [ ] Backup/restore
- [ ] Export CSV/PDF
- [ ] Auto-transfer (butuh payment gateway)