# GoWeSin Backend - Panduan Deploy

README ini menjelaskan cara deploy backend Next.js ke Vercel dan setup Supabase.

---

## Step 1: Setup Supabase

1. Buka [supabase.com](https://supabase.com) → Login / Daftar
2. Klik **"New Project"** → Isi:
   - **Name**: `gowesin`
   - **Database Password**: buat password yang kuat (simpan!)
   - **Region**: pilih yang terdekat (misal Singapore)
3. Tunggu project dibuat (~1-2 menit)
4. Pergi ke **Settings → API** dan catat:
   - `Project URL` → ini adalah `SUPABASE_URL`
   - `anon public` → ini adalah `SUPABASE_ANON_KEY`
   - `service_role secret` → ini adalah `SUPABASE_SERVICE_ROLE_KEY`

### Jalankan SQL Schema

1. Di Supabase dashboard → klik **SQL Editor** di sidebar
2. Klik **"New Query"**
3. Copy-paste isi file `supabase-schema.sql`
4. Klik **Run** (tombol hijau)
5. Pastikan tidak ada error → tabel `users`, `bikes`, `rentals` sudah dibuat

---

## Step 2: Isi `.env.local`

Buka file `.env.local` di folder ini dan ganti dengan nilai dari Supabase:

```
SUPABASE_URL=https://abcdefghijk.supabase.co
SUPABASE_SERVICE_ROLE_KEY=eyJhbGc....(panjang)
SUPABASE_ANON_KEY=eyJhbGc....(panjang)
JWT_SECRET=gowesin-super-secret-jwt-2026-widia
```

---

## Step 3: Install dependencies & Test Lokal

```bash
# Di folder C:\Widia Sulistiani\Gowesin-Backend
npm install

# Jalankan lokal
npm run dev
```

Backend akan jalan di `http://localhost:3000`.

Test endpoint:
```
GET  http://localhost:3000/api/bikes
POST http://localhost:3000/api/auth/login
     Body: {"email":"admin@gowes.in","password":"admin123"}
```

---

## Step 4: Deploy ke Vercel

### Cara 1: Lewat GitHub (Direkomendasikan)
1. Upload folder `Gowesin-Backend` ke GitHub repository
2. Buka [vercel.com](https://vercel.com) → Login
3. Klik **"New Project"** → Import dari GitHub
4. Pilih repository `Gowesin-Backend`
5. Di bagian **Environment Variables**, tambahkan:
   - `SUPABASE_URL` = nilai dari Supabase
   - `SUPABASE_SERVICE_ROLE_KEY` = nilai dari Supabase
   - `SUPABASE_ANON_KEY` = nilai dari Supabase
   - `JWT_SECRET` = `gowesin-super-secret-jwt-2026-widia`
6. Klik **Deploy**
7. Tunggu ~2 menit → dapat URL seperti `https://gowesin-backend-xxx.vercel.app`

### Cara 2: Lewat Vercel CLI
```bash
npm install -g vercel
vercel login
vercel --prod
```

---

## Step 5: Update URL di Aplikasi

Setelah dapat URL Vercel (misal `https://gowesin-backend.vercel.app`):

### Web Panel (admin.html & index.html)
Buka file `C:\xampp\htdocs\gowesin\assets\js\app.js` dan `admin.js`:
```js
// Ganti baris ini di bagian atas kedua file:
const API_URL = 'https://gowesin-backend.vercel.app'; // ← ganti dengan URL kamu
```

### Android App
Buka `C:\Widia Sulistiani\Gowesin\app\src\main\java\com\goesin\app\api\RetrofitClient.kt`:
```kotlin
// Ganti baris ini:
private const val BASE_URL = "https://gowesin-backend.vercel.app/api/"  // ← ganti dengan URL kamu
```
Lalu **build ulang APK** di Android Studio.

---

## Login Admin Default

Setelah setup:
- **Email**: `admin@gowes.in`  
- **Password**: `admin123`

---

## Struktur API Endpoints

| Method | Endpoint | Auth | Keterangan |
|--------|----------|------|------------|
| POST | `/api/auth/login` | ❌ | Login user |
| POST | `/api/auth/register` | ❌ | Daftar user baru |
| GET | `/api/bikes` | ❌ | Ambil semua sepeda |
| POST | `/api/bikes` | ✅ Admin | Tambah sepeda |
| PUT | `/api/bikes/:id` | ✅ Admin | Edit sepeda |
| DELETE | `/api/bikes/:id` | ✅ Admin | Hapus sepeda |
| POST | `/api/bikes/:id/stock` | ✅ Admin | Update stok |
| GET | `/api/rentals?user_id=X` | ✅ User | Riwayat sewa user |
| POST | `/api/rentals` | ✅ User | Buat rental baru |
| GET | `/api/rentals/all` | ✅ Admin | Semua rental |
| POST | `/api/rentals/:id/return` | ✅ User | Kembalikan sepeda |
| POST | `/api/rentals/:id/start` | ✅ Admin | Setujui rental |
| POST | `/api/rentals/:id/reject` | ✅ Admin | Tolak rental |

✅ = perlu JWT token di header `Authorization: Bearer <token>`
