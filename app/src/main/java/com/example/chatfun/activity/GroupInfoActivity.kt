package com.example.chatfun.activity

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.chatfun.R
import com.example.chatfun.adapter.AddParticipantAdapter
import com.example.chatfun.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_group_info.*
import java.lang.Exception
import java.util.*

class GroupInfoActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var groupId: String
    private lateinit var myGroupRole: String
    private lateinit var userList: ArrayList<User>
    private lateinit var addParticipantAdapter: AddParticipantAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_info)
        setSupportActionBar(findViewById(R.id.toolbar_info_group))
        supportActionBar!!.title = "Add Participant"
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        firebaseAuth = FirebaseAuth.getInstance()
        groupId = intent.getStringExtra("groupId")
        loadGroupInfo()
        loadMyGroupRole()
        edit_group.setOnClickListener {
            val intent = Intent(this, EditGroupActivity::class.java)
            intent.putExtra("groupId",groupId)
            startActivity(intent)
        }
        add_participant.setOnClickListener {
            val intent = Intent(this, AddParticipantGroupActivity::class.java)
            intent.putExtra("groupId",groupId)
            startActivity(intent)
        }
        leave_group.setOnClickListener {
            var dialogTitle : String = ""
            var dialogDescription : String = ""
            var posititiveButtonTitle : String = ""
            if (myGroupRole.equals("creator")){
                dialogTitle = "Delete Group"
                dialogDescription = " Are you want to delete group!"
                posititiveButtonTitle = "DELETE"
            } else{
                dialogTitle = "Leave Group"
                dialogDescription = " Are you want to leave group!"
                posititiveButtonTitle = "LEAVE"
            }
            val builder = AlertDialog.Builder(this)
            builder.setTitle(dialogTitle)
                .setMessage(dialogDescription)
                .setPositiveButton(posititiveButtonTitle,object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        if (myGroupRole.equals("creator")){
                            deleteGroup()
                        } else {
                            leaveGroup()
                        }
                    }

                })
                .setNegativeButton("CANCEL", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog!!.dismiss()
                    }

                })
                builder.create().show()
        }
    }

    private fun leaveGroup() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").child(firebaseAuth!!.uid!!)
            .removeValue()
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(this@GroupInfoActivity, "Leave Success", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@GroupInfoActivity,
                        MainActivity::class.java)
                    startActivity(intent)
                }
            }
            }
            .addOnFailureListener {object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(this@GroupInfoActivity, p0.message, Toast.LENGTH_LONG).show()
                }
            }
            }
    }

    private fun deleteGroup() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!)
            .removeValue()
            .addOnCompleteListener {
                Toast.makeText(this@GroupInfoActivity, "Delete Success", Toast.LENGTH_LONG).show()
                onBackPressed()
//                val intent = Intent(this@GroupInfoActivity,MainActivity::class.java)
//                startActivity(intent)
            }
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(this@GroupInfoActivity, "Delete Success", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@GroupInfoActivity,
                        MainActivity::class.java)
                    startActivity(intent)
                }
            }
            }
            .addOnFailureListener {object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(this@GroupInfoActivity, p0.message, Toast.LENGTH_LONG).show()
                }
            }
            }

    }

    private fun loadMyGroupRole() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").orderByChild("uid")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        myGroupRole = ""+ds.child("role").value
                        if (myGroupRole.equals("participant")){
                            leave_group.text = "Leave Group"
                        } else if (myGroupRole.equals("admin")){
                            leave_group.text = "Leave Group"
                        } else if (myGroupRole.equals("creator")){
                            leave_group.text = "Delete Group"
                        }
                    }
                    loadParticipants()
                }

            })

    }

    private fun loadParticipants() {
        userList = arrayListOf()
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                }
                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        //lay uid tu group
                        val uid = ""+ds.child("uid").value
                        //lay thong tin user
                        val ref = FirebaseDatabase.getInstance().getReference("Users")
                        ref.orderByChild("uid").equalTo(uid)
                            .addValueEventListener(object : ValueEventListener{
                                override fun onCancelled(p0: DatabaseError) {

                                }

                                override fun onDataChange(p0: DataSnapshot) {
                                    for (ds in p0.children){
//                                        userList.clear()
                                        //set adater
                                        val modelUser = ds.getValue(User::class.java)
                                        if (modelUser != null) {
                                            userList.add(modelUser)
                                        }
                                        addParticipantAdapter = AddParticipantAdapter(this@GroupInfoActivity, userList, ""+groupId, ""+myGroupRole)
                                        rc_participant.adapter = addParticipantAdapter
                                        count_participant.text = "Participants"+"("+userList.size+")"
                                    }
                                }

                            })
                    }
                }

            })

    }

    private fun loadGroupInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children) {
                        //get group info
                        val groupId = ""+ds.child("groupId").value
                        val groupTitle = ""+ds.child("groupTitle").value
                        val groupDescription = ""+ds.child("groupDescription").value
                        val groupIcon = ""+ds.child("groupIcon").value
                        val timestamp = ""+ds.child("timestamp").value
                        val createdBy = ""+ds.child("createdBy").value

                        //convert time to dd/mm/yyyy hh:mm am/pm
                        val calendar = Calendar.getInstance(Locale.getDefault())
                        if (timestamp != null) {
                            calendar.timeInMillis = timestamp.toLong()
                        }
                        val gTime: String = android.text.format.DateFormat.format("dd/MM/yyyy hh:mm aa",calendar).toString()
                        loadCreateInfo(gTime,createdBy)
                        //set info
                        supportActionBar!!.title = groupTitle
                        description.text = groupDescription
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.bellerin).into(iv_group)
                        }catch(e: Exception) {

                        }

                    }
                }
            })
    }

    private fun loadCreateInfo(gTime: String, createdBy: String) {
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.orderByChild("uid").equalTo(createdBy)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children) {
                        val name = ""+ds.child("username").value
                        createBy.text = "Created By "+name+" on " + gTime
                    }
                }

            })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
}