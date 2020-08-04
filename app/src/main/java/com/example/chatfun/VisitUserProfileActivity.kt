package com.example.chatfun

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatfun.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfileActivity : AppCompatActivity() {
    var userIdVisit: String = ""
    var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visit_user_profile)
        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
//        firebaseUser = FirebaseAuth.getInstance().currentUser

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    user = p0.getValue(User::class.java)
                    tv_username_display.text = user!!.getUsername()
                    Picasso.get().load(user!!.getProfile()).into(img_avatar_display)
                    Picasso.get().load(user!!.getCover()).into(img_view_cover_display)
                }


            }
        )
        ic_display_facebook.setOnClickListener {
            val uri = Uri.parse(user!!.getFacebook())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        ic_display_instagram.setOnClickListener {
            val uri = Uri.parse(user!!.getInstagram())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        ic_display_youtobe.setOnClickListener {
            val uri = Uri.parse(user!!.getWebsite())
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        btn_send_display.setOnClickListener {
            val intent = Intent()
            intent.setClass(this, MessageChatActivity::class.java)
            intent.putExtra("visit_id",user!!.getUid())
            startActivity(intent)
        }
    }
}