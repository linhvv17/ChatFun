package com.example.chatfun.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.model.Chat
import com.example.chatfun.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_post_detail.*
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


class ChatAdapter(
    mContext: Context,
    mChatList: ArrayList<Chat>,
    urlImg: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder?>(){
    private val mContext : Context
    private val mChatList: ArrayList<Chat>
    private val urlImg : String
    private var zoomOut: Boolean = false
    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    init {
        this.mContext = mContext
        this.mChatList = mChatList
        this.urlImg = urlImg
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
    {
        return if (position == MSG_TYPE_RIGHT)
        {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_right,parent, false)
            ViewHolder(view)
        }
        else
        {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_left,parent, false)
            ViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return mChatList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = mChatList[position]
        var messTime: String? = mChatList[position].getMessageTime()
        //convert time to dd/mm/yyyy hh:mm am/pm
        val calendar = Calendar.getInstance(Locale.getDefault())
        if (messTime != null) {
            calendar.timeInMillis = messTime.toLong()
        }
        val mTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()
        try {
            Picasso.get().load(urlImg).placeholder(R.drawable.bellerin).into(holder.img_profile_receiver)
        }catch(e: Exception) {

        }
        //nếu gửi ảnh
        if (chat.getMessage() == "sen your an image" && chat.getUrl() != "")
            {
                // người gửi
                if (chat.getSender() == firebaseUser!!.uid)
                {
                    holder.show_text_message!!.visibility = GONE
                    holder.show_send_image_message!!.visibility = View.VISIBLE
                    Picasso.get().load(chat.getUrl()).into(holder.show_send_image_message)
                    holder.time_mess!!.text = mTime
//
                }
                //người nhận
                else if (chat.getSender() != firebaseUser!!.uid){
                    holder.show_text_message!!.visibility = GONE
                    holder.show_image_message_receiver!!.visibility = View.VISIBLE
                    Picasso.get().load(chat.getUrl()).into(holder.show_image_message_receiver)
                    holder.time_mess!!.text = mTime
                }

            }
            //gửi text
        else{
                holder.show_text_message!!.text = chat.getMessage().toString()
                holder.time_mess!!.text = mTime
            }
        //send and seen
        if (position == mChatList.size -1){
            if (chat.isIsSeen()!!){
                holder.tv_seen!!.text = "Seen"
//                if (chat.getMessage() == "sen your an image" && chat.getUrl() != "")
//                {
//                    val lp: RelativeLayout.LayoutParams? = holder.tv_seen!!.layoutParams as RelativeLayout.LayoutParams?
//                    lp!!.setMargins(0,250,10,0)
//                    holder.tv_seen!!.layoutParams = lp
//                }
            } else{
                holder.tv_seen!!.text = "Sent"
//                if (chat.getMessage() == "sen your an image" && chat.getUrl() != "")
//                {
//                    val lp: RelativeLayout.LayoutParams? = holder.tv_seen!!.layoutParams as RelativeLayout.LayoutParams?
//                    lp!!.setMargins(0,250,10,0)
//                    holder.tv_seen!!.layoutParams = lp
//                }
            }
        }
        else
        {
            holder.tv_seen!!.visibility = GONE
        }

    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var img_profile_receiver : CircleImageView? = null
        var show_text_message : TextView? = null
        var tv_seen : TextView? = null
        var time_mess : TextView? = null
        var show_send_image_message : ImageView? = null
        var show_image_message_receiver : ImageView? = null
        init {
            img_profile_receiver = itemView.findViewById(R.id.img_profile_receiver)
            show_text_message = itemView.findViewById(R.id.show_text_message)
            tv_seen = itemView.findViewById(R.id.tv_seen)
            time_mess = itemView.findViewById(R.id.time_mess)
            show_send_image_message = itemView.findViewById(R.id.show_send_image_message)
            show_image_message_receiver = itemView.findViewById(R.id.show_image_message_receiver)
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (mChatList[position].getSender() == firebaseUser!!.uid) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    /*override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {

    }*/
}