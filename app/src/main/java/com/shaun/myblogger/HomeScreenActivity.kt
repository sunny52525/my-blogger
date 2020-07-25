package com.shaun.myblogger

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.shaun.myblogger.Fragments.FragmentAbout
import com.shaun.myblogger.Fragments.FragmentHome
import com.shaun.myblogger.Fragments.FragmentSetting
import com.shaun.myblogger.Fragments.Fragment_profile
import com.shaun.myblogger.ModelClasses.UserInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.backdrop_fragment.*
import kotlinx.android.synthetic.main.duo_view_header.*
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "HOMESCRENN"

class HomeScreenActivity : AppCompatActivity(), DuoMenuView.OnMenuClickListener {
    private val RequestCode = 438
    var userRef: DatabaseReference? = null
    private var storageRef: StorageReference? = null
    private var mMenuAdapter: MenuAdapter? = null
    private var mViewHolder: ViewHolder? = null
    val ref: FirebaseDatabase? = null
    var UserData: UserInfo? = null
    var postCover: Uri? = null
    private var mTitles = ArrayList<String>()
    private var imageUrls = ArrayList<String>()

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        mTitles = ArrayList(
            Arrays.asList(
                *resources.getStringArray(R.array.menuOptions)
            )
        )
        post_it.setOnClickListener {


            if (post_title.text!!.isNotEmpty() && post_content.text!!.isNotEmpty()) {
                hideKeyboard(this)
                SavePostToSerer()

            } else {
                if (post_title.text!!.isEmpty())
                    Toast.makeText(this, "Title Can't Be empty", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this, "Body Can't Be empty", Toast.LENGTH_SHORT).show()

            }
        }
        hide_bsb.setOnClickListener {

            hideKeyboard(this)
            post_title.setText("")
            postCover = null
            post_content.setText("")
        }
        select_img.setOnClickListener {

            pickImg()
        }

        setNames()
        configureBackdrop()
        // Initialize the views
        mViewHolder = ViewHolder()

        // Handle toolbar actions
        handleToolbar()

        // Handle menu actions
        handleMenu()


        // Handle drawer actions
        handleDrawer()

