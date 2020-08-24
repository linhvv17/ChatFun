package com.example.chatfun.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.activity.AddGroupActivity
import com.example.chatfun.adapter.GroupChatListAdapter
import com.example.chatfun.model.GroupChatList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.fragment_group.view.*

class GroupFragment: Fragment {
    private lateinit var groupChatListLists: ArrayList<GroupChatList>
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatListAdapter: GroupChatListAdapter
    var firebaseAuth: FirebaseAuth? = null

    constructor()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view =  inflater.inflate(R.layout.fragment_group,container,false)
        recyclerView = view.findViewById(R.id.rc_group_chat)
        val layoutManager = LinearLayoutManager(activity)
        recyclerView!!.layoutManager = layoutManager
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView!!.setHasFixedSize(true)
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupChatList()
        view.float_create_group.setOnClickListener {
            showDialogCreateGroup()
        }
        return view
    }

    private fun loadGroupChatList() {
        groupChatListLists = ArrayList()
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                groupChatListLists.clear()
                for (ds in p0.children){
                    //neu current user la nguoi tao
                    if (ds.child("Participants").child(firebaseAuth!!.uid!!).exists()){
                        val model = ds.getValue(GroupChatList::class.java)
                        if (model != null) {
                            groupChatListLists.add(model)
                        }
                    }
                    chatListAdapter = GroupChatListAdapter(context!!,groupChatListLists)
                    recyclerView!!.adapter = chatListAdapter
                }
            }

        })
    }

    private fun searchGroupChatList(query: String) {
        groupChatListLists = ArrayList()
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                groupChatListLists.clear()
                for (ds in p0.children){
                    //neu current user la nguoi tao
                    if (ds.child("Participants").child(firebaseAuth!!.uid!!).exists()){
                        //search bang title
                        if (ds.child("groupTitle").toString().toLowerCase().contains(query.toLowerCase())){
                            val model = ds.getValue(GroupChatList::class.java)
                            if (model != null) {
                                groupChatListLists.add(model)
                            }
                        }

                    }
                    chatListAdapter = GroupChatListAdapter(context,groupChatListLists)
                    recyclerView.adapter = chatListAdapter
                }
            }

        })
    }

    private fun showDialogCreateGroup() {
//        val options = arrayOf("Create New Group")
//        val builder = AlertDialog.Builder(activity)
//        builder.setTitle("Choose Action")
//        builder.setItems(options,object : DialogInterface.OnClickListener{
//            override fun onClick(dialog: DialogInterface?, which: Int) {
                val intent = Intent(context,
                    AddGroupActivity::class.java)
                startActivity(intent)
//            }
//        })
//        builder.create().show()
    }
}