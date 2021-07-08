package com.example.googlemap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class SecondActivity : AppCompatActivity() {

    private lateinit var distance: TextView
    private lateinit var time: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        distance = findViewById(R.id.distance)
        time = findViewById(R.id.timeDistnce)

        val intent = intent


        val double = String.format("%.2f", intent.getStringExtra("metr")!!.toDouble() / 100)

        distance.setText(double+" km")

        time.setText(intent.getStringExtra("time"))
        Log.i("AAAAA", "distance: $distance")
        Log.i("AAAAA", "time: $time")

    }
}