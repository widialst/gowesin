import { supabase } from '../../../lib/supabase';
import { handleCors } from '../../../lib/auth';
import bcrypt from 'bcryptjs';

export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    const { name, email, password, phone } = req.body;

    // Validasi input
    if (!name || !email || !password) {
      return res.status(400).json({ success: false, message: 'Nama, email, dan password wajib diisi' });
    }
    if (password.length < 6) {
      return res.status(400).json({ success: false, message: 'Password minimal 6 karakter' });
    }

    // Cek apakah email sudah terdaftar
    const { data: existing } = await supabase
      .from('users')
      .select('id')
      .eq('email', email.toLowerCase().trim())
      .single();

    if (existing) {
      return res.status(409).json({ success: false, message: 'Email sudah terdaftar' });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Insert user baru
    const { data: newUser, error } = await supabase
      .from('users')
      .insert([{
        name: name.trim(),
        email: email.toLowerCase().trim(),
        password: hashedPassword,
        phone: phone || null,
        role: 'user',
      }])
      .select('id, name, email, phone, role, created_at')
      .single();

    if (error) {
      console.error('Register error:', error);
      return res.status(500).json({ success: false, message: 'Gagal mendaftar, coba lagi' });
    }

    return res.status(201).json({
      success: true,
      message: 'Registrasi berhasil! Silakan login.',
      user: newUser,
    });
  } catch (err) {
    console.error('Register error:', err);
    return res.status(500).json({ success: false, message: 'Terjadi kesalahan server' });
  }
}
