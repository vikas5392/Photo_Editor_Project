package com.training.sticker

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.opengl.Visibility
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    lateinit var card_view: CardView
    lateinit var getImageUri: ActivityResultLauncher<String>
    lateinit var m_image: ImageView
    lateinit var sticker_view: StickerView
    lateinit var textview:TextView

    companion object {
        val READ_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_image = findViewById(R.id.imageView)
        sticker_view = findViewById(R.id.stickerView)
        card_view = findViewById(R.id.card_view)
        textview=findViewById(R.id.textView2)

        getImageUri = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri -> m_image.setImageURI(uri) })
        textview.visibility=View.INVISIBLE
    }
    // onclick to display text over the photo
    fun addText(view:View){
        textview.visibility=View.VISIBLE
    }

    fun save(view: View) {
        // write permission to access the storage
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            1
        )
        val bitmap = getScreenshotFromView(card_view)
        //if bitmap is not null than we save it to gallery
        if (bitmap != null) {
            saveMediaToStorage(bitmap)
        }
    }

    fun getPhoto(view: View) {
        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )) == (PackageManager.PERMISSION_GRANTED)
        ) {
            performActivity()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_PERMISSION_CODE
            )
        }
    }

    fun getScreenshotFromView(v: View): Bitmap? {
//create a bitmap object
        var screenshot_bitmap: Bitmap? =
            Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = screenshot_bitmap?.let { Canvas(it) }
        v.draw(canvas)
        return screenshot_bitmap

    }

    fun saveMediaToStorage(bitmap: Bitmap) {
        // Generating a file name
        val filename = "${System.currentTimeMillis()}.jpg"

        // Output stream
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //get the content resolver, gere i have resolver=getcontentresolver()
            this.contentResolver?.also { resolver ->
                // Content resolver will process the contentvalues
                val contentValues = ContentValues().apply {

                    // putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                //now we add this content value to the content resolver so that it can handle them, and getting the uri of the image that is to be saved
                val image_uri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                // open a stream to the image_uri
                fos = image_uri?.let { resolver.openOutputStream(it) }
            }
        } else {
            // These for devices running on android < Q
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            // Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Captured View and saved to Gallery", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PERMISSION_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                performActivity()
            }
        }
    }

    fun performActivity() {
        getImageUri.launch("image/*")
    }
}