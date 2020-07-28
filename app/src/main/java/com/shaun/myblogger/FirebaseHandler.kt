package com.shaun.myblogger

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase

//LETS MAKE IT Splash SCREEN TOO
class FirebaseHandler : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
//
//
//        val appLinkAction = intent.action
//        val appLinkData: Uri? = intent.data
//        if (Intent.ACTION_VIEW == appLinkAction) {
//            appLinkData?.lastPathSegment?.also { recipeId ->
//                Uri.parse("content://com.recipe_app/recipe/")
//                    .buildUpon()
//                    .appendPath(recipeId)
//                    .build().also { appData ->
//                        Log.d("TAG", "handleIntent: ${appData.lastPathSegment}")
//                        val postid = appData.lastPathSegment.toString()
//                        val intent = Intent(this, FullBlogActivity::class.java)
//                        intent.putExtra("data", postid)
//                        startActivity(intent)
//                        finish()
//
//                    }
//            }
//
//        } else {


        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
//        }
    }
}