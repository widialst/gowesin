package com.goesin.app.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goesin.app.MainActivity
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityLoginBinding
import com.goesin.app.model.LoginResponse
import com.goesin.app.util.SessionManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            moveToDashboard()
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            login(email, password)
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login(email: String, password: String) {
        val body = mapOf("email" to email, "password" to password)
        RetrofitClient.instance.login(body).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.success == true) {
                        val user = responseBody.user
                        val token = responseBody.token
                        if (user != null && token != null) {
                            // Simpan user + token ke SharedPreferences
                            sessionManager.saveUser(user, token)
                            // Set token di RetrofitClient agar semua request berikutnya pakai token
                            RetrofitClient.authToken = token
                            moveToDashboard()
                        } else {
                            Toast.makeText(this@LoginActivity, "Data user tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val message = responseBody?.message ?: "Login Gagal"
                        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Mengambil pesan error dari errorBody jika status code bukan 2xx
                    val errorMsg = try {
                        val errorObj = JSONObject(response.errorBody()?.string() ?: "{}")
                        errorObj.getString("message")
                    } catch (e: Exception) {
                        "Error Server: ${response.code()}"
                    }
                    Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("LoginError", "Failure: ${t.message}")
                Toast.makeText(this@LoginActivity, "Koneksi Gagal: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun moveToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}