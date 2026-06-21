package com.goesin.app.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.goesin.app.adapter.RentalAdapter
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityRentalHistoryBinding
import com.goesin.app.model.RentalResponse
import com.goesin.app.util.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RentalHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRentalHistoryBinding
    private lateinit var sessionManager: SessionManager
    private var rentalAdapter: RentalAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRentalHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Riwayat Penyewaan"

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user == null) {
            Toast.makeText(this, "Sesi habis, silakan login ulang", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        fetchRentals(user.id)
    }

    private fun fetchRentals(userId: Int) {
        // Tampilkan loading
        binding.progressBar.visibility = View.VISIBLE
        binding.rvHistory.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE

        RetrofitClient.instance.getRentals(userId).enqueue(object : Callback<RentalResponse> {
            override fun onResponse(
                call: Call<RentalResponse>,
                response: Response<RentalResponse>
            ) {
                binding.progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val rentals = response.body()?.data ?: emptyList()

                    if (rentals.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvHistory.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE

                        rentalAdapter = RentalAdapter(rentals)
                        binding.rvHistory.layoutManager = LinearLayoutManager(this@RentalHistoryActivity)
                        binding.rvHistory.adapter = rentalAdapter
                    }
                } else {
                    binding.tvEmpty.visibility = View.VISIBLE
                    Toast.makeText(
                        this@RentalHistoryActivity,
                        "Gagal memuat riwayat",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<RentalResponse>, t: Throwable) {
                binding.progressBar.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                Toast.makeText(
                    this@RentalHistoryActivity,
                    "Error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        rentalAdapter?.cancelAllTimers()
    }
}