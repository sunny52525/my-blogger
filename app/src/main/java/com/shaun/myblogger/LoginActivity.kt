package com.shaun.myblogger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class LoginActivity : AppCompatActivity() {
    val RC_SIGN_IN: Int = 1

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        firebaseAuth = FirebaseAuth.getInstance()
        configureGoogleSignIn()
        sign_in_google.setOnClickListener {
            login_anim.show()
            sign_in_google.isClickable = false
            signIn()
        }
    }


    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)


    }

    private fun signIn() {
        val gApiClient = GoogleApiClient.Builder(this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
            .build()
        GlobalScope.launch {
            val c = gApiClient.blockingConnect()
            if (c.isSuccess && gApiClient.isConnected) {
                gApiClient.clearDefaultAccountAndReconnect().await()
            }
        }

        val mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
        val signInIntent: Intent = mGoogleSignInClient.signInIntent

        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                login_anim.hide()
                sign_in_google.isClickable = true

                Log.e("My", "firebaseAuthWithGoogle: ${e.message}")
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                login_anim.hide()
                sign_in_google.isClickable = true
                Log.e("My", "firebaseAuthWithGoogle: ${e.message}")
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }

        }
    }


    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct!!.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
//                val intent:Intent


                val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
                val rootRef = FirebaseDatabase.getInstance().reference
                val userNameRef = rootRef.child("Users").child(firebaseUser)
                val eventListener: ValueEventListener = object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        Log.d("TAG", "onDataChange: $dataSnapshot")
                        if (!dataSnapshot.exists()) {
                            intent = Intent(this@LoginActivity, UserInfoActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            intent = Intent(this@LoginActivity, HomeScreenActivity::class.java)
                            startActivity(intent)
                            finish()

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d(
                            "TAG",
                            databaseError.message
                        ) //Don't ignore errors!
                    }
                }
                userNameRef.addListenerForSingleValueEvent(eventListener)

            } else {
                login_anim.hide()
                sign_in_google.isClickable = true

                Log.e("My", "firebaseAuthWithGoogle: ${it.exception.toString()}")
                Log.d("TAG", "firebaseAuthWithGoogle: HERE")
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()

            }

        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            loading.visibility = View.VISIBLE
            loading.playAnimation()
            sign_in_google.visibility = View.GONE
            sign_in_text.visibility = View.GONE
            val firebaseUser = FirebaseAuth.getInstance().currentUser!!.uid
            val rootRef = FirebaseDatabase.getInstance().reference
            rootRef.keepSynced(true)
            val userNameRef = rootRef.child("Users").child(firebaseUser)
            val eventListener: ValueEventListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("TAG", "onDataChange: $dataSnapshot")
                    if (!dataSnapshot.exists()) {
                        Handler().postDelayed({
                            intent = Intent(this@LoginActivity, UserInfoActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000)


                    } else {
                        Handler().postDelayed({
                            intent = Intent(this@LoginActivity, HomeScreenActivity::class.java)
                            startActivity(intent)
                            finish()
                        }, 2000)


                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(
                        "TAG",
                        databaseError.message
                    ) //Don't ignore errors!
                }
            }
            userNameRef.addListenerForSingleValueEvent(eventListener)
        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, LoginActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}