package com.example.bluetoothclient

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.FusedLocationProviderClient
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

    val co2 = 50.0
    val humidity = 44.0
    val temperature = 21.0
    val pollution = 63.0
    val ozone = 200.0

    var enviroment = Enviroment(0)

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
            val json = Json.encodeToString(enviroment)
            val data = Intent()
            data.putExtra("json", json)
            setResult(RESULT_OK, data)
            finish()
        }

        updateBtn.setOnClickListener{
            getCurrentEnviroment()
            co2Data.text = enviroment.co2.toString()
            humidityData.text = enviroment.humidity.toString()
            temperatureData.text = enviroment.temperature.toString()
            pollutionData.text = enviroment.pollution.toString()
            ozoneData.text = enviroment.ozone.toString()
            Toast.makeText(this, "Enviroment updated", Toast.LENGTH_SHORT).show()
            updateBtn.setText(R.string.update)
        }
    }

    fun getCurrentEnviroment(){
        enviroment.co2 = co2
        enviroment.humidity = humidity
        enviroment.ozone = ozone
        enviroment.pollution = pollution
        enviroment.temperature = temperature
    }

    @Serializable
    data class Enviroment(var version: Int){

        var temperature: Double = 0.0
        var humidity: Double = 0.0
        var ozone: Double = 0.0
        var pollution: Double = 0.0
        var co2: Double = 0.0

    }


}