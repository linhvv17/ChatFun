package com.example.chatfun.adapter

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.activity.PostDetailActivity
import com.example.chatfun.R
import com.example.chatfun.activity.VisitUserProfileActivity
import com.example.chatfun.model.AESHelper
import com.example.chatfun.model.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter(
    mContext: Context?,
    mPosts: ArrayList<Post>
) : RecyclerView.Adapter<PostAdapter.ViewHolder?>() {
    private val mContext: Context? = mContext
    private val mPosts: ArrayList<Post> = mPosts
    private var mProcessLike: Boolean = false

    private  var likesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Likes")
    private  var postsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
    private  var myUId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): PostAdapter.ViewHolder {
        val row: View =
            LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false)
        return ViewHolder(row)
    }
    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //get data
        var postId: String? = mPosts[position].postId
        var postDes: String? = mPosts[position].postDes
        var postTitle: String? = mPosts[position].postTitle
        var postImage: String? = mPosts[position].postImage
        var postTime: String? = mPosts[position].postTime
        var postLikes: String? = mPosts[position].postLikes
        var postComments: String? = mPosts[position].postComments
        var userIdPost: String? = mPosts[position].uid
        var userName: String? = mPosts[position].uName
        var userProfile: String? = mPosts[position].userProfile

        //convert time to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (postTime != null) {
            calendar.timeInMillis = postTime.toLong()
        }
        val pTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()

        //set data
        holder.tvUserName.text = userName
        holder.tvTime.text = pTime
        holder.tvTitle.text = AESHelper().decrypt(postTitle)!!
        holder.tvDescr.text = AESHelper().decrypt(postDes)!!
        holder.tvLike.text = "$postLikes Likes"
        holder.tvComment.text = "$postComments Comments"
        Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(holder.imgUser)
//        Picasso.get().load(userProfile).placeholder(R.drawable.profile).into(holder.imgAvatar)

        //set like tung post
        setLikes(holder, postId)

        //set user data
        if (postImage.equals("noImage")){
            //no image post
            holder.imgPost.visibility = View.GONE

        } else{
            try {
                Picasso.get().load(postImage).placeholder(R.drawable.bellerin).into(holder.imgPost)

            }catch(e: Exception) {

            }
        }

        //click
        holder.btnLike.setOnClickListener {
//            Toast.makeText(mContext,"Like", Toast.LENGTH_LONG).show()
            val postLikes : Int = mPosts[position].postLikes!!.toInt()
            mProcessLike = true
            //
            val postIde : String = mPosts[position].postId!!.toString()
            likesRef.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    if(mProcessLike){
                        if(p0.child(postIde).hasChild(myUId)){
                            //khi da like roi
                            postsRef.child(postIde).child("postLikes").setValue(""+(postLikes-1))
                            likesRef.child(postIde).child(myUId).removeValue()
                            mProcessLike=false

                        } else{
                            //khi chua like
                            postsRef.child(postIde).child("postLikes").setValue(""+(postLikes+1))
                            likesRef.child(postIde).child(myUId).setValue("Liked")
                            mProcessLike=false

                        }
                    }
                }

            })
        }

        holder.btnComment.setOnClickListener {
//            Toast.makeText(mContext,"Comment", Toast.LENGTH_LONG).show()
            val intent = Intent(mContext, PostDetailActivity::class.java)
            intent.putExtra("postId",postId)
            mContext!!.startActivity(intent)
        }

//        holder.btnShare.setOnClickListener {
//            Toast.makeText(mContext,"Share", Toast.LENGTH_LONG).show()
//        }
        //
        holder.lnProfilePost.setOnClickListener {
            val intent = Intent(mContext!!, VisitUserProfileActivity::class.java)
            intent.putExtra("visit_id",userIdPost)
            mContext.startActivity(intent)
        }

        //delete
        holder.icMore.setOnClickListener {
            showMoreOption(holder.icMore, userIdPost, myUId, postId, postImage)
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    private fun showMoreOption(
        icMore: ImageView,
        userIdPost: String?,
        myUId: String,
        postId: String?,
        postImage: String?
    ) {
        var options: Array<String>? = null
        val builder = AlertDialog.Builder(mContext)
        builder.setTitle("Choose Option")
                options = arrayOf("Delete Post")
                builder.setItems(options, object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        if (which==0){
                            beginDelete(postId, postImage)
                        } else {
                        }
                    }

                })
                builder.create().show()
    }

    private fun beginDelete(postId: String?, postImage: String?) {
        if (postImage.equals("noImage")){
            deleteNoImage(postId)
        } else {
            deleteWithImage(postId, postImage)
        }
    }

    private fun deleteWithImage(postId: String?, postImage: String?) {
        val pd = ProgressDialog(mContext)
        pd.setMessage("Đang xóa")

        val picRef = FirebaseStorage.getInstance().getReferenceFromUrl(postImage!!)
        picRef.delete()
            .addOnCompleteListener {
                val query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(postId)
                query.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for (ds in p0.children){
                            ds.ref.removeValue()
                        }
                        Toast.makeText(mContext, "Delete Done",Toast.LENGTH_LONG).show()
                        pd.dismiss()
                    }
                })
            }

    }

    private fun deleteNoImage(postId: String?) {
        val pd = ProgressDialog(mContext)
        pd.setMessage("Đang xóa")
        val query = FirebaseDatabase.getInstance().getReference("Posts").orderByChild("postId").equalTo(postId)
        query.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (ds in p0.children){
                    ds.ref.removeValue()
                }
                Toast.makeText(mContext, "Delete Done",Toast.LENGTH_LONG).show()
                pd.dismiss()
            }
        })

    }

    private fun setLikes(holder: PostAdapter.ViewHolder, postKey: String?) {
        likesRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.child(postKey!!).hasChild(myUId)){
                    //user like
                    holder.btnLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_baseline_thumb_up_24,0,0,0)
                    holder.btnLike.text = "Liked"

                } else {
                    //user not like
                    holder.btnLike.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_like,0,0,0)
                    holder.btnLike.text = "Like"
                }

            }
        })

    }

    override fun getItemCount(): Int {
        return mPosts.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init
        var tvUserName: TextView = itemView.findViewById(R.id.tv_show_username_post)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time_post)
        var tvDescr: TextView = itemView.findViewById(R.id.tv_show_description_post)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_show_title_post)
        var tvLike: TextView = itemView.findViewById(R.id.tv_count_like)
        var tvComment: TextView = itemView.findViewById(R.id.tv_count_comment)
        var imgUser: CircleImageView = itemView.findViewById(R.id.img_show_user_post)
//        var imgAvatar: CircleImageView = itemView.findViewById(R.id.img_my_avatar)
        var imgPost: ImageView = itemView.findViewById(R.id.img_content_post)
        var icMore: ImageView = itemView.findViewById(R.id.ic_more_post)
        var btnLike: Button = itemView.findViewById(R.id.btn_like)
        var btnComment: Button = itemView.findViewById(R.id.btn_comment)
//        var btnShare: Button = itemView.findViewById(R.id.btn_share)
        var lnProfilePost: LinearLayout = itemView.findViewById(R.id.ln_profile_post)
    }
}