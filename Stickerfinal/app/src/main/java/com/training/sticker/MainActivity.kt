package com.training.sticker

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    lateinit var getImageUri: ActivityResultLauncher<String>
    lateinit var m_image: ImageView
    lateinit var sticker_view: StickerView

    companion object {
        val READ_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        m_image = findViewById(R.id.imageView)
        sticker_view = findViewById(R.id.stickerView)

        getImageUri = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri -> m_image.setImageURI(uri) })

        // on touchlistner to sticker_view


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