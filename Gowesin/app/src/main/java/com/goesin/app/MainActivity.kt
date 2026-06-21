package com.goesin.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.goesin.app.activity.AdminActivity
import com.goesin.app.activity.BikeDetailActivity
import com.goesin.app.activity.ProfileActivity
import com.goesin.app.activity.RentalHistoryActivity
import com.goesin.app.adapter.BikeAdapter
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityMainBinding
import com.goesin.app.model.BikeResponse
import com.goesin.app.util.SessionManager
import com.google.gson.Gson
import android.os.CountDownTimer
import android.util.Log
import com.goesin.app.model.Rental
import com.goesin.app.model.RentalResponse
import com.goesin.app.util.NotificationHelper
import com.goesin.app.util.ReminderBroadcastReceiver
import com.goesin.app.model.BaseResponse
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.app.AlertDialog

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: BikeAdapter
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val user = sessionManager.getUser()

        if (user == null) {
            finish()
            return
        }

        // Restore JWT token ke RetrofitClient agar semua request berikutnya pakai token
        RetrofitClient.authToken = sessionManager.getToken()

        binding.tvWelcome.text = "Halo, ${user.name}!"
        
        if (user.role == "admin") {
            binding.btnAdmin.visibility = View.VISIBLE
        }

        setupRecyclerView()
        fetchBikes()
        checkActiveRental()
        setupBottomNavigation()

        binding.btnAdmin.setOnClickListener {
            startActivity(Intent(this, AdminActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_home
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_map -> {
                    startActivity(Intent(this, com.goesin.app.activity.MapActivity::class.java))
                    false
                }
                R.id.nav_history -> {
                    startActivity(Intent(this, RentalHistoryActivity::class.java))
                    false
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    false
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = BikeAdapter(emptyList()) { bike ->
            val intent = Intent(this, BikeDetailActivity::class.java)
            intent.putExtra("BIKE_DATA", Gson().toJson(bike))
            startActivity(intent)
        }
        binding.rvBikes.layoutManager = LinearLayoutManager(this)
        binding.rvBikes.adapter = adapter
    }

    private fun fetchBikes() {
        RetrofitClient.instance.getBikes().enqueue(object : Callback<BikeResponse> {
            override fun onResponse(call: Call<BikeResponse>, response: Response<BikeResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        adapter.updateData(it)
                    }
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BikeResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkActiveRental() {
        val user = sessionManager.getUser() ?: return
        RetrofitClient.instance.getRentals(user.id).enqueue(object : Callback<RentalResponse> {
            override fun onResponse(call: Call<RentalResponse>, response: Response<RentalResponse>) {
                if (response.isSuccessful) {
                    val rentals = response.body()?.data ?: emptyList()
                    
                    // Prioritas: Tampilkan yang sedang disewa (renting)
                    val activeRental = rentals.find { it.status == "renting" }
                    // Jika tidak ada, cek apakah ada yang menunggu approval (pending)
                    val pendingRental = rentals.find { it.status == "pending" }

                    when {
                        activeRental != null -> {
                            binding.cardActiveRental.visibility = View.VISIBLE
                            binding.btnReturnBike.visibility = View.VISIBLE
                            binding.btnReturnBike.setOnClickListener {
                                showReturnDialog(activeRental)
                            }
                            startRentalTimer(activeRental)
                        }
                        pendingRental != null -> {
                            binding.cardActiveRental.visibility = View.VISIBLE
                            binding.btnReturnBike.visibility = View.GONE
                            binding.tvActiveBikeName.text = "Menunggu Persetujuan Admin"
                            binding.tvTimer.text = "Sepeda: ${pendingRental.bikeName}"
                            binding.tvTimer.textSize = 16f
                            countDownTimer?.cancel()
                        }
                        else -> {
                            binding.cardActiveRental.visibility = View.GONE
                            binding.btnReturnBike.visibility = View.GONE
                            countDownTimer?.cancel()
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RentalResponse>, t: Throwable) {
                Log.e("MainActivity", "Error checking rental: ${t.message}")
            }
        })
    }

    private fun startRentalTimer(rental: Rental) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        try {
            val cleanTime = rental.startTime?.replace("T", " ")?.substringBefore(".")
            val startTime = sdf.parse(cleanTime ?: "")?.time ?: return
            val durationMillis = rental.duration * 60000L // Menit ke Milidetik (karena di DB simpan menit)
            val endTime = startTime + durationMillis
            val currentTime = System.currentTimeMillis()
            val remainingTime = endTime - currentTime

            if (remainingTime > 0) {
                binding.cardActiveRental.visibility = View.VISIBLE
                binding.tvActiveBikeName.text = "Sedang Menyewa: ${rental.bikeName ?: "Sepeda"}"
                binding.tvTimer.textSize = 24f
                
                // Schedule Reminder 10 mins before end
                if (remainingTime > 600000L) {
                    val reminderTime = endTime - 600000L
                    val intent = Intent(this, ReminderBroadcastReceiver::class.java)
                    intent.putExtra("BIKE_NAME", rental.bikeName)
                    val pendingIntent = PendingIntent.getBroadcast(
                        this, rental.id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
                }

                countDownTimer?.cancel()
                countDownTimer = object : CountDownTimer(remainingTime, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val hours = millisUntilFinished / 3600000
                        val minutes = (millisUntilFinished % 3600000) / 60000
                        val seconds = (millisUntilFinished % 60000) / 1000
                        binding.tvTimer.text = String.format("Sisa Waktu: %02d:%02d:%02d", hours, minutes, seconds)
                    }

                    override fun onFinish() {
                        binding.tvTimer.text = "Waktu Habis! Segera Kembalikan"
                        binding.tvTimer.setTextColor(getColor(android.R.color.holo_red_dark))
                        NotificationHelper(this@MainActivity).showWaktuHabis(rental.bikeName)
                    }
                }.start()
            } else {
                // Jika waktu sudah lewat (terlambat)
                val overtimeMillis = currentTime - endTime
                val hours = overtimeMillis / 3600000
                val minutes = (overtimeMillis % 3600000) / 60000
                
                binding.cardActiveRental.visibility = View.VISIBLE
                binding.tvActiveBikeName.text = "Keterlambatan: ${rental.bikeName}"
                binding.tvTimer.text = String.format("Telat: %02d jam %02d menit", hours, minutes)
                binding.tvTimer.setTextColor(getColor(android.R.color.holo_red_dark))
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Timer error: ${e.message}")
        }
    }

    private fun showReturnDialog(rental: Rental) {
        AlertDialog.Builder(this)
            .setTitle("Kembalikan Sepeda")
            .setMessage("Apakah anda yakin ingin berhenti menyewa sepeda ini?")
            .setPositiveButton("Iya") { _, _ ->
                returnBike(rental.id)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun returnBike(rentalId: Int) {
        RetrofitClient.instance.returnBike(rentalId).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@MainActivity, "Sepeda berhasil dikembalikan!", Toast.LENGTH_SHORT).show()
                    checkActiveRental() // Refresh UI
                    fetchBikes() // Refresh daftar sepeda (stok)
                } else {
                    Toast.makeText(this@MainActivity, "Gagal mengembalikan sepeda", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        fetchBikes()
        checkActiveRental()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}