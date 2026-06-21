package com.goesin.app.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityRegisterBinding
import com.goesin.app.model.RegisterResponse
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString()
            val email = binding.etEmail.text.toString()
            val phone = binding.etPhone.text.toString()
            val password = binding.etPassword.text.toString()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            register(name, email, password, phone)
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun register(name: String, email: String, password: String, phone: String) {
        val body = mapOf(
            "name" to name,
            "email" to email,
            "password" to password,
            "phone" to phone
        )
        RetrofitClient.instance.register(body).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        Toast.makeText(this@RegisterActivity, "Registrasi Berhasil, silakan login", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        val message = responseBody?.message ?: "Registrasi Gagal"
                        Toast.makeText(this@RegisterActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Mengambil pesan error dari errorBody jika status code bukan 2xx
                    val errorMsg = try {
                        val errorObj = JSONObject(response.errorBody()?.string() ?: "{}")
                        errorObj.getString("message")
                    } catch (e: Exception) {
                        "Error Server: ${response.code()}"
                    }
                    Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Log.e("RegisterError", "Failure: ${t.message}")
                Toast.makeText(this@RegisterActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}