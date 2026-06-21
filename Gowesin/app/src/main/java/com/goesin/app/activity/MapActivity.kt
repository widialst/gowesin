package com.goesin.app.activity

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goesin.app.MainActivity
import com.goesin.app.R
import com.goesin.app.api.RetrofitClient
import com.goesin.app.databinding.ActivityMapBinding
import com.goesin.app.model.BikeResponse
import com.google.gson.Gson
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup Osmdroid Configuration
        Configuration.getInstance().load(applicationContext, PreferenceManager.getDefaultSharedPreferences(applicationContext))
        Configuration.getInstance().userAgentValue = packageName

        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupMap()
        setupBottomNavigation()
        fetchBikesForMap()
    }

    private fun setupMap() {
        binding.mapView.setTileSource(TileSourceFactory.MAPNIK)
        binding.mapView.setMultiTouchControls(true)
        
        // Default Center: Monas, Jakarta
        val mapController = binding.mapView.controller
        mapController.setZoom(14.0)
        val startPoint = GeoPoint(-6.1754, 106.8272)
        mapController.setCenter(startPoint)
    }

    private fun fetchBikesForMap() {
        RetrofitClient.instance.getBikes().enqueue(object : Callback<BikeResponse> {
            override fun onResponse(call: Call<BikeResponse>, response: Response<BikeResponse>) {
                if (response.isSuccessful) {
                    response.body()?.data?.let { bikes ->
                        for (bike in bikes) {
                            if (bike.status == "available" && bike.stock > 0) {
                                val marker = Marker(binding.mapView)
                                marker.position = GeoPoint(bike.lat, bike.lng)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = bike.name
                                marker.snippet = "Sisa Stok: ${bike.stock} | Rp ${bike.pricePerHour.toLong()}/jam\nKlik untuk sewa!"
                                
                                marker.setOnMarkerClickListener { m, _ ->
                                    m.showInfoWindow()
                                    true
                                }
                                
                                // Open Detail on InfoWindow Click
                                marker.infoWindow = org.osmdroid.views.overlay.infowindow.MarkerInfoWindow(
                                    org.osmdroid.library.R.layout.bonuspack_bubble, binding.mapView
                                )
                                
                                binding.mapView.overlays.add(marker)
                            }
                        }
                        binding.mapView.invalidate()
                    }
                }
            }

            override fun onFailure(call: Call<BikeResponse>, t: Throwable) {
                Toast.makeText(this@MapActivity, "Gagal memuat peta: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_map
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_map -> true
                R.id.nav_history -> {
                    startActivity(Intent(this, RentalHistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }
}
