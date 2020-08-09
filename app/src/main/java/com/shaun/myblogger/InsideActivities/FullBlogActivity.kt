package com.shaun.myblogger.InsideActivities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.hzn.lib.EasyTransition
import com.shaun.myblogger.HtmlImageGetter
import com.shaun.myblogger.LoginActivity
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.ProfileActivity
import com.shaun.myblogger.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_full_blog.*


class FullBlogActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    var finishEnter = false
    var userId = ""
    private var savedInst: Bundle? = null
    lateinit var postData: PostData
    lateinit var contentView: TextView
    private var layoutAbout: androidx.core.widget.NestedScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_blog)
        savedInst = savedInstanceState
        handleIntent(intent)

        contentView =
            findViewById(R.id.post_content_full)








        share_blog.setOnClickListener {
            sharePost(postData.getid(), postData.getnameOp())
        }




        header.setOnClickListener {

        }
    }

    private fun handlePost(savedInstanceState: Bundle?, check: Boolean) {


        //TODO Add loading icon
        initViews(postData.gettitle())
        var transitionDuration: Long = 800
        if (null != savedInstanceState) transitionDuration = 0
        Log.d("TAG", "handlePost: ${postData.getcontent()}")
        // transition enter
        finishEnter = false
        EasyTransition.enter(
            this,
            transitionDuration,
            DecelerateInterpolator(),

            object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // init other views after transition anim
                    finishEnter = true


                }
            })
        initOtherViews(postData.getcontent(), postData.getpostCover())
        Log.d("FullBlog", "onCreate: $postData")
        toolbar = findViewById(R.id.anim_toolbar)
        setSupportActionBar(toolbar)
        if (!check)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL
    }

    private fun sharePost(id: String, user: String) {

        val strBuilder = StringBuilder()
        strBuilder.appendln("Read this Blog Post by $user")
        strBuilder.appendln("https://my-blogger-sunny.herokuapp.com/posts/$id")

        val shareIntent =
            Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, strBuilder.toString())
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Blog Share")
        ContextCompat.startActivity(
            this,
            Intent.createChooser(shareIntent, "Share"),
            Bundle.EMPTY
        )

    }

    private fun initViews(heading: String) {


        val tvName = findViewById<CollapsingToolbarLayout>(R.id.each_post_title)
        tvName!!.title = heading
    }

    private fun initOtherViews(text: String, imgLinks: String) {
        if (imgLinks.isNotEmpty()) {
            Picasso.get().load(imgLinks).into(header)
        }
        Log.e("TAG", "initOtherViews: $text")

        poster.text = postData.getusername()
        layoutAbout = findViewById(R.id.test)
//        post_content_full.text = text.replace("[img*]", "")


//        val v= RichTextDocumentElement.TextBuilder(editor!!.getHtml()).build()
//        contentView.setText(text)
        val imageGetter = HtmlImageGetter(lifecycleScope, resources, contentView)
        val styledText = HtmlCompat.fromHtml(text, taskId, imageGetter, null)
        contentView.text = styledText
        Handler().postDelayed({
            layoutAbout!!.visibility = View.VISIBLE
        }, 500)
        layoutAbout!!.alpha = 0f
        layoutAbout!!.translationY = -30f
        layoutAbout!!.animate()
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)

//        if (imgLinks != null)
//            initContentWithImage(text, imgLinks)


    }

