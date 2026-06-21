const bcrypt = require('bcryptjs');
console.log('New hash:', bcrypt.hashSync('admin123', 10));
