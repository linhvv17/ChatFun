package com.example.chatfun.adapter

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.model.GroupChat
import com.example.chatfun.model.User
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.lang.Exception
import java.lang.StringBuilder

class AddParticipantAdapter(
    mContext: Context,
    mUserList: ArrayList<User>,
    groupId : String,
    myGroupRole : String
): RecyclerView.Adapter<AddParticipantAdapter.ViewHolder?>() {
    private val groupId : String
    private val myGroupRole : String
    private val mContext : Context
    private val mUserList: ArrayList<User>
    init {
        this.mContext = mContext
        this.mUserList = mUserList
        this.groupId = groupId
        this.myGroupRole = myGroupRole
    }
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var nameTv : TextView? = null
        var roleTv : TextView? = null
        var avatarImg : CircleImageView? = null
        init {
            nameTv = itemView.findViewById(R.id.name_user)
            roleTv = itemView.findViewById(R.id.role_user)
            avatarImg = itemView.findViewById(R.id.avatar_user)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // infalater
        val view = LayoutInflater.from(mContext).inflate(R.layout.row_user_add_to_group, parent, false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mUserList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = mUserList[position]
        //get data
        val uid = model.getUid()
        val image = model.getProfile()
        val name = model.getUsername()
        //set data
        holder.nameTv!!.text = name
        try {
            Picasso.get().load(image).placeholder(R.drawable.bellerin).into(holder.avatarImg)
        }catch(e: Exception) {

        }
        checkIfAlreadyExits(model,holder)

        //
        holder.itemView.setOnClickListener {
            val ref = FirebaseDatabase.getInstance().getReference("Groups")
            ref.child(groupId).child("Participants").child(uid!!)
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {

                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()){
                            val currentRole = ""+p0.child("role").value
                            //
                            var options: Array<String>? = null
                            val builder = AlertDialog.Builder(mContext)
                            builder.setTitle("Choose Option")
                            if (myGroupRole.equals("creator")){
                                if (currentRole.equals("admin")){
                                    options = arrayOf("Remove Admin","Remove User")
                                    builder.setItems(options, object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            if (which==0){
                                                removeAdminRole(model)
                                            } else {
                                                removeUser(model)
                                            }
                                        }

                                    })
                                    builder.create().show()

                                }
                                else if (currentRole.equals("participant")){
                                    options = arrayOf("Make Admin","Remove User")
                                    builder.setItems(options, object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            if (which==0){
                                                makeAdminRole(model)
                                            } else {
                                                removeUser(model)
                                            }
                                        }

                                    })
                                    builder.create().show()
                                }
                            }
                            else if (myGroupRole.equals("admin")){
                                if (currentRole.equals("creator")){
                                    Toast.makeText(mContext,"Creator of Group",Toast.LENGTH_LONG).show()
                                }
                                else if (currentRole.equals("admin")){
                                    options = arrayOf("Remove Admin","Remove User")
                                    builder.setItems(options, object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            if (which==0){
                                                removeAdminRole(model)
                                            } else {
                                                removeUser(model)
                                            }
                                        }

                                    })
                                    builder.create().show()
                                }
                                }
                            else if (myGroupRole.equals("participant")){
                                    options = arrayOf("Make Admin","Remove User")
                                    builder.setItems(options, object : DialogInterface.OnClickListener{
                                        override fun onClick(dialog: DialogInterface?, which: Int) {
                                            if (which==0){
                                                makeAdminRole(model)
                                            } else {
                                                removeUser(model)
                                            }
                                        }

                                    })
                                    builder.create().show()

                            }

                        } else {
                            val builder = AlertDialog.Builder(mContext)
                            builder.setTitle("Add User")
                                .setMessage("Add this user to group")
                                .setPositiveButton("Add", object : DialogInterface.OnClickListener{
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        addUserToGroup(model)
                                    }
                                })
                                .setNegativeButton("Cancel", object : DialogInterface.OnClickListener{
                                    override fun onClick(dialog: DialogInterface?, which: Int) {
                                        dialog!!.dismiss()
                                    }

                                })
                            builder.create().show()
                        }
                    }

                })
        }
    }

    private fun addUserToGroup(model: User) {
        //setup data user
        val timestamp: String = ""+System.currentTimeMillis()
        val hashMap : HashMap<String, String> = HashMap()
        hashMap["uid"] = ""+model.getUid()
        hashMap["role"] = "participant"
        hashMap["timestamp"]= ""+timestamp
        //add to group
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").child(model.getUid()!!).setValue(hashMap)
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(mContext, "Add Success",Toast.LENGTH_LONG).show()
                }

            }
            }
            .addOnFailureListener { object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(mContext, ""+p0.message,Toast.LENGTH_LONG).show()
                }

            }
            }
    }

    private fun makeAdminRole(model: User) {
        //setup data user
        val timestamp: String = ""+System.currentTimeMillis()
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["role"] = "admin"
        //update to database
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").child(model.getUid()!!).updateChildren(hashMap)
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(mContext, "Update To Admin",Toast.LENGTH_LONG).show()
                }

            }
            }
            .addOnFailureListener { object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(mContext, ""+p0.message,Toast.LENGTH_LONG).show()
                }

            }
            }

    }

    private fun removeUser(model: User) {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").child(model.getUid()!!).removeValue()
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(mContext, "Update To Admin",Toast.LENGTH_LONG).show()
                }

            }
            }
            .addOnFailureListener { object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(mContext, ""+p0.message,Toast.LENGTH_LONG).show()
                }

            }
            }

    }

    private fun removeAdminRole(model: User) {
        //setup data user
        val timestamp: String = ""+System.currentTimeMillis()
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["role"] = "participant"
        //update to database
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").child(model.getUid()!!).updateChildren(hashMap)
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    Toast.makeText(mContext, "Update To Participants",Toast.LENGTH_LONG).show()
                }

            }
            }
            .addOnFailureListener { object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                    Toast.makeText(mContext, ""+p0.message,Toast.LENGTH_LONG).show()
                }

            }
            }


    }

    private fun checkIfAlreadyExits(model: User, holder: ViewHolder) {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId).child("Participants").child(model.getUid()!!)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()){
                        val hisRole = ""+p0.child("role").value
                        holder.roleTv!!.text = hisRole

                    } else {
                        holder.roleTv!!.text = ""
                    }
                }

            })
    }
}