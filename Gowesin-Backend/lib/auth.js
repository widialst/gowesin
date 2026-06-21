import jwt from 'jsonwebtoken';

const JWT_SECRET = process.env.JWT_SECRET || 'gowesin-secret';

/**
 * Membuat JWT token untuk user yang berhasil login
 */
export function createToken(user) {
  return jwt.sign(
    { id: user.id, email: user.email, role: user.role },
    JWT_SECRET,
    { expiresIn: '30d' }
  );
}

/**
 * Verifikasi JWT token dari Authorization header
 * Kembalikan payload jika valid, null jika tidak valid
 */
export function verifyToken(req) {
  try {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) return null;
    const token = authHeader.substring(7);
    return jwt.verify(token, JWT_SECRET);
  } catch {
    return null;
  }
}

/**
 * Helper: balas dengan CORS headers
 */
export function corsResponse(res, status, data) {
  return res.status(status).json(data);
}

/**
 * Handle OPTIONS preflight untuk CORS
 */
export function handleCors(req, res) {
  if (req.method === 'OPTIONS') {
    res.status(200).end();
    return true;
  }
  return false;
}
