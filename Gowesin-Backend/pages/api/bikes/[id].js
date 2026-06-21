import { supabase } from '../../../lib/supabase';
import { verifyToken, handleCors } from '../../../lib/auth';

export default async function handler(req, res) {
  if (handleCors(req, res)) return;

  const { id } = req.query;

  // ── PUT: Update sepeda (admin only) ───────────────────────────────────
  if (req.method === 'PUT') {
    const user = verifyToken(req);
    if (!user || user.role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
    }

    try {
      const { name, description, price_per_hour, location, battery, image_url, status, stock, lat, lng } = req.body;

      if (!name || !price_per_hour) {
        return res.status(400).json({ success: false, message: 'Nama dan harga wajib diisi' });
      }

      const updateData = {
        name,
        description: description || null,
        price_per_hour: parseFloat(price_per_hour),
        location: location || null,
        battery: parseInt(battery) || 100,
        image_url: image_url || null,
        status: status || 'available',
        stock: parseInt(stock) || 1,
      };

      if (lat) updateData.lat = parseFloat(lat);
      if (lng) updateData.lng = parseFloat(lng);

      const { data: updated, error } = await supabase
        .from('bikes')
        .update(updateData)
        .eq('id', parseInt(id))
        .select()
        .single();

      if (error) throw error;
      if (!updated) return res.status(404).json({ success: false, message: 'Sepeda tidak ditemukan' });

      return res.status(200).json({ success: true, message: 'Sepeda berhasil diperbarui', data: updated });
    } catch (err) {
      console.error('Update bike error:', err);
      return res.status(500).json({ success: false, message: 'Gagal memperbarui sepeda' });
    }
  }

  // ── DELETE: Hapus sepeda (admin only) ─────────────────────────────────
  if (req.method === 'DELETE') {
    const user = verifyToken(req);
    if (!user || user.role !== 'admin') {
      return res.status(403).json({ success: false, message: 'Akses ditolak. Admin only.' });
    }

    try {
      const { error } = await supabase
        .from('bikes')
        .delete()
        .eq('id', parseInt(id));

      if (error) throw error;

      return res.status(200).json({ success: true, message: 'Sepeda berhasil dihapus' });
    } catch (err) {
      console.error('Delete bike error:', err);
      return res.status(500).json({ success: false, message: 'Gagal menghapus sepeda' });
    }
  }

  return res.status(405).json({ success: false, message: 'Method not allowed' });
}
