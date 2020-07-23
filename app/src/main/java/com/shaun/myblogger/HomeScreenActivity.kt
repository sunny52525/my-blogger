package com.shaun.myblogger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.shaun.myblogger.Fragments.FragmentAbout
import com.shaun.myblogger.Fragments.FragmentHome
import com.shaun.myblogger.Fragments.FragmentSetting
import com.shaun.myblogger.Fragments.Fragment_profile
import com.shaun.myblogger.ModelClasses.UserInfo
import kotlinx.android.synthetic.main.activity_home_screen.*
import kotlinx.android.synthetic.main.backdrop_fragment.*
import kotlinx.android.synthetic.main.duo_view_header.*
import nl.psdcompany.duonavigationdrawer.views.DuoDrawerLayout
import nl.psdcompany.duonavigationdrawer.views.DuoMenuView
import nl.psdcompany.duonavigationdrawer.widgets.DuoDrawerToggle
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "HOMESCRENN"

class HomeScreenActivity : AppCompatActivity(), DuoMenuView.OnMenuClickListener {
    private var mMenuAdapter: MenuAdapter? = null
    private var mViewHolder: ViewHolder? = null
    val ref: FirebaseDatabase? = null
    var UserData: UserInfo? = null
    private var mTitles = ArrayList<String>()

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

            hideKeyboard(this)
            SavePostToSerer()
        }
        hide_bsb.setOnClickListener {

            hideKeyboard(this)
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
        setTitle(mTitles.get(0))
    }

    private fun SavePostToSerer() {

        val sdf = SimpleDateFormat("dd/mm hh:mm")
        val currentTime = sdf.format(Date())

        val postHashMap = HashMap<String, Any>()
        val key = FirebaseDatabase.getInstance().getReference("posts").push().key
        postHashMap["id"] = key.toString()
        postHashMap["nameOP"] = UserData!!.getname()
        postHashMap["username"] = UserData!!.getusername()
        postHashMap["time"] = currentTime
        postHashMap["title"] = post_title.text.toString()
        postHashMap["content"] = post_content.text.toString()
        postHashMap["like_count"] = 0

        val reference = FirebaseDatabase.getInstance().reference.child("posts").child(key!!)
            .setValue(postHashMap)


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
                    snapshot.getValue(com.shaun.myblogger.ModelClasses.UserInfo::class.java)
                this@HomeScreenActivity.UserData = UserData
                duo_view_header_text_sub_title.text = UserData!!.getusername()
                duo_view_header_text_title.text = UserData!!.getname()
                Log.d(TAG, "onDataChange1: $UserData")
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
        addToBackStack: Boolean
    ) {
        val transaction =
            supportFragmentManager.beginTransaction()
        if (addToBackStack) {
            transaction.addToBackStack(null)
        }
        transaction.add(R.id.container, fragment).commit()
    }

    override fun onOptionClicked(position: Int, objectClicked: Any) {
        // Set the toolbar title
        title = mTitles[position]

        // Set the right options selected
        mMenuAdapter?.setViewSelected(position, true)
        when (position) {
            0 -> {
                fab_add_post.visibility = View.VISIBLE
                goToFragment(FragmentHome(), true)
            }
            1 -> {
                fab_add_post.visibility = View.GONE
                goToFragment(Fragment_profile(), true)
            }
            2 -> {
                fab_add_post.visibility = View.GONE
                goToFragment(FragmentSetting(), true)
            }
            3 -> {
                fab_add_post.visibility = View.GONE
                goToFragment(FragmentAbout(), true)
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
            BottomSheetBehavior.from(fragment.requireView())?.let { bsb ->
                // Set the initial state of the BottomSheetBehavior to HIDDEN
                bsb.state = BottomSheetBehavior.STATE_HIDDEN

                // Set the trigger that will expand your view
                fab_add_post.setOnClickListener { bsb.state = BottomSheetBehavior.STATE_EXPANDED }

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

        } else {
            super.onBackPressed()
        }

    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = activity.getCurrentFocus()
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)

        post_title.setText("")
        post_content.setText("")
        Handler().postDelayed(
            {
                hide()
            }, 300
        )
    }
}