package com.marplex.gpslocationdemo

import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.marplex.gpslocation.GPSLocation
import com.marplex.gpslocation.GpsLocationListener
import com.marplex.gpslocation.LocationStatus
import java.lang.Exception

class MainActivity : AppCompatActivity(), GpsLocationListener {

    private val gpsLocation: GPSLocation = GPSLocation(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gpsLocation.gpsLocationListener = this
        gpsLocation.startLocationUpdates()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsLocation.handleActivityResult(requestCode, resultCode)
    }

    override fun onPause() {
        gpsLocation.stopLocationUpdates()
        super.onPause()
    }

    override fun onLocationReceived(locations: List<Location>) {
        //Access location
        val location = locations[0]

        //Use this location
    }

    override fun onLocationStatusReceived(status: LocationStatus) {
        when(status) {
            LocationStatus.MISSING_PERMISSIONS -> TODO("Ask gps permissions")
            LocationStatus.PERMISSIONS_DENIED -> TODO("Ask gps permissions")
            LocationStatus.NO_GPS -> gpsLocation.showLocationSettings(this)
        }
    }

    override fun onLocationException(exception: Exception) {
        exception.printStackTrace()
    }
}