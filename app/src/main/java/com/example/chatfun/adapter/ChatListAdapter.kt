package com.example.chatfun.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.MessageChatActivity
import com.example.chatfun.R
import com.example.chatfun.VisitUserProfileActivity
import com.example.chatfun.model.ChatList
import com.example.chatfun.model.User
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.collections.ArrayList

class ChatListAdapter(
    mContext: Context?,
    mListChat: ArrayList<ChatList>,
    isChatCheck: Boolean
): RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {
    private val mContext: Context? = mContext
    private val mListChat: ArrayList<ChatList> = mListChat
    private val isChatCheck: Boolean = isChatCheck

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvCountMessage: TextView = itemView.findViewById(R.id.count_message)
        var tvUserNameSend: TextView = itemView.findViewById(R.id.username_send)
        var tvLastMessage: TextView = itemView.findViewById(R.id.message_last)
        //        var lastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        var imgAvatarSend: CircleImageView = itemView.findViewById(R.id.avatar_send)
//        var imgOnline: CircleImageView = itemView.findViewById(R.id.img_view_online)
//        var img_offline: CircleImageView = itemView.findViewById(R.id.img_view_offline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // infalater
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.user_search_item_layout,parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mListChat.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatlist: ChatList = mListChat[position]
//        holder.tvName.text = user.getSearch()
//        Picasso.get().load(user.getProfile()).into(holder.imgProfile)
//        holder.itemView.setOnClickListener {
//            val options = arrayOf<CharSequence>(
//                "Send Message",
//                "Visit Profile"
//            )
//            val builder = AlertDialog.Builder(mContext)
//            builder.setTitle("What do you want?")
//            builder.setItems(options, DialogInterface.OnClickListener{ dialog, position ->
//                if (position == 0){
//                    val intent = Intent(mContext!!, MessageChatActivity::class.java)
//                    intent.putExtra("visit_id",user.getUid())
//                    mContext.startActivity(intent)
//                }
//                if (position == 1){
//                    val intent = Intent(mContext!!, VisitUserProfileActivity::class.java)
//                    intent.putExtra("visit_id",user.getUid())
//                    mContext.startActivity(intent)
//                }
//            })
//
//            builder.show()
//        }
    }
}