package com.aurelioklv.dicodingstoryapp.presentation.maps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.aurelioklv.dicodingstoryapp.R
import com.aurelioklv.dicodingstoryapp.data.Result
import com.aurelioklv.dicodingstoryapp.databinding.ActivityMapsBinding
import com.aurelioklv.dicodingstoryapp.presentation.details.DetailsActivity
import com.aurelioklv.dicodingstoryapp.presentation.utils.ViewModelFactory
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val viewModel: MapsViewModel by viewModels<MapsViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private val boundsBuilder = LatLngBounds.Builder()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setTitle(R.string.story)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        setMapStyle()

        val borders = listOf(
            LatLng(6.0, 95.0),
            LatLng(6.0, 141.0),
            LatLng(-11.0, 95.0),
            LatLng(-11.0, 141.0)
        )
        borders.forEach { boundsBuilder.include(it) }
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                50
            )
        )

        mMap.setOnInfoWindowClickListener {
            val storyId = it.tag.toString()
            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.EXTRA_ID, storyId)
            startActivity(intent)
        }

        viewModel.stories.observe(this) {
            when (it) {
                is Result.Error -> Log.e(TAG, it.error)
                Result.Loading -> Log.d(TAG, "onMapReady: Loading")
                is Result.Success -> {
                    it.data.forEach { story ->
                        val latLng = LatLng(story.lat!!, story.lon!!)
                        mMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(story.name)
                                .snippet(story.description)
                        )?.apply { tag = story.id }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.normal_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                true
            }

            R.id.satellite_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                true
            }

            R.id.terrain_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                true
            }

            R.id.hybrid_type -> {
                mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setMapStyle() {
        return
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}