package com.marplex.gpslocation

import android.location.Location
import java.lang.Exception

interface GpsLocationListener {
    fun onLocationReceived(locations: List<Location>)
    fun onLocationStatusReceived(status: LocationStatus)
    fun onLocationException(exception: Exception)
}