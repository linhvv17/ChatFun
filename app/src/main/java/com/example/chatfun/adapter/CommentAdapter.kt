package com.example.chatfun.adapter

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.model.Comment
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.ArrayList

class CommentAdapter(
    mContext: Context?,
    mComments: ArrayList<Comment>,
    myId: String?,
    postId: String?
) : RecyclerView.Adapter<CommentAdapter.ViewHolder?>() {
    private val mContext: Context? = mContext
    private val mComments: ArrayList<Comment> = mComments
    private val myId: String? = myId
    private val postId: String? = postId
//    private  var likesRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Likes")
//    private  var postsRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
//    private  var myUId: String = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): CommentAdapter.ViewHolder {
        val row: View =
            LayoutInflater.from(mContext).inflate(R.layout.row_comments, parent, false)
        return ViewHolder(row)
    }
    override fun onBindViewHolder( holder: ViewHolder, position: Int) {
        //get data
        var commentId: String? = mComments[position].commentId
        var commentTime: String? = mComments[position].commentTime
        var commentContent: String? = mComments[position].commentContent
        var userCommentId: String? = mComments[position].userCommentId
        var userCommentName: String? = mComments[position].userCommentName
        var userCommentProfile: String? = mComments[position].userCommentProfile

        //convert time to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (commentTime != null) {
            calendar.timeInMillis = commentTime.toLong()
        }
        val cTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()

        //set data
        holder.tvUserName.text = userCommentName
        holder.tvComment.text = commentContent
        holder.tvTime.text = cTime
        Picasso.get().load(userCommentProfile).placeholder(R.drawable.profile).into(holder.avataUser)

        // xoa comment
        holder.itemView.setOnClickListener {
            if (myId.equals(userCommentId)){
                val builder = AlertDialog.Builder(it.rootView.context)
                builder.setTitle("Delete Comment")
                builder.setMessage("Are you sure delete???")
                builder.setPositiveButton("Delete", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        //xoa
                        deleteComment(commentId)
                    }

                })
                builder.setNegativeButton("Cancel", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        //huy bo
                        dialog!!.cancel()
                    }

                })
                //show
                builder.create().show()
            }
        }
    }

    private fun deleteComment(commentId: String?) {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId!!)
        ref.child("Comments").child(commentId!!).removeValue()//lenh xoa comment
        //update lai so comment
        ref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val comment : String = ""+p0.child("postComments").value
                val newCommentValue = comment.toInt()-1
                ref.child("postComments").setValue(""+newCommentValue)
            }

        })

    }


    override fun getItemCount(): Int {
        return mComments.size
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //init
        var avataUser: ImageView = itemView.findViewById(R.id.img_ava_user_comment)
        var tvUserName: TextView = itemView.findViewById(R.id.tv_username_comment)
        var tvComment: TextView = itemView.findViewById(R.id.tv_comment)
        var tvTime: TextView = itemView.findViewById(R.id.tv_time_comment)

    }
}