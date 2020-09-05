package com.shaun.myblogger.Fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.shaun.myblogger.ModelClasses.UserInfo
import com.shaun.myblogger.R
import com.shaun.myblogger.ViewFullImage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_setting.*
import java.io.ByteArrayOutputStream


private const val TAG = "SETTING FRAG"

class FragmentSetting : Fragment() {
    var userData: UserInfo? = null

    var userRef: DatabaseReference? = null
    private val RequestCode = 438

    private var storageRef: StorageReference? = null
    private var imageuri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container!!.removeAllViews()


        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_setting, container, false)
        initData()
        val edit_description = view.findViewById<ImageView>(R.id.edit_description)
        val edit_name = view.findViewById<ImageView>(R.id.edit_name)
        val edit_username = view.findViewById<ImageView>(R.id.edit_username)
        val user_photo: CircleImageView = view.findViewById(R.id.user_photo)


        updateImageinUi(user_photo)


        edit_description.setOnClickListener {
            CreateDialog("Description")
        }
        edit_name.setOnClickListener {
            CreateDialog("Name")

        }
        edit_username.setOnClickListener {
            CreateDialog("Username")
        }
        user_photo.setOnClickListener {

            val options = arrayOf<CharSequence>(
                "View Image",
                "Upload New",
                "Remove"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setTitle("Profile Photo")
            builder.setItems(options) { dialog, which ->
                if (which == 0) {
                    viewImage()
                } else if (which == 1) {
                    uploadNewImage()
                } else if (which == 2) {
                    removeImage()
                }
            }
            builder.show()
        }
        return view
    }

    private fun removeImage() {
        val ref =
            FirebaseDatabase.getInstance().reference
                .child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Pphoto").setValue(resources.getString(R.string.defaultProfile))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(context, "Photo Removed", Toast.LENGTH_SHORT).show()
//                        updateImageinUi(userPhotoRef = user_photo)

                    } else {
                        Toast.makeText(context, "Uhh", Toast.LENGTH_SHORT).show()
                    }
                }
    }


    private fun uploadNewImage() {
        pickImg()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageuri = data.data
            Toast.makeText(context, "Image Uploading", Toast.LENGTH_SHORT).show()
            uploadImg()
        }
    }

    private fun uploadImg() {
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image Uploading.Please wait..........")
        progressBar.show()
        if (imageuri != null) {


//            val bmp: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imgUri)
//            val baos = ByteArrayOutputStream()
//            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
//            var data: ByteArray? = baos.toByteArray()
//
//            uploadTask = fileRef.putBytes(data!!)

            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")
            val uploadTask: StorageTask<*>

            val bmp: Bitmap = MediaStore.Images.Media.getBitmap(activity?.contentResolver, imageuri)
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
            val data: ByteArray? = baos.toByteArray()

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

                    val map = HashMap<String, Any>()
                    map["Pphoto"] = url
                    userRef = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    userRef!!.updateChildren(map)

                    progressBar.dismiss()
                }
            }
        }
    }

    private fun pickImg() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    private fun viewImage() {

        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val UserData =
                    snapshot.getValue(UserInfo::class.java)

                var photoUrl = UserData?.getPphoto()
                if (photoUrl!!.isEmpty()) {
                    photoUrl = FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()
                }
                val intent = Intent(context, ViewFullImage::class.java)
                intent.putExtra("url", photoUrl)
                intent.putExtra("0", "true")
                Log.d(TAG, "viewImage: ${userData!!.getPphoto()}")
                startActivity(intent)
            }

        })


    }


    private fun updateImageinUi(userPhotoRef: CircleImageView) {

        val ref =
            FirebaseDatabase.getInstance().reference.child("Users")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Pphoto")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: Snap $snapshot")
                if (snapshot.value != null && snapshot.value != "") {
                    Picasso.get().load(snapshot.value.toString())
                        .placeholder(resources.getDrawable(R.drawable.user))
                        .into(userPhotoRef)
                    userData!!.setPphoto(resources.getString(R.string.defaultProfile))
                } else {
                    Log.d(
                        TAG,
                        "onDataChange: Snap null ${FirebaseAuth.getInstance().currentUser!!.photoUrl.toString()}"
                    )

                    Picasso.get().load(FirebaseAuth.getInstance().currentUser!!.photoUrl.toString())
                        .placeholder(resources.getDrawable(R.drawable.user))
                        .into(userPhotoRef)
                }
            }

        })


    }


    private fun CreateDialog(description: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle(description)

        val viewInflated: View = LayoutInflater.from(context)
            .inflate(
                R.layout.text_inpu_password,
                view as ViewGroup?,
                false
            )
        val input =
            viewInflated.findViewById<View>(R.id.input) as EditText
        builder.setView(viewInflated)

        builder.setPositiveButton(
            "Ok"
        ) { dialog, _ ->
            dialog.dismiss()
            val mText = input.text.toString()
            if (mText.isNotEmpty()) {
                when (description) {

                    "Description" -> {
                        editDescription(mText)
                    }
                    "Name" -> {
                        editName(mText)
                    }
                    "Username" -> {
                        editUsername(mText)
                    }
                }
            }


        }


        builder.setNegativeButton(
            "Cancel",
            { dialog, which -> dialog.cancel() })

        val dialog = builder.create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(resources.getColor(R.color.White))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(resources.getColor(R.color.White))

        }
        dialog.show()

    }

    private fun editUsername(username: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val userNameRef = rootRef.child("username").child(username)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: $dataSnapshot")
                if (!dataSnapshot.exists()) {

                    val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                    ref.child("username").setValue(username)
                        .addOnCompleteListener {
                            Toast.makeText(context, "Username Updated", Toast.LENGTH_SHORT).show()
                            val oldUsername = userData!!.getusername()
                            initData()
                            val Ref = FirebaseDatabase.getInstance().reference.child("username")
                                .child(username).setValue(username)
                            val ref = FirebaseDatabase.getInstance().reference.child("username")
                                .child(oldUsername).removeValue()

                        }

                } else {
                    Toast.makeText(context, "Please Choose Unique Username", Toast.LENGTH_SHORT)
                        .show()

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(
                    "TAG",
                    databaseError.message
                ) //Don't ignore errors!
            }
        }
        userNameRef.addListenerForSingleValueEvent(eventListener)
    }

    private fun editName(name: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.child("name").setValue(name)
            .addOnCompleteListener {
                Toast.makeText(context, "Name Updated", Toast.LENGTH_SHORT).show()
                initData()
            }

    }

    private fun editDescription(description: String) {

        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.child("description").setValue(description)
            .addOnCompleteListener {
                Toast.makeText(context, "Bio Updated", Toast.LENGTH_SHORT).show()
                initData()
            }

    }

    private fun initData() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userInfo =
                    snapshot.getValue(UserInfo::class.java)
                Log.d(TAG, "onDataChange: $snapshot")
                userData = userInfo
                Log.d(TAG, "onDataChange:INIT $userData")
                user_name_settings.text = userInfo!!.getname()
                user_name_bio.text = userInfo.getdescription()
                user_name_username.text = userInfo.getusername()

            }

        })


    }


}