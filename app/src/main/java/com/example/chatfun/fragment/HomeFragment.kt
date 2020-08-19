package com.example.chatfun.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.AddPostActivity
import com.example.chatfun.R
import com.example.chatfun.VisitUserProfileActivity
import com.example.chatfun.adapter.PostAdapter
import com.example.chatfun.model.Chat
import com.example.chatfun.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.home_fragment.view.*
import kotlinx.android.synthetic.main.post_item.*
import kotlinx.android.synthetic.main.post_item.view.*

class HomeFragment: Fragment() {
    private lateinit var mPostAdapter: PostAdapter
    private lateinit var mPosts: ArrayList<Post>
    private lateinit var recyclerView: RecyclerView


    var firebaseUser: FirebaseUser? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val view =  inflater.inflate(R.layout.home_fragment,container,false)

        //ntent = intent
        view.btn_post.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
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
                    mPosts.add(post!!)

                    mPostAdapter = PostAdapter(activity, mPosts)

                    //set adapter
                    recyclerView.adapter = mPostAdapter
                }
            }

        })
    }
}