package com.training.videoeditor

import android.app.ProgressDialog
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.ExecuteCallback
import com.arthenica.mobileffmpeg.FFmpeg
import org.florescu.android.rangeseekbar.RangeSeekBar
import java.io.*


class TrimVideoActivity : AppCompatActivity() {
    lateinit var videoView: VideoView
    lateinit var i: Intent
    lateinit var video_url: Uri
    lateinit var buttonPlayPause: ImageButton
    lateinit var leftText: TextView
    lateinit var rightText: TextView
    lateinit var inputText:EditText
    lateinit var textButton:ImageButton
    lateinit var rangeSeekBar: RangeSeekBar<Number>
    var isPlaying = false
    var filePath: String? = null
    lateinit var trim_button: Button
    lateinit var r: Runnable
    lateinit var videoPath: String
    lateinit var progressDialog: ProgressDialog
    lateinit var compressButton: Button
    lateinit var slowButton: Button
    lateinit var overlayButton:Button
    lateinit var getPhotoUri: ActivityResultLauncher<String>
    lateinit var photoFromGalleryUri:Uri
    lateinit var dir:File
    lateinit var outFile:File
    lateinit var pathOfFontDir:String
    var textInput:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trim_video)
        videoView = findViewById(R.id.videoView)
        buttonPlayPause = findViewById(R.id.buttonPlay_Pause)
        leftText = findViewById(R.id.leftText)
        rightText = findViewById(R.id.rightText)
        rangeSeekBar = findViewById(R.id.rangeSeekBar)
        trim_button = findViewById(R.id.trim_button)
        compressButton = findViewById(R.id.compress_button)
        slowButton = findViewById(R.id.slow_button)
        overlayButton=findViewById(R.id.overlayButton)

        inputText=findViewById(R.id.inputText)
        textButton=findViewById(R.id.textButton)

        // initially the inputText and TextButton are invisible
        inputText.visibility=View.INVISIBLE
        textButton.visibility=View.INVISIBLE

        // creating the progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Please wait..")
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)

        // to get image to be added in the video from gallery
        getPhotoUri=registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri->
                photoFromGalleryUri=uri
            })

        i = intent
        videoPath = i.getStringExtra("videoUri")!!
        video_url = Uri.parse(videoPath)
        videoView.setVideoURI(video_url)
        videoView.start()
        isPlaying = true
        // copy the assets to device
        copyAssets("OpenSans-Light.ttf")

        trim_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                progressDialog.show()
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
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })
        // to compress the video
        compressButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                progressDialog.show()
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
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })
        // to get video in slow motion

        slowButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                progressDialog.show()
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
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })

        //to get overlay
        overlayButton.setOnClickListener(object : View.OnClickListener {

            override fun onClick(v: View?) {
                progressDialog.show()

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
                        overlayVideo(
                            rangeSeekBar.selectedMinValue.toInt() * 1000,
                            rangeSeekBar.selectedMaxValue.toInt() * 1000
                        )
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

                        e.printStackTrace()
                    }
                } else Toast.makeText(applicationContext, "Please upload video", Toast.LENGTH_SHORT)
                    .show()
            }

        })

        // to overlay the user enter text

        textButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                progressDialog.show()
                textInput=inputText.text.toString()

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
                        textOverlay(
                            rangeSeekBar.selectedMinValue.toInt() * 1000,
                            rangeSeekBar.selectedMaxValue.toInt() * 1000
                        )
                    } catch (e: Exception) {
                        Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_SHORT).show()

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
    fun getImage(view:View){
        getPhotoUri.launch("image/*")
    }

    //function to trim the video
    private fun trimVideo(startMs: Int, endMs: Int) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "cut_video"
        val fileExtn = ".mp4"
        val yourRealPath: String = getPath(applicationContext, video_url)!!
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        Log.d(ContentValues.TAG, "startTrim: src: $yourRealPath")
        Log.d(ContentValues.TAG, "startTrim: dest: " + dest.absolutePath)
        Log.d(ContentValues.TAG, "startTrim: startMs: $startMs")
        Log.d(ContentValues.TAG, "startTrim: endMs: $endMs")
        filePath = dest.absolutePath
var start=startMs/1000
        var end=endMs/1000
        val complexCommand = arrayOf(
            "-ss",
            "" + startMs / 1000,
            "-y",
            "-i",
            yourRealPath,
            "-t",
            "" + (endMs - startMs) / 1000,
            "-vcodec",
            "mpeg4",
            "-b:v",
            "2097152",
            "-b:a",
            "48000",
            "-ac",
            "2",
            "-ar",
            "22050",
            filePath
        )

        execFFmpegBinary(complexCommand)

    }

    //function to compress the video
    private fun compressVideo(startMs: Int, endMs: Int) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "compressed_video"
        val fileExtn = ".mp4"
        val yourRealPath: String = getPath(applicationContext, video_url)!!
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        Log.d(ContentValues.TAG, "startTrim: src: $yourRealPath")
        Log.d(ContentValues.TAG, "startTrim: dest: " + dest.absolutePath)
        Log.d(ContentValues.TAG, "startTrim: startMs: $startMs")
        Log.d(ContentValues.TAG, "startTrim: endMs: $endMs")
        filePath = dest.absolutePath

        val complexCommand = arrayOf(
            "-y",
            "-i",
            yourRealPath,
            "-s",
            "160x120",
            "-r",
            "25",
            "-vcodec",
            "mpeg4",
            "-b:v",
            "150k",
            "-b:a",
            "48000",
            "-ac",
            "2",
            "-ar",
            "22050",
            filePath
        )

        execFFmpegBinary(complexCommand)

    }

    // function to get slow motion
    private fun slowVideo(startMs: Int, endMs: Int) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "slow_motion"
        val fileExtn = ".mp4"
        val yourRealPath: String = getPath(applicationContext, video_url)!!
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        Log.d(ContentValues.TAG, "startTrim: src: $yourRealPath")
        Log.d(ContentValues.TAG, "startTrim: dest: " + dest.absolutePath)
        Log.d(ContentValues.TAG, "startTrim: startMs: $startMs")
        Log.d(ContentValues.TAG, "startTrim: endMs: $endMs")
        filePath = dest.absolutePath

        val complexCommand = arrayOf(
            "-y",
            "-i",
            yourRealPath,
            "-filter_complex",
            "[0:v]setpts=2.0*PTS[v];[0:a]atempo=0.5[a]",
            "-map",
            "[v]",
            "-map",
            "[a]",
            "-b:v",
            "2097k",
            "-r",
            "60",
            "-vcodec",
            "mpeg4",
            filePath
        )
        execFFmpegBinary(complexCommand)

    }


    private fun overlayVideo(startMs: Int, endMs: Int) {


        val moviesDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "overlay_video"
        val fileExtn = ".mp4"


        val yourRealPath: String = getPath(applicationContext, video_url)!!
        //*//
       val myImagepath:String= photoFromGalleryUri?.let { getPath(applicationContext, it) }!!

        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        Log.d(ContentValues.TAG, "startTrim: src: $yourRealPath")
        Log.d(ContentValues.TAG, "startTrim: dest: " + dest.absolutePath)
        Log.d(ContentValues.TAG, "startTrim: startMs: $startMs")
        Log.d(ContentValues.TAG, "startTrim: endMs: $endMs")
        filePath = dest.absolutePath

       // val complexCommand =
          //here i used 50:50 , which reduces the image size to 50 percent
       // "-y -i $yourRealPath -i $myImagepath -filter_complex \"[1]scale=50:-1[b];[0][b] overlay=25:25\" -pix_fmt yuv420p -c:a copy $filePath"
//"overlay=(main_w-overlay_w)/2:(main_h-overlay_h)/2" -codec:a copy output.mp4
        val complexCommand = arrayOf(
            "-i",
            yourRealPath,
            "-i",
            myImagepath,
            "-filter_complex",
           "overlay=5:5",
            "-codec:a",
            "copy",
            filePath
        )

        execFFmpegBinary(complexCommand)


    }
   // function to control text overlay


    // function to get text overlay
    private fun textOverlay(startMs: Int, endMs: Int) {
        val moviesDir = Environment.getExternalStoragePublicDirectory(
           Environment.DIRECTORY_MOVIES
        )
        val filePrefix = "text_overlay"
        val fileExtn = ".mp4"
        val yourRealPath: String = getPath(applicationContext, video_url)!!
        var dest = File(moviesDir, filePrefix + fileExtn)
        var fileNo = 0
        val font_filePath =  pathOfFontDir +"OpenSans-Light.ttf"
        while (dest.exists()) {
            fileNo++
            dest = File(moviesDir, filePrefix + fileNo + fileExtn)
        }
        Log.d(ContentValues.TAG, "startTrim: src: $yourRealPath")
        Log.d(ContentValues.TAG, "startTrim: dest: " + dest.absolutePath)
        Log.d(ContentValues.TAG, "startTrim: startMs: $startMs")
        Log.d(ContentValues.TAG, "startTrim: endMs: $endMs")
        filePath = dest.absolutePath
//  -i inputClip.mp4 -vf "drawtext=text='Centered Text':x=(w-text_w)/2:y=(h-text_h)/2:fontsize=24:fontcolor=white" -c:a copy output.mp4

     val complexCommand = arrayOf(
         "-i",
         yourRealPath,
         "-vf",
         "drawtext=fontfile=$font_filePath:text='SiteName hulluway':fontsize=40:fontcolor=black: x=w-(t-4.5)*(w+tw)/5.5:y=100",
         "-acodec",
         "copy",
         filePath
     )

       execFFmpegBinary(complexCommand)

    }
    // to control play and pause functin of video
    fun play_pause(view: View) {
        if (isPlaying) {
            buttonPlayPause.setImageResource(R.drawable.ic_baseline_play_arrow_24)
            videoView.pause()
            isPlaying = false
        } else {
            videoView.start()
            buttonPlayPause.setImageResource(R.drawable.ic_baseline_pause_24)
            isPlaying = true
        }
    }
 // to activate on click to enter text
    fun enterText(view:View){
      inputText.visibility=View.VISIBLE
     textButton.visibility=View.VISIBLE
    }
    private fun execFFmpegBinary(command: Array<String?> ) {

        val executionId: Long = FFmpeg.executeAsync(command, object : ExecuteCallback {
            override fun apply(executionId: Long, returnCode: Int) {
                if (returnCode == Config.RETURN_CODE_SUCCESS) {
                    videoView.setVideoURI(Uri.parse(filePath))
                    videoPath = filePath!!
                    videoView.start()
                    progressDialog.dismiss()

                } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                    Log.i(Config.TAG, "Async command execution cancelled by user.")
                } else {
                    Log.i(
                        Config.TAG,
                        String.format(
                            "Async command execution failed with returnCode=%d.",
                            returnCode
                        )
                    )
                }
            }
        })

        Toast.makeText(this,"video edited",Toast.LENGTH_SHORT).show() }




    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     */
    private fun getPath(context: Context, uri: Uri): String? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":").toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(
                    split[1]
                )
                return contentUri?.let { getDataColumn(context, it, selection, selectionArgs) }
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context, uri: Uri, selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(
            column
        )
        try {
            cursor = context.getContentResolver().query(
                uri, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
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
    private fun copyAssets(fileName:String) {
        pathOfFontDir=(applicationContext.getExternalFilesDir(null)) !!.absolutePath+"/"+"VideoEditor"
       dir=File(pathOfFontDir)
        if(!dir.exists()){
            dir.mkdir()
        }
        val assetManager:AssetManager=assets
        var ins:InputStream?=null
        val outs:OutputStream?=null
        try{
            ins=assetManager.open(fileName)
             outFile=File(dir,fileName)
            var outs=FileOutputStream(outFile)
            copyFile(ins,outs)
        }
        catch (e:IOException){
           e.printStackTrace()
        }
        finally{
            if(ins!=null){
                ins.close()
            }
            if(outs!=null){
                outs.close()
            }
        }

    }

    @Throws(IOException::class)
    private fun copyFile(`in`: InputStream, out: OutputStream) {
        val buffer = ByteArray(1024)
        var read: Int
        while (`in`.read(buffer).also { read = it } != -1) {
            out.write(buffer, 0, read)

        }
    }
    }



