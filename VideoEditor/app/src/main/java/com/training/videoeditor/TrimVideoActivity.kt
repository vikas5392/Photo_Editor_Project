package com.training.videoeditor

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config.*
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import com.training.videoeditor.FileUtils.getFileFromUri
import org.florescu.android.rangeseekbar.RangeSeekBar
import java.io.File


class TrimVideoActivity : AppCompatActivity() {
    lateinit var videoView:VideoView
    lateinit var i: Intent
    lateinit var video_url:Uri
    lateinit var buttonPlayPause: ImageButton
    lateinit var leftText:TextView
    lateinit var rightText:TextView
    lateinit var rangeSeekBar:RangeSeekBar<Number>
    var isPlaying=false
    var duration:Int = 0
    var filePrefix:String?=null
    lateinit var dest:File
    var original_path:String?=null
    var filePath:String?=null
    lateinit var command:Array<String?>
    lateinit var trim_button:Button
    lateinit var r:Runnable
   var uri_ofFileCreated: Uri?=null
    val root:String=Environment.getExternalStorageDirectory().toString()
    val app_folder:String="$root/Trimmed/"
    lateinit var videoPath:String
    lateinit var   progressDialog: ProgressDialog
    lateinit var compressButton:Button
    lateinit var slowButton:Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim_video)
        videoView=findViewById(R.id.videoView)
        buttonPlayPause=findViewById(R.id.buttonPlay_Pause)
        leftText=findViewById(R.id.leftText)
        rightText=findViewById(R.id.rightText)
        rangeSeekBar=findViewById(R.id.rangeSeekBar)
        trim_button=findViewById(R.id.trim_button)
        compressButton=findViewById(R.id.compress_button)
        slowButton=findViewById(R.id.slow_button)


        // creating the progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)


        i=intent
        videoPath= i.getStringExtra("videoUri")!!
        video_url= Uri.parse(videoPath)
        videoView.setVideoURI(video_url)
        videoView.start()
        isPlaying=true

        trim_button.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"

                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"
                if (videoPath != null) {
                    // a try-catch block to handle all necessary exceptions
                    // like File not found, IOException
                    try {
                        trimVideo(
                            rangeSeekBar.selectedMinValue.toInt() * 1000,
                            rangeSeekBar.selectedMaxValue.toInt() * 1000
                        )
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })
        // to compress the video
        compressButton.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"

                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"
                if (videoPath != null) {
                    // a try-catch block to handle all necessary exceptions
                    // like File not found, IOException
                    try {
                        compressVideo(
                            rangeSeekBar.selectedMinValue.toInt() * 1000,
                            rangeSeekBar.selectedMaxValue.toInt() * 1000
                        )
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })
        // to get video in slow motion

        slowButton.setOnClickListener(object:View.OnClickListener{
            override fun onClick(v: View?) {
                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"

                // check if the user has selected any video or not
                // In case a user hasn't selected any video and press the button,
                // we will show an warning, stating "Please upload the video"
                if (videoPath != null) {
                    // a try-catch block to handle all necessary exceptions
                    // like File not found, IOException
                    try {
                        slowVideo(
                            rangeSeekBar.selectedMinValue.toInt() * 1000,
                            rangeSeekBar.selectedMaxValue.toInt() * 1000
                        )
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })

        // set up the VideoView.
        // We will be using VideoView to view our video

        // set up the VideoView.
        // We will be using VideoView to view our video
        videoView.setOnPreparedListener { mp -> // get the duration of the video
            val duration = mp.duration / 1000

            // initially set the left TextView to "00:00:00"
            leftText.setText("00:00:00")

            // initially set the right Text-View to the video length
            // the getTime() method returns a formatted string in hh:mm:ss
            rightText.setText(getTime(mp.duration / 1000))

            // this will run he video in loop
            // i.e. the video won't stop
            // when it reaches its duration
            mp.isLooping = true

            // set up the initial values of rangeSeekbar
            rangeSeekBar.setRangeValues(0, duration)
            rangeSeekBar.setSelectedMinValue(0)
            rangeSeekBar.setSelectedMaxValue(duration)
            rangeSeekBar.isEnabled = true
            rangeSeekBar.setOnRangeSeekBarChangeListener({ bar, minValue, maxValue ->
                // we seek through the video when the user
                // drags and adjusts the seekbar
                videoView.seekTo(minValue as Int * 1000)

                // changing the left and right TextView according to
                // the minValue and maxValue
                leftText.setText(getTime(bar.selectedMinValue as Int))
                rightText.setText(getTime(bar.selectedMaxValue as Int))
            })

            // this method changes the right TextView every 1 second
            // as the video is being played
            // It works same as a time counter we see in any Video Player
            val handler = Handler()
            handler.postDelayed(Runnable {
                if (videoView.currentPosition >= rangeSeekBar.selectedMaxValue.toInt() * 1000) videoView.seekTo(
                    rangeSeekBar.selectedMinValue.toInt() * 1000
                )
                handler.postDelayed(r, 1000)
            }.also { r = it }, 1000)
        }
    }

    //function to trim the video
    fun trimVideo(startMs:Int,endMs:Int){

        progressDialog.show()
    // create a new file in the storage to store the trimmed video
        val filePath:String
        val filePrefix="trimmedVideo"
        val fileExtn=".mp4"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // With introduction of scoped storage in Android Q the primitive method gives error
            // So, it is recommended to use the below method to create a video file in storage.

                val valueVideos =ContentValues().apply {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder")
                    put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix + System.currentTimeMillis() + fileExtn)
                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                    put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                   uri_ofFileCreated = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, this)
                }
            val file: File? = uri_ofFileCreated?.let { getFileFromUri(this, it) }
            filePath = file!!.absolutePath

        } else {
            var dest = File(File(app_folder), filePrefix + fileExtn)
            var fileNo = 0
            while (dest.exists()) {
                fileNo++
                dest = File(File(app_folder), filePrefix + fileNo + fileExtn)
            }
            filePath = dest.absolutePath
        }
        // the "exe" string contains the command to process video.The details of command are discussed later in this post.

        var start=startMs/1000
        var end=endMs/1000
        var exe:String="ffmpeg -ss $start -i $videoPath -c:v libX264 -preset ultrafast -crf 22 -to $end -c copy -copyts $filePath"


           /* "-ss"+ "" + startMs / 1000+ "-y"+ "-i"+ videoPath+"-c:v libx264"+" -preset"+" ultrafast"+"-crf28"+"-filter:v"+ "-t"+ "" + (endMs - startMs) / 1000+
        "-s"+ "320x240"+ "-r"+ "15"+ "-vcodec"+ "mpeg4"+ "-b:v"+ "2097152"+ "-b:a"+ "48000"+ "-ac"+ "2"+ "-ar"+ "22050"+ filePath*/



       val executionId:Long = FFmpeg . executeAsync (exe, object : ExecuteCallback {
            override fun apply(executionId: Long, returnCode: Int) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    videoView.setVideoURI(Uri.parse(filePath))
                    videoPath = filePath
                    videoView.start()
                    // remove the progress dialog
                    // remove the progress dialog
                    progressDialog.dismiss()

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(TAG, "Async command execution cancelled by user.")
                } else {
                    Log.i(
                        TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }
        })
    }
    //function to trim the video
    fun compressVideo(startMs:Int,endMs:Int){

        progressDialog.show()
        // create a new file in the storage to store the trimmed video
        val filePath:String
        val filePrefix="comressedVideo"
        val fileExtn=".mp4"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // With introduction of scoped storage in Android Q the primitive method gives error
            // So, it is recommended to use the below method to create a video file in storage.

            val valueVideos =ContentValues().run {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder")
                put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix + System.currentTimeMillis() + fileExtn)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                uri_ofFileCreated = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, this)
            }
            val file: File? = uri_ofFileCreated?.let { getFileFromUri(this, it) }
            filePath = file!!.absolutePath

        } else {
            var dest = File(File(app_folder), filePrefix + fileExtn)
            var fileNo = 0
            while (dest.exists()) {
                fileNo++
                dest = File(File(app_folder), filePrefix + fileNo + fileExtn)
            }
            filePath = dest.absolutePath
        }
        // the "exe" string contains the command to process video.The details of command are discussed later in this post.
        var exe:String= "ffmpeg -i $videoPath -vcodec h264 -acodec mp3 $filePath"

        val executionId:Long = FFmpeg . executeAsync (exe, object : ExecuteCallback {
            override fun apply(executionId: Long, returnCode: Int) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    videoView.setVideoURI(Uri.parse(filePath))
                    videoPath = filePath
                    videoView.start()
                    // remove the progress dialog
                    // remove the progress dialog
                    progressDialog.dismiss()

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(TAG, "Async command execution cancelled by user.")
                } else {
                    Log.i(
                        TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }
        })
    }

    // function to get slow motion

    //function to trim the video
    fun slowVideo(startMs:Int,endMs:Int){

        progressDialog.show()
        // create a new file in the storage to store the trimmed video
        val filePath:String
        val filePrefix="slowedVideo"
        val fileExtn=".mp4"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // With introduction of scoped storage in Android Q the primitive method gives error
            // So, it is recommended to use the below method to create a video file in storage.

            val valueVideos =ContentValues().run {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/" + "Folder")
                put(MediaStore.Video.Media.DISPLAY_NAME, filePrefix + System.currentTimeMillis() + fileExtn)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis())
                uri_ofFileCreated = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, this)
            }
            val file: File? = uri_ofFileCreated?.let { getFileFromUri(this, it) }
            filePath = file!!.absolutePath

        } else {
            var dest = File(File(app_folder), filePrefix + fileExtn)
            var fileNo = 0
            while (dest.exists()) {
                fileNo++
                dest = File(File(app_folder), filePrefix + fileNo + fileExtn)
            }
            filePath = dest.absolutePath
        }
        // the "exe" string contains the command to process video.The details of command are discussed later in this post.

        var start=startMs/1000
        var end=endMs/1000
        var exe:String=
            "-y -i " + video_url + " -filter_complex [0:v]trim=0:" + startMs / 1000 + ",setpts=PTS-STARTPTS[v1];[0:v]trim=" + startMs / 1000 + ":" +
                    endMs / 1000 + ",setpts=2*(PTS-STARTPTS)[v2];[0:v]trim=" + endMs / 1000 + ",setpts=PTS-STARTPTS[v3];[0:a]atrim=0:" +
                    startMs / 1000 + ",asetpts=PTS-STARTPTS[a1];[0:a]atrim=" + startMs / 1000 + ":" + endMs / 1000 +
                    ",asetpts=PTS-STARTPTS,atempo=0.5[a2];[0:a]atrim=" + endMs / 1000 +
                    ",asetpts=PTS-STARTPTS[a3];[v1][a1][v2][a2][v3][a3]concat=n=3:v=1:a=1 " + "-b:v 2097k -vcodec mpeg4 -crf 0 -preset superfast " +
                    filePath

        val executionId:Long = FFmpeg . executeAsync (exe, object : ExecuteCallback {
            override fun apply(executionId: Long, returnCode: Int) {
                if (returnCode == RETURN_CODE_SUCCESS) {
                    videoView.setVideoURI(Uri.parse(filePath))
                    videoPath = filePath
                    videoView.start()
                    // remove the progress dialog

                    progressDialog.dismiss()

                } else if (returnCode == RETURN_CODE_CANCEL) {
                    Log.i(TAG, "Async command execution cancelled by user.")
                } else {
                    Log.i(
                        TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }
        })
    }
    fun play_pause(view: View){
        if(isPlaying){
          buttonPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            videoView.pause()
            isPlaying=false
        }
        else{
            videoView.start()
            buttonPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
            isPlaying=true
        }
    }

    private fun getTime(seconds: Int): String? {
        val hr = seconds / 3600
        val rem = seconds % 3600
        val mn = rem / 60
        val sec = rem % 60
        return String.format("%02d", hr) + ":" + String.format(
            "%02d",
            mn
        ) + ":" + String.format("%02d", sec)
    }








}
