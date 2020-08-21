package com.example.chatfun

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.adapter.CommentAdapter
import com.example.chatfun.model.Comment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.lang.Exception
import java.util.*

class PostDetailActivity : AppCompatActivity() {

    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentLists: ArrayList<Comment>
    private lateinit var recyclerViewComment: RecyclerView

    private var mProcessComment: Boolean = false
    private var mProcessLike: Boolean = false

    //progress bar
    private lateinit var pd : ProgressDialog

    //
    private lateinit var firebaseAuth: FirebaseAuth

    // get cho comment
    var postId: String? = null
    var postLikes: String? = null
    var uName: String? = null
    var userProfile: String? = null

    //
//    var postDes: String? = null
//    var postTitle: String? = null
//    var postImage: String? = null
//    var postTime: String? = null
//    var uid: String? = null

    //
    var myId: String? = null
    var myName: String? = null
    var myProfile: String? = null
    var myComment: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        //actionbar
//        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar
//        applicationContext.actionBar!!.title = "Add new post"
//        actionBar!!.setDisplayShowHomeEnabled(true)
//        actionBar!!.setDisplayHomeAsUpEnabled(true)

        //get postId
        val intent = intent
        postId = intent.getStringExtra("postId")

        //
        pd = ProgressDialog(this@PostDetailActivity)

