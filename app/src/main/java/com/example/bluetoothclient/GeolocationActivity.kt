package com.example.bluetoothclient

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GeolocationActivity: AppCompatActivity() {

    private val REQUEST_CODE_LOCATION = 6

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var latitudeData: TextView
    private lateinit var longitudeData: TextView
    private lateinit var altitudeData: TextView
    private lateinit var magnetometerData: TextView
    private lateinit var gyroscopeData: TextView

    private lateinit var latitudeData2: TextView
    private lateinit var longitudeData2: TextView
    private lateinit var altitudeData2: TextView
    private lateinit var magnetometerData2: TextView
    private lateinit var gyroscopeData2: TextView

    private lateinit var updateBtn: Button
    private lateinit var sendBtn: Button

    var magnetometer = 1
    var gyroscope = 1

    var geolocation = Geolocation(0)

    override fun onCreate(savedInstancestate: Bundle?) {
        super.onCreate(savedInstancestate)
        setContentView(R.layout.activity_geolocation)

        updateBtn = findViewById(R.id.geo_updateBtn)
        sendBtn = findViewById(R.id.geo_sendBtn)

        latitudeData = findViewById(R.id.latitudeData)
        longitudeData = findViewById(R.id.longitudeData)
        altitudeData = findViewById(R.id.altitudeData)
        magnetometerData = findViewById(R.id.magnetometerData)
        gyroscopeData = findViewById(R.id.gyroscopeData)
        latitudeData2 = findViewById(R.id.latitudeData2)
        longitudeData2 = findViewById(R.id.longitudeData2)
        altitudeData2 = findViewById(R.id.altitudeData2)
        magnetometerData2 = findViewById(R.id.magnetometerData2)
        gyroscopeData2 = findViewById(R.id.gyroscopeData2)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getCurrentGps(0)

        updateBtn.setOnClickListener{
            getCurrentGps(0)
            getCurrentOrientation(0)
            latitudeData.text = geolocation.gps[0].lat.toString()
            longitudeData.text = geolocation.gps[0].lon.toString()
            altitudeData.text= geolocation.gps[0].alt.toString()
            Thread.sleep(3000)
            getCurrentGps(1)
            getCurrentOrientation(1)
            latitudeData2.text = geolocation.gps[1].lat.toString()
            longitudeData2.text = geolocation.gps[1].lon.toString()
            altitudeData2.text = geolocation.gps[1].alt.toString()
            magnetometerData.text = magnetometer.toString()
            gyroscopeData.text = gyroscope.toString()
            Toast.makeText(this, "Location updated", Toast.LENGTH_SHORT).show()
            updateBtn.setText(R.string.update)
        }

        sendBtn.setOnClickListener {
            val json = Json.encodeToString(geolocation)
            val data = Intent()
            data.putExtra("json", json)
            setResult(RESULT_OK, data)
            finish()
        }
    }

    fun getCurrentGps(pos:Int){
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
            }
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) = CancellationTokenSource().token
                override fun isCancellationRequested() = false
            }).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    geolocation.gps[pos].lat = location.latitude
                    geolocation.gps[pos].lon = location.longitude
                    geolocation.gps[pos].alt = location.altitude
                }
                else {
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                }
            }
        }

    fun getCurrentOrientation(pos:Int){
        geolocation.orientation[pos].x = 5.0
        geolocation.orientation[pos].y = 5.0
        geolocation.orientation[pos].z = 5.0
    }

    /*fun getLastKnownLocation() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_LOCATION)
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                    altitude = location.altitude

                    *//*Log.i(TAG, "Latitude: $latitude")
                    Log.i(TAG, "Longitude: $longitude")
                    Log.i(TAG, "Altitude: $altitude")*//*

                    //mylocation = "Latitude:"+latitude+ "\nLongitude:"+longitude
                }
                else{
                    Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                }
            }
    }*/

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //var allPermsGranted = false
        var allPermsGranted: Boolean

        when (requestCode){
            REQUEST_CODE_LOCATION -> {
                allPermsGranted = true
                for (res in grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED){
                        allPermsGranted = false
                        break
                    }
                }
                if (allPermsGranted){
                    // Success (do nothing, the code is written so only if there is permission continues executing)
                } else {
                    Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                // What to do when any other permission was requested
            }
        }
    }

    @Serializable
    data class Geolocation(var version: Int){

        var gps: List<MyGps> = listOf(MyGps(0.0,0.0,0.0),MyGps(0.0,0.0,0.0))
        var orientation: List<MyOrientation> = listOf(MyOrientation(0.0,0.0,0.0),MyOrientation(0.0,0.0,0.0))

        @Serializable
        data class MyOrientation(var x:Double,var y:Double,var z:Double){}

        @Serializable
        data class MyGps(var lon:Double,var lat:Double,var alt:Double){}
    }
}

