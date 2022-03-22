package com.marplex.gpslocation

import android.app.Activity
import android.content.Intent

/**
 * Use this activity to automatically handle [onActivityResult] method
 */
abstract class GPSLocationActivity : Activity() {
    abstract val gpsLocation: GPSLocation
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gpsLocation.handleActivityResult(requestCode, resultCode)
    }
}