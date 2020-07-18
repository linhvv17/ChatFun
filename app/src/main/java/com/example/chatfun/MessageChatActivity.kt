package com.example.chatfun

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.adapter.ChatAdapter
import com.example.chatfun.model.Chat
import com.example.chatfun.model.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import java.util.ArrayList

class MessageChatActivity : AppCompatActivity() {
     var userIdVisit: String = ""
     var firebaseUser: FirebaseUser? = null
     var chatAdapter: ChatAdapter? = null
     var mChatList: List<Chat>? = null
    lateinit var rcMessageChat : RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        rcMessageChat = findViewById(R.id.rc_message_chat)
        rcMessageChat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        rcMessageChat!!.layoutManager = linearLayoutManager


        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    tv_username_chat.text = user!!.username
                    Picasso.get().load(user.profile).into(img_profile_message_chat)

                    retrieveMessage(firebaseUser!!.uid, userIdVisit, user.profile)
                }

            }
        )

        send_message_btn.setOnClickListener {
            val message = text_message_chat.text.toString()
            if (message == ""){
                Toast.makeText(this, "Please write message! ", Toast.LENGTH_LONG).show()
            }else{
                sendMessageToReceiverUser(firebaseUser!!.uid, userIdVisit, message)
            }
            //gửi xong reset lại
            text_message_chat.setText("")
        }
        ic_attach_file_btn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }
    }

    private fun retrieveMessage(senderId: String, receiverId: String?, urlImgReceiver: String) {
        mChatList = ArrayList<Chat>()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.receiver == senderId && chat!!.sender == receiverId ||
                            chat!!.sender == receiverId && chat!!.receiver == senderId
                    )
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(
                        this@MessageChatActivity,
                        mChatList as ArrayList<Chat>,
                        urlImgReceiver
                    )
                    chatAdapter!!.notifyDataSetChanged()
                    rcMessageChat.adapter = chatAdapter
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun sendMessageToReceiverUser(
        senderId: String,
        receiverId: String?,
        message: String
    ) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String, Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["receiver"] = receiverId
        messageHashMap["message"] = message
        messageHashMap["isSeen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val chatListReference = FirebaseDatabase
                        .getInstance()
                        .reference
                        .child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)
                    chatListReference.addListenerForSingleValueEvent(
                        object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {

                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()){
                                    chatListReference.child("id").setValue(userIdVisit)
                                }
                                val chatListReceiverReference = FirebaseDatabase
                                    .getInstance()
                                    .reference
                                    .child("ChatList")
                                    .child(userIdVisit)
                                    .child(firebaseUser!!.uid)
                                chatListReceiverReference.child("id").setValue(firebaseUser!!.uid)

                            }

                        })


                    //implement notification



                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data!=null && data!!.data!=null){

            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Uploading, please wait...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask : StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener {
                if (it.isSuccessful){
                    val downloadUrl = it.result
                    val url = downloadUrl.toString()
                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["message"] = "sen your an image"
                    messageHashMap["isSeen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId
                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)
                    progressBar.dismiss()
                }
            }

        }
    }
}