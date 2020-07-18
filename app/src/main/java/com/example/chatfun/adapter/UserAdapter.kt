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
import com.example.chatfun.model.User
import com.example.chatfun.VisitUserProfileActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class UserAdapter(
    mContext: Context?,
    mUsers: ArrayList<User>,
    isChatCheck: Boolean
): RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    private val mContext: Context? = mContext
    private val mUsers: ArrayList<User> = mUsers
    private val isChatCheck: Boolean = isChatCheck

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvName: TextView = itemView.findViewById(R.id.tv_username_profile_search)
//        var lastMessage: TextView = itemView.findViewById(R.id.tv_last_message)
        var imgProfile: CircleImageView = itemView.findViewById(R.id.img_view_profile_search)
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
        return mUsers.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user: User = mUsers[position]
        holder.tvName.text = user.search
        Picasso.get().load(user.profile).into(holder.imgProfile)
        holder.itemView.setOnClickListener {
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options, DialogInterface.OnClickListener{ dialog, position ->
                if (position == 0){
                    val intent = Intent(mContext!!, MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.uid)
                    mContext.startActivity(intent)
                }
                if (position == 1){
                    val intent = Intent(mContext!!, VisitUserProfileActivity::class.java)
                    intent.putExtra("visit_id",user.uid)
                    mContext.startActivity(intent)
                }
            })

            builder.show()
        }
    }


}