package com.shaun.myblogger.Fragments

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hzn.lib.EasyTransition
import com.hzn.lib.EasyTransitionOptions
import com.shaun.myblogger.InsideActivities.FullBlogActivity
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.R
import com.shaun.myblogger.adapters.home_recyclerView
import kotlinx.android.synthetic.main.fragment_home.*

private const val TAG = "HOME FRAG"

class FragmentHome : Fragment(), home_recyclerView.OnPostClicked {
    private val postAdapter = home_recyclerView(ArrayList(), this)
    private var postList: ArrayList<PostData>? = null
    private var firebaseUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        container!!.removeAllViews()


//        val fab=view?.findViewById<FloatingActionButton>(R.id.fab_add_post)
//        if(fab !=null)
//        container.addView(fab)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val refreshView =
            view.findViewById<com.dinuscxj.refresh.RecyclerRefreshLayout>(R.id.refresh_layout)

        refreshView.setOnRefreshListener {
            loadPost()
            refresh_layout.setRefreshing(true)
        }
        loadPost()

        val recycerView = view.findViewById<RecyclerView>(R.id.recycler_view_home)
        recycerView.layoutManager = LinearLayoutManager(context)
        recycerView.adapter = postAdapter
        return view
    }

    fun loadPost() {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        postList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("posts")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(p0: DataSnapshot) {

                (postList as ArrayList).clear()
                for (postsnap in p0.children)
                {
                    try
                    {  val singlePost=postsnap.getValue(PostData::class.java)
                        Log.d(ContentValues.TAG, "onDataChangeHOME: $singlePost")
                        Log.d(ContentValues.TAG, "onDataChangeHOME: ********************")
                        (postList as ArrayList).add(singlePost!!)

                    }

                    catch (e:Exception){
                        Log.e(TAG, "onDataChangeHOME: ${e.message}" )
                    }


                }
                try {
                    val reversedPost =postList!!.reversed()
                    postAdapter.loadNewData(reversedPost)

                    refresh_layout.setRefreshing(false)

                }catch (e:Exception){
                    Log.d(TAG, "onDataChangeError: ${postList!!.size}")
                    Log.d(TAG, "onDataChangeError: ${e.message}")

                }
            }

        })


    }

    override fun onPostClicked(data: PostData) {
        Log.d(TAG, "onPostClicked: LISTENER")
        val options = EasyTransitionOptions.makeTransitionOptions(
            this.activity, view!!.findViewById(R.id.each_post_title)
        )


        val intent = Intent(this.context, FullBlogActivity::class.java)
        intent.putExtra("data", data)
        EasyTransition.startActivity(intent, options)
    }


}