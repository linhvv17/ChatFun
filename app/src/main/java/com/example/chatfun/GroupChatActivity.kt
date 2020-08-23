package com.example.chatfun

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatfun.R
import com.example.chatfun.adapter.GroupChatAdapter
import com.example.chatfun.model.GroupChat
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_group_chat.*
import java.lang.Exception

class GroupChatActivity : AppCompatActivity() {
    //
    //code xin quyen
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200
    private val IMAGE_PICK_CAMERA_CODE = 500
    private val IMAGE_PICK_GALLERY_CODE = 600
    //permission array
    lateinit var camerapermissions :Array<String>
    lateinit var storagepermissions :Array<String>
    // image uri
    private var img_uri : Uri? = null
    //progress
    private lateinit var progressDialog: ProgressDialog
    //

    private var groupId : String? = ""
    private var myGroupRole : String? = ""
    private var firebaseAuth : FirebaseAuth? = null
    private lateinit var groupChatList : ArrayList<GroupChat>
    private lateinit var adapterGroupChat : GroupChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_group_chat)
        setSupportActionBar(toolbar)

        //init permission array
        camerapermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE )
        storagepermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE )

        val intent = intent
        groupId = intent.getStringExtra("groupId")
        //
        firebaseAuth = FirebaseAuth.getInstance()
        loadGroupInfo()
        loadGroupMessage()
        loadMyGroupRole()
        ic_pick_img.setOnClickListener {
            pickUpImageDialog()
        }
        ic_send.setOnClickListener {
            val message = edt_chat.text.toString().trim()
            if (message.isEmpty()){
                Toast.makeText(applicationContext,"Hãy nhâp tin nhắn!!",Toast.LENGTH_LONG).show()
            } else {
                sendMessage(message)
            }

        }
        btn_add.setOnClickListener {
            val intent = Intent(this@GroupChatActivity,AddParticipantGroupActivity::class.java)
            intent.putExtra("groupId",groupId)
            startActivity(intent)
        }
        btn_info.setOnClickListener {
            val intent = Intent(this@GroupChatActivity,GroupInfoActivity::class.java)
            intent.putExtra("groupId",groupId)
            startActivity(intent)
        }
    }

    private fun pickUpImageDialog() {
        var options = arrayOf<String>("Camera", "Gallery")
        //dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Chose Image From")
        //set option
        builder.setItems(options, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                if (which==0){
                    //camera
                    if (!checkCameraPermission()){
                        requestCameraPermission()
                    } else{
                        pickFromCamera()
                    }
                }
                if (which==1){
                    //gallery
                    if (!checkStoragePermission()){
                        requestStoragePermission()
                    } else {
                        pickFromGallery()
                    }
                }

            }
        }
        )

        //create va show dialog
        builder.create().show()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)

    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(this@GroupChatActivity,storagepermissions,STORAGE_REQUEST_CODE)
    }

    private fun checkStoragePermission(): Boolean {
        val result: Boolean = ContextCompat.checkSelfPermission(this@GroupChatActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED)
        return result
    }

    private fun pickFromCamera() {
        val cv: ContentValues = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title")
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description")
        img_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri)
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE)
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(this@GroupChatActivity,camerapermissions,CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermission(): Boolean {
        val result: Boolean = ContextCompat.checkSelfPermission(this@GroupChatActivity,
            Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED)
        val result1: Boolean = ContextCompat.checkSelfPermission(this@GroupChatActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED)
        return result&&result1
    }

    private fun loadMyGroupRole() {
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Participants").orderByChild("uid").equalTo(firebaseAuth!!.uid)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        val myGroupRole = ""+ds.child("role").value
                    }
                }

            })
    }

    private fun loadGroupMessage() {
        groupChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(groupId!!).child("Messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    groupChatList!!.clear()
                    for (ds in p0.children){
                        val model = ds.getValue(GroupChat::class.java)
                        groupChatList!!.add(model!!)
                    }
                    //adapter
                    adapterGroupChat = GroupChatAdapter(applicationContext, groupChatList!!)
                    //recyclerview
                    rc_detail_group_chat.adapter = adapterGroupChat
                }

            })
    }

    private fun sendMessage(message: String) {
        val timestamp: String = ""+System.currentTimeMillis()//ep ve string
        val hashMap : HashMap<String, Any> = HashMap()
        hashMap["sender"] = ""+firebaseAuth!!.uid
        hashMap["message"] = ""+message
        hashMap["timestamp"] = ""+timestamp
        hashMap["type"] = "text" //text/image/file

        //
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        //them vao truong Message
        ref.child(groupId!!).child("Messages").child(timestamp)
            .setValue(hashMap)
            .addOnCompleteListener {
                edt_chat.setText("")
            }
            .addOnSuccessListener {object : OnSuccessListener<Void>{
                override fun onSuccess(p0: Void?) {
                    edt_chat.setText("")
                }
            }
            }
            .addOnFailureListener {object : OnFailureListener{
                override fun onFailure(p0: Exception) {
                }
            }
            }

    }

    private fun loadGroupInfo() {
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        ref.orderByChild("groupId").equalTo(groupId)
            .addValueEventListener(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (ds in p0.children){
                        val groupTitle = ""+ds.child("groupTitle").value
                        val groupDescription = ""+ds.child("groupTitle").value
                        val groupIcon = ""+ds.child("groupIcon").value
                        val createdBy = ""+ds.child("createdBy").value
                        val timestamp = ""+ds.child("timestamp").value
                        title_group_detail.text = groupTitle
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.bellerin).into(icon_group_chat_detail)
                        }catch (e: Exception){

                        }
                    }
                }

            })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE->{
//                if (grantResults.isNotEmpty()){
//                    val cameraAccept:Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    val storageAccept:Boolean = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                    if (cameraAccept&&storageAccept){
                //được phép
                pickFromCamera()
//                    } else{
//                        //chưa đủ quyền
//                        Toast.makeText(this,"Chưa đủ quyền gọi Camera",Toast.LENGTH_LONG).show()
//                    }
//                }
            }
            STORAGE_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    val storageAccept: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccept){
                        pickFromGallery()
                    } else{
                        Toast.makeText(this,"Chưa đủ quyền Gallery",Toast.LENGTH_LONG).show()
                    }
                }
                else{

                }

            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //result khi pick
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //camera
                //get uri
                img_uri = data!!.data
