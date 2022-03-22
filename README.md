<h1 align="center">gpslocation</h1>
<p align="center">
	An android kotlin library to easily access gps location<br/>
	It uses Fused Location Provider API and Android Location API if google services are not installed
</p>

<p align="center">
  <a href="https://opensource.org/licenses/Apache-2.0"><img alt="License" src="https://img.shields.io/badge/License-Apache%202.0-blue.svg"/></a>
  <a href="https://jitpack.io/#marplex/gpslocation"><img alt="JitPack" src="https://jitpack.io/v/Marplex/gpslocation.svg"/></a>
  <a href="https://android-arsenal.com/api?level=16"><img alt="API" src="https://img.shields.io/badge/API-16%2B-brightgreen.svg?style=flat"/></a>
</p>

## Download

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

```gradle
dependencies {
    implementation "com.github.marplex:gpslocation:1.0.0"
}
```

## Usage

```kotlin
val gpsLocation = GPSLocation(context)
```

```kotlin
// Start and stop location updates
gpsLocation.startLocationUpdates()
gpsLocation.stopLocationUpdates()
```

```kotlin
//Get last cached best location
gpsLocation.getLastKnownLocation { location -> println(location) }

//Get current location (with active location computation)
gpsLocation.getCurrentLocation { location -> println(location) }
```
```kotlin
// Listen for location updates and status
gpsLocation.gpsLocationListener = object: GpsLocationListener {
	override fun onLocationReceived(locations: List<Location>) { }
	override fun onLocationStatusReceived(status: LocationStatus) {  
	    when(status) {  
	        LocationStatus.MISSING_PERMISSIONS -> TODO("Ask gps permissions")  
	        LocationStatus.PERMISSIONS_DENIED -> TODO("Ask gps permissions")  
	        LocationStatus.NO_GPS -> gpsLocation.showLocationSettings(this)  
	    }  
	}
}
```
## License
```xml
Copyright 2022 marplex (Marco Cimolai)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.