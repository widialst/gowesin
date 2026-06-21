package com.goesin.app.adapter

import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.goesin.app.databinding.ItemRentalBinding
import com.goesin.app.model.Rental
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RentalAdapter(
    private val rentals: List<Rental>
) : RecyclerView.Adapter<RentalAdapter.RentalViewHolder>() {

    // Simpan timer aktif agar bisa di-cancel saat view di-recycle
    private val activeTimers = mutableMapOf<Int, CountDownTimer>()

    inner class RentalViewHolder(val binding: ItemRentalBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RentalViewHolder {
        val binding = ItemRentalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RentalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RentalViewHolder, position: Int) {
        val rental = rentals[position]

        // Cancel timer lama jika ada
        activeTimers[position]?.cancel()

        holder.binding.apply {
            tvBikeName.text = rental.bikeName ?: "Sepeda #${rental.bikeId}"
            tvDate.text = "📅 ${rental.rentalDate}"
            tvTotal.text = "💰 Total: Rp ${formatRupiah(rental.totalPrice.toLong())}"
            tvPayment.text = rental.paymentMethod

            when (rental.status) {
                "pending" -> {
                    tvStatus.text = "Menunggu Persetujuan"
                    tvStatus.setBackgroundColor(Color.parseColor("#FFCA28")) // Yellow
                    layoutTimer.visibility = View.GONE
                    tvDurationInfo.text = "Durasi: ${rental.duration / 60} jam"
                }
                "active", "renting" -> {
                    tvStatus.text = "Aktif"
                    tvStatus.setBackgroundColor(Color.parseColor("#81C784")) // Green
                    layoutTimer.visibility = View.VISIBLE
                    tvDurationInfo.text = "Durasi: ${rental.duration / 60} jam"

                    // Hitung sisa waktu
                    val millisLeft = calculateMillisLeft(rental.startTime, rental.duration)

                    if (millisLeft > 0) {
                        val timer = object : CountDownTimer(millisLeft, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                val h = millisUntilFinished / 3600000
                                val m = (millisUntilFinished % 3600000) / 60000
                                val s = (millisUntilFinished % 60000) / 1000
                                tvTimer.text = String.format("%02d:%02d:%02d", h, m, s)

                                // Ubah warna merah jika < 10 menit
                                if (millisUntilFinished < 600000) {
                                    tvTimer.setTextColor(Color.parseColor("#E57373"))
                                }
                            }

                            override fun onFinish() {
                                tvTimer.text = "00:00:00"
                                tvStatus.text = "Selesai"
                                tvStatus.setBackgroundColor(Color.parseColor("#90A4AE")) // Gray
                                layoutTimer.visibility = View.GONE
                            }
                        }.start()

                        activeTimers[position] = timer
                    } else {
                        tvTimer.text = "00:00:00"
                        tvStatus.text = "Selesai"
                        tvStatus.setBackgroundColor(Color.parseColor("#90A4AE"))
                        layoutTimer.visibility = View.GONE
                    }
                }
                "canceled" -> {
                    tvStatus.text = "Dibatalkan"
                    tvStatus.setBackgroundColor(Color.parseColor("#E57373")) // Red
                    layoutTimer.visibility = View.GONE
                }
                else -> {
                    tvStatus.text = "Selesai"
                    tvStatus.setBackgroundColor(Color.parseColor("#90A4AE")) // Gray
                    layoutTimer.visibility = View.GONE
                }
            }
        }
    }

    private fun calculateMillisLeft(startTime: String?, durationMinutes: Int): Long {
        if (startTime == null) return 0
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val cleanTime = startTime.replace("T", " ").substringBefore(".")
            val startMillis = sdf.parse(cleanTime)?.time ?: return 0
            val endMillis = startMillis + (durationMinutes * 60000L)
            val remaining = endMillis - System.currentTimeMillis()
            if (remaining > 0) remaining else 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatRupiah(amount: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
    }

    override fun getItemCount(): Int = rentals.size

    // Penting! Cancel semua timer saat adapter di-destroy
    fun cancelAllTimers() {
        activeTimers.values.forEach { it.cancel() }
        activeTimers.clear()
    }
}