package com.example.chatfun.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.model.Chat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class ChatAdapter(
    mContext: Context?,
    mChatList: List<Chat>,
    urlImg: String) : RecyclerView.Adapter<ChatAdapter.ViewHolder>(){
    private var mContext = mContext
    private var mChatList: List<Chat> = mChatList
    private var urlImg = urlImg
    private var firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var imgProfileReceiver : CircleImageView? = null
        var showTextMessage : TextView? = null
        var tvSeen : TextView? = null
        var showSendImageMessage : ImageView? = null
        var showImageMessageReceiver : ImageView? = null
        init {
            imgProfileReceiver = itemView.findViewById(R.id.img_profile_receiver)
            showTextMessage = itemView.findViewById(R.id.show_text_message)
            tvSeen = itemView.findViewById(R.id.tv_seen)
            showSendImageMessage = itemView.findViewById(R.id.show_send_image_message)
            showImageMessageReceiver = itemView.findViewById(R.id.show_image_message_receiver)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder
    {
        return if (position == 1){
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_right,parent, false)
            ViewHolder(view)
        } else{
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.message_item_left,parent, false)
            ViewHolder(view)
        }
//        val view = LayoutInflater.from(mContext)
//            .inflate(R.layout.user_search_item_layout,parent, false)
//        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mChatList!!.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val chat = mChatList[position]
        Picasso.get().load(urlImg).into(holder.imgProfileReceiver)
        //nếu gửi ảnh
        if (chat.message == "sen your an image" &&
            chat.url != ""
        ){

            // người gửi
            if (chat.sender == firebaseUser!!.uid){
                holder.showTextMessage!!.visibility = View.GONE
                holder.showSendImageMessage!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.showSendImageMessage)

            }
            //người nhận
            else if (chat.sender != firebaseUser!!.uid){
                holder.showTextMessage!!.visibility = View.GONE
                holder.showImageMessageReceiver!!.visibility = View.VISIBLE
                Picasso.get().load(chat.url).into(holder.showImageMessageReceiver)
            }

        }
        //gửi text
        else{
            //cho cac view hien len
            holder.showTextMessage!!.text = chat.message
//            holder.showTextMessage!!.visibility = View.VISIBLE
        }
        //send and seen
        if (position == mChatList.size -1){
            if (chat.isSeen){
                holder.tvSeen!!.text = "Seen"

                if (chat.message == "sen your an image" &&
                    chat.url != ""
                ){
                    val lp: RelativeLayout.LayoutParams? = holder.tvSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0,250,10,0)
                    holder.tvSeen!!.layoutParams = lp
                }
            } else{
                holder.tvSeen!!.text = "Sent"

                if (chat.message == "sen your an image" &&
                    chat.url != ""
                ){
                    val lp: RelativeLayout.LayoutParams? = holder.tvSeen!!.layoutParams as RelativeLayout.LayoutParams?
                    lp!!.setMargins(0,250,10,0)
                    holder.tvSeen!!.layoutParams = lp
                }
            }


        }else{
            holder.tvSeen!!.visibility = View.GONE
        }

    }

    override fun getItemViewType(position: Int): Int {
//        return super.getItemViewType(position)
        return if (mChatList[position].sender == firebaseUser!!.uid)
        {
            1
        }
        else
        {
            0
        }
    }
}