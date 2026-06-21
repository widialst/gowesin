import { supabase } from '../../../lib/supabase';
import { verifyToken, handleCors } from '../../../lib/auth';

export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  // ── GET: Ambil semua sepeda (public, tidak perlu login) ────────────────
  if (req.method === 'GET') {
    try {
      const { data: bikes, error } = await supabase
        .from('bikes')
        .select('*')
        .order('id', { ascending: true });

      if (error) throw error;

      return res.status(200).json({ success: true, data: bikes || [] });
    } catch (err) {
      console.error('Get bikes error:', err);
      return res.status(500).json({ success: false, message: 'Gagal mengambil data sepeda' });
    }
  }

  // ── POST: Tambah sepeda baru (admin only) ──────────────────────────────
  if (req.method === 'POST') {
    const user = verifyToken(req);
    if (!user || user.role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
    }

    try {
      const { name, description, price_per_hour, location, battery, image_url, status, stock, lat, lng } = req.body;

      if (!name || !price_per_hour) {
        return res.status(400).json({ success: false, message: 'Nama dan harga wajib diisi' });
      }

      const { data: newBike, error } = await supabase
        .from('bikes')
        .insert([{
          name,
          description: description || null,
          price_per_hour: parseFloat(price_per_hour),
          location: location || null,
          battery: parseInt(battery) || 100,
          image_url: image_url || null,
          status: status || 'available',
          stock: parseInt(stock) || 1,
          rating: 5.0,
          lat: parseFloat(lat) || null,
          lng: parseFloat(lng) || null,
        }])
        .select()
        .single();

      if (error) throw error;

      return res.status(201).json({ success: true, message: 'Sepeda berhasil ditambahkan', data: newBike });
    } catch (err) {
      console.error('Add bike error:', err);
      return res.status(500).json({ success: false, message: 'Gagal menambahkan sepeda' });
    }
  }

  return res.status(405).json({ success: false, message: 'Method not allowed' });
}
