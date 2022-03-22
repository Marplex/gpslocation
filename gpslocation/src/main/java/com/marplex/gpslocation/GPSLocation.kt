package com.marplex.gpslocation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.*
import android.location.LocationListener
import android.os.Build
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.*
import java.util.function.Consumer

/**
 * A utility class that makes it easy to receive location updates.
 * It uses FusedLocationProvider as default and vanilla location apis as fallback.
 * Use this class with a singleton or a dependency injection framework (Dagger, Hilt, Koin, ecc...)
 */
class GPSLocation(private val context: Context) {

    private val defaultLocationRequest = LocationRequest.create().apply {
        interval = 5000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Customizable location request, use this setter to change the [LocationRequest].
     * Default value is [defaultLocationRequest]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var locationRequest: LocationRequest = defaultLocationRequest
        set(value) {
            builder.addLocationRequest(value)
            field = value
        }

    private val builder = LocationSettingsRequest.Builder()
        .addLocationRequest(locationRequest)

    private val client: SettingsClient = LocationServices.getSettingsClient(context)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Bind this interface to your class to listen for location updates and receive
     * location status (missing permissions, gps off)
     */
    var gpsLocationListener: GpsLocationListener? = null

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            gpsLocationListener?.onLocationReceived(locationResult.locations)
        }

        override fun onLocationAvailability(availability: LocationAvailability) {
            if(!availability.isLocationAvailable)
                gpsLocationListener?.onLocationStatusReceived(LocationStatus.NO_GPS)
        }
    }

    private val locationListener = object : LocationListener {
        override fun onProviderEnabled(provider: String) { }
        override fun onLocationChanged(location: Location) {
            gpsLocationListener?.onLocationReceived(Collections.singletonList(location))
        }

        override fun onProviderDisabled(provider: String) {
            gpsLocationListener?.onLocationStatusReceived(LocationStatus.NO_GPS)
        }
    }

    private fun isGooglePlayServicesAvailable() = GoogleApiAvailability.getInstance()
        .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    private fun isAllPermissionsGranted() = if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        gpsLocationListener?.onLocationStatusReceived(LocationStatus.PERMISSIONS_DENIED)
        false
    }else true

    /**
     * Start location updates with [locationRequest]
     * It uses [FusedLocationProviderClient] as default and [LocationManager] as fallback.
     * This method does not asks for permissions, is your own responsibility to ask them when
     * the received [LocationStatus] is [LocationStatus.MISSING_PERMISSIONS]
     * or [LocationStatus.PERMISSIONS_DENIED]
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        val gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if(!gpsStatus) gpsLocationListener?.onLocationStatusReceived(LocationStatus.NO_GPS)
        else if(isAllPermissionsGranted()){
            if(isGooglePlayServicesAvailable()) {
                fusedLocationClient.requestLocationUpdates(locationRequest,
                    locationCallback,
                    Looper.getMainLooper())
            }else locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                0F,
                locationListener
            )
        }else gpsLocationListener?.onLocationStatusReceived(LocationStatus.MISSING_PERMISSIONS)
    }

    /**
     * Stop location updates
     */
    fun stopLocationUpdates() {
        if(isGooglePlayServicesAvailable()) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }else {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            locationManager.removeUpdates(locationListener)
        }
    }

    /**
     * Returns the best most recent location currently available.
     */
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(locationCallback: ((Location) -> Unit)) {
        if(isGooglePlayServicesAvailable()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { locationCallback(it) }
                .addOnFailureListener { gpsLocationListener?.onLocationException(it) }
        }else {
            val locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if(location != null) locationCallback(location)
            else gpsLocationListener?.onLocationException(Exception("Location not available"))
        }
    }

    /**
     * Returns a single current location fix on the device.
     * Unlike [getLastKnownLocation] that returns a cached location, this method could
     * cause active location computation on the device
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(locationCallback: ((Location) -> Unit)) {
        if(isGooglePlayServicesAvailable()) {
            val token = CancellationTokenSource().token
            fusedLocationClient.getCurrentLocation(locationRequest.priority, token)
                .addOnSuccessListener { locationCallback(it) }
                .addOnFailureListener { gpsLocationListener?.onLocationException(it) }
        }else getLastKnownLocation(locationCallback)
    }

    /**
     * Utility method to open android location settings.
     * It uses the new SettingsAPI or opens the activity as fallback
     */
    fun showLocationSettings(activity: Activity) {
        val task = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                try {
                    // Handle result in onActivityResult()
                    e.startResolutionForResult(activity, 100)
                } catch (sendEx: IntentSender.SendIntentException) { }
            }
        }
    }

    /**
     * Call this method in every activity in [Activity.onActivityResult]
     */
    fun handleActivityResult(requestCode: Int, resultCode: Int) {
        if(requestCode == 100) when(resultCode) {
            Activity.RESULT_OK -> startLocationUpdates()
            Activity.RESULT_CANCELED -> gpsLocationListener?.onLocationStatusReceived(LocationStatus.NO_GPS)
        }
    }

}