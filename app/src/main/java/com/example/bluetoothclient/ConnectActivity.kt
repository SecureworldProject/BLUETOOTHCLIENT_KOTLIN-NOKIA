package com.example.bluetoothclient


import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

@SuppressLint("MissingPermission")
class ConnectActivity : AppCompatActivity() {

    // Constants
    private val REQUEST_CODE_CAPTURE_PHOTO = 1
    private val REQUEST_CODE_BLUETOOTH = 2
    private val REQUEST_CODE_BLUETOOTH_CONNECT = 3
    private val REQUEST_CODE_RECORD_AUDIO = 4

    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var selectedDevice: BluetoothDevice? = null
    private var macAddress: String? = null

    private var myConnection: Connection? = null

    private lateinit var configBtn: Button
    private lateinit var connectBtn: Button
    private lateinit var imageBtn: Button
    private lateinit var videoBtn: Button
    private lateinit var audioBtn: Button
    private lateinit var textBtn: Button
    private lateinit var geoBtn: Button
    private lateinit var envBtn: Button

    private lateinit var myView: ScrollView

    private lateinit var selectedImageUri: Uri
    private lateinit var selectedVideoUri: Uri
    private lateinit var selectedAudioUri: Uri

    override fun onCreate(savedInstancestate: Bundle?) {
        super.onCreate(savedInstancestate)
        setContentView(R.layout.activity_connect)

        configBtn = findViewById(R.id.configBtn)
        connectBtn = findViewById(R.id.connectBtn)
        imageBtn = findViewById(R.id.imageBtn)
        videoBtn = findViewById(R.id.videoBtn)
        audioBtn = findViewById(R.id.audioBtn)
        textBtn = findViewById(R.id.textBtn)
        geoBtn = findViewById(R.id.geoBtn)
        envBtn = findViewById(R.id.envBtn)

        myView = findViewById(R.id.buttonsSv)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.i(TAG, "Compatible")
            bluetoothManager = getSystemService(BluetoothManager::class.java)
            bluetoothAdapter = bluetoothManager.adapter

            //Checks if device supports Bluetooth
            if (bluetoothAdapter == null) {
                //Device doesn't support Bluetooth
                Toast.makeText(this, "Device doesn't support Bluetooth ", Toast.LENGTH_SHORT).show()
            }
            else{
                configBtn.setOnClickListener{
                    val intentConfig = Intent(this, ConfigActivity::class.java)
                    intentConfigLauncher.launch(intentConfig)
                }

                connectBtn.setOnClickListener {
                    val neededPerms = arrayListOf(android.Manifest.permission.BLUETOOTH_CONNECT)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH)) {
                        Toast.makeText(this, "Not enough permissions...", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.i(TAG, "Mac address is: $macAddress")
                        if(macAddress != null){
                            myConnection = Connection(selectedDevice)
                            try{
                                myConnection!!.startConnection()
                            }
                            catch(noServerException: Exception ){
                                Toast.makeText(this, "Error connecting to ${selectedDevice?.name}", Toast.LENGTH_SHORT).show()
                                Log.e(TAG, "No bluetooth server")
                            }
                            if (myConnection!!.socket?.isConnected == true){
                                printGreen()
                            }
                        }
                        else{
                            Toast.makeText(this, "Not mac address", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                imageBtn.setOnClickListener {
                    val neededPerms = arrayListOf(android.Manifest.permission.CAMERA)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_CAPTURE_PHOTO)) {
                        Toast.makeText(this, "Not enough permissions...", Toast.LENGTH_SHORT).show()
                    } else if (myConnection != null){
                        selectImage()
                    }
                    else{
                            Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                videoBtn.setOnClickListener {
                    val neededPerms = arrayListOf(android.Manifest.permission.CAMERA)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_CAPTURE_PHOTO)) {
                        Toast.makeText(this, "Not enough permissions...", Toast.LENGTH_SHORT).show()
                    } else if (myConnection != null){
                        selectVideo()
                    }
                    else{
                        Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                    }
                }

                audioBtn.setOnClickListener {
                    val neededPerms = arrayListOf(android.Manifest.permission.RECORD_AUDIO)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_RECORD_AUDIO)) {
                        Toast.makeText(this, "Not enough permissions...", Toast.LENGTH_SHORT).show()
                    } else if (myConnection != null){
                        selectAudio()
                    }
                    else{
                        Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                    }
                }

                textBtn.setOnClickListener {
                    val neededPerms = arrayListOf(android.Manifest.permission.RECORD_AUDIO)
                    if (!ensureAppPermissions(neededPerms, REQUEST_CODE_RECORD_AUDIO)) {
                        Toast.makeText(this, "Not enough permissions...", Toast.LENGTH_SHORT).show()
                    } else if (myConnection != null){
                        myConnection?.sendMessage("Hello World")
                        myConnection?.endConnection()
                    }
                    else{
                        Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                    }
                }

                geoBtn.setOnClickListener{
                    if (myConnection != null) {
                        val geoIntent = Intent(this, GeolocationActivity::class.java)
                        intentGeoLauncher.launch(geoIntent)
                    }
                    else{
                        Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                    }
                }


            envBtn.setOnClickListener{
                if (myConnection != null) {
                    val envIntent = Intent(this, EnvironmentActivity::class.java)
                    intentEnvLauncher.launch(envIntent)
                }
                else{
                    Toast.makeText(this, "Connect to a device first", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            Log.d(TAG, "Not compatible")
        }
    }

    private fun printGreen(){
        imageBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
        videoBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
        audioBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
        textBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
        geoBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
        envBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
    }

    private fun printRed(){
        imageBtn.setBackgroundColor(Color.parseColor("#B82217"))
        videoBtn.setBackgroundColor(Color.parseColor("#B82217"))
        audioBtn.setBackgroundColor(Color.parseColor("#B82217"))
        textBtn.setBackgroundColor(Color.parseColor("#B82217"))
        geoBtn.setBackgroundColor(Color.parseColor("#B82217"))
        envBtn.setBackgroundColor(Color.parseColor("#B82217"))
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val column = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if(column >= 0 ){
                        result = cursor.getString(column)
                    }
                    else{
                        result = null
                        Log.e(TAG, "Column does not exist")
                    }
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }

    /*private var intentJsonLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val json: String = data.extras?.getString("json").toString()
                myConnection?.sendMessage("\u000C")
                myConnection?.sendMessage("capture.json")
                myConnection?.sendMessage(json)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error receiving json", Toast.LENGTH_SHORT).show()
            }
        }*/

    private var intentGeoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val json: String = data.extras?.getString("json").toString()
                val filename = "capture.gps"
                myConnection?.sendMessage("\u000B")
                myConnection?.sendMessage(filename)
                myConnection?.sendMessage(json) //sending the file
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error receiving json", Toast.LENGTH_SHORT).show()
            }
        }

