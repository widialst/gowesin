package com.goesin.app.activity

import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityBikeDetailBinding
import com.goesin.app.databinding.DialogCashPaymentBinding
import com.goesin.app.databinding.DialogQrPaymentBinding
import com.goesin.app.model.BaseResponse
import com.goesin.app.model.Bike
import com.goesin.app.util.SessionManager
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class BikeDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBikeDetailBinding
    private lateinit var bike: Bike
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBikeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        val bikeJson = intent.getStringExtra("BIKE_DATA")
        bike = Gson().fromJson(bikeJson, Bike::class.java)

        displayBikeDetail()

        binding.btnBack.setOnClickListener { finish() }

        binding.etDuration.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateTotal()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnRent.setOnClickListener {
            val durationStr = binding.etDuration.text.toString()
            if (durationStr.isEmpty() || durationStr == ".") {
                Toast.makeText(this, "Masukkan durasi sewa", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val duration = durationStr.toDoubleOrNull()
            if (duration == null || duration <= 0) {
                Toast.makeText(this, "Durasi tidak valid", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val totalPrice = duration * bike.pricePerHour
            val selectedPayment = binding.spPayment.selectedItem.toString()

            when {
                selectedPayment.contains("Cash") -> showCashDialog(totalPrice, duration)
                selectedPayment.contains("QR")   -> showQrDialog(totalPrice, duration)
                else -> rentBike(totalPrice, duration, selectedPayment)
            }
        }
    }

    private fun displayBikeDetail() {
        binding.apply {
            tvBikeNameDetail.text = bike.name
            tvBikeDesc.text = bike.description
            tvPriceDetail.text = "Rp ${formatRupiah(bike.pricePerHour.toLong())} / jam"
            tvBatteryDetail.text = "${bike.battery}%"

            tvStatusDetail.text = if (bike.status == "available" && bike.stock > 0) "✅ Tersedia" else "❌ Tidak Tersedia"
            tvStatusDetail.setBackgroundColor(
                if (bike.status == "available" && bike.stock > 0) Color.parseColor("#81C784") else Color.parseColor("#E57373")
            )
            
            tvStockDetail.text = "Sisa Stok: ${bike.stock}"
            if (bike.stock == 0) {
                btnRent.isEnabled = false
                btnRent.text = "Stok Habis"
                btnRent.setBackgroundColor(Color.parseColor("#90A4AE"))
            }

            Glide.with(this@BikeDetailActivity)
                .load(bike.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(ivBikeDetail)
        }
    }

    private fun calculateTotal() {
        val durationStr = binding.etDuration.text.toString()
        val duration = durationStr.toDoubleOrNull()
        if (duration != null && duration > 0) {
            val total = duration * bike.pricePerHour
            binding.tvTotalPrice.text = "Rp ${formatRupiah(total.toLong())}"
        } else {
            binding.tvTotalPrice.text = "Rp 0"
        }
    }

    private fun showCashDialog(totalPrice: Double, duration: Double) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogCashPaymentBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvCashAmount.text = "Total: Rp ${formatRupiah(totalPrice.toLong())}"

        dialogBinding.btnConfirmCash.setOnClickListener {
            dialog.dismiss()
            rentBike(totalPrice, duration, "Cash")
        }
        dialog.show()
    }

    private fun showQrDialog(totalPrice: Double, duration: Double) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val dialogBinding = DialogQrPaymentBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding.tvQrAmount.text = "Total: Rp ${formatRupiah(totalPrice.toLong())}"

        val qrContent = "GOWESIN|${bike.name}|${totalPrice.toLong()}|${System.currentTimeMillis()}"
        dialogBinding.ivQrCode.setImageBitmap(generateQrCode(qrContent))

        dialogBinding.btnConfirmPayment.setOnClickListener {
            dialog.dismiss()
            rentBike(totalPrice, duration, "QR Code (QRIS)")
        }
        dialog.show()
    }

    private fun generateQrCode(content: String): Bitmap? {
        return try {
            val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, 600, 600)
            BarcodeEncoder().createBitmap(bitMatrix)
        } catch (e: Exception) { null }
    }

    private fun rentBike(totalPrice: Double, duration: Double, paymentMethod: String) {
        val user = sessionManager.getUser() ?: return
        val rentalDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val durationInMinutes = (duration * 60).toInt()

        binding.btnRent.isEnabled = false
        binding.btnRent.text = "Memproses..."

        val body: Map<String, Any> = mapOf(
            "bike_id" to bike.id,
            "rental_date" to rentalDate,
            "duration" to durationInMinutes,
            "total_price" to totalPrice,
            "payment_method" to paymentMethod
        )

        RetrofitClient.instance.rentBike(body).enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                binding.btnRent.isEnabled = true
                binding.btnRent.text = "🚲  Sewa Sekarang"

                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@BikeDetailActivity, 
                        "Sewa berhasil! Selamat bersepeda!", 
                        Toast.LENGTH_LONG).show()
                    finish()
                } else {
                    Toast.makeText(this@BikeDetailActivity,
                        response.body()?.message ?: "Gagal menyewa", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                binding.btnRent.isEnabled = true
                binding.btnRent.text = "🚲  Sewa Sekarang"
                Toast.makeText(this@BikeDetailActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(amount: Long): String {
        return NumberFormat.getNumberInstance(Locale("id", "ID")).format(amount)
    }
}