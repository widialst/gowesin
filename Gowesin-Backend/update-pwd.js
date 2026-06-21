const path = require('path');
require('dotenv').config({ path: path.resolve(__dirname, '.env.local') });
const { createClient } = require('@supabase/supabase-js');

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);

async function updateAdminPassword() {
  const { data, error } = await supabase
    .from('users')
    .update({ password: '$2a$10$ubVzUYvPQWdusSGqGZxcFOl0XKywbvXs9xvcvSiJ2A8m8ebjykJHW' })
    .eq('email', 'admin@gowes.in');
  
  if (error) {
    console.error('Error updating password:', error);
  } else {
    console.log('Password updated successfully. Rows affected:', data);
  }
}

updateAdminPassword();