        //
        loadPostInfo()
        checkUserStatus()
        loadUserInfo()
        setLikes()
        loadComments()
        btn_comment_detail.setOnClickListener {
            postComment()
        }
        //
        btn_like_detail.setOnClickListener {
            likePost()
        }


    }

    private fun loadComments() {
        //set recyclerview
        recyclerViewComment = findViewById(R.id.rc_comments)
        val layoutManager = LinearLayoutManager(applicationContext)
//        layoutManager.stackFromEnd = true
//        layoutManager.reverseLayout = true
        recyclerViewComment.layoutManager = layoutManager
        //
        commentLists = arrayListOf()
        val commentRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId!!).child("Comments")
        commentRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                commentLists.clear()
                for (ds in p0.children){
                    val comment = ds.getValue(Comment::class.java)
                    commentLists.add(comment!!)
                    commentAdapter = CommentAdapter(applicationContext, commentLists, myId, postId!!)
                    recyclerViewComment.adapter = commentAdapter
                }

            }

        })
    }

    private fun likePost() {
        mProcessLike = true
        val refLikes : DatabaseReference= FirebaseDatabase.getInstance().getReference("Likes")
        val refPosts : DatabaseReference= FirebaseDatabase.getInstance().getReference("Posts")
        refLikes.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(mProcessLike){
                    if(p0.child(postId!!).hasChild(myId!!)){
                        //khi da like roi
                        refPosts.child(postId!!).child("postLikes").setValue(""+(postLikes!!.toInt()-1))
                        refLikes.child(postId!!).child(myId!!).removeValue()
                        mProcessLike=false

//                        btn_like_detail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like, 0,0,0)
//                        btn_like_detail.text = "Like"

                    } else{
                        //khi chua like
                        refPosts.child(postId!!).child("postLikes").setValue(""+(postLikes!!.toInt()+1))
                        refLikes.child(postId!!).child(myId!!).setValue("Liked")
                        mProcessLike=false

//                        btn_like_detail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24, 0,0,0)
//                        btn_like_detail.text = "Liked"

                    }
                }
            }

        })


    }
    private fun setLikes() {
        val likesRef = FirebaseDatabase.getInstance().reference.child("Likes")
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postId!!).hasChild(myId!!)){
                    //user like
                    btn_like_detail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24,0,0,0)
                    btn_like_detail.text = "Liked"

                } else {
                    //user not like
                    btn_like_detail.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0)
                    btn_like_detail.text = "Like"
                }

            }
        })
    }

    private fun postComment() {
        pd!!.setMessage("Publishing post")
        pd!!.show()

        // lay comment
        val comment: String = edt_comment_detail.text.toString().trim()
        if (TextUtils.isEmpty(comment)){
            Toast.makeText(applicationContext, "Hãy thêm comment", Toast.LENGTH_LONG).show()
            return
        }
        //thoi gian
        val timeStamp: String = (System.currentTimeMillis()).toString()
        // moi post co 1 child comment
        val myRef : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId!!).child("Comments")
        val postHashMap = HashMap<Any?, String?>()
        //post
        postHashMap["commentId"] = timeStamp
        postHashMap["commentContent"] = comment
        postHashMap["commentTime"] = timeStamp
        postHashMap["userCommentId"] = myId
        postHashMap["userCommentName"] = myName
        postHashMap["userCommentProfile"] = myProfile

        //
        myRef.child(timeStamp).setValue(postHashMap)
            .addOnCompleteListener {
                //reset view
                Toast.makeText(this@PostDetailActivity, "Comment Published",Toast.LENGTH_LONG).show()
                edt_comment_detail.setText("")
                updateCommentCount()
                pd.dismiss()
            }
            .addOnSuccessListener {object : OnSuccessListener<Void> {
                override fun onSuccess(p0: Void?) {
                    pd!!.dismiss()
                    Toast.makeText(this@PostDetailActivity, "Comment Published",Toast.LENGTH_LONG).show()
                    edt_comment_detail.setText("")
                    updateCommentCount()
                }

            }
            }
            .addOnFailureListener {object: OnFailureListener {
                override fun onFailure(p0: java.lang.Exception) {
                    pd!!.dismiss()
                    Toast.makeText(this@PostDetailActivity, ""+p0.message,Toast.LENGTH_LONG).show()
                }

            }
            }

    }

    //
    private fun updateCommentCount() {
        mProcessComment = true
        val refComment : DatabaseReference= FirebaseDatabase.getInstance().getReference("Posts").child(postId!!)
        refComment.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if(mProcessComment){
                    val comment : String = ""+p0.child("postComments").value
                    val newCommentValue = comment.toInt()+1
                    refComment.child("postComments").setValue(""+newCommentValue)
                    mProcessComment = false
                }

            }

        })
    }

    private fun loadUserInfo() {
        val myRef : Query = FirebaseDatabase.getInstance().getReference("Users")
        myRef!!.orderByChild("uid").equalTo(myId)
            .addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children) {
                        myName = "" + ds.child("username").value
                        myProfile = "" + ds.child("profile").value
                        try {
                            Picasso.get().load(myProfile).placeholder(R.drawable.bellerin).into(ava_user_comment_detail)
                        }catch(e: Exception) {

                        }
                    }
                }

            })
    }

    private fun loadPostInfo() {
        //lay post bang postId
        val ref = FirebaseDatabase.getInstance().getReference("Posts")
        val query = ref!!.orderByChild("postId").equalTo(postId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (ds in p0.children){
                    postLikes = ""+ds.child("postLikes").value
                    uName = ""+ds.child("uName").value
                    userProfile = ""+ds.child("userProfile").value

                    //
                    var postDes: String? = ""+ds.child("postDes").value
                    var postTitle: String? = ""+ds.child("postTitle").value
                    var postImage: String? = ""+ds.child("postImage").value
                    var postTime: String? = ""+ds.child("postTime").value
                    var commentCount: String? = ""+ds.child("postComments").value
                    var uid: String? = ""+ds.child("uid").value
//                    //convert thoi gian
                    val calendar: Calendar = Calendar.getInstance(Locale.getDefault())
                    if (postTime != null) {
                        calendar.timeInMillis = postTime!!.toLong()
                    }
                    val pTime: String = DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()

                    //
                    tv_show_username_post_detail.text = uName
                    tv_time_post_detail.text = pTime
                    Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(ava_user_comment_detail)
                    tv_show_title_post_detail.text = postTitle
                    tv_show_description_post_detail.text = postDes
                    tv_count_like_detail.text = "$postLikes Likes"
                    tv_count_comment_detail.text = "$commentCount Comments"
                    //image post
                    if (postImage.equals("noImage")){
                        //no image post
                        img_content_post_detail.visibility = View.GONE

                    } else{
                        try {
                            Picasso.get().load(postImage).placeholder(R.drawable.bellerin).into(img_content_post_detail)
                        }catch(e: Exception) {

                        }
                    }

//                    //set anh cho user comment
//                    try {
//                        Picasso.get().load(userProfile).placeholder(R.drawable.bellerin).into(ava_user_comment_detail)
//                    }catch(e: Exception) {
//
//                    }

                }
            }

        })
    }

    private fun checkUserStatus(){
        //get current user
        firebaseAuth = FirebaseAuth.getInstance()
        val user = firebaseAuth!!.currentUser
        if (user != null){
            myId = user.uid

        }else{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}