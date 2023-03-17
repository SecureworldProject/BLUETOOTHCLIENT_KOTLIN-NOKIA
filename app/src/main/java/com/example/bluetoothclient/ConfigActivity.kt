package com.example.bluetoothclient

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*


const val TAG: String = "secureworld"   // tag to use in the log

@Suppress("DEPRECATION")
class ConfigActivity : AppCompatActivity() {

    // Constants
    private val bluetoothPermissionRequestCode = 3
    private val bluetoothConnectPermissionRequestCode = 4

    //Widgets
    private lateinit var pairedTV: TextView
    private lateinit var discoverableBtn: Button
    private lateinit var turnOnBtn: Button
    private lateinit var turnOffBtn: Button
    private lateinit var pairedBtn: Button
    private lateinit var indexDeviceT: TextInputEditText
    private lateinit var inputLayout: TextInputLayout

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var index: Int = -1
    private lateinit var myDevice: BluetoothDevice
    private var pairedDevices: Set<BluetoothDevice>? = null
    private lateinit var pairedDevicesNotEmpty: Set<BluetoothDevice>

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstancestate: Bundle?) {
        super.onCreate(savedInstancestate)
        setContentView(R.layout.activity_config)

        pairedTV = findViewById(R.id.pairedTv)
        discoverableBtn = findViewById(R.id.discoverableBtn)
        turnOnBtn = findViewById(R.id.turnOnBtn)
        turnOffBtn = findViewById(R.id.turnOffBtn)
        pairedBtn = findViewById(R.id.pairedBtn)
        indexDeviceT = findViewById(R.id.pairedTiet)
        inputLayout = findViewById(R.id.pairedTil)

        checkBluetoothPermissions()
        //Creates BluetoothManager and BluetoothAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter

            //Checks if myDevice supports Bluetooth
            if (bluetoothAdapter == null) {
                //Device doesn't support Bluetooth
                Toast.makeText(this, "Device does not support Bluetooth ", Toast.LENGTH_SHORT).show()
            }

            inputLayout.setEndIconOnClickListener {
                listBtDevices()
                if (indexDeviceT.text.toString().isNotEmpty()) { //Checks myDevice field is not empty
                    if (indexDeviceT.text.toString()
                            .toInt() <= pairedDevicesNotEmpty.size
                    ) { //Checks number is in the range of devices list
                        index = indexDeviceT.text.toString().toInt()
                        myDevice = pairedDevicesNotEmpty.elementAt(index)
                        val intentSendDevice = Intent()
                        intentSendDevice.putExtra("MacAddress", myDevice.address)
                        setResult(RESULT_OK, intentSendDevice)
                        finish()
                    } else {
                        Toast.makeText(this, "Number out of range", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Type a number", Toast.LENGTH_SHORT).show()
                }
            }


            turnOnBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == false) {
                    enableBt()
                } else {
                    Toast.makeText(this, "Bluetooth already on", Toast.LENGTH_SHORT).show()
                }
            }

            turnOffBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    disableBt()
                } else {
                    Toast.makeText(this, "Bluetooth already off", Toast.LENGTH_SHORT).show()
                }
            }

            pairedBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    listBtDevices()
                } else {
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }

            discoverableBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    discoverableBt()
                } else {
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            return
        }
    }
    private var intentBtEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            }
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
                //handle cancel
            }
        }
    private fun enableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        intentBtEnableLauncher.launch(enableBtIntent)
        //Toast.makeText(this, "Bluetooth turned on", Toast.LENGTH_SHORT).show()
    }
    @SuppressLint("MissingPermission")
    private fun disableBt() {
        bluetoothAdapter?.disable()
        Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show()
    }

    private var intentBtDiscoverabilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not discoverable", Toast.LENGTH_SHORT).show()
                //handle cancel
            }
        }
    private fun discoverableBt(){
        val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
        }
        intentBtDiscoverabilityLauncher.launch(discoverableIntent)
    }

    @SuppressLint("MissingPermission")
    private fun listBtDevices(){
        pairedTV.text = ""
        pairedDevices = bluetoothAdapter?.bondedDevices
        if(pairedDevices?.isNotEmpty() == true){
            pairedDevicesNotEmpty = pairedDevices as MutableSet<BluetoothDevice>
            pairedDevicesNotEmpty.forEachIndexed {i, device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                pairedTV.append("$i - $deviceName ($deviceHardwareAddress)\n")
            }
        }
        else{
            pairedTV.setText(R.string.no_devices)
            Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBluetoothPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            // If the Android version is greater than 11.0 (Android 11)
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // If Bluetooth permissions have not been granted, we ask for BLUETOOTH_CONNECT permission
                requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH_CONNECT ), bluetoothConnectPermissionRequestCode)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            // If the Android version is greater than 6.0 (Marshmallow)
            if (checkSelfPermission(android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                // If Bluetooth permissions have not been granted, we ask for BLUETOOTH permission
                requestPermissions(arrayOf(android.Manifest.permission.BLUETOOTH), bluetoothPermissionRequestCode)
            }
        }
        else{
            // If the Android version is lower than 6.0 (Marshmallow)
            // Bluetooth permissions are not required
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            bluetoothPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted
                    Log.i(TAG, "Bluetooth permission granted")
                    Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Bluetooth permission denied
                    Log.i(TAG, "Bluetooth permission not granted")
                    Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
            bluetoothConnectPermissionRequestCode -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted
                    Log.i(TAG, "Bluetooth permission granted")
                    Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    // Bluetooth permission denied
                    Log.i(TAG, "Bluetooth permission not granted")
                    Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
