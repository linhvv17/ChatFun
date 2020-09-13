package com.example.chatfun.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.adapter.ChatListAdapter
import com.example.chatfun.model.Chat
import com.example.chatfun.model.ChatList
import com.example.chatfun.model.User
import com.example.chatfun.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.chat_fragment.*
import kotlin.collections.ArrayList

class ChatFragment: Fragment() {
    private lateinit var adapter: ChatListAdapter
//    private lateinit var  mUserAdapter: UserAdapter
    private lateinit var userList: ArrayList<User>
    private lateinit var chatlistList: ArrayList<ChatList>
    private lateinit var recyclerView: RecyclerView
    var firebaseAuth: FirebaseAuth? = null
    var currentUser: FirebaseUser? = null
    var reference: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.chat_fragment,container,false)
        recyclerView = view.findViewById(R.id.rc_chat)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView!!.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.setHasFixedSize(true)
        firebaseAuth = FirebaseAuth.getInstance()
        currentUser = FirebaseAuth.getInstance().currentUser
        chatlistList = ArrayList()

        reference = FirebaseDatabase.getInstance().reference.child("ChatList").child(currentUser!!.uid)
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (chatlistList as ArrayList<ChatList>).clear()
                for (dataSnapshot in p0.children){
                    val chatList: ChatList? = dataSnapshot.getValue(ChatList::class.java)
                    chatlistList.add(chatList!!)
//                    retrieveChatList()
                }
                retrieveChatList()
            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun loadChats() {

    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(currentUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList(){
        userList = ArrayList<User>()
//        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (userList as ArrayList<User>).clear()
                    for (dataSnapshot in p0.children){
                        val user: User? = dataSnapshot.getValue(User::class.java)
                        for (eachChatList in chatlistList){
                            if (user!!.getUid().equals(eachChatList.getId())){
                                (userList as ArrayList).add(user!!)
                                break
                            }
                        }
                    }
                adapter = ChatListAdapter(context!!, userList!!)
                recyclerView!!.adapter = adapter
                //
                for (i in 0 until userList!!.size){
                    lastMessage(userList[i].getUid())
                }
                }

        })

    }

    private fun lastMessage(userId: String?) {
        val reference = FirebaseDatabase.getInstance().getReference("Chats")
        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                var lastMessage = "default"
                var lastMessageTime = "11:11"
                for (dataSnapshot in p0.children){
                    val chat: Chat = dataSnapshot.getValue(Chat::class.java) ?: continue
                    val sender = chat.getSender()
                    val receiver = chat.getReceiver()
                    if (sender==null&&receiver==null){
                        continue
                    }
                    if (chat.getReceiver().equals(currentUser!!.uid)&& chat.getSender().equals(userId) ||
                        chat.getSender().equals(currentUser!!.uid)&& chat.getReceiver().equals(userId)){
                        lastMessage = chat.getMessage()!!
                        lastMessageTime = chat.getMessageTime()!!
                    }
                }
                adapter.setLastMessageHashMap(userId!!,lastMessage)
                adapter.setLastMessageHashMapTime(userId!!, lastMessageTime)
                adapter.notifyDataSetChanged()
            }
        })
    }
}