//    private fun initContentWithImage(text: String, imgLinks: ArrayList<String>) {
//        var imgBitmap = ArrayList<Bitmap>(imgLinks.size)
//        Log.d("TAG", "initContentWithImage: $imgLinks")
//        GlobalScope.launch {
//            for (i in 0..imgLinks.size - 1) {
//                try {
//                    imgBitmap.add(
//                        BitmapFactory.decodeStream(
//                            URL(imgLinks[i]).openConnection().getInputStream()
//                        )
//                    )
//                } catch (e: Exception) {
//                    Log.d("TAG", "initContentWithImage: ${e.message}")
//                }
//            }
//            withContext(Dispatchers.Main) {
//                parseContent(text, imgBitmap)
//            }
//        }
//
//
//    }
//
//    private fun parseContent(text: String, imgBitmaps: ArrayList<Bitmap>) {
//        if (imgBitmaps == null) {
//            Log.d("TAG", "parseContent: Returned")
//            return
//        }
//        var count = 0
//        val final = SpannableStringBuilder()
//        var i = 0
//
//        while (i <= text.length - 1) {
//
//            if (text[i] == '[') {
//                try {
//                    if (text[i + 1] == 'i' && text[i + 2] == 'm' && text[i + 4] == '*') {
//
//                        final.append(imgBitmaps.get(count).let { getImageSpannable(it) })
//
//                        count++
//                        i += 5
//                    }
//                } catch (e: java.lang.Exception) {
//                    Log.d("TAG", "parseContent: ${e.message}")
//                }
//            } else
//                final.append(text[i])
//            i++
//        }
//
//        Log.d("TAG", "parseContent: $final")
//        post_content_full.text = final
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()

            }
            R.id.menu_blog_report_post -> {

                val ref = FirebaseDatabase.getInstance().reference.child("reports")
                val key = ref.push().key
                val ReportHashMap = HashMap<String, String>()
                ReportHashMap["reporter"] = FirebaseAuth.getInstance().currentUser!!.uid
                ReportHashMap["reportedPost"] = postData.getid()
                ref.child(key!!).setValue(ReportHashMap).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Post Reported", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(
                            this, "Some error Occured." +
                                    "Please Try Again", Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }
            R.id.full_blog_view_profile -> {

                if (userId == "") {

                    return true
                }
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {

        val appLinkAction = intent.action
        if (Intent.ACTION_VIEW == appLinkAction)
            finish()

        if (finishEnter) {
            finishEnter = false
            startBackAnim()

        }
    }

    private fun startBackAnim() {
        // forbidden scrolling
        val sv = findViewById<androidx.core.widget.NestedScrollView>(R.id.test)
        sv.setOnTouchListener { v, event -> true }

        // start our anim
        test.animate()
            .setDuration(200)
            .scaleX(0f)

            .scaleY(0f)
        layoutAbout!!.animate()
            .setDuration(200)
            .alpha(0f)
            .translationY(-30f)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // transition exit after our anim
                    EasyTransition.exit(
                        this@FullBlogActivity,
                        500,
                        DecelerateInterpolator()
                    )
                }
            })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val appLinkAction = intent.action
        if (Intent.ACTION_VIEW == appLinkAction) {

            return false
        }

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_full_blog, menu)
        for (i in 0 until menu!!.size()) {
            val item = menu.getItem(i)
            val spanString =
                SpannableString(menu.getItem(i).title.toString())
            spanString.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0,
                spanString.length,
                0
            ) //fix the color to white
            item.title = spanString
        }


        val item = menu.findItem(R.id.full_blog_view_profile)
        if (userId == "") {
            item.isVisible = false
        }


        return true
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val appLinkAction = intent.action
        val appLinkData: Uri? = intent.data
        if (Intent.ACTION_VIEW == appLinkAction) {
            appLinkData?.lastPathSegment?.also { recipeId ->
                Uri.parse("content://com.recipe_app/recipe/")
                    .buildUpon()
                    .appendPath(recipeId)
                    .build().also { appData ->
                        Log.d("TAG", "handleIntent: ${appData.lastPathSegment}")
                        val postid = appData.lastPathSegment.toString()

                        processView(postid, true)

                    }
            }
        } else {
            val intent = intent
            val postid = intent.getStringExtra("data")
            processView(postid, false)


        }
    }

    private fun processView(id: String, check: Boolean) {

        val ref = FirebaseDatabase.getInstance().reference.child("posts").child(id)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d("TAG", "onDataChange: $dataSnapshot")
                if (!dataSnapshot.exists()) {
                    val intent = Intent(this@FullBlogActivity, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(this@FullBlogActivity, "Post Not Found", Toast.LENGTH_LONG)
                        .show()

                } else {
                    if (check)
                        makeActionDisappear()
                    postData = dataSnapshot.getValue(PostData::class.java)!!
                    userId = postData.getuserId()
                    handlePost(savedInst, check)


                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(
                    "TAG",
                    databaseError.message
                ) //Don't ignore errors!
            }
        }
        ref.addListenerForSingleValueEvent(eventListener)
    }

    private fun makeActionDisappear() {

    }


}