//                //set to img
                sendImageMessage()

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //gallery
                //set to img
                sendImageMessage()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendImageMessage() {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Waiting")
        progressDialog.setMessage("Send Image Message")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        //upload image
        //image name & path
        val fileNameAndPath = "Chat Images/"+""+System.currentTimeMillis()
        val storeReference = FirebaseStorage.getInstance().reference.child(fileNameAndPath)
        var uploadTask: StorageTask<*>
        uploadTask = storeReference.putFile(img_uri!!)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation storeReference.downloadUrl

        }).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result
                val url = downloadUrl.toString()
                val timestamp: String = ""+System.currentTimeMillis()//ep ve string
                val hashMap : HashMap<String, Any> = HashMap()
                hashMap["sender"] = ""+firebaseAuth!!.uid
                hashMap["message"] = ""+url
                hashMap["timestamp"] = ""+timestamp
                hashMap["type"] = "image" //text/image/file

                //
                val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
                //them vao truong Message
                ref.child(groupId!!).child("Messages").child(timestamp)
                    .setValue(hashMap)
                    .addOnCompleteListener {
                        edt_chat.setText("")
                        progressDialog.dismiss()
                    }
                    .addOnSuccessListener {object : OnSuccessListener<Void>{
                        override fun onSuccess(p0: Void?) {
                            edt_chat.setText("")
                        }
                    }
                    }
                    .addOnFailureListener {object : OnFailureListener{
                        override fun onFailure(p0: Exception) {
                        }
                    }
                    }
            }
        }
    }
}