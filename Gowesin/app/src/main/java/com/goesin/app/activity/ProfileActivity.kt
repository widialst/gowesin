package com.goesin.app.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goesin.app.MainActivity
import com.goesin.app.R
import com.goesin.app.databinding.ActivityProfileBinding
import com.goesin.app.util.SessionManager

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser() ?: run { finish(); return }

        binding.tvProfileName.text = user.name
        binding.tvProfileEmail.text = user.email
        binding.tvProfilePhone.text = user.phone



        binding.btnLogout.setOnClickListener {
            sessionManager.logout()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}