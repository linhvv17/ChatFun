package com.example.chatfun.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.activity.AddPostActivity
import com.example.chatfun.R
import com.example.chatfun.adapter.PostAdapter
import com.example.chatfun.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import java.lang.Exception

class HomeFragment: Fragment() {
    private var refUsers: DatabaseReference? = null
    private lateinit var mPostAdapter: PostAdapter
    private lateinit var mPosts: ArrayList<Post>
    private lateinit var recyclerView: RecyclerView
    private lateinit var img_my_avatar: ImageView


    var firebaseUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        val view =  inflater.inflate(R.layout.home_fragment,container,false)
        img_my_avatar = view.findViewById(R.id.img_my_avatar)

        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                Picasso.get().load(p0.child("profile").value.toString()).into(img_my_avatar)
            }

        })

        view.btn_post.setOnClickListener {
            val myIntent = Intent(context, AddPostActivity::class.java)
            startActivity(myIntent)
        }
        view.tv_description.setOnClickListener(View.OnClickListener {
            val myIntent = Intent(context, AddPostActivity::class.java)
            startActivity(myIntent)
        })



        //set recyclerview
        recyclerView = view.findViewById(R.id.rc_post)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        //init post list
        mPosts = arrayListOf()
        loadAllPost()

        return view
    }

    private fun loadAllPost() {
        //path cua all post
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        //get all data
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(activity, ""+p0.message, Toast.LENGTH_LONG).show()

            }

            override fun onDataChange(p0: DataSnapshot) {
                mPosts.clear()
                for (ds in p0.children){
                    val post: Post? = ds.getValue(Post::class.java)
                    //set image
//                    try {
//                        Picasso.get().load(post!!.userProfile).placeholder(R.drawable.profile).into(img_my_avatar)
//                    }catch(e: Exception) {
//
//                    }
                    mPosts.add(post!!)

                    mPostAdapter = PostAdapter(activity, mPosts)

                    //set adapter
                    recyclerView.adapter = mPostAdapter
                }
            }

        })
    }
}