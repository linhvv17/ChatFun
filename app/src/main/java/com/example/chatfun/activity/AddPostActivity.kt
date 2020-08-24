package com.example.chatfun.activity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatfun.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_post.*
import kotlin.collections.HashMap

class AddPostActivity: AppCompatActivity() {
    private val CAMERA_REQUEST_CODE = 100
    private val STORAGE_REQUEST_CODE = 200
    private val IMAGE_PICK_CAMERA_CODE = 300
    private val IMAGE_PICK_GALLERY_CODE = 400

    // image uri
    private var img_uri : Uri? = null
    //permissions
    lateinit var camerapermissions :Array<String>
    lateinit var storagepermissions :Array<String>
    //var storagepermissions = arrayOf<String>()

    //user
    lateinit var firebaseAuth: FirebaseAuth
    lateinit var referenceUser: DatabaseReference
    var username : String? = null
    var email : String? = null
    var uid : String? = null
    var userprofile : String? = null

    //progress bar
    private lateinit var pd : ProgressDialog
//    val progressBar = ProgressDialog(this@AddPostActivity)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post)
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_add_post)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = "Add new post"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        //get user
        firebaseAuth = FirebaseAuth.getInstance()
        checkUserStatus()
        //get info
        referenceUser = FirebaseDatabase.getInstance().getReference("Users")
        val query = referenceUser!!.orderByChild("uid").equalTo(uid)
        query.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                for (data in p0.children){
                    username = ""+data.child("username").value
                    uid = ""+data.child("uid").value
//                    email = ""+data.child("email").value
                    userprofile = ""+data.child("profile").value
                }

            }

        })

        //init permission array
        camerapermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE )
        storagepermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE )

        pd = ProgressDialog(this@AddPostActivity)

        //get image from phone
        imgview_post.setOnClickListener {
            showImagePickDialog()
        }

        //btn_post_done click
        btn_post_done.setOnClickListener {
                val title = tv_title_post.text.toString().trim()
                val description = tv_description_post.text.toString().trim()

                if (TextUtils.isEmpty(title)){
                    Toast.makeText(applicationContext, "Hãy thêm title",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(description)){
                    Toast.makeText(applicationContext, "Hãy thêm description",Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                if (img_uri == null){
                    uploadPost(title,description,"noImage")
                } else {
                    uploadPost(title,description, img_uri.toString())
                }

        }
    }

    private fun uploadPost(title: String, description: String, uri: String) {
        pd!!.setMessage("Publishing post")
        pd!!.show()
        val timeStamp: String = (System.currentTimeMillis()).toString()
        val filePathAndName: String = "Posts/post_$timeStamp"
        if (uri != "noImage"){
            //có hình
            val ref : StorageReference = FirebaseStorage.getInstance().reference.child(filePathAndName)
            var uploadTask : StorageTask<*>
            uploadTask = ref.putFile(Uri.parse(uri))
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation ref.downloadUrl

            }).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val postHashMap = HashMap<Any?, String?>()
                    //post
                    postHashMap["uid"] = uid
                    postHashMap["uName"] = username
                    postHashMap["userProfile"] = userprofile
                    postHashMap["postId"] = timeStamp
                    postHashMap["postTitle"] = title
                    postHashMap["postDes"] = description
                    postHashMap["postImage"] = url
                    postHashMap["postTime"] = timeStamp
                    postHashMap["postLikes"] = "0"
                    postHashMap["postComments"] = "0"

                    //path to storage post data
                    val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
                    //put data in this ref
                    ref.child(timeStamp).setValue(postHashMap)
                        .addOnSuccessListener {object : OnSuccessListener<Void>{
                            override fun onSuccess(p0: Void?) {
                                pd!!.dismiss()
                                Toast.makeText(this@AddPostActivity, "Post Published",Toast.LENGTH_LONG).show()
                                //reset view
                                tv_title_post.setText("")
                                tv_description_post.setText("")
                                imgview_post.setImageURI(null)
                                img_uri=null
                            }

                        }
                        }
                        .addOnFailureListener {object: OnFailureListener{
                            override fun onFailure(p0: java.lang.Exception) {
                                pd!!.dismiss()
                                Toast.makeText(this@AddPostActivity, ""+p0.message,Toast.LENGTH_LONG).show()
                            }

                        }
                        }
                    //reset view
                    tv_title_post.setText("")
                    tv_description_post.setText("")
                    imgview_post.setImageURI(null)
                    img_uri=null
                    pd.dismiss()
                }

            }
        } else{
            //không hình
            //url is received upload post to firebase
            val postHashMap = HashMap<String, Any?>()
            //post
            postHashMap["uid"] = uid
            postHashMap["uName"] = username
//            postHashMap["uEmail"] = email
            postHashMap["userProfile"] = userprofile
            postHashMap["postId"] = timeStamp
            postHashMap["postTitle"] = title
            postHashMap["postDes"] = description
            postHashMap["postImage"] = "noImage"
            postHashMap["postTime"] = timeStamp
            postHashMap["postLikes"] = "0"
            postHashMap["postComments"] = "0"

            //path to storage post data
            val ref = FirebaseDatabase.getInstance().getReference("Posts")
//            pd.dismiss()
            //put data in this ref
            ref.child(timeStamp).setValue(postHashMap)
                .addOnCompleteListener {
                    //reset view
                    tv_title_post.setText("")
                    tv_description_post.setText("")
                    imgview_post.setImageURI(null)
                    img_uri=null
                    pd.dismiss()
                }
                .addOnSuccessListener {
                    OnSuccessListener<Void> {
                        pd.dismiss()
                        Toast.makeText(this@AddPostActivity, "Post Published",Toast.LENGTH_LONG).show()
                        //reset view
                        tv_title_post.setText("")
                        tv_description_post.setText("")
                        imgview_post.setImageURI(null)
                        img_uri=null
                    }

                }
                .addOnFailureListener {
                    OnFailureListener { p0 -> //                        progressBar!!.dismiss()
                        pd.dismiss()
                        Toast.makeText(this@AddPostActivity, ""+p0.message,Toast.LENGTH_LONG).show()
                    }

                }

        }

    }

    private fun checkUserStatus(){
        //get current user
        val user = firebaseAuth!!.currentUser
        if (user != null){
            email = user.email
            uid = user.uid

        }else{
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    //storage
    private fun checkStoragePermission(): Boolean{
        val result: Boolean = ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result
    }
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this, storagepermissions, STORAGE_REQUEST_CODE)

    }

    //camera
    private fun checkCameraPermission(): Boolean{
        val result: Boolean = ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val result1: Boolean = ContextCompat.checkSelfPermission(
            this@AddPostActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this, camerapermissions, CAMERA_REQUEST_CODE)

    }

    private fun showImagePickDialog() {
        var options = arrayOf<String>("Camera", "Gallery")
        //dialog
        val builder = AlertDialog.Builder(this)
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

    private fun pickFromCamera() {
        val cv: ContentValues = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE, "Temp Pick")
        cv.put(MediaStore.Images.Media.DESCRIPTION, "Temp Descr")
        img_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, img_uri)
        startActivityForResult(intent, IMAGE_PICK_CAMERA_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            CAMERA_REQUEST_CODE -> {
//                if (grantResults.isNotEmpty()){
//                    val cameraAccept:Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
//                    val storageAccept: Boolean = grantResults[1] == PackageManager.PERMISSION_GRANTED
//                    if (cameraAccept && storageAccept){
                        pickFromCamera()
//                    }
//                    else{
//                        Toast.makeText(applicationContext, " Chưa đủ quyền", Toast.LENGTH_LONG).show()
//                    }
//                }
//                else{
//
//                }

            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()){
                    val storageAccept: Boolean = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccept){
                        pickFromGallery()
                    }
                    else{
                        Toast.makeText(applicationContext, " Chưa đủ quyền", Toast.LENGTH_LONG).show()
                    }
                }
                else{

                }
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        imgview_post.setImageURI(img_uri)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == IMAGE_PICK_GALLERY_CODE){
                //camera
                //get uri
                img_uri = data!!.data
                //set to img
                imgview_post.setImageURI(img_uri)

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //gallery
                //set to img
                imgview_post.setImageURI(img_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}