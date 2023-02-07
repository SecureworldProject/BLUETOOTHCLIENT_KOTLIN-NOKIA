package com.example.bluetoothclient

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*


const val TAG: String = "secureworld"   // tag to use in the log

class ConfigActivity : AppCompatActivity() {

    // Constants
    private val REQUEST_CODE_BLUETOOTH = 3

    //Widgets
    private lateinit var pairedTV: TextView
    private lateinit var discoverableBtn: Button
    private lateinit var turnOnBtn: Button
    private lateinit var turnOffBtn: Button
    private lateinit var pairedBtn: Button
    private lateinit var indexDeviceT: TextInputEditText
    private lateinit var inputLayout: TextInputLayout

    lateinit var bluetoothManager: BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = null
    var index: Int = -1
    private lateinit var myDevice: BluetoothDevice
    var pairedDevices: Set<BluetoothDevice>? = null
    private lateinit var pairedDevicesNotEmpty: Set<BluetoothDevice>
    private lateinit var selectedImageUri: Uri

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
                val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                    println("Cannot turn off due to not enough permissions...")
                    //return
                } else {
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
            }

            turnOnBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == false) {
                    val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                        println("Cannot turn off due to not enough permissions...")
                        //return
                    } else {
                        enableBt()
                    }
                } else {
                    Toast.makeText(this, "Bluetooth already on", Toast.LENGTH_SHORT).show()
                }
            }

            turnOffBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                        println("Cannot turn off due to not enough permissions...")
                        //return
                    } else {
                        disableBt()
                    }
                } else {
                    Toast.makeText(this, "Bluetooth already off", Toast.LENGTH_SHORT).show()
                }
            }

            pairedBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                        println("Cannot list devices due to not enough permissions...")
                        //return
                    } else {
                        listBtDevices()
                    }
                } else {
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }

            discoverableBtn.setOnClickListener {
                if (bluetoothAdapter?.isEnabled == true) {
                    val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                        println("Cannot list devices due to not enough permissions...")
                        //return
                    } else {
                        discoverableBt()
                    }
                } else {
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            return
        }
    }
    var intentBtEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            }
            if (result.resultCode == AppCompatActivity.RESULT_CANCELED) {
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

    var intentBtDiscoverabilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_CANCELED) {
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

    private fun ensureAppPermissions(neededPerms: ArrayList<String>, requestCode: Int): Boolean {
        val permsToRequest = arrayListOf<String>()

        // Check already satisfied permissions
        for (p in neededPerms) {
            if (p != "" && ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED){
                permsToRequest.add(p)
            }
        }

        // Request all the non-satisfied permissions at once
        if (permsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permsToRequest.toTypedArray(),
                requestCode
            )
            return false
        }

        // After asking the user for permissions, check again and return if they are granted now (does not work because permission requesting is asynchronous)
        /*for (p in neededPerms) {
            if (p != "" && ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED){
                return false
            }
        }*/
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // TODO: super call was commented in an example (check why)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var allPermsGranted: Boolean

        when (requestCode){
            REQUEST_CODE_BLUETOOTH -> {
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
                    Log.i(TAG, "Bluetooth permission not granted")
                    Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                // What to do when any other permission was requested
            }
        }
    }
}
