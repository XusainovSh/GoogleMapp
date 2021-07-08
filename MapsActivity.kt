package com.example.googlemap

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import com.example.googlemap.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import java.util.*

class MapsActivity : AppCompatActivity(), GoogleMap.OnMarkerClickListener, OnMapReadyCallback,
    GoogleMap.OnMarkerDragListener {


    //view binding
    private lateinit var binding: ActivityMapsBinding

    //google map
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


    //push notification
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channelName"
    private var time = 0

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
    }

    @SuppressLint("RemoteViewLayout", "WrongConstant")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createChannel()


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        setUpMap()
    }

    private fun setUpMap() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), LOCATION_REQUEST_CODE
            )
            return
        }
        mMap.isMyLocationEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener(this) { location ->

            if (location != null) {
                lastLocation = location
                val currentLocation = LatLng(location.latitude, location.longitude)
                placeMarceronMap(currentLocation)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 17f))
            }


        }

        mMap.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener {
            override fun onMapLongClick(p0: LatLng) {
                //eski markerni tozalash
                mMap.clear()
                //yangi qilingan markerni latlongi
                mMap.addMarker(MarkerOptions().position(p0))
                    .title = ("${p0.latitude}" + "  " + "${p0.longitude}")

                binding.startButton.setOnClickListener {

                    Toast.makeText(this@MapsActivity, "Start walking", Toast.LENGTH_SHORT).show()

                    var timer = Timer()
                    val task: TimerTask = object : TimerTask() {
                        override fun run() {
                            time++
                            runOnUiThread {
                                setTime(time)
                            }
                        }
                    }
                    timer.schedule(task, 1000, 1000)
                    Log.i("DDDDD", "onMapLongClick: $time")

                    val latLng = LatLng(lastLocation.latitude, lastLocation.longitude)
                    val distance = SphericalUtil.computeDistanceBetween(latLng, p0)
                    val double = String.format("%.2f", distance / 100)
//                    Toast.makeText(this@MapsActivity, "$double", Toast.LENGTH_SHORT).show()
                    Log.i("AAAAA", "double: $double")

                    if (((Math.round(lastLocation.latitude * 10000.0) / 10000.0) == (Math.round(p0.latitude * 10000.0) / 10000.0))
                        && ((Math.round(lastLocation.longitude * 10000.0) / 10000.0) >= (Math.round(
                            p0.longitude * 100000.0
                        ) / 100000.0))
                    ) {
                        if (timer != null) {
                            timer.cancel()
                            timer == null
                        }
                        val sekund = time % 60
                        val hour = time / 3600
                        val minute = (time - hour * 3600) / 60
                        val currentTime = String.format("%02d:%02d:%02d", hour, minute, sekund)

                        val intent = Intent(this@MapsActivity, SecondActivity::class.java)
                        intent.putExtra("metr", distance.toString())
                        intent.putExtra("time", currentTime.toString())
                        val peddingINtent = TaskStackBuilder.create(this@MapsActivity).run {
                            addNextIntentWithParentStack(intent)
                            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
                        }
                        val notification =
                            NotificationCompat.Builder(this@MapsActivity, CHANNEL_ID)
                                .setContentTitle(double + " km")
                                .setContentText(currentTime)
                                .setSmallIcon(R.drawable.ic_launcher_foreground)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setContentIntent(peddingINtent)
                                .build()

                        val notificationManager =
                            NotificationManagerCompat.from(this@MapsActivity)
                        notificationManager.notify(0, notification)
                        Log.i("AAAAA", "onMapLongClick: $notification")

                    }

                }


            }
        })


    }

    private fun placeMarceronMap(currentLocation: LatLng) {

        val markerOptions = MarkerOptions().position(currentLocation)
        markerOptions.title("$currentLocation")
        mMap.addMarker(markerOptions)

    }

    override fun onMarkerClick(p0: Marker) = false

    override fun onMarkerDragStart(p0: Marker) {
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onMarkerDragEnd(p0: Marker) {
        val latlong: LatLng = p0.position
        //logging
    }


    private fun createChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val chanel: NotificationChannel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    lightColor = Color.GREEN
                    enableLights(true)
                }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(chanel)

        }
    }

    fun setTime(time: Int) {
        val sekund = time % 60
        val hour = time / 3600
        val minute = (time - hour * 3600) / 60
        val currentTime = String.format("%02d:%02d:%02d", hour, minute, sekund)
        //logging
    }

}