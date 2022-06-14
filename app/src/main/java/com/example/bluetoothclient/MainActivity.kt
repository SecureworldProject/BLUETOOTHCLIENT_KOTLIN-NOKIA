package com.example.bluetoothclient

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toFile
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.IOException
import java.net.Socket
import java.util.*
import kotlin.collections.ArrayList

const val TAG: String = "secureworld"   // tag to use in the log

class MainActivity : AppCompatActivity() {

    // Constants
    private val REQUEST_CODE_CAPTURE_PHOTO = 1
    private val REQUEST_CODE_FAKE_CAPTURE = 2
    private val REQUEST_CODE_BLUETOOTH = 3
    private val REQUEST_CODE_PICK_PHOTO = 4

    val my_uuid: UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

    //Widgets
    private lateinit var pairedTV: TextView
    private lateinit var discoverableBtn: Button
    private lateinit var turnOnBtn: Button
    private lateinit var turnOffBtn: Button
    private lateinit var testBtn: Button
    private lateinit var messageBtn: Button
    private lateinit var pairedBtn: Button
    private lateinit var indexDeviceT: TextInputEditText
    private lateinit var inputLayout: TextInputLayout
    private lateinit var imageview: ImageView

    lateinit var bluetoothManager: BluetoothManager
    var bluetoothAdapter: BluetoothAdapter? = null
    var index: Int = -1
    private lateinit var device: BluetoothDevice
    var pairedDevices: Set<BluetoothDevice>? = null
    private lateinit var pairedDevicesNotEmpty: Set<BluetoothDevice>
    private lateinit var picture: File
    private lateinit var uri: Uri
    private lateinit var myConnection: Connect

