package com.example.chatfun

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.example.chatfun.adapter.AddParticipantAdapter
import com.example.chatfun.adapter.UserAdapter
import com.example.chatfun.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.platforminfo.UserAgentPublisher
import kotlinx.android.synthetic.main.activity_add_participant_group.*

class AddParticipantGroupActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var groupId: String
    private lateinit var myGroupRole: String
    private lateinit var userList: ArrayList<User>
    private lateinit var addParticipantAdapter: AddParticipantAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_participant_group)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        supportActionBar!!.title = "Add Participant"
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        groupId = intent.getStringExtra("groupId")
        loadGroupInfo()

    }

    private fun getAllUser() {
        //init
        userList = arrayListOf()
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                userList.clear()
                for (ds in p0.children){
                    val modelUser = ds.getValue(User::class.java)
                    if (!firebaseAuth.uid.equals(modelUser!!.getUid())){
                        //khong phai toi
                        userList.add(modelUser)
                    }
                }
                //set adater
                addParticipantAdapter = AddParticipantAdapter(this@AddParticipantGroupActivity, userList, ""+groupId, ""+myGroupRole)
                rc_add_user_to_group.adapter = addParticipantAdapter
            }

        })

    }

    private fun loadGroupInfo() {
        val ref1 = FirebaseDatabase.getInstance().getReference("Groups")

        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        val groupId = ""+ds.child("groupId").value
                        val groupTitle = ""+ds.child("groupTitle").value
                        val groupDescription = ""+ds.child("groupDescription").value
                        val groupIcon = ""+ds.child("groupIcon").value
                        val timestamp = ""+ds.child("timestamp").value
                        val createdBy = ""+ds.child("createdBy").value
                        supportActionBar!!.title = "Add Participants"
                        ///////
                        ref1.child(groupId).child("Participants").child(firebaseAuth!!.uid!!)
                            .addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {

                                }
                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()){
                                        myGroupRole = ""+p0.child("role").value
                                        supportActionBar!!.title = "$groupTitle($myGroupRole)"
                                        getAllUser()
                                    }
                                }

                            })

                    }
                }

            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}