import { supabase } from '../../../lib/supabase';
import { verifyToken, handleCors } from '../../../lib/auth';

// GET /api/rentals/all — Ambil SEMUA rental (admin only)
export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  if (req.method !== 'GET') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  const user = verifyToken(req);
  if (!user || user.role !== 'admin') {
    return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
  }

  try {
    const { data: rentals, error } = await supabase
      .from('rentals')
      .select(`
        *,
        bikes (name, image_url),
        users (name, email)
      `)
      .order('created_at', { ascending: false });

    if (error) throw error;

    const formatted = (rentals || []).map(r => ({
      ...r,
      bike_name: r.bikes?.name || null,
      bike_image: r.bikes?.image_url || null,
      user_name: r.users?.name || null,
      user_email: r.users?.email || null,
    }));

    return res.status(200).json({ success: true, data: formatted });
  } catch (err) {
    console.error('Get all rentals error:', err);
    return res.status(500).json({ success: false, message: 'Gagal mengambil semua data rental' });
  }
}
