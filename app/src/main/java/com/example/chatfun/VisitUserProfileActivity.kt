package com.example.chatfun

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.adapter.PostAdapter
import com.example.chatfun.model.Post
import com.example.chatfun.model.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_visit_user_profile.*

class VisitUserProfileActivity : AppCompatActivity() {
    private lateinit var mPostAdapter: PostAdapter
    private lateinit var mPosts: ArrayList<Post>
    private lateinit var recyclerView: RecyclerView
    var userIdVisit: String = ""
    var user: User? = null
//    var firebaseUser: FirebaseUser? = null
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

    //load post
    //set recyclerview
    recyclerView = findViewById(R.id.rc_personal_post)
    val layoutManager = LinearLayoutManager(this@VisitUserProfileActivity)
    layoutManager.stackFromEnd = true
    layoutManager.reverseLayout = true
    recyclerView.layoutManager = layoutManager
    //init post list
    mPosts = arrayListOf()
    loadAllPost()
    }

    private fun loadAllPost() {
        //path cua all post
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        //get all data
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@VisitUserProfileActivity, ""+p0.message, Toast.LENGTH_LONG).show()

            }

            override fun onDataChange(p0: DataSnapshot) {
                mPosts.clear()
                for (ds in p0.children){
                    val post: Post? = ds.getValue(Post::class.java)
                    if (post!!.uid == userIdVisit){
                        mPosts.add(post!!)

                        mPostAdapter = PostAdapter(this@VisitUserProfileActivity, mPosts)

                        //set adapter
                        recyclerView.adapter = mPostAdapter
                    }

                }
            }

        })
    }
}