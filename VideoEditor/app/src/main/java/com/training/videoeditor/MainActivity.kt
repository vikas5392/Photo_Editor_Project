package com.training.videoeditor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    lateinit var getVideoUri:ActivityResultLauncher<String>
    lateinit var i:Intent
    val READ_PERMISSION_CODE=101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        i=Intent(this,TrimVideoActivity::class.java)
        getVideoUri=registerForActivityResult(ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri->
                run {
                    i.putExtra("videoUri", uri.toString())
                    startActivity(i)
                }
            })
    }
    fun  getVideo(view: View){
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
        getVideoUri.launch("video/*")
    }
}