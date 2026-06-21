import { supabase } from '../../../lib/supabase';
import { verifyToken, handleCors } from '../../../lib/auth';

export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  const user = verifyToken(req);
  if (!user) {
    return res.status(401).json({ success: false, message: 'Silakan login terlebih dahulu' });
  }

  // ── GET: Ambil rental berdasarkan user_id ──────────────────────────────
  if (req.method === 'GET') {
    try {
      const { user_id } = req.query;
      const targetUserId = parseInt(user_id);

      // User biasa hanya bisa lihat rental miliknya sendiri
      if (user.role !== 'admin' && user.id !== targetUserId) {
        return res.status(403).json({ success: false, message: 'Akses ditolak' });
      }

      const { data: rentals, error } = await supabase
        .from('rentals')
        .select(`
          *,
          bikes (name, image_url),
          users (name, email)
        `)
        .eq('user_id', targetUserId)
        .order('created_at', { ascending: false });

      if (error) throw error;

      // Format data agar kompatibel dengan format lama
      const formatted = (rentals || []).map(r => ({
        ...r,
        bike_name: r.bikes?.name || null,
        bike_image: r.bikes?.image_url || null,
        user_name: r.users?.name || null,
        user_email: r.users?.email || null,
      }));

      return res.status(200).json({ success: true, data: formatted });
    } catch (err) {
      console.error('Get rentals error:', err);
      return res.status(500).json({ success: false, message: 'Gagal mengambil data rental' });
    }
  }

  // ── POST: Buat rental baru ─────────────────────────────────────────────
  if (req.method === 'POST') {
    try {
      const { bike_id, rental_date, duration, total_price, payment_method } = req.body;
      const userId = user.id;

      if (!bike_id || !rental_date || !duration || !total_price) {
        return res.status(400).json({ success: false, message: 'Data rental tidak lengkap' });
      }

      // Cek apakah sepeda tersedia
      const { data: bike, error: bikeError } = await supabase
        .from('bikes')
        .select('status, stock, name')
        .eq('id', parseInt(bike_id))
        .single();

      if (bikeError || !bike) {
        return res.status(404).json({ success: false, message: 'Sepeda tidak ditemukan' });
      }

      if (bike.status !== 'available' || parseInt(bike.stock) <= 0) {
        return res.status(400).json({ success: false, message: `Sepeda ${bike.name} sedang tidak tersedia` });
      }

      // Buat rental dengan status 'pending' (menunggu persetujuan admin)
      const { data: rental, error: rentalError } = await supabase
        .from('rentals')
        .insert([{
          user_id: userId,
          bike_id: parseInt(bike_id),
          rental_date,
          duration: parseInt(duration),
          total_price: parseFloat(total_price),
          payment_method: payment_method || 'Cash',
          status: 'pending'
        }])
        .select()
        .single();

      if (rentalError) throw rentalError;

      // Update status sepeda menjadi 'rented' dan kurangi stok
      const newStock = Math.max(0, parseInt(bike.stock) - 1);
      const newStatus = newStock === 0 ? 'rented' : 'available';

      await supabase
        .from('bikes')
        .update({ status: newStatus, stock: newStock })
        .eq('id', parseInt(bike_id));

      return res.status(201).json({
        success: true,
        message: 'Sewa sepeda berhasil! Selamat bersepeda!',
        data: rental,
      });
    } catch (err) {
      console.error('Rent bike error:', err);
      return res.status(500).json({ success: false, message: 'Gagal menyewa sepeda' });
    }
  }

  return res.status(405).json({ success: false, message: 'Method not allowed' });
}
