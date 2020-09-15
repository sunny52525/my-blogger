package com.shaun.myblogger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dd.processbutton.iml.ActionProcessButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase.getInstance
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user_info.*

private const val TAG="UserInfo"


class UserInfoActivity : AppCompatActivity(), ProgressGenerator.OnCompleteListener {
    val progressGenerator = ProgressGenerator(this)
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var firebasUserID:String=""
    private var hasChild=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

            firebasUserID=FirebaseAuth.getInstance().currentUser!!.uid
        lottieAnimationView.playAnimation()
        val save_button: ActionProcessButton = findViewById(R.id.save_button)
        save_button.setMode(ActionProcessButton.Mode.ENDLESS)
        save_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (user_info_name.text!!.isNotEmpty() && user_info_username.text!!.isNotEmpty()) {
                    progressGenerator.start(save_button)
                    save_button.isEnabled = false
                    user_info_name.isEnabled = false
                    user_info_username.isEnabled = false
                    userIDExist(user_info_username.text.toString())

                }else
                {
                    Toast.makeText(applicationContext, "Please Fill Up Everything", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun saveUserInfo(name: String, username: String, firebaseId: String) {

        val userHashMap=HashMap<String,Any>()
        val postId=ArrayList<String>()
        postId.add("abc")
        userHashMap["id"]=firebaseId
        userHashMap["name"]=name
        userHashMap["username"]=username.toLowerCase()
        userHashMap["Pphoto"]=""
        userHashMap["postIDs"]=postId
           refUsers= getInstance().reference.child("Users") .child(firebaseId)
           refUsers.updateChildren(userHashMap).addOnCompleteListener {
               if(it.isSuccessful){
                   val intent= Intent(this,HomeScreenActivity::class.java)
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                   startActivity(intent)
                   finish()
               }
           }

        refUsers= getInstance().reference.child("username").child(username)
        refUsers.setValue(FirebaseAuth.getInstance().currentUser!!.uid)

    }

    private fun userIDExist(username:String) {

        val rootRef = getInstance().reference
        val userNameRef = rootRef.child("username").child(username)
        val eventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: $dataSnapshot")
                if (!dataSnapshot.exists()) {
                    saveUserInfo(user_info_name.text.toString(),user_info_username.text.toString(),firebasUserID)
                }else{
                    Toast.makeText(applicationContext, "Please Choose Unique Username", Toast.LENGTH_SHORT).show()
                    save_button.isEnabled = true
                    user_info_name.isEnabled = true
                    user_info_username.isEnabled = true

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG",
                    databaseError.message
                ) //Don't ignore errors!
            }
        }
        userNameRef.addListenerForSingleValueEvent(eventListener)


    }

    override fun onComplete() {
        save_button.progress = 40
    }
}