        // Show main fragment in container
        goToFragment(FragmentHome(), false)
        mMenuAdapter!!.setViewSelected(0, true)
        title = mTitles.get(0)
    }


    private fun SavePostToSerer() {

        val sdf = SimpleDateFormat("dd/MM hh:mm")
        val currentTime = sdf.format(Date())

        val postHashMap = HashMap<String, Any>()
        val key = FirebaseDatabase.getInstance().getReference("posts").push().key
        postHashMap["id"] = key.toString()
        postHashMap["nameOP"] = UserData!!.getname()
        postHashMap["username"] = UserData!!.getusername()
        postHashMap["time"] = currentTime
        postHashMap["title"] = post_title.text.toString()
        postHashMap["content"] = post_content.text.toString() + "    "
        postHashMap["like_count"] = 0
        postHashMap["photosInpost"] = imageUrls
        if (checkbox.isChecked) {
            postHashMap["nameOP"] = "Anonymous"
            postHashMap["username"] = "anonymous"
        } else postHashMap["userId"] = FirebaseAuth.getInstance().currentUser!!.uid
        var reference = FirebaseDatabase.getInstance().reference.child("posts").child(key!!)

        reference.setValue(postHashMap).addOnCompleteListener {
            saveId(key)
        }




        post_title.setText("")
        post_content.setText("")

    }

    private fun saveId(key: String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userData: UserInfo? = snapshot.getValue(UserInfo::class.java)
                var postArray = userData!!.getpostIDs()
                Log.d(TAG, "onDataChange: $postArray")
                if (postArray == null) {
                    val array = ArrayList<String>(1)
                    array.add(key)
                    val ref = FirebaseDatabase.getInstance().reference.child("Users")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid).child("postIDs")
                        .setValue(array)

                } else {


                    postArray.add(0, key)
                }


                val ref = FirebaseDatabase.getInstance().reference.child("Users")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).child("postIDs")
                    .setValue(postArray)
                Log.d(TAG, "onDataChange: $postArray")


            }

        })
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
        bmp.compress(Bitmap.CompressFormat.JPEG, 25, baos)
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
                post_content.text!!.append("\n[img*]" + "\n")
                imageUrls.add(url)

                progressBar.dismiss()
            }
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
            postCover = data.data
            uploadImg(postCover!!)
            Log.d(TAG, "onActivityResult: ${postCover}")
        }
    }

    private fun setNames() {
        val name = FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
        name.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val UserData =
                    snapshot.getValue(UserInfo::class.java)
                this@HomeScreenActivity.UserData = UserData
                duo_view_header_text_sub_title.text = UserData!!.getusername()
                duo_view_header_text_title.text = UserData.getname()
                Log.d(TAG, "onDataChange1: $UserData")
                val url = UserData.getPphoto()
                if (url.isNotEmpty()) {
                    Picasso.get().load(url)
                        .placeholder(resources.getDrawable(R.drawable.user))
                        .into(profile_photo)
                }

            }

        })


    }

    private fun handleToolbar() {
        setSupportActionBar(mViewHolder!!.mToolbar)
    }

    private fun handleDrawer() {
        val duoDrawerToggle = DuoDrawerToggle(
            this,
            mViewHolder?.mDuoDrawerLayout,
            mViewHolder?.mToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        mViewHolder?.mDuoDrawerLayout?.setDrawerListener(duoDrawerToggle)
        duoDrawerToggle.syncState()
    }

    private fun handleMenu() {
        mMenuAdapter = MenuAdapter(mTitles)
        mViewHolder?.mDuoMenuView?.setOnMenuClickListener(this)
        mViewHolder?.mDuoMenuView?.adapter = mMenuAdapter
    }

    override fun onFooterClicked() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()

    }

    override fun onHeaderClicked() {
        Toast.makeText(this, "onHeaderClicked", Toast.LENGTH_SHORT)
            .show()
    }

    private fun goToFragment(
        fragment: Fragment,

        addToBack: Boolean
    ) {
        setNames()

        val transaction =
            supportFragmentManager.beginTransaction()


        if (addToBack) {
            transaction.addToBackStack(null)
        }
        transaction.add(R.id.container, fragment)
            .commit()

    }

    override fun onOptionClicked(position: Int, objectClicked: Any) {
        // Set the toolbar title
        title = mTitles[position]

        // Set the right options selected
        mMenuAdapter?.setViewSelected(position, true)
        when (position) {
            0 -> {


                goToFragment(FragmentHome(), false)

            }
            1 -> {
                goToFragment(Fragment_profile(), false)
            }
            2 -> {
                goToFragment(FragmentSetting(), false)
            }
            3 -> {

                goToFragment(FragmentAbout(), false)
            }
            else -> goToFragment(MainFragment(), false)
        }

        // Close the drawer
        mViewHolder?.mDuoDrawerLayout?.closeDrawer()
    }

    private inner class ViewHolder internal constructor() {
        val mDuoDrawerLayout: DuoDrawerLayout
        val mDuoMenuView: DuoMenuView
        val mToolbar: Toolbar

        init {
            mDuoDrawerLayout = findViewById<View>(R.id.drawer) as DuoDrawerLayout
            mDuoMenuView = mDuoDrawerLayout.menuView as DuoMenuView
            mToolbar =
                findViewById<View>(R.id.toolbar) as Toolbar
        }
    }


    private var mBottomSheetBehavior: BottomSheetBehavior<View?>? = null


    private fun configureBackdrop() {
        // Get the fragment reference
        val fragment = supportFragmentManager.findFragmentById(R.id.filter_fragment)
        fragment?.let {
            // Get the BottomSheetBehavior from the fragment view
            BottomSheetBehavior.from(fragment.requireView()).let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                // Set the trigger that will expand your view

                // Set the reference into class attribute (will be used latter)
                mBottomSheetBehavior = bsb

                mBottomSheetBehavior?.isDraggable = false
            }
        }
    }

    fun hide() {
        mBottomSheetBehavior!!.isHideable = true

        mBottomSheetBehavior?.let {
            if (it.state == BottomSheetBehavior.STATE_EXPANDED) {
                it.state = BottomSheetBehavior.STATE_HIDDEN

            }
        }
    }

    override fun onBackPressed() {
        // With the reference of the BottomSheetBehavior stored
        if (mBottomSheetBehavior!!.state == BottomSheetBehavior.STATE_EXPANDED) {
            hide()
            post_content.setText("")
            post_title.setText("")

        } else {
            super.onBackPressed()
        }


    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)


        Handler().postDelayed(
            {
                hide()
                checkbox.isChecked = false
            }, 300
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.home_screen_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected: ${item}")
        when (item.itemId) {
            R.id.new_post -> {

                mBottomSheetBehavior!!.isHideable = true

                mBottomSheetBehavior?.let {
                    it.state = BottomSheetBehavior.STATE_EXPANDED

                }
            }
            else -> {
                Toast.makeText(this, "Not Possible", Toast.LENGTH_SHORT).show()

            }
        }
        return super.onOptionsItemSelected(item)
    }


}