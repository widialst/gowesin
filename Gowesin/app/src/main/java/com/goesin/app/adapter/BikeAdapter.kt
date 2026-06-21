package com.goesin.app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goesin.app.databinding.ItemBikeBinding
import com.goesin.app.model.Bike

class BikeAdapter(
    private var bikes: List<Bike>,
    private val onClick: (Bike) -> Unit
) : RecyclerView.Adapter<BikeAdapter.BikeViewHolder>() {

    class BikeViewHolder(val binding: ItemBikeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BikeViewHolder {
        val binding = ItemBikeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BikeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BikeViewHolder, position: Int) {
        val bike = bikes[position]
        holder.binding.apply {
            tvBikeName.text = bike.name
            tvPrice.text = "Rp ${bike.pricePerHour} / jam"
            tvBattery.text = "Battery: ${bike.battery}%"
            tvStatus.text = bike.status
            tvStock.text = "Sisa Stok: ${bike.stock}"
            if (bike.stock == 0) {
                tvStock.setTextColor(android.graphics.Color.parseColor("#E57373")) // Red if out of stock
            } else {
                tvStock.setTextColor(android.graphics.Color.parseColor("#757575")) // Gray
            }

            Glide.with(root.context)
                .load(bike.imageUrl)
                .into(ivBike)

            root.setOnClickListener { onClick(bike) }
        }
    }

    override fun getItemCount(): Int = bikes.size

    fun updateData(newBikes: List<Bike>) {
        bikes = newBikes
        notifyDataSetChanged()
    }
}