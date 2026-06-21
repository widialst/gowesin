import { supabase } from '../../../../lib/supabase';
import { verifyToken, handleCors } from '../../../../lib/auth';

export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  // Admin only
  const user = verifyToken(req);
  if (!user || user.role !== 'admin') {
    return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
  }

  const { id } = req.query;
  const { action } = req.body; // 'add' atau 'reduce'

  if (!action || !['add', 'reduce'].includes(action)) {
    return res.status(400).json({ success: false, message: "Action harus 'add' atau 'reduce'" });
  }

  try {
    // Ambil stok sekarang
    const { data: bike, error: fetchError } = await supabase
      .from('bikes')
      .select('stock, status')
      .eq('id', parseInt(id))
      .single();

    if (fetchError || !bike) {
      return res.status(404).json({ success: false, message: 'Sepeda tidak ditemukan' });
    }

    const currentStock = parseInt(bike.stock) || 0;

    if (action === 'reduce' && currentStock <= 0) {
      return res.status(400).json({ success: false, message: 'Stok sudah 0, tidak bisa dikurangi' });
    }

    const newStock = action === 'add' ? currentStock + 1 : currentStock - 1;

    // Update stok
    const { error: updateError } = await supabase
      .from('bikes')
      .update({ stock: newStock })
      .eq('id', parseInt(id));

    if (updateError) throw updateError;

    return res.status(200).json({
      success: true,
      message: 'Stok berhasil diperbarui',
      new_stock: newStock,
    });
  } catch (err) {
    console.error('Stock update error:', err);
    return res.status(500).json({ success: false, message: 'Gagal memperbarui stok' });
  }
}
