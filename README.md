# Proses Checkout Marketplace - BPMN 2.0

![Diagram BPMN Checkout](/customerMarketplace.png)

## Gambaran Umum
Di dokumen ini dijelasin alur **proses checkout di marketplace** pakai notasi **BPMN 2.0**. Intinya, kita mau nunjukin gimana **Pelanggan** dan **Sistem Marketplace** saling berinteraksi saat orang mau beli barang online.

---

## Pool dan Lane

### Pools
1. **Pelanggan**  
   Ini mewakili user yang mau beli produk, mulai dari ngecek barang sampai checkout.

2. **Sistem Marketplace**  
   Ini bagian backend-nya, yang urus pembayaran, stok, konfirmasi pesanan, kirim notifikasi, sampai ngatur pengiriman.

---

## Alur Proses

### Pool Pelanggan
1. **Start Event:** Pelanggan klik tombol checkout.
2. **User Task:** Isi semua detail checkout (alamat, metode pembayaran, dll.).
3. **User Task:** Konfirmasi pesanan.
4. **Message Flow:** Kirim request checkout ke sistem marketplace.

### Pool Sistem Marketplace
1. **Start Event:** Sistem nerima request dari pelanggan.
2. **Service Task:** Proses pembayaran lewat **Payment Service**.
3. **Exclusive Gateway:** Cek apakah pembayaran berhasil.
   - **Gagal:**  
     - **Service Task:** Kirim notifikasi pembayaran gagal ke pelanggan.  
     - **End Event:** Proses berhenti untuk kedua pihak.
   - **Berhasil:**  
     - **Service Task:** Update stok barang lewat **Inventory Service**.  
     - **Service Task:** Konfirmasi pesanan lewat **Order Service**.  
     - **Service Task:** Kirim notifikasi konfirmasi ke pelanggan lewat **Notification Service**.  
     - **Service Task:** Atur pengiriman barang lewat **Shipping Service**.  
     - **End Event:** Proses selesai, pelanggan dapet konfirmasi dan info pengiriman.

---

## Elemen BPMN yang Dipakai

| Elemen BPMN          | Penjelasan Singkat                                                       |
|---------------------|-------------------------------------------------------------------------|
| **Pool**             | Peserta dalam proses (Pelanggan & Sistem Marketplace).                 |
| **Lane**             | Peran atau bagian di dalam pool (opsional).                            |
| **Start Event**      | Titik awal proses.                                                      |
| **End Event**        | Titik akhir proses.                                                     |
| **User Task**        | Tugas yang dikerjain manusia (Pelanggan).                               |
| **Service Task**     | Tugas otomatis yang dijalanin sistem atau service.                     |
| **Exclusive Gateway**| Titik keputusan, misal: pembayaran berhasil atau gagal.                |
| **Message Flow**     | Komunikasi antar pool (Pelanggan ↔ Sistem Marketplace).               |

---

## Ringkasan
Jadi intinya, bikin proses checkout jadi jelas dan terstruktur. mulai dari:
- Pelanggan ngisi data dan konfirmasi pesanan  
- Sistem urus pembayaran, stok, notifikasi, sampai pengiriman  
- Ada alur khusus kalau pembayaran gagal  
- Komunikasi antar pool jelas pakai **message flow**

