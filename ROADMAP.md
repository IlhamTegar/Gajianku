# Roadmap GajianKu

## Fase 1 — Fondasi (Minggu 1-2)

### Setup
- [x] Install Android Studio
- [x] Buat project Kotlin + Compose
- [x] Setup repo GitHub
- [x] Dokumentasi (README, NOTES, ROADMAP)

### Database & Logic
- [ ] Skema Room (entity, DAO, database)
- [ ] Logic validasi preset (total = 100%)
- [ ] Logic alokasi gaji (gaji + preset → breakdown)
- [ ] Logic bagi kantong tabungan (prioritas T/S/R)
- [ ] Unit test untuk logic inti

## Fase 2 — UI Dasar (Minggu 3-4)

- [ ] Onboarding (input gaji pertama)
- [ ] Home (tampilkan alokasi bulan ini)
- [ ] Preset management (list, tambah, edit)
- [ ] Target tabungan (list, tambah, prioritas)
- [ ] History (alokasi bulan sebelumnya)

## Fase 3 — Auto-Detect (Minggu 5)

- [ ] NotificationListenerService setup
- [ ] Izin notifikasi ke user
- [ ] Whitelist package bank/e-wallet
- [ ] Regex parser BCA
- [ ] Regex parser GoPay
- [ ] Trigger alokasi otomatis
- [ ] Test dengan notifikasi real

## Fase 4 — Polish & Portfolio (Minggu 6)

- [ ] Screenshot semua screen
- [ ] Demo video (1-2 menit)
- [ ] README final dengan screenshot
- [ ] Publish ke Play Store (internal testing)
- [ ] Tambah bank lain (Mandiri, OVO, DANA)

## Fase 5 — Fitur Lanjutan (Opsional)

- [ ] Notifikasi reminder alokasi
- [ ] Statistik & chart
- [ ] Backup/restore
- [ ] Export CSV/PDF
- [ ] Auto-transfer (butuh payment gateway)