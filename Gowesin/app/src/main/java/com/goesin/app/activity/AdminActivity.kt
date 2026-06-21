package com.goesin.app.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.goesin.app.adapter.BikeAdapter
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityAdminBinding
import com.goesin.app.model.BikeResponse
import com.goesin.app.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: BikeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user == null || user.role != "admin") {
            Toast.makeText(this, "Akses ditolak", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        fetchBikes()

        binding.btnBack.setOnClickListener { finish() }
        
        binding.fabAddBike.setOnClickListener {
            // Logic to add bike (e.g., open a dialog or new activity)
            Toast.makeText(this, "Fitur Tambah Sepeda", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = BikeAdapter(emptyList()) { bike ->
            // Logic for edit/delete
            Toast.makeText(this, "Edit: ${bike.name}", Toast.LENGTH_SHORT).show()
        }
        binding.rvAdminBikes.layoutManager = LinearLayoutManager(this)
        binding.rvAdminBikes.adapter = adapter
    }

    private fun fetchBikes() {
        RetrofitClient.instance.getBikes().enqueue(object : Callback<BikeResponse> {
            override fun onResponse(call: Call<BikeResponse>, response: Response<BikeResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        adapter.updateData(it)
                    }
                }
            }

            override fun onFailure(call: Call<BikeResponse>, t: Throwable) {
                Toast.makeText(this@AdminActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}