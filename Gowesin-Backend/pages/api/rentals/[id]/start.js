import { supabase } from '../../../../lib/supabase';
import { verifyToken, handleCors } from '../../../../lib/auth';

// POST /api/rentals/[id]/start — Admin setujui rental
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
    const { data: rental, error: fetchError } = await supabase
      .from('rentals')
      .select('status')
      .eq('id', parseInt(id))
      .single();

    if (fetchError || !rental) {
      return res.status(404).json({ success: false, message: 'Rental tidak ditemukan' });
    }

    if (rental.status !== 'pending') {
      return res.status(400).json({ success: false, message: 'Rental tidak dalam status menunggu' });
    }

    const { error } = await supabase
      .from('rentals')
      .update({
        status: 'renting',
        start_time: new Date().toISOString(),
      })
      .eq('id', parseInt(id));

    if (error) throw error;

    return res.status(200).json({ success: true, message: 'Rental disetujui dan waktu sewa dimulai' });
  } catch (err) {
    console.error('Start rental error:', err);
    return res.status(500).json({ success: false, message: 'Gagal menyetujui rental' });
  }
}
