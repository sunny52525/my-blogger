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
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}