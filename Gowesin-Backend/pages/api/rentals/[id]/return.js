import { supabase } from '../../../../lib/supabase';
import { verifyToken, handleCors } from '../../../../lib/auth';

// POST /api/rentals/[id]/return — User kembalikan sepeda
export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  const user = verifyToken(req);
  if (!user) {
    return res.status(401).json({ success: false, message: 'Silakan login terlebih dahulu' });
  }

  const { id } = req.query;

  try {
    // Ambil data rental
    const { data: rental, error: fetchError } = await supabase
      .from('rentals')
      .select('*, bikes(stock)')
      .eq('id', parseInt(id))
      .single();

    if (fetchError || !rental) {
      return res.status(404).json({ success: false, message: 'Data rental tidak ditemukan' });
    }

    // Pastikan yang mengembalikan adalah pemilik rental (atau admin)
    if (user.role !== 'admin' && rental.user_id !== user.id) {
      return res.status(403).json({ success: false, message: 'Akses ditolak' });
    }

    if (rental.status !== 'renting') {
      return res.status(400).json({ success: false, message: 'Rental ini sudah selesai atau dibatalkan' });
    }

    // Update status rental → finished
    const { error: rentalUpdateError } = await supabase
      .from('rentals')
      .update({ status: 'finished' })
      .eq('id', parseInt(id));

    if (rentalUpdateError) throw rentalUpdateError;

    // Kembalikan stok dan status sepeda
    const newStock = (parseInt(rental.bikes?.stock) || 0) + 1;
    await supabase
      .from('bikes')
      .update({ status: 'available', stock: newStock })
      .eq('id', rental.bike_id);

    return res.status(200).json({
      success: true,
      message: 'Sepeda berhasil dikembalikan. Terima kasih telah menggunakan GoWeSin!',
    });
  } catch (err) {
    console.error('Return bike error:', err);
    return res.status(500).json({ success: false, message: 'Gagal mengembalikan sepeda' });
  }
}
