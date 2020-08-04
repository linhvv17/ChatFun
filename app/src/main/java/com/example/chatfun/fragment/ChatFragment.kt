package com.example.chatfun.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.adapter.UserAdapter
import com.example.chatfun.model.Chat
import com.example.chatfun.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class ChatFragment: Fragment() {
    private lateinit var mUserAdapter: UserAdapter
    private lateinit var mListChat: List<Chat>
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEdtText: EditText
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mListChat = ArrayList<Chat>()
        retrieveAllUsers()
        val view =  inflater.inflate(R.layout.chat_fragment,container,false)
        recyclerView = view.findViewById(R.id.rc_search)
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.layoutManager = layoutManager
        recyclerView!!.setHasFixedSize(true)
        searchEdtText = view.findViewById(R.id.edt_search_user)
        mUserAdapter = UserAdapter(context, mListChat!! as ArrayList<User>,false)
        recyclerView!!.adapter = mUserAdapter

        searchEdtText!!.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchForUser(s.toString().toLowerCase())
            }

        })
        return view
    }

    private fun retrieveAllUsers(){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")
        refUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (mListChat as ArrayList<User>).clear()
                if (searchEdtText!!.text.toString() == ""){
                    for (dataSnapshot in p0.children){
                        val chat: Chat? = dataSnapshot.getValue(Chat::class.java)
                        if (!(chat!!.getMessageId()).equals(firebaseUserID)){
                            (mListChat as ArrayList<Chat>).add(chat)
                        }
                    }
                    mUserAdapter = UserAdapter(context!!, mListChat!! as ArrayList<User>,false)
                    mUserAdapter!!.notifyDataSetChanged()
                    recyclerView!!.adapter = mUserAdapter
                }
            }

        })

    }
    private fun searchForUser(str:String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance().reference
            .child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str+"\uf8ff")
        queryUsers.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mListChat as ArrayList<User>).clear()
                for (dataSnapshot in p0.children) {
                    val user: User? = dataSnapshot.getValue(User::class.java)
                    if (user!!.getUid() != firebaseUserID) {
                        (mListChat as ArrayList<User>).add(user)
                    }
                }
                mUserAdapter = UserAdapter(context!!, mListChat!! as ArrayList<User>,false)
                mUserAdapter!!.notifyDataSetChanged()
                recyclerView!!.adapter = mUserAdapter
            }
        })
    }
}