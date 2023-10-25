package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(),OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map:GoogleMap
    var selectLocation=true
    lateinit var latLng: LatLng
    lateinit var locationName:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)
        val supportMapFragment =
            childFragmentManager.findFragmentById(R.id.google_map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)



        binding.saveButton.setOnClickListener {
            if(selectLocation){
                Toast.makeText(this.requireContext(),"please select location",Toast.LENGTH_LONG).show()
            }
            else {
                onLocationSelected()
            }
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if(::latLng.isInitialized&&::locationName.isInitialized){
            _viewModel.longitude.value=latLng.longitude
            _viewModel.latitude.value=latLng.latitude
            _viewModel.reminderSelectedLocationStr.value=locationName
            _viewModel.navigationCommand.postValue(NavigationCommand.Back)
        }
        else{
            Toast.makeText(this.requireContext(),"please select location",Toast.LENGTH_LONG).show()
        }
    }

    private fun isPermissionGranted() : Boolean {
        return ContextCompat.checkSelfPermission(
            this.requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }
    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        }
        else {
            requestPermissions(
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map=googleMap
        enableMyLocation()
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this.requireContext(),R.raw.map_style))

        if(isPermissionGranted()){
            val manager = this.requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val Criteria = Criteria()
            val bestProvider = java.lang.String.valueOf(manager.getBestProvider(Criteria, true))

            var location = manager.getLastKnownLocation(bestProvider)
            location?.let {
                val currentLatitude: Double = location.getLatitude()
                val currentLongitude: Double = location.getLongitude()
                map.moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            currentLatitude,
                            currentLongitude
                        ), 15f
                    )
                )
            }
        }
        map.setOnMapLongClickListener { latLng ->
            if(selectLocation) {
                selectLocation=false
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                )
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses: List<Address>? = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                var addressName=StringBuilder("")
                addresses?.let {
                    addressName.append(addresses[0].locality).append("\n").append(addresses[0].adminArea.toString())
                    this.locationName=addressName.toString()
                }
                this.latLng=latLng
            }
            else{
                Toast.makeText(this.requireContext(),"please select only one location",Toast.LENGTH_LONG).show()
            }
        }
        map.setOnPoiClickListener { poi ->
            if(selectLocation) {
                selectLocation=false
                val poiMarker = map.addMarker(
                    MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name)
                )
                this.locationName=poi.name
                this.latLng=poi.latLng
                poiMarker.showInfoWindow()
            }
            else{
                Toast.makeText(this.requireContext(),"please select only one location",Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
            else{
                _viewModel.showToast.value="please enable the location"
                Log.i("test","\"please enable the location\"")
            }

        }
    }


}
