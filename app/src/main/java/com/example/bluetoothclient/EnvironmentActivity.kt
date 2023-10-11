package com.example.bluetoothclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class EnvironmentActivity: AppCompatActivity() {

    private lateinit var co2Data: TextView
    private lateinit var humidityData: TextView
    private lateinit var temperatureData: TextView
    private lateinit var pollutionData: TextView
    private lateinit var ozoneData: TextView

    private lateinit var updateBtn: Button
    private lateinit var sendBtn: Button

    private val co2 = 50.0
    private val humidity = 44.0
    private val temperature = 21.0
    private val pollution = 63.0
    private val ozone = 200.0

    private var environment = Environment(0)

    override fun onCreate(savedInstancestate: Bundle?) {
        super.onCreate(savedInstancestate)
        setContentView(R.layout.activity_environment)

        updateBtn = findViewById(R.id.env_updateBtn)
        sendBtn = findViewById(R.id.env_sendBtn)

        co2Data = findViewById(R.id.co2Data)
        humidityData = findViewById(R.id.humidityData)
        temperatureData = findViewById(R.id.temperatureData)
        pollutionData = findViewById(R.id.pollutionData)
        ozoneData = findViewById(R.id.ozoneData)

        sendBtn.setOnClickListener {
            val json = Json.encodeToString(environment)
            val data = Intent()
            data.putExtra("json", json)
            setResult(RESULT_OK, data)
            finish()
        }

        updateBtn.setOnClickListener{
            getCurrentEnvironment()
            co2Data.text = environment.co2.toString()
            humidityData.text = environment.humidity.toString()
            temperatureData.text = environment.temperature.toString()
            pollutionData.text = environment.pollution.toString()
            ozoneData.text = environment.ozone.toString()
            Toast.makeText(this, "Environment updated", Toast.LENGTH_SHORT).show()
            updateBtn.setText(R.string.update)
        }
    }

    private fun getCurrentEnvironment(){
        environment.co2 = co2
        environment.humidity = humidity
        environment.ozone = ozone
        environment.pollution = pollution
        environment.temperature = temperature
    }

    @Serializable
    data class Environment(var version: Int){

        var temperature: Double = 0.0
        var humidity: Double = 0.0
        var ozone: Double = 0.0
        var pollution: Double = 0.0
        var co2: Double = 0.0

    }


}