    override fun onCreate(savedInstancestate: Bundle?) {

        super.onCreate(savedInstancestate)
        setContentView(R.layout.activity_main)

        pairedTV = findViewById(R.id.pairedTv)
        discoverableBtn = findViewById(R.id.discoverableBtn)
        turnOnBtn = findViewById(R.id.turnOnBtn)
        turnOffBtn = findViewById(R.id.turnOffBtn)
        testBtn = findViewById(R.id.testBtn)
        messageBtn = findViewById(R.id.messageBtn)
        pairedBtn = findViewById(R.id.pairedBtn)
        indexDeviceT = findViewById(R.id.pairedTiet)
        inputLayout = findViewById(R.id.pairedTil)
        imageview = findViewById(R.id.imageView)

        //Creates BluetoothManager and BluetoothAdapter
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Log.d(TAG, "Compatible")
            bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter

            //Checks if device supports Bluetooth
            if (bluetoothAdapter == null) {
                //Device doesn't support Bluetooth
                Toast.makeText(this, "Device doesn't support Bluetooth ", Toast.LENGTH_SHORT).show()
            }

            inputLayout.setEndIconOnClickListener {
                listBtDevices()
                if(indexDeviceT.text.toString().isNotEmpty()){ //Checks device field is not empty
                    if(indexDeviceT.text.toString().toInt() <= pairedDevicesNotEmpty.size){ //Checks number is in the range of devices list
                        index = indexDeviceT.text.toString().toInt()
                        device = pairedDevicesNotEmpty.elementAt(index)
                        myConnection = Connect(device)
                        myConnection.startConnection()
                        Toast.makeText(this, "Connected to ${device}", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(this, "Number out of range", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(this, "Type a number", Toast.LENGTH_SHORT).show()
                }
            }

            inputLayout.setStartIconOnClickListener{
                if(myConnection.socket!=null){
                    myConnection.endConnection()
                }else Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
            }

            messageBtn.setOnClickListener{
                if(myConnection.socket!=null){
                    myConnection.sendMessage()
                    //Thread.sleep(1000)
                    //myConnection.sendMessage()
                }else Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
            }

            testBtn.setOnClickListener{
                openGalleryForImage()
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
                if (bluetoothAdapter?.isEnabled == true){
                    val neededPerms = arrayListOf<String>(android.Manifest.permission.BLUETOOTH)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)){
                        println("Cannot list devices due to not enough permissions...")
                        //return
                    }
                    else{
                        listBtDevices()
                    }
                }
                else{
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }

            discoverableBtn.setOnClickListener {
                if(bluetoothAdapter?.isEnabled==true){
                    val discoverableIntent: Intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                    intentBtDiscoverabilityLauncher.launch(discoverableIntent)
                }
                else{
                    Toast.makeText(this, "Turn on Bluetooth first", Toast.LENGTH_SHORT).show()
                }
            }
        }
        else{
            Log.d(TAG, "Not compatible")
        }

    }

    private fun enableBt() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        intentBtEnableLauncher.launch(enableBtIntent)
        Toast.makeText(this, "Bluetooth turned on", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun disableBt() {
        bluetoothAdapter?.disable()
        Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show()
    }

    private fun openGalleryForImage() {
        val intentOpenGallery = Intent(Intent.ACTION_PICK)
        intentOpenGallery.type = "image/*"
        intentOpenGalleyLauncher.launch(intentOpenGallery)
    }

    var intentOpenGalleyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                uri = data?.data!!
                //picture = uri.toFile.readBytes
                //Log.e(TAG, "Uri to picture file FAIL")
                imageview.setImageURI(uri)
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }


    var intentBtDiscoverabilityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not discoverable", Toast.LENGTH_SHORT).show()
                //handle cancel
            }
        }

    var intentBtEnableLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                //val contents = data?.getStringExtra("SCAN_RESULT")
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show()
            }
            if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
                //handle cancel
            }
        }

    @SuppressLint("MissingPermission")
    private fun listBtDevices(){
        pairedDevices = bluetoothAdapter?.bondedDevices
        if(pairedDevices?.isNotEmpty() == true){
            pairedDevicesNotEmpty = pairedDevices as MutableSet<BluetoothDevice>
            pairedTV.text = ""
            pairedDevicesNotEmpty.forEachIndexed {i, device ->
                val deviceName = device.name
                val deviceHardwareAddress = device.address // MAC address
                pairedTV.append("$i: $deviceName ($deviceHardwareAddress)\n")
                pairedTV.movementMethod = ScrollingMovementMethod()
                println("${deviceName},${deviceHardwareAddress}")
            }
        }
        else{
            pairedTV.text = "No paired devices"
            Toast.makeText(this, "No paired devices", Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("MissingPermission")
    private inner class Connect(device: BluetoothDevice?){

        var socket: BluetoothSocket? = null

        fun startConnection() {
           socket = device.createRfcommSocketToServiceRecord(my_uuid)
            Log.i(TAG, "Client: Connecting")
            this.socket?.connect()
        }

        fun sendMessage(){
            val message = "Hello world\n"

            Log.i(TAG, "Client: Sending")
            val outputStream = this.socket?.outputStream
            val inputStream = this.socket?.inputStream
            try {
                outputStream?.write(uri.toString().toByteArray())
                //outputStream?.write(picture.readBytes())
                outputStream?.flush()
                Log.i(TAG, "Client: Sent")
            } catch(e: Exception) {
                Log.e(TAG, "Client: Cannot send", e)
            }
        }
        // Closes the client socket and causes the thread to finish.
        fun endConnection() {
            try {
                this.socket?.close()
            } catch (e: IOException) {
                Log.e(TAG, "Client: Could not close the client socket", e)
            }
        }
    }

    private fun ensureAppPermissions(neededPerms: ArrayList<String>, requestCode: Int): Boolean {
        var permsToRequest = arrayListOf<String>()

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

        var allPermsGranted = false

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
                    listBtDevices()
                } else {
                    Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                }
            }

            else -> {
                // What to do when any other permission was requested
            }
        }
    }

}
