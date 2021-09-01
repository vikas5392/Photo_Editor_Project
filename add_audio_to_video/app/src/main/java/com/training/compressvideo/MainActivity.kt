package com.training.compressvideo

import android.Manifest
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.arthenica.mobileffmpeg.FFmpeg
import com.arthenica.mobileffmpeg.FFmpegExecution


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
    fun merge(view: View?) {
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
        val c = arrayOf(
            "-i",
            Environment.getExternalStorageDirectory().path
                    + "/Download/mp4.mp4",
            "-i",
            Environment.getExternalStorageDirectory().path
                    + "/Download/song.mp3",
            "-c:v",
            "copy",
            "-c:a",
            "aac",
            "-map",
            "0:v:0",
            "-map",
            "1:a:0",
            "-shortest",
            Environment.getExternalStorageDirectory().path
                    + "/Download/2Merge Video.mp4"
        )
        MergeVideo(c)
    }
    private fun MergeVideo(co: Array<String>) {
        FFmpeg.executeAsync(co) { executionId, returnCode ->
            Log.d("hello", "return  $returnCode")
            Log.d("hello", "executionID  $executionId")
            Log.d("hello", "FFMPEG  " + FFmpegExecution(executionId, co))
        }
    }
}