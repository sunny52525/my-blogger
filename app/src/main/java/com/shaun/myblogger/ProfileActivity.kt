package com.shaun.myblogger

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hzn.lib.EasyTransition
import com.hzn.lib.EasyTransitionOptions
import com.shaun.myblogger.InsideActivities.FullBlogActivity
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.ModelClasses.UserInfo
import com.shaun.myblogger.adapters.home_recyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.fragment_home.*

class ProfileActivity : AppCompatActivity(), home_recyclerView.OnPostClicked {
    private val postAdapter = home_recyclerView(ArrayList(), this)
    private var currentUserData: UserInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val userId = intent.getStringExtra("user_id")
        Log.d("TAG", "onCreated: $userId")
        val ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(userId)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserData = snapshot.getValue(UserInfo::class.java)
                Log.d("TAG", "onDataChange111: $currentUserData ")
                name_profile_activity.text = currentUserData?.getname()
                username_profile_activity.text = currentUserData?.getusername()
                bio_profile_activity.text = currentUserData?.getdescription()
                Picasso.get().load(getPhoto(currentUserData?.getPphoto()!!))
                    .placeholder(resources.getDrawable(R.drawable.user))
                    .error(resources.getDrawable(R.drawable.user))
                    .into(profile_photo_profile_activity)

                loadData(currentUserData?.getpostIDs())
                val recycerView = findViewById<RecyclerView>(R.id.recycler_view_profile_activity)
                recycerView.layoutManager = LinearLayoutManager(this@ProfileActivity)
                recycerView.adapter = postAdapter
            }

        })


    }

    private fun getPhoto(pphoto: String): String {
        if (pphoto.isEmpty()) {
            Log.d("TAG",
                "onDataChange111: ${resources.getString(R.string.defaultProfile)
                    .replace("&amp;", "")}"
            )
            return resources.getString(R.string.defaultProfile).replace("&amp;", "")
        }
        return pphoto
    }

    private fun loadData(postIds: ArrayList<String>?) {

        var postList: ArrayList<PostData>
        postList = ArrayList()

        var allPost: List<PostData>
        val ref = FirebaseDatabase.getInstance().reference.child("posts")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                (postList).clear()
                for (postsnap in snapshot.children) {
                    try {
                        val singlePost = postsnap.getValue(PostData::class.java)
                        Log.d(ContentValues.TAG, "onDataChangeHOME: $singlePost")
                        Log.d(ContentValues.TAG, "onDataChangeHOME: ********************")

                        if (postIds!!.contains(singlePost!!.getid())) {
                            postList.add(singlePost)
                        }

                    } catch (e: Exception) {
                        Log.e("TAG", "onDataChangeHOME: ${e.message}")
                    }


                }
                try {
                    val reversedPost = postList.reversed()
                    postAdapter.loadNewData(reversedPost)

                    refresh_layout.setRefreshing(false)

                } catch (e: Exception) {
                    Log.d("TAG", "onDataChangeError: ${postList.size}")
                    Log.d("TAG", "onDataChangeError: ${e.message}")

                }


            }

        })


    }

    override fun onPostClicked(data: PostData) {
        Log.d("TAG", "onPostClicked: LISTENER")
        val options = EasyTransitionOptions.makeTransitionOptions(
            this, findViewById(R.id.each_post_title)
        )


        val intent = Intent(this, FullBlogActivity::class.java)
        intent.putExtra("data", data.getid())
        EasyTransition.startActivity(intent, options)
    }

    override fun onProfileClicked(userId: String) {
        recycler_view_profile_activity.smoothScrollToPosition(0)
    }

}