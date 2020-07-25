package com.shaun.myblogger.InsideActivities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.DynamicDrawableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.hzn.lib.EasyTransition
import com.shaun.myblogger.ModelClasses.PostData
import com.shaun.myblogger.R
import kotlinx.android.synthetic.main.activity_full_blog.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class FullBlogActivity : AppCompatActivity() {
    private var toolbar: Toolbar? = null
    var finishEnter = false
    lateinit var postData: PostData
    private var layoutAbout: androidx.core.widget.NestedScrollView? = null
    lateinit var collapsingToolbar: CollapsingToolbarLayout
//    lateinit var imgBitmaps:ArrayList<Bitmap>

    private var appBarLayout: AppBarLayout? = null
    private var recList: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_blog)

        val intent = intent
        val data: PostData = intent.getSerializableExtra("data") as PostData
        postData = data
        initViews(data.gettitle())
        var transitionDuration: Long = 800
        if (null != savedInstanceState) transitionDuration = 0

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


                    initOtherViews(data.getcontent(), data.getphotosInpost())
                }
            })

        Log.d("FullBlog", "onCreate: $data")
        toolbar = findViewById(R.id.anim_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL



        header.setOnClickListener {

        }
    }

    private fun initViews(heading: String) {


        val tvName = findViewById<CollapsingToolbarLayout>(R.id.each_post_title)
        tvName!!.title = heading
    }

    private fun initOtherViews(text: String, imgLinks: ArrayList<String>?) {
        poster.text = postData.getusername()
        layoutAbout = findViewById(R.id.test)
        post_content_full.text = text.replace("[img*]", "")

        Handler().postDelayed({

            layoutAbout!!.visibility = View.VISIBLE
        }, 500)
        layoutAbout!!.alpha = 0f
        layoutAbout!!.translationY = -30f
        layoutAbout!!.animate()
            .setDuration(300)
            .alpha(1f)
            .translationY(0f)

        if (imgLinks != null)
            initContentWithImage(text, imgLinks)


    }

    private fun initContentWithImage(text: String, imgLinks: ArrayList<String>) {
        var imgBitmap = ArrayList<Bitmap>(imgLinks.size)
        Log.d("TAG", "initContentWithImage: $imgLinks")
        GlobalScope.launch {
            for (i in 0..imgLinks.size - 1) {
                try {
                    imgBitmap.add(
                        BitmapFactory.decodeStream(
                            URL(imgLinks[i]).openConnection().getInputStream()
                        )
                    )
                } catch (e: Exception) {
                    Log.d("TAG", "initContentWithImage: ${e.message}")
                }
            }
            withContext(Dispatchers.Main) {
                parseContent(text, imgBitmap)
            }
        }


    }

    private fun parseContent(text: String, imgBitmaps: ArrayList<Bitmap>) {
        if (imgBitmaps == null) {
            Log.d("TAG", "parseContent: Returned")
            return
        }
        var count = 0
        val final = SpannableStringBuilder()
        var i = 0

        while (i <= text.length - 1) {

            if (text[i] == '[') {
                try {
                    if (text[i + 1] == 'i' && text[i + 2] == 'm' && text[i + 4] == '*') {

                        final.append(imgBitmaps.get(count).let { getImageSpannable(it) })

                        count++
                        i += 5
                    }
                } catch (e: java.lang.Exception) {
                    Log.d("TAG", "parseContent: ${e.message}")
                }
            } else
                final.append(text[i])
            i++
        }
//        i-=1
//        if (text[text.length - 1] != ']') {
//            for (i in 4 downTo 1)
//                final.append(text[text.length - i])
//        }

        Log.d("TAG", "parseContent: $final")
        post_content_full.text = final
    }

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
                TODO("Implement Profile Function")
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onBackPressed() {
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
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_full_blog, menu)
        for (i in 0 until menu!!.size()) {
            val item = menu!!.getItem(i)
            val spanString =
                SpannableString(menu!!.getItem(i).title.toString())
            spanString.setSpan(
                ForegroundColorSpan(Color.WHITE),
                0,
                spanString.length,
                0
            ) //fix the color to white
            item.title = spanString
        }
        return true
    }


    private fun getImageSpannable(Bm: Bitmap): Spannable? {


        val originalBitmap = Bm
        val bitmap = Bitmap.createScaledBitmap(
            originalBitmap,
            originalBitmap.width,
            originalBitmap.height,
            true
        )
        val dr: Drawable = BitmapDrawable(resources, bitmap)
        dr.setBounds(0, 0, bitmap.width - 50, bitmap.height - 50)
        val imageSpannable: Spannable = SpannableString("\uFFFC")
        val imgSpan = ImageSpan(dr, DynamicDrawableSpan.ALIGN_BOTTOM)
        imageSpannable.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return imageSpannable
    }
}