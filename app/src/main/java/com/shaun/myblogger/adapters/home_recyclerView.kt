package com.shaun.myblogger.adapters

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.ldoublem.thumbUplib.ThumbUpView
import com.shaun.myblogger.HtmlImageGetter
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.R
import java.lang.Integer.parseInt
import java.lang.Long.parseLong


class HomeRecyclerViewAdapter(view: View) : RecyclerView.ViewHolder(view) {
    var postTitle: TextView = view.findViewById(R.id.each_post_title)
    var postContent: TextView = view.findViewById(R.id.each_post_post)
    var likeCount: TextView = view.findViewById(R.id.each_post_like)
    var postUsername: TextView = view.findViewById(R.id.each_post_username)
    var postTime: TextView = view.findViewById(R.id.each_post_time)
    var likeButton: ThumbUpView = view.findViewById(R.id.like_button)
}


private const val TAG = "VIEW ADAPTER"

class home_recyclerView(private var posts: List<PostData>, private val listener: OnPostClicked,lifecycleOwner: LifecycleCoroutineScope) :
    RecyclerView.Adapter<HomeRecyclerViewAdapter>() {
    private val lifecycleOwner=lifecycleOwner
    interface OnPostClicked {
        fun onPostClicked(data: PostData)
        fun onProfileClicked(userId: String)
    }

    fun loadNewData(newPostData: List<PostData>) {
        Log.d(TAG, "loadNewData: Adapter Called with data")
        for (i in newPostData) {
            Log.d(TAG, "loadNewData: $i")
        }
        posts = newPostData
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeRecyclerViewAdapter {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.each_post, parent, false)
        return HomeRecyclerViewAdapter(view)
    }

    override fun getItemCount(): Int {
        val size = posts.size
        Log.d(TAG, "getItemCount: $size")
        return size
    }

    override fun onBindViewHolder(holder: HomeRecyclerViewAdapter, position: Int) {
        Log.d(TAG, "onBindViewHolder: IS CALLEd")
        if (posts.isEmpty()) {
            //TODO
        } else {
            val currentPost = posts[position]

            holder.postTitle.text = currentPost.gettitle().trimIndent()
            val intoString = currentPost.getcontent().trimIndent()
//            val sp: Spanned? = Html.fromHtml(intoString)

            val imageGetter = HtmlImageGetter(lifecycleOwner, Resources.getSystem(), holder.postContent)
            val styledText = HtmlCompat.fromHtml(intoString, position, imageGetter, null)
//            contentView.text = styledText

            holder.postContent.text = styledText.trimStart()
            holder.postContent.append("......")
            holder.likeCount.text = currentPost.getlike_count().toString()
            holder.postUsername.text = currentPost.getusername()
            holder.postTime.text = currentPost.gettime()

            holder.likeButton.setFillColor(Color.rgb(29, 161, 242))
            holder.likeButton.setOnThumbUp {

                holder.likeButton.Like()

                holder.likeCount.text = (parseInt(holder.likeCount.text.toString()) + 1).toString()

                val likeMap = HashMap<String, Long>()
                likeMap["like_count"] = (parseLong(holder.likeCount.text.toString()))
                val ref = FirebaseDatabase.getInstance().reference.child("posts")
                    .child(currentPost.getid()).updateChildren(
                        likeMap as Map<String, Any>
                    )


            }

            val eachPostClickListener = View.OnClickListener {
                listener.onPostClicked(currentPost)
            }
            holder.postContent.setOnClickListener(eachPostClickListener)
            holder.postTitle.setOnClickListener(eachPostClickListener)
            holder.likeCount.setOnClickListener(eachPostClickListener)

            val viewProfileListener = View.OnClickListener {
                listener.onProfileClicked(currentPost.getuserId())
            }
            holder.postUsername.setOnClickListener(viewProfileListener)
            holder.postTime.setOnClickListener(viewProfileListener)

            Log.d(TAG, "onBindViewHolder: $currentPost")
        }

    }

    fun Context.lifecycleOwner(): LifecycleOwner? {
        var curContext = this
        var maxDepth = 20
        while (maxDepth-- > 0 && curContext !is LifecycleOwner) {
            curContext = (curContext as ContextWrapper).baseContext
        }
        return if (curContext is LifecycleOwner) {
            curContext as LifecycleOwner
        } else {
            null
        }
    }

}