    private var intentEnvLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                val json: String = data.extras?.getString("json").toString()
                val filename = "capture.env"
                myConnection?.sendMessage("\u000B")
                myConnection?.sendMessage(filename)
                myConnection?.sendMessage(json)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error receiving json", Toast.LENGTH_SHORT).show()
            }
        }


    private var intentConfigLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val neededPerms = arrayListOf(android.Manifest.permission.BLUETOOTH_CONNECT)
            if (!ensureAppPermissions(neededPerms, REQUEST_CODE_BLUETOOTH_CONNECT)) {
                val data: Intent? = result.data
                if (result.resultCode == RESULT_OK && data != null) {
                    macAddress = data.extras?.getString("MacAddress").toString()
                    selectedDevice = bluetoothAdapter!!.getRemoteDevice(macAddress)
                    //Toast.makeText(this, "You selected: ${selectedDevice?.name}", Toast.LENGTH_SHORT).show()
                    connectBtn.text = getString(R.string.connect_to_device,selectedDevice?.name)
                    connectBtn.setBackgroundColor(Color.parseColor("#FF4CAF50"))
                }
                else if (result.resultCode == RESULT_CANCELED) {
                    Log.e(TAG, "$macAddress")
                    Toast.makeText(this, "Error receiving mac address", Toast.LENGTH_SHORT).show()
                }
            }
            else Log.e(TAG, "Bluetooth permission not granted")
        }


    private var intentPictureGalleyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedImageUri = data.data!!
                val filename = getFileName(selectedImageUri)
                Log.d(TAG, "Uri: $selectedImageUri")
                val inputData: ByteArray? = getBytes(this,selectedImageUri)
                myConnection?.sendFile(filename,inputData)

                /*val path = data.data!!.path
                val rawTakenImage = BitmapFactory.decodeFile(path)
                val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(rawTakenImage, 500, 500, false)
                val bytes = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 40, bytes)
                myConnection?.sendBitmap(filename,bytes)*/
                Toast.makeText(this, "Image sent", Toast.LENGTH_SHORT).show()
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error selecting image", Toast.LENGTH_SHORT).show()
            }
        }
    private var intentCapturePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedImageUri = data.extras?.get("data") as Uri
                Log.d(TAG, "Uri: $selectedImageUri")
                val filename = getFileName(selectedImageUri)
                Log.d(TAG, "Uri: $selectedImageUri")
                val inputData: ByteArray? = getBytes(this,selectedImageUri)
                myConnection?.sendFile(filename,inputData)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show()
            }
        }
    private fun selectImage() {
        val choice = arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")
        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        myAlertDialog.setCancelable(false)
        myAlertDialog.setTitle("Select Image")
        myAlertDialog.setItems(choice) { _, item ->
            when {
                //Select "Choose from Gallery" to pick image from gallery
                choice[item] == "Choose from Gallery" -> {
                    val pictureFromGallery = Intent(
                        Intent.ACTION_GET_CONTENT,
                    )
                    pictureFromGallery.addCategory(Intent.CATEGORY_OPENABLE)
                    pictureFromGallery.type = "image/*"
                    pictureFromGallery.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    pictureFromGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intentPictureGalleyLauncher.launch(pictureFromGallery)
                }
                //Select "Take Photo" to take a photo
                choice[item] == "Take Photo" -> {
                    val cameraPicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    intentCapturePhotoLauncher.launch(cameraPicture)
                }
                //Select "Cancel" to cancel the task
                choice[item] == "Cancel" -> {
                    myAlertDialog.create().dismiss()
                }
            }
        }
        myAlertDialog.show()
    }

    private var intentVideoGalleyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedVideoUri = data.data!!
                val filename = getFileName(selectedVideoUri)
                val inputData: ByteArray? = getBytes(this,selectedVideoUri)
                myConnection?.sendFile(filename,inputData)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error selecting video", Toast.LENGTH_SHORT).show()
            }
        }
    private var intentCaptureVideoLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedVideoUri = data.extras?.get("data") as Uri
                val filename = getFileName(selectedVideoUri)
                val inputData: ByteArray? = getBytes(this,selectedVideoUri)
                myConnection?.sendFile(filename,inputData)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error capturing image", Toast.LENGTH_SHORT).show()
            }
        }
    private fun selectVideo() {
        val choice = arrayOf<CharSequence>("Take Video", "Choose from Gallery", "Cancel")
        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        myAlertDialog.setCancelable(false)
        myAlertDialog.setTitle("Select Video")
        myAlertDialog.setItems(choice) { _, item ->
            when {
                //Select "Choose from Gallery" to pick image from gallery
                choice[item] == "Choose from Gallery" -> {
                    val videoFromGallery = Intent(
                        Intent.ACTION_GET_CONTENT,
                    )
                    //change to pickFromVideoGallery
                    videoFromGallery.addCategory(Intent.CATEGORY_OPENABLE)
                    videoFromGallery.type = "video/*"
                    intentVideoGalleyLauncher.launch(videoFromGallery)
                }
                //Select "Take Video" to take a photo
                choice[item] == "Take Video" -> {
                    val cameraVideo = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    cameraVideo.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 5)
                    intentCaptureVideoLauncher.launch(cameraVideo)
                }
                //Select "Cancel" to cancel the task
                choice[item] == "Cancel" -> {
                    myAlertDialog.create().dismiss()
                }
            }
        }
        myAlertDialog.show()
    }

    private var intentAudioGalleyLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedAudioUri = data.data!!
                val filename = getFileName(selectedAudioUri)
                val inputData: ByteArray? = getBytes(this,selectedAudioUri)
                myConnection?.sendFile(filename,inputData)
                myConnection?.endConnection()
                myConnection = null
                printRed()
            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error selecting audio", Toast.LENGTH_SHORT).show()
            }
        }
    private var intentCaptureAudioLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data: Intent? = result.data
            if (result.resultCode == RESULT_OK && data != null) {
                selectedAudioUri = data.data!!

            }
            else if (result.resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Error capturing audio", Toast.LENGTH_SHORT).show()
            }
        }
    private fun selectAudio() {
        val choice = arrayOf<CharSequence>("Record audio", "Choose from Gallery", "Cancel")
        val myAlertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
        myAlertDialog.setCancelable(false)
        myAlertDialog.setTitle("Select audio")
        myAlertDialog.setItems(choice) { _, item ->
            when {
                //Select "Choose from Gallery" to pick audio from gallery
                choice[item] == "Choose from Gallery" -> {
                    val audioFromGallery = Intent(
                        Intent.ACTION_GET_CONTENT,
                    )
                    audioFromGallery.addCategory(Intent.CATEGORY_OPENABLE)
                    audioFromGallery.type = "audio/*"
                    audioFromGallery.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    audioFromGallery.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intentAudioGalleyLauncher.launch(audioFromGallery)
                }
                //Select "Record audio" to record an audio
                choice[item] == "Record audio" -> {
                    val recordAudio = Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
                    if (recordAudio.resolveActivity(packageManager) != null) {
                        intentCaptureAudioLauncher.launch(recordAudio)
                    }
                    //device doesn't have any application which allows you to record an audio
                    else Toast.makeText(this, "Error recording audio", Toast.LENGTH_SHORT).show()
                }
                //Select "Cancel" to cancel the task
                choice[item] == "Cancel" -> {
                    myAlertDialog.create().dismiss()
                }
            }
        }
        myAlertDialog.show()
    }

    private fun getBytes(context: Context, uri: Uri): ByteArray? =
        context.contentResolver.openInputStream(uri)?.buffered().use {it?.readBytes()}

    @SuppressLint("MissingPermission")
    class Connection(device: BluetoothDevice?) {

        val myUuid: UUID = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee")

        var myDevice =  device
        var socket= myDevice?.createRfcommSocketToServiceRecord(myUuid)

        fun startConnection() {
            this.socket?.connect()
        }

        fun sendMessage(message: String){
            Log.i(TAG, "Client: Sending")
            val outputStream = this.socket?.outputStream
            try {
                outputStream?.write(message.toByteArray())
                outputStream?.flush()
                Log.i(TAG, "Client: Sent")
            } catch(e: Exception) {
                Log.e(TAG, "Client: Cannot send", e)
            }
        }

        /*fun sendBitmap(filename: String, bitmap: ByteArrayOutputStream){
            Log.i(TAG, "Client: Sending")
            val outputStream = this.socket?.outputStream
            try {
                outputStream?.write(bitmap.toByteArray())
                Log.i(TAG, "Filepath: $bitmap")
                outputStream?.flush()
                Log.i(TAG, "Client: Sent")
            } catch(e: Exception) {
                Log.e(TAG, "Client: Cannot send", e)
            }
        }*/

        fun sendFile(filename: String, inputData: ByteArray?){
            Log.i(TAG, "Client: Sending")
            val outputStream = this.socket?.outputStream
            try {
                val filenameLength = ByteArray(1)
                filenameLength[0] = filename.length.toByte()
                outputStream?.write(filenameLength)
                outputStream?.write(filename.toByteArray())
                outputStream?.write(inputData)
                Log.i(TAG, "Filename: $filename")
                Log.i(TAG, "Bytes: $inputData")
                outputStream?.flush()
                //outputStream?.close()
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //var allPermsGranted = false
        var allPermsGranted: Boolean

        when (requestCode){
            /*REQUEST_CODE_BLUETOOTH -> {
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
                    Toast.makeText(this, "Bluetooth permission not granted", Toast.LENGTH_SHORT).show()
                }
            }*/
            REQUEST_CODE_BLUETOOTH_CONNECT -> {
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
            REQUEST_CODE_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Audio permission Granted", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_CODE_CAPTURE_PHOTO-> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission Granted", Toast.LENGTH_SHORT).show()
                }
                else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }


            else -> {
                // What to do when any other permission was requested
            }
        }
    }

}
