package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val TAG: String = "AppDebug"

    private val IMAGE_CAPTURE_CODE: Int=1001;
    private val PERMISSION_CODE: Int =1000;
    var image_uri: Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun capture_btn(view: View) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_DENIED){
                // permission was not enable
                val permission= arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                //show popup to request permission
                requestPermissions(permission,PERMISSION_CODE)

            }else{
                //permission already granted
                openCamera()
            }
        }else{
            //Sysytem os is < marshmallow
            openCamera()

        }
    }

    private fun openCamera() {
        val value= ContentValues()
        value.put(MediaStore.Images.Media.TITLE,"New Picture")
        value.put(MediaStore.Images.Media.DESCRIPTION,"From the camera")
        image_uri=contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,value)
        //camera intent
        val cameraIntent= Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri)
        startActivityForResult(cameraIntent,IMAGE_CAPTURE_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    //permission for popup was granted
                    openCamera()
                }else{
                    //permission from popup was denied
                    Toast.makeText(this,"Permission denied ", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //called when image was captured form camera intent
        if(resultCode == Activity.RESULT_OK){

            image_uri?.let { launchImageCrop(it) }

        }
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    setImage(result.uri)
                    saveImageIntoGallery(result.uri)
                    Log.d(TAG, "onActivityResult: "+result.uri)
                }
                else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e(TAG, "Crop error: ${result.getError()}" )
                }
            }
        }

    }

    private fun setImage(uri: Uri){
        Glide.with(this)
            .load(uri)
            .into(image_view)
    }
    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(1080, 1200)
            .setCropShape(CropImageView.CropShape.OVAL) // default is
            .start(this)

    }

private  fun saveImageIntoGallery(uri: Uri){
    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)

    var outputStream: FileOutputStream? = null
    val file = Environment.getExternalStorageDirectory()
    val dir = File(file.absolutePath + "/MyPics")
    dir.mkdirs()
    val filename = String.format("%d.png", System.currentTimeMillis())
    val outFile = File(dir, filename)
    try {
        outputStream = FileOutputStream(outFile)
    } catch (e: Exception) {
        e.printStackTrace()
    }
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    try {
        outputStream!!.flush()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    try {
        outputStream!!.close()
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}
}