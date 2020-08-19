package com.example.chatfun

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.InterfaceApp.APIService2
import com.example.chatfun.adapter.ChatAdapter
import com.example.chatfun.fragment.ChatFragment
import com.example.chatfun.model.Chat
import com.example.chatfun.model.User
import com.example.chatfun.notifications.*
import com.example.chatfun.notifications.Client.Client.getClient
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import retrofit2.Call
import retrofit2.Response
import java.util.ArrayList

class MessageChatActivity : AppCompatActivity() {
     var userIdVisit: String = ""
     var firebaseUser: FirebaseUser? = null
     var chatAdapter: ChatAdapter? = null
     var mChatList: List<Chat>? = null
     var reference: DatabaseReference? = null
     lateinit var rcMessageChat : RecyclerView
     var notify = false
     var APIService2 : APIService2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_message_chat)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        APIService2 = getClient("https://fcm.googleapis.com/")!!.create(com.example.chatfun.InterfaceApp.APIService2::class.java)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser
        rcMessageChat = findViewById(R.id.rc_message_chat)
        rcMessageChat.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        rcMessageChat.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference.addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    val user = p0.getValue(User::class.java)
                    tv_username_chat.text = user!!.getUsername()
                    Picasso.get().load(user.getProfile()).into(img_profile_message_chat)
                    retrieveMessage(firebaseUser!!.uid, userIdVisit, user.getProfile()!!)
                }
            }
        )
        send_message_btn.setOnClickListener {
            notify = true
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
            notify = true
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), 438)
        }

        seenMessage(userIdVisit)
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
                                if (!p0.exists()) {

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
                }
            }
//        implement notification using fcm
        val usersReference = FirebaseDatabase.getInstance().reference
            .child("Users").child(firebaseUser!!.uid)

        usersReference.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val user = p0.getValue(User::class.java)
                if (notify){
                    sendNotification(receiverId, user!!.getUsername(), message)
                }
                notify = false
            }

        })
    }

    private fun sendNotification(receiverId: String?, username: String?, message: String) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val query = ref.orderByKey().equalTo(receiverId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children){
                    val token: Token? = dataSnapshot.getValue(Token::class.java)
                    val data = Data(
                        firebaseUser!!.uid,
                        R.mipmap.ic_launcher,
                        "$username: $message",
                        "New Message",
                        userIdVisit
                    )

                    val sender = Sender(data, token!!.getToken().toString())
                    APIService2!!.sendNotification(sender)!!
                        .enqueue(object : retrofit2.Callback<MyRespone?>{
                            override fun onFailure(call: Call<MyRespone?>, t: Throwable) {

                            }

                            override fun onResponse(
                                call: Call<MyRespone?>,
                                response: Response<MyRespone?>
                            ) {
                                if (response.code() == 200){
                                    if (response.body()!!.success !=1){
                                        Toast.makeText(this@MessageChatActivity, "Failed, Nothing happen", Toast.LENGTH_LONG).show()
                                    }
                                }

                            }

                        })
                }

            }

        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK && data!=null && data!!.data!=null){
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
                        .addOnCompleteListener { it ->
                            if (it.isSuccessful){
                                progressBar.dismiss()
                                //implement notification
                                val reference = FirebaseDatabase.getInstance().reference
                                    .child("Users").child(firebaseUser!!.uid)

                                reference.addValueEventListener(object : ValueEventListener{
                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val user = p0.getValue(User::class.java)
                                        if (notify){
                                            sendNotification(userIdVisit, user!!.getUsername(), "sent you an image")
                                        }
                                        notify = false
                                    }

                                })

                            }

                        }

                }
            }

        }
    }

    private fun retrieveMessage(senderId: String, receiverId: String?, urlImgReceiver: String) {
        mChatList = ArrayList()

        reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference!!.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver() == senderId && chat!!.getSender() == receiverId ||
                        chat!!.getSender() == senderId && chat!!.getReceiver() == receiverId
                    )
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(
                        applicationContext,
                        mChatList!! as ArrayList<Chat>,
                        urlImgReceiver
                    )
                    rcMessageChat.adapter = chatAdapter
                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }


    var seenListener : ValueEventListener? = null
    private fun seenMessage(userId: String){
        val refChatList = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListener = refChatList.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children){
                    val chat: Chat? = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userId)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isSeen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }

                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
    }
}