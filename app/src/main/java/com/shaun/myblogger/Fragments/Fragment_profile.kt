package com.shaun.myblogger.Fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dinuscxj.refresh.RecyclerRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hzn.lib.EasyTransition
import com.hzn.lib.EasyTransitionOptions
import com.shaun.myblogger.InsideActivities.FullBlogActivity
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.ModelClasses.UserInfo
import com.shaun.myblogger.R
import com.shaun.myblogger.adapters.home_recyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*

private const val TAG = "Profile Frag"

class Fragment_profile : Fragment(), home_recyclerView.OnPostClicked {

    private val postAdapter = home_recyclerView(ArrayList(), this)

    var currentUserData: UserInfo? = null
    lateinit var ref: DatabaseReference
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container!!.removeAllViews()
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        ref = FirebaseDatabase.getInstance().reference.child("Users")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUserData = snapshot.getValue(UserInfo::class.java)
                Log.d(TAG, "onDataChange11: $currentUserData ")
                name_profile.text = currentUserData!!.getname()
                username_profile.text = currentUserData!!.getusername()
                bio_profile.text = currentUserData?.getdescription()
                Picasso.get().load(getPhoto(currentUserData?.getPphoto()!!))
                    .placeholder(resources.getDrawable(R.drawable.user))
                    .error(resources.getDrawable(R.drawable.user)).into(profile_photo_profile)

                loadData(currentUserData?.getpostIDs())
                val recycerView = view.findViewById<RecyclerView>(R.id.recycler_view_profile)
                recycerView.layoutManager = LinearLayoutManager(context)
                recycerView.adapter = postAdapter
            }

        })
        val refresh_layout_profile =
            view.findViewById<RecyclerRefreshLayout>(R.id.refresh_layout_profile)
        refresh_layout_profile.setOnRefreshListener {
            loadData(currentUserData?.getpostIDs())
            refresh_layout_profile.setRefreshing(true)
        }

        return view
    }

    private fun loadData(postIds: ArrayList<String>?) {

        var postList: ArrayList<PostData>
        postList = ArrayList()

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
                        Log.e(TAG, "onDataChangeHOME: ${e.message}")
                    }


                }
                try {
                    val reversedPost = postList.reversed()
                    Log.d(TAG, "onDataChange11111: $postList")
                    postAdapter.loadNewData(reversedPost)
                    val refresh_layout_profile =
                        view?.findViewById<RecyclerRefreshLayout>(R.id.refresh_layout_profile)

                    refresh_layout_profile?.setRefreshing(false)

                } catch (e: Exception) {
                    Log.d(TAG, "onDataChangeError: ${postList.size}")
                    Log.d(TAG, "onDataChangeError: ${e.message}")

                }


            }

        })


    }

    private fun getPhoto(pphoto: String): String? {
        if (pphoto.isEmpty()) {
            Log.d(
                "TAG",
                "onDataChange111: ${resources.getString(R.string.defaultProfile)
                    .replace("&amp;", "")}"
            )
            return resources.getString(R.string.defaultProfile).replace("&amp;", "")
        }
        return pphoto
    }

    override fun onPostClicked(data: PostData) {
        Log.d(TAG, "onPostClicked: LISTENER")
        val options = EasyTransitionOptions.makeTransitionOptions(
            this.activity, view!!.findViewById(R.id.each_post_title)
        )


        val intent = Intent(this.context, FullBlogActivity::class.java)
        intent.putExtra("data", data.getid())
        EasyTransition.startActivity(intent, options)
    }

    override fun onProfileClicked(userId: String) {
        Toast.makeText(activity, "CLICKED", Toast.LENGTH_SHORT).show()
        recycler_view_profile.smoothScrollToPosition(0)
    }


}