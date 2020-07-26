package com.shaun.myblogger

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import io.square1.richtextlib.ui.RichContentView
import kotlinx.android.synthetic.main.activity_rich_text.*
import net.dankito.richtexteditor.android.RichTextEditor
import java.io.ByteArrayOutputStream


class RichTextActivity : AppCompatActivity() {
    private val RequestCode = 438
    private var editor: RichTextEditor? = null
    lateinit var contentView: RichContentView
    private var editorToolbar: ExampleToolbar? = null

    private var storageRef: StorageReference? = null
    private var imgUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rich_text)
        val sharedPref =
            getSharedPreferences("MyData", Context.MODE_PRIVATE)
        sharedPref.edit().clear().apply()
        setSupportActionBar(toolbar2)
        editor = findViewById(R.id.editor)
        editorToolbar = findViewById(R.id.editorToolbar) as ExampleToolbar
        editorToolbar!!.editor = editor
        editor!!.setEditorFontSize(20)
        editor!!.setPadding((4 * resources.displayMetrics.density).toInt())
        editor!!.focusEditorAndShowKeyboardDelayed()
        select_img_rich.setOnClickListener {
            pickImg()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.save -> {
                val sharedPref =
                    getSharedPreferences("MyData", Context.MODE_PRIVATE)
                val DATA = sharedPref.edit()
                DATA.putString("content", editor!!.getHtml())

                DATA.apply()

                finish()

            }
            else -> {
                Toast.makeText(this, "Not Possible", Toast.LENGTH_SHORT).show()

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (!editorToolbar!!.handlesBackButtonPress()) {
            super.onBackPressed()
        }
    }


    private fun pickImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imgUri = data.data
            uploadImg(imgUri!!)
            Log.d("TAG", "onActivityResult: ${imgUri}")
        }
    }

    private fun uploadImg(imgUri: Uri) {
        storageRef = FirebaseStorage.getInstance().reference.child("Post Images")
        val progressBar = ProgressDialog(this)
        progressBar.setMessage("Please wait,Posting..")
        progressBar.show()

        val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
        var uploadTask: StorageTask<*>
        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
        val baos = ByteArrayOutputStream()
        var b = Bitmap.createScaledBitmap(bmp, 300, 300, false)
        b.compress(Bitmap.CompressFormat.JPEG, 25, baos)
        var data: ByteArray? = baos.toByteArray()

        uploadTask = fileRef.putBytes(data!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
            if (!it.isSuccessful) {
                it.exception?.let {
                    throw  it
                }
            }
            return@Continuation fileRef.downloadUrl
        }).addOnCompleteListener {
            if (it.isSuccessful) {
                val downloadUrl = it.result
                val url = downloadUrl.toString()
                editor!!.insertImage(url, "Image")
                progressBar.dismiss()
            }
        }


    }


}