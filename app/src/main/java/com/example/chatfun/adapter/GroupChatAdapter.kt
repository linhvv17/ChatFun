package com.example.chatfun.adapter

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.activity.ViewFullSizeImageActivity
import com.example.chatfun.model.GroupChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class GroupChatAdapter(
    mContext: Context,
    mGroupChatList: ArrayList<GroupChat>
): RecyclerView.Adapter<GroupChatAdapter.ViewHolder?>() {
    private val mContext : Context
    private val mGroupChatList: ArrayList<GroupChat>

    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    var firebaseAuth = FirebaseAuth.getInstance()
    init {
        this.mContext = mContext
        this.mGroupChatList = mGroupChatList
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var nameTv : TextView? = null
        var messageTv : TextView? = null
        var timeTv : TextView? = null
        var messageIv : ImageView? = null
        init {
            nameTv = itemView.findViewById(R.id.tv_name)
            messageTv = itemView.findViewById(R.id.tv_mess)
            timeTv = itemView.findViewById(R.id.tv_time)
            messageIv = itemView.findViewById(R.id.iv_message)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == MSG_TYPE_RIGHT)
        {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.row_group_chat_right,parent, false)
            ViewHolder(view)
        }
        else
        {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.row_group_chat_left,parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mGroupChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model : GroupChat = mGroupChatList[position]
        val message = model.message
        val timestamp = model.timestamp
        val senderUid = model.sender
        val messageType = model.type


//set data
        if (messageType.equals("text")){
            //mess text
            holder.messageIv!!.visibility = View.GONE
            holder.messageTv!!.visibility = View.VISIBLE
            holder.messageTv!!.text = message
        } else {
            //mess image
            holder.messageIv!!.visibility = View.VISIBLE
            holder.messageTv!!.visibility = View.GONE
            try {
                Picasso.get().load(message).placeholder(R.drawable.bellerin).into(holder.messageIv)
            }catch(e: Exception) {

            }
            holder.messageIv!!.setOnClickListener {
                val myIntent = Intent(mContext, ViewFullSizeImageActivity::class.java)
                myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                myIntent.putExtra("url",message)
                mContext.startActivity(myIntent)
            }
//
//                val options = arrayOf<CharSequence>("View Full Size","Delete", "Cancel")
//                        //dialog
//                        var builder :AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
//                        builder.setTitle("Chose Option")
//                        //set option
//                        builder.setItems(options, object : DialogInterface.OnClickListener {
//                            override fun onClick(dialog: DialogInterface?, which: Int) {
//                                if (which==0){
//                                    val myIntent = Intent(mContext, ViewFullSizeImageActivity::class.java)
//                                    myIntent.putExtra("url",message)
//                                    mContext.startActivity(myIntent)
//                                }
////                                if (which==1){
////                                    deleteMessage(holder,position)
////                                }
//                            }
//                        })
//                        builder.create().show()
//
//            }
        }
        //convert time to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (timestamp != null) {
            calendar.timeInMillis = timestamp.toLong()
        }
        val mTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()
        holder.timeTv!!.text = mTime
        setUserName(model,holder)
    }



    private fun setUserName(model: GroupChat, holder: ViewHolder) {
        //lay thong tin nguoi gui tu uid cua model
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("uid").equalTo(model.sender)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        val name = ""+ds.child("username").value
                        holder.nameTv!!.text = name
                    }
                }

            })
    }

    override fun getItemViewType(position: Int): Int {
        return if (mGroupChatList[position].sender == firebaseAuth!!.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }
}