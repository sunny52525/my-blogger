package com.shaun.myblogger

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.StorageReference
import io.square1.richtextlib.ui.RichContentView
import net.dankito.richtexteditor.android.RichTextEditor


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
//        val cache = intent.getStringExtra("cached")
//
//        val sharedPref =
//            getSharedPreferences("MyData", Context.MODE_PRIVATE)
//        sharedPref.edit().clear().apply()
//        setSupportActionBar(toolbar2)
//        editor = findViewById(R.id.editor)
//        if (cache.isNotEmpty()) {
//            editor!!.setHtml(cache)
//        }
//        editorToolbar = findViewById(R.id.editorToolbar)
//        editorToolbar!!.editor = editor
//        editor!!.setEditorFontSize(20)
//        editor!!.setPadding((4 * resources.displayMetrics.density).toInt())
//        editor!!.focusEditorAndShowKeyboardDelayed()
//        select_img_rich.setOnClickListener {
//            pickImg()
//        }
//
//
//        ClipboardManager.OnPrimaryClipChangedListener {
//
//
//        }
//
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater = menuInflater
//        inflater.inflate(R.menu.menu, menu)
//        return true
//    }
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.save -> {
//
//                val sharedPref =
//                    getSharedPreferences("MyData", Context.MODE_PRIVATE)
//                val DATA = sharedPref.edit()
//                DATA.putString("content", editor!!.getHtml())
//
//                DATA.apply()
//                Log.d("TAG", "oof ${editor!!.getHtml()}")
//                Handler().postDelayed(
//                    {
//                        onBackPressed()
//                    }, 500
//                )
//
////                finish()
////                val clipboard =
////                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
////                clipboard.setPrimaryClip(ClipData.newPlainText("text", "text"));
////                Log.d("TAG", "onOptionsItemSelected: ${clipboard.text}")
//
//            }
//            android.R.id.paste -> {
////                 var clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//
//
//            }
//            else -> {
//                Toast.makeText(this, "Not Possible", Toast.LENGTH_SHORT).show()
//
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//
//    override fun onBackPressed() {
//
//        editor!!.insertHtml(".<br>")
//
//        Handler().postDelayed({
//
//            if (!editorToolbar!!.handlesBackButtonPress()) {
//                super.onBackPressed()
////                val sharedPref =
////                    getSharedPreferences("MyData", Context.MODE_PRIVATE)
////                val DATA = sharedPref.edit()
////                DATA.putString(
////                    "content",
////                    (editor!!.getHtml()).subSequence(0, (editor!!.getHtml()).length - 4).toString()
////                )
////
////                DATA.apply()
////                Log.d("TAG", "oof ${editor!!.getHtml()}")
//
////                finish()
//            }
//        }, 500)
//    }
//
//
//    private fun pickImg() {
//        val intent = Intent()
//        intent.type = "image/*"
//        intent.action = Intent.ACTION_GET_CONTENT
//        startActivityForResult(intent, RequestCode)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
//            imgUri = data.data
//            uploadImg(imgUri!!)
//            Log.d("TAG", "onActivityResult: ${imgUri}")
//        }
//    }
//
//    private fun uploadImg(imgUri: Uri) {
//        storageRef = FirebaseStorage.getInstance().reference.child("Post Images")
//        val progressBar = ProgressDialog(this)
//        progressBar.setMessage("Please wait,Posting..")
//        progressBar.show()
//
//        val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
//        val uploadTask: StorageTask<*>
//        val bmp: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
//        val baos = ByteArrayOutputStream()
//        val b = Bitmap.createScaledBitmap(bmp, 400, 300, false)
//        b.compress(Bitmap.CompressFormat.JPEG, 50, baos)
//        val data: ByteArray? = baos.toByteArray()
//
//        uploadTask = fileRef.putBytes(data!!)
//        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {
//            if (!it.isSuccessful) {
//                it.exception?.let {
//                    throw  it
//                }
//            }
//            return@Continuation fileRef.downloadUrl
//        }).addOnCompleteListener {
//            if (it.isSuccessful) {
//                val downloadUrl = it.result
//                val url = downloadUrl.toString()
//                editor!!.insertImage(url, "Image")
//                progressBar.dismiss()
//            }
//        }


    }


}