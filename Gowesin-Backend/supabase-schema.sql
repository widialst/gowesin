-- ============================================================
-- GoWeSin - SQL Schema untuk Supabase (PostgreSQL)
-- Jalankan ini di Supabase SQL Editor
-- Dashboard → SQL Editor → New Query → paste ini → Run
-- ============================================================

-- ── Tabel users ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
  id          SERIAL PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  email       VARCHAR(100) NOT NULL UNIQUE,
  password    VARCHAR(255) NOT NULL,
  phone       VARCHAR(20)  DEFAULT NULL,
  role        VARCHAR(10)  DEFAULT 'user' CHECK (role IN ('user', 'admin')),
  created_at  TIMESTAMPTZ  DEFAULT NOW()
);

-- ── Tabel bikes ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS bikes (
  id             SERIAL PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  description    TEXT         DEFAULT NULL,
  price_per_hour DECIMAL(10,2) NOT NULL,
  location       VARCHAR(100) DEFAULT NULL,
  battery        INT          DEFAULT 100,
  rating         DECIMAL(2,1) DEFAULT 5.0,
  image_url      VARCHAR(500) DEFAULT NULL,
  status         VARCHAR(20)  DEFAULT 'available' CHECK (status IN ('available', 'rented')),
  stock          INT          DEFAULT 1,
  lat            DECIMAL(10,6) DEFAULT NULL,
  lng            DECIMAL(10,6) DEFAULT NULL
);

-- ── Tabel rentals ──────────────────────────────────────────
CREATE TABLE IF NOT EXISTS rentals (
  id              SERIAL PRIMARY KEY,
  user_id         INT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  bike_id         INT          NOT NULL REFERENCES bikes(id) ON DELETE CASCADE,
  rental_date     DATE         NOT NULL,
  duration        INT          NOT NULL,  -- dalam menit
  total_price     DECIMAL(10,2) NOT NULL,
  payment_method  VARCHAR(50)  DEFAULT NULL,
  status          VARCHAR(20)  DEFAULT 'renting' CHECK (status IN ('pending', 'renting', 'finished', 'canceled')),
  created_at      TIMESTAMPTZ  DEFAULT NOW(),
  start_time      TIMESTAMPTZ  DEFAULT NULL
);

-- ── Row Level Security (RLS) ────────────────────────────────
-- Nonaktifkan RLS agar backend (service role key) bisa akses semua
ALTER TABLE users   DISABLE ROW LEVEL SECURITY;
ALTER TABLE bikes   DISABLE ROW LEVEL SECURITY;
ALTER TABLE rentals DISABLE ROW LEVEL SECURITY;

-- ── Data awal: Admin ────────────────────────────────────────
-- Password: admin123 (sudah di-hash dengan bcrypt)
INSERT INTO users (name, email, password, phone, role)
VALUES (
  'Admin Gowes',
  'admin@gowes.in',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVCalmexp2',
  '08123456789',
  'admin'
) ON CONFLICT (email) DO NOTHING;

-- ── Data awal: Sepeda ───────────────────────────────────────
INSERT INTO bikes (name, description, price_per_hour, location, battery, rating, image_url, status, stock)
VALUES
  (
    'Gowes Pro X1',
    'Sepeda listrik kencang dengan ketahanan baterai hingga 50km.',
    15000.00,
    'Stasiun Tugu',
    95, 4.8,
    'https://images.unsplash.com/photo-1571068316344-75bc76f77894?q=80&w=500',
    'available', 2
  ),
  (
    'City Roller E2',
    'Sangat cocok untuk berkeliling kota dengan santai.',
    10000.00,
    'Malioboro',
    80, 4.5,
    'https://images.unsplash.com/photo-1593133630449-b7b28f1ed35c?q=80&w=500',
    'available', 3
  ),
  (
    'Mountain E-Bike',
    'Ban tebal, cocok untuk jalanan tidak rata.',
    20000.00,
    'Alun-alun Kidul',
    100, 4.9,
    'https://images.unsplash.com/photo-1621067510443-417646698661?q=80&w=500',
    'available', 1
  )
ON CONFLICT DO NOTHING;
