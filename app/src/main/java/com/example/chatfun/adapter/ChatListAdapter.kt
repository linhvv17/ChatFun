package com.example.chatfun.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.activity.MessageChatActivity
import com.example.chatfun.R
import com.example.chatfun.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatListAdapter(
    mContext: Context?,
    mUserList: ArrayList<User>
): RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    private val mContext: Context? = mContext
    private val mUserList: ArrayList<User> = mUserList
    private val lastMessageMap: HashMap<String, String>? = HashMap()
    private val lastMessageMapTime: HashMap<String, String>? = HashMap()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvTimeLast: TextView = itemView.findViewById(R.id.time_last_mess)
        var tvUserNameSend: TextView = itemView.findViewById(R.id.username_send)
        var tvLastMessage: TextView = itemView.findViewById(R.id.message_last)
        var imgAvatarSend: CircleImageView = itemView.findViewById(R.id.avatar_send)
//        var imgOnline: CircleImageView = itemView.findViewById(R.id.img_view_online)
//        var img_offline: CircleImageView = itemView.findViewById(R.id.img_view_offline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // infalater
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_message, parent, false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var userChatId: String? = mUserList[position].getUid()
        var userChatName: String? = mUserList[position].getUsername()
        var userChatImg: String? = mUserList[position].getProfile()
        var lastMessage = lastMessageMap!![userChatId]
        var lastMessageTime = lastMessageMapTime!![userChatId]
        //set dư lieu
        holder.tvUserNameSend.text = userChatName
        if (lastMessage == null) {
            holder.tvLastMessage.visibility = View.GONE
        } else {
            holder.tvLastMessage.visibility = View.VISIBLE
            holder.tvLastMessage.text = lastMessage
            //convert time to dd/mm/yyyy hh:mm am/pm
            val calendar = Calendar.getInstance(Locale.getDefault())
            if (lastMessageTime != null) {
                calendar.timeInMillis = lastMessageTime.toLong()
            }
            val lastTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()
            holder.tvTimeLast.text = lastTime
        }
        try {
            Picasso.get().load(userChatImg).placeholder(R.drawable.bellerin)
                .into(holder.imgAvatarSend)
        } catch (e: Exception) {

        }

        //click
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext!!, MessageChatActivity::class.java)
            intent.putExtra("visit_id", userChatId)
            mContext.startActivity(intent)
        }
    }

    fun setLastMessageHashMap(userId: String, lastMessage: String) {
        lastMessageMap!![userId] = lastMessage
    }

    fun setLastMessageHashMapTime(userId: String, lastMessageTime: String) {
        lastMessageMapTime!![userId] = lastMessageTime
    }
}

