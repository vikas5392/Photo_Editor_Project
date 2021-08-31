package com.training.finalsticker2

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {
    lateinit var inputText:EditText
    lateinit var input_textView: TextView
    lateinit var button_showText:ImageButton
    lateinit var image_window:ImageView
    lateinit var image_sticker:ImageView
    lateinit var text_sticker:ImageView
    lateinit var card_view:CardView
    lateinit var okButton:ImageButton
    lateinit var cancelButton:ImageButton
    lateinit var button_getSticker:ImageButton
    lateinit var button_inputText:ImageButton

    //for getting image from gallery
    lateinit var getImageUri: ActivityResultLauncher<String>
    companion object {
        val READ_PERMISSION_CODE = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //from the layout
        inputText=findViewById(R.id.inputText)
        input_textView=findViewById(R.id.input_text_view)
        button_showText=findViewById(R.id.displayTextSticker)
        image_window=findViewById(R.id.imageWindow)
        image_sticker=findViewById(R.id.sticker)
        text_sticker=findViewById(R.id.TextSticker)
        card_view=findViewById(R.id.cardView)
        button_getSticker=findViewById(R.id.button_getSticker)
        button_inputText=findViewById(R.id.button_enterText)
        okButton=findViewById(R.id.okButton)
        cancelButton=findViewById(R.id.cancelButton)

        //to get the bmap from the edittext
        inputText.isDrawingCacheEnabled=true

    //set on touch listner to two images
        image_sticker.setOnTouchListener(Touch())
        text_sticker.setOnTouchListener(Touch())

        // set invisibility for the stickers and edit text
        image_sticker.visibility=View.INVISIBLE
        text_sticker.visibility=View.INVISIBLE
        inputText.visibility=View.INVISIBLE
       button_showText.visibility=View.INVISIBLE
        okButton.visibility=View.INVISIBLE
        cancelButton.visibility=View.INVISIBLE
        input_textView.visibility=View.INVISIBLE

        //get the image uri from the gallery and set it in the image window
        getImageUri = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { uri -> image_window.setImageURI(uri) })
    }
    //onclick for the get sticker imagebutton
    fun getSticker(view: View){

        okButton.visibility=View.VISIBLE
        cancelButton.visibility=View.VISIBLE
        ViewCompat.setElevation(text_sticker,0f)
        ViewCompat.setElevation(image_sticker,2f)
        image_sticker.visibility=View.VISIBLE
        button_inputText.isEnabled=false

    }
   //onclickfor enterTextimagebutton
    fun enterText(view:View){
       //while i click on display text i dont want the sticker button to work
       button_getSticker.isEnabled=false
       inputText.visibility=View.VISIBLE
       button_showText.visibility=View.VISIBLE

    }
    //onclick for the tick mark , that shows te text
    fun displayText(view:View){
        //inputText.buildDrawingCache()
        input_textView.text=inputText.text
        inputText.visibility=View.INVISIBLE
        input_textView.visibility=View.VISIBLE
       var  text_bmap=getScreenshotFromView(input_textView)
        text_sticker.setImageBitmap(text_bmap)
        input_textView.visibility=View.INVISIBLE
        inputText.text=null
        ViewCompat.setElevation(text_sticker,2f)
        ViewCompat.setElevation(image_sticker,0f)
        text_sticker.visibility=View.VISIBLE

        button_showText.visibility=View.INVISIBLE
        // here the ok and cancel buttton gets visible
        okButton.visibility=View.VISIBLE
        cancelButton.visibility=View.VISIBLE

    }
    //onclick for ok button, to show the image with sticker
    fun ok(view:View){
        //this part deals with the ok on click for the sticker
        okButton.visibility=View.INVISIBLE
        cancelButton.visibility=View.INVISIBLE
        //finally we get the screen shot and set in ten image window
        val ok_bitmap = getScreenshotFromView(card_view)
        image_window.setImageBitmap(ok_bitmap)
        image_sticker.visibility=View.INVISIBLE
        button_inputText.isEnabled=true
        //this part deals with the ok onclick for th etext_sticker
        text_sticker.visibility=View.INVISIBLE
        button_getSticker.isEnabled=true
        //inputText.text.clear()
    }

    // on click for cancel button to cancel the sticker
    fun cancel(view:View){
        button_getSticker.isEnabled=true
        button_inputText.isEnabled=true
        okButton.visibility=View.INVISIBLE
        cancelButton.visibility=View.INVISIBLE
        // to hide the sticker as i cancelled them
        image_sticker.visibility=View.INVISIBLE
        text_sticker.visibility=View.INVISIBLE
        // let us assume that i entered the text then i showed the text sticker but cancelled after wards so hiding the text sticker hides it but the text input remains so to clear it we -

    }


    // hereby functions show to get permission and to get image from gallery

    fun getImageFromGallery(view: View) {
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

    //now to save the bimap to gallery

    fun saveToGallery(view: View) {
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
    //create a bitmap object
    fun getScreenshotFromView(v: View): Bitmap? {
        var screenshot_bitmap: Bitmap? = Bitmap.createBitmap(v.measuredWidth, v.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = screenshot_bitmap?.let { Canvas(it) }
        v.draw(canvas)
        return screenshot_bitmap
    }

    //actual save method which is called in our save on click
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

}