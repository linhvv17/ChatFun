package com.example.chatfun.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.model.GroupChatList
import com.example.chatfun.GroupChatActivity
import com.example.chatfun.model.GroupChat
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class GroupChatListAdapter(
    mContext: Context?,
    mGroupListList: ArrayList<GroupChatList>
): RecyclerView.Adapter<GroupChatListAdapter.ViewHolder>() {
    private val mContext: Context? = mContext
    private val mGroupListList: ArrayList<GroupChatList> = mGroupListList
    //view holder class
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imageGroup : CircleImageView
        var tvTitleGroupChat : TextView
        var tvNameSender : TextView
        var tvMessageLast : TextView
        var tvTimeLast : TextView
        init {
            tvTitleGroupChat = itemView.findViewById(R.id.title_group_chat)
            tvNameSender = itemView.findViewById(R.id.name_sender)
            tvMessageLast = itemView.findViewById(R.id.tv_message_last)
            tvTimeLast = itemView.findViewById(R.id.tv_time_last)
            imageGroup = itemView.findViewById(R.id.icon_group_chat)
        }

//        var img_offline: CircleImageView = itemView.findViewById(R.id.img_view_offline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // infalater
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_group_chat, parent, false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mGroupListList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mGroupListList[position]
        //get data
        val groupId = model.groupId
        val groupIcon = model.groupIcon
        val groupTitle = model.groupTitle
        //
        holder.tvNameSender.text =""
        holder.tvMessageLast.text =""
        holder.tvTimeLast.text =""
        //load last message & time
        loadLastMessage(model,holder)
        //set data
        holder.tvTitleGroupChat!!.text = groupTitle
        try {
            Picasso.get().load(groupIcon).placeholder(R.drawable.bellerin).into(holder.imageGroup)
        }catch(e: Exception) {

        }
        //click
        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, GroupChatActivity::class.java)
            intent.putExtra("groupId",groupId)
            mContext!!.startActivity(intent)
        }

    }
    private fun loadLastMessage(
        model: GroupChatList,
        holder: GroupChatListAdapter.ViewHolder
    ) {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(model.groupId).child("Messages").limitToLast(1)//lay item message cuoi
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        //get data
                        val message = ""+ds.child("message").value
                        val timestamp = ""+ds.child("timestamp").value
                        val sender = ""+ds.child("sender").value
                        val type = ""+ds.child("type").value
                        //convert time to dd/mm/yyyy hh:mm am/pm
                        val calendar = Calendar.getInstance(Locale.getDefault())
                        if (timestamp != null) {
                            calendar.timeInMillis = timestamp.toLong()
                        }
                        val mTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()

                        if (type.equals("image")){
                            holder.tvMessageLast.text = "Send a photo!"
                        } else{
                            holder.tvMessageLast.text = message
                        }

                        holder.tvTimeLast.text  = mTime
                        //get infor cua user gui cuoi cung
                        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Users")
                        ref.orderByChild("uid").equalTo(sender)
                            .addValueEventListener(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    for (ds in p0.children){
                                        val name = ""+ds.child("username").value
                                        holder.tvNameSender.text = name
                                    }
                                }
                            })
                    }

                }

            })

    }
}