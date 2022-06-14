package com.example.bluetoothclient

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import kotlin.reflect.typeOf

class CapturePhotoActivity : AppCompatActivity(), View.OnClickListener  {
    // Variables
    private var resultImageUri: Uri? = null

    // Widgets
    private lateinit var imgCapturedPhoto: ImageView
    private lateinit var btnCapture: Button
    private lateinit var btnFakeCapture : Button
    private lateinit var btnConfirmUpload : Button

    // Constants
    private val REQUEST_CODE_CAPTURE_PHOTO = 1
    private val REQUEST_CODE_FAKE_CAPTURE = 2

    private fun initializeWidgets() {
        imgCapturedPhoto = findViewById(R.id.imgCapturedPhoto)
        btnCapture = findViewById(R.id.btnCapture)
        btnFakeCapture = findViewById(R.id.btnFakeCapture)
        btnConfirmUpload = findViewById(R.id.btnConfirmUpload)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_capture_photo)

        // This together with "lateinit" allow accessing the widget variables everywhere in the code without having to findViewById() again
        initializeWidgets()

        // Set click listeners to this activity (which extends from View). Check this.onClick()
        imgCapturedPhoto.setOnClickListener(this)   // Not needed
        btnCapture.setOnClickListener(this)
        btnFakeCapture.setOnClickListener(this)
        btnConfirmUpload.setOnClickListener(this)

        /*btnCapture.setOnClickListener{capturePhoto()}
        btnFakeCapture.setOnClickListener{
            //check permission at runtime
            val checkSelfPermission = ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED){
                //Requests permissions to be granted to this application at runtime
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
            }
            else{
                openGallery()
            }
        }*/
    }

    override fun onClick(v: View) {
        when (v.id) {
            btnCapture.id -> {
                // Check camera permissions
                val neededPerms = arrayListOf<String>(android.Manifest.permission.CAMERA)
                if (!ensureAppPermissions(neededPerms, REQUEST_CODE_CAPTURE_PHOTO)){
                    println("Cannot enter capturePhoto() due to not enough permissions...")
                    return
                }

                // Launch camera to make a photo
                capturePhoto()
            }
            btnFakeCapture.id -> {
                // Show all possible resources for this capture type (images) and allow to select one of them
                fakeCapture()
            }
            imgCapturedPhoto.id -> {
                // Do nothing
                println("Clicked in the ImageView")
            }

            else -> {
                // Do nothing
                println("Unrecognized widget id")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun capturePhoto(){
        val almacenaje="externalFile" // externalcache , externalFile, internal, resource
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var capturedImage = File(dir, "Photo_capture.jpg")
        // Select a directory for the photo (options are: filesDir, externalCacheDir or getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        //var capturedImage = File(this.filesDir, "Photo_capture.jpg")

        // Overwrite the file if necessary
        if (capturedImage.exists()) {
            println("File already existed. Removing...")
            capturedImage.delete()
        }
        capturedImage.createNewFile()

        resultImageUri = if(Build.VERSION.SDK_INT >= 24){
            println("SDK version 24 or more. Getting URI with fileprovider...")
            FileProvider.getUriForFile(
                this,
                "com.example.challengecapturerapp.fileprovider", //BuildConfig.APPLICATION_ID + ".provider",
                capturedImage
            )
        } else {
            println("SDK version less than 24. Getting URI directly...")
            Uri.fromFile(capturedImage)
        }

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)    // MediaStore.ACTION_IMAGE_CAPTURE = "android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, resultImageUri)

        // Launch the activity and get called onActivityResult() when finished
        startActivityForResult(intent, REQUEST_CODE_CAPTURE_PHOTO)
    }

    private fun fakeCapture() {
        showToast("Not yet implemented")
        // TODO: Not yet implemented
    }

    // Checks the permissions and asks for them if they have not been granted. Returns true if all were granted.
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            REQUEST_CODE_CAPTURE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    imgCapturedPhoto.setImageURI(resultImageUri)
                } else {
                    showToast("Error retrieving photo from camera")
                }
            }
            REQUEST_CODE_FAKE_CAPTURE -> {
                showToast("Not implemented yet")
                TODO("Not implemented yet")
            }
            else -> {
                // Do nothing
                println("Unrecognized requestCode")
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // TODO: super call was commented in an example (check why)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var allPermsGranted = false

        when (requestCode){
            REQUEST_CODE_CAPTURE_PHOTO -> {
                allPermsGranted = true
                for (res in grantResults) {
                    if (res != PackageManager.PERMISSION_GRANTED){
                        allPermsGranted = false
                        break
                    }
                }
                if (allPermsGranted){
                    // Success (do nothing, the code is written so only if there is permission continues executing)
                    capturePhoto()
                } else {
                    showToast("You need camera permission to do a photo capture!")
                }
            }

            // TODO: check that if uses internal storage then no permission is needed
            REQUEST_CODE_FAKE_CAPTURE -> {
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
                    showToast("You need folder access permission use the fake data feature!")
                }
            }

            else -> {
                // What to do when any other permission was requested
            }
        }
    }

}

/*


    private fun openGallery(){
        val intent = Intent("android.intent.action.GET_CONTENT")    // Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        startActivityForResult(intent, OPERATION_CHOOSE_PHOTO)
    }
    private fun renderImage(imagePath: String?){
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imgCapturedPhoto?.setImageBitmap(bitmap)
        }
        else {
            show("ImagePath is null")
        }
    }
    private fun getImagePath(uri: Uri?, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null )
        if (cursor != null){
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }
    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)){
            val docId = DocumentsContract.getDocumentId(uri)
            if ("com.android.providers.media.documents" == uri.authority){
                val id = docId.split(":")[1]
                val selsetion = MediaStore.Images.Media._ID + "=" + id
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    selsetion)
            }
            else if ("com.android.providers.downloads.documents" == uri.authority){
                val contentUri = ContentUris.withAppendedId(Uri.parse(
                    "content://downloads/public_downloads"), java.lang.Long.valueOf(docId))
                imagePath = getImagePath(contentUri, null)
            }
        }
        else if ("content".equals(uri.scheme, ignoreCase = true)){
            imagePath = getImagePath(uri, null)
        }
        else if ("file".equals(uri.scheme, ignoreCase = true)){
            imagePath = uri.path
        }
        renderImage(imagePath)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>
                                            , grantedResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when(requestCode){
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED){
                    openGallery()
                }else {
                    show("Unfortunately You are Denied Permission to Perform this Operataion.")
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            OPERATION_CAPTURE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    val bitmap = BitmapFactory.decodeStream(
                        getContentResolver().openInputStream(resultImageUri))
                    imgCapturedPhoto!!.setImageBitmap(bitmap)
                }
            OPERATION_CHOOSE_PHOTO ->
                if (resultCode == Activity.RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= 19) {
                        handleImageOnKitkat(data)
                    }
                }
        }
    }


 */