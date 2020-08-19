package com.example.chatfun.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.adapter.ChatListAdapter
import com.example.chatfun.adapter.UserAdapter
import com.example.chatfun.model.Chat
import com.example.chatfun.model.ChatList
import com.example.chatfun.model.User
import com.example.chatfun.notifications.Token
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.chat_fragment.*
import java.util.ArrayList

class ChatFragment: Fragment() {
    private lateinit var mChatListAdapter: ChatListAdapter
    private lateinit var  mUserAdapter: UserAdapter
    private lateinit var mUsers: List<User>
    private lateinit var mListChat: List<ChatList>
    private lateinit var recyclerView: RecyclerView
    var firebaseUser: FirebaseUser? = null

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


        firebaseUser = FirebaseAuth.getInstance().currentUser
        mListChat = ArrayList()

        val refChatList = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        refChatList.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (mListChat as ArrayList<User>).clear()
                for (dataSnapshot in p0.children){
                    val chatList: ChatList? = dataSnapshot.getValue(ChatList::class.java)
                    (mListChat as ArrayList).add(chatList!!)
                }
                retrieveChatList()
            }
        })

        updateToken(FirebaseInstanceId.getInstance().token)

        return view
    }

    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    private fun retrieveChatList(){
        mUsers = ArrayList<User>()
//        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<User>).clear()
                    for (dataSnapshot in p0.children){
                        val user: User? = dataSnapshot.getValue(User::class.java)
                        for (eachChatList in mListChat){
                            if (user!!.getUid().equals(eachChatList.getId())){
                                (mUsers as ArrayList).add(user!!)
                            }
                        }
                    }
                mUserAdapter = UserAdapter(context!!, mUsers!! as ArrayList<User>,false)
                rc_chat.adapter = mUserAdapter
                }

        })

    }
}