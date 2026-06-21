import { supabase } from '../../../../lib/supabase';
import { verifyToken, handleCors } from '../../../../lib/auth';

// POST /api/rentals/[id]/reject — Admin tolak rental
export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  const user = verifyToken(req);
  if (!user || user.role !== 'admin') {
    return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
  }

  const { id } = req.query;

  try {
    // Ambil data rental untuk mendapatkan bike_id
    const { data: rental, error: fetchError } = await supabase
      .from('rentals')
      .select('status, bike_id, bikes(stock)')
      .eq('id', parseInt(id))
      .single();

    if (fetchError || !rental) {
      return res.status(404).json({ success: false, message: 'Rental tidak ditemukan' });
    }

    if (rental.status !== 'pending') {
      return res.status(400).json({ success: false, message: 'Hanya rental berstatus pending yang bisa ditolak' });
    }

    // Update status rental → canceled
    const { error } = await supabase
      .from('rentals')
      .update({ status: 'canceled' })
      .eq('id', parseInt(id));

    if (error) throw error;

    // Kembalikan stok sepeda
    const newStock = (parseInt(rental.bikes?.stock) || 0) + 1;
    await supabase
      .from('bikes')
      .update({ status: 'available', stock: newStock })
      .eq('id', rental.bike_id);

    return res.status(200).json({ success: true, message: 'Rental berhasil ditolak dan stok sepeda dikembalikan' });
  } catch (err) {
    console.error('Reject rental error:', err);
    return res.status(500).json({ success: false, message: 'Gagal menolak rental' });
  }
}
