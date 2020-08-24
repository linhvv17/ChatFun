package com.example.chatfun.activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.chatfun.R
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_group.*

class AddGroupActivity: AppCompatActivity() {
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
    private lateinit var actionbar: ActionBar
    //
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
//        icGroup = findViewById(R.id.ic_group)
        ///
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_add_group)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Create Group Chat"
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        // tao permission
        //init permission array
        camerapermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE )
        storagepermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE )
        //
        firebaseAuth = FirebaseAuth.getInstance()
//        checkUser()

        //thay doi anh nhom
        ic_group.setOnClickListener {
            showImagePickUpDialog()
        }
        //hoan thanh
        float_done.setOnClickListener {

            //nhap title va description
            val groupTitle = edt_title_group.text.toString().trim()
            val groupDescription = edt_description_group.text.toString().trim()
            //Kiem tra du lieu
            if(groupTitle.isEmpty()||groupDescription.isEmpty()){
                Toast.makeText(this,"Ban chua nhap du thong tin !!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (img_uri==null){
                startCreateGroup(groupTitle,groupDescription,"noPhoto")
            } else {
                startCreateGroup(groupTitle,groupDescription,img_uri.toString())
            }

        }

    }

    private fun startCreateGroup(
        groupTitle: String,
        groupDescription: String,
        uri: String
    ) {
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Creating Group")
        progressDialog.show()
        //thoi gian, avata
        val g_TimeCreate = ""+System.currentTimeMillis()
        if (uri=="noPhoto"){
            //create khong co anh
            createGroup(
                ""+g_TimeCreate,
                ""+groupTitle,
                ""+groupDescription,
                "")
            //reset view
//            edt_title_group.setText("")
//            edt_description_group.setText("")
//            img_uri=null
//            imgview_post.setImageURI(null)
//            progressDialog.dismiss()
        } else{
            //create co anh dai dien
            //upload image
            //image name & path
            val fileNameAndPath = "Group_Images/image$g_TimeCreate"
            val storeReference = FirebaseStorage.getInstance().reference.child(fileNameAndPath)
            var uploadTask : StorageTask<*>
            uploadTask = storeReference.putFile(Uri.parse(uri))
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation storeReference.downloadUrl

            }).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    createGroup(
                        ""+g_TimeCreate,
                        ""+groupTitle,
                        ""+groupDescription,
                        ""+url.toString())
                    //reset view
//                    edt_title_group.setText("")
//                    edt_description_group.setText("")
//                    img_uri=null
//                    imgview_post.setImageURI(null)
//                    progressDialog.dismiss()

                }

//            storeReference.putFile(Uri.parse(uri))
//                .addOnSuccessListener {
//                    OnSuccessListener<UploadTask.TaskSnapshot> {
//                        //uploaded-lay url
//                        val pUriTask : Task<Uri> = it.storage.downloadUrl
//                        while (!pUriTask.isSuccessful){
//                        }
//                        val p_downloadUrl : Uri? = pUriTask.result
//                        if (pUriTask.isSuccessful){
//                            createGroup(
//                                ""+g_TimeCreate,
//                                ""+groupTitle,
//                                ""+groupDescription,
//                                ""+p_downloadUrl.toString())
//                        }
//
//                    }
//
//                }
//                .addOnFailureListener{
//                    OnFailureListener {
//                        progressDialog.dismiss()
//                        Toast.makeText(this,""+it.message,Toast.LENGTH_LONG).show()
//                    }
//                }

            }
        }

    }

    private fun createGroup(g_TimeCreate: String, g_Title: String, g_Description: String,g_Icon:String) {
        //info cua group
        val hashMap : HashMap<String, String> = HashMap()
        hashMap["groupId"] = ""+g_TimeCreate
        hashMap["groupTitle"] = ""+g_Title
        hashMap["groupDescription"] = ""+g_Description
        hashMap["groupIcon"] = ""+g_Icon
        hashMap["timestamp"] = ""+g_TimeCreate
        hashMap["createdBy"] = ""+firebaseAuth.uid
        //create group
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
        ref.child(g_TimeCreate).setValue(hashMap)
            .addOnCompleteListener {}
            .addOnSuccessListener {
//                progressDialog.dismiss()
//                Toast.makeText(this,"Group Created",Toast.LENGTH_LONG).show()
                //add member
                val hashMap1 : HashMap<String, String> = HashMap()
                hashMap1["uid"]= firebaseAuth.uid!!
                hashMap1["role"]= "creator"
                hashMap1["timestamp"]= g_TimeCreate
                val ref1 : DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
                ref1.child(g_TimeCreate).child("Participants").child(firebaseAuth.uid!!)
                    .setValue(hashMap1)
                    .addOnSuccessListener {
                        //add được
                        progressDialog.dismiss()
                        Toast.makeText(this,"Group Created",Toast.LENGTH_LONG).show()
                        //reset view
                    edt_title_group.setText("")
                    edt_description_group.setText("")
//                    img_uri=null
//                    ic_group.setImageURI(null)
                    progressDialog.dismiss()

                    }
                    .addOnFailureListener{
                        //add loi
                        progressDialog.dismiss()
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()

                    }

            }
            .addOnFailureListener {
                progressDialog.dismiss()
                Toast.makeText(this,""+it.message,Toast.LENGTH_LONG).show()

            }
    }

    private fun showImagePickUpDialog() {
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
    private fun pickFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE)
    }
    private fun pickFromCamera(){
        val cv: ContentValues = ContentValues()
        cv.put(MediaStore.Images.Media.TITLE,"Group Image Icon Title")
        cv.put(MediaStore.Images.Media.DESCRIPTION,"Group Image Icon Description")
        img_uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,cv)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,img_uri)
        startActivityForResult(intent,IMAGE_PICK_CAMERA_CODE)
    }
    //chẹkPermission
    //storage
    private fun checkStoragePermission():Boolean{
        val result: Boolean = ContextCompat.checkSelfPermission(this@AddGroupActivity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED)
        return result
    }
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this@AddGroupActivity,storagepermissions,STORAGE_REQUEST_CODE)
    }
    //camera
    private fun checkCameraPermission(): Boolean{
        val result: Boolean = ContextCompat.checkSelfPermission(this@AddGroupActivity,
        Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED)
        val result1: Boolean = ContextCompat.checkSelfPermission(this@AddGroupActivity,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED)
        return result&&result1
    }
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this@AddGroupActivity,camerapermissions,CAMERA_REQUEST_CODE)
    }
//    private fun checkUser() {
//        val user: FirebaseUser = firebaseAuth.currentUser!!
//        if (user != null){
//            email = user.email
//            uid = user.uid
//
//        }else{
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }
//    }

//    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
//        return super.onSupportNavigateUp()
//    }

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
                //set to img
                ic_group.setImageURI(img_uri)

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //gallery
                //set to img
                ic_group.setImageURI(img_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}