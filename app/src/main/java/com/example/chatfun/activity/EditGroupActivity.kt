package com.example.chatfun.activity

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
import androidx.appcompat.app.ActionBar
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
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_add_group.*
import kotlinx.android.synthetic.main.activity_edit_group.*
import kotlinx.android.synthetic.main.activity_group_info.*
import java.lang.Exception
import java.util.*

class EditGroupActivity : AppCompatActivity() {
    private lateinit var groupId: String
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
        setContentView(R.layout.activity_edit_group)
        //
        groupId = intent.getStringExtra("groupId")
        ///
        val toolbar : androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar_edit_group)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Update Group Chat"
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
        loadGroupInfo()

        //thay doi anh nhom
        ic_group_edit.setOnClickListener {
            showImagePickUpDialog()
        }
        float_done_edit.setOnClickListener {
            startUpdatingGroup()
        }
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
                        //set info
//                        supportActionBar!!.title = groupTitle
                        edit_title_group.setText(groupTitle)
                        edit_description_group.setText(groupDescription)
                        try {
                            Picasso.get().load(groupIcon).placeholder(R.drawable.bellerin).into(ic_group_edit)
                        }catch(e: Exception) {

                        }

                    }
                }
            })
    }

    private fun startUpdatingGroup() {

        //
        val groupTitle = edit_title_group.text.toString().trim()
        val groupDescription = edit_description_group.text.toString().trim()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Updating Group")
        progressDialog.show()

        if (img_uri==null){

            val hashMap : HashMap<String, Any> = HashMap()
            hashMap["groupTitle"] = ""+groupTitle
            hashMap["groupDescription"] = ""+groupDescription

            val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
            ref.child(groupId).updateChildren(hashMap)
                .addOnCompleteListener{
                    progressDialog.dismiss()
                    Toast.makeText(this,"Update thanh cong!", Toast.LENGTH_LONG).show()
                }

        } else {
            val timestamp = ""+System.currentTimeMillis()
            val fileNameAndPath = "Group_Images/image$timestamp"
            val storeReference = FirebaseStorage.getInstance().reference.child(fileNameAndPath)
            var uploadTask : StorageTask<*>
            uploadTask = storeReference.putFile(img_uri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation storeReference.downloadUrl

            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val hashMap : HashMap<String, Any> = HashMap()
                    hashMap["groupTitle"] = ""+groupTitle
                    hashMap["groupDescription"] = ""+groupDescription
                    hashMap["groupIcon"] = ""+url

                    val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("Groups")
                    ref.child(groupId).updateChildren(hashMap)
                        .addOnCompleteListener{
                            progressDialog.dismiss()
                            Toast.makeText(this,"Update thanh cong!", Toast.LENGTH_LONG).show()
                        }
                }
            }

        }
    }

    //
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
        val result: Boolean = ContextCompat.checkSelfPermission(this@EditGroupActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE) ==(PackageManager.PERMISSION_GRANTED)
        return result
    }
    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this@EditGroupActivity,storagepermissions,STORAGE_REQUEST_CODE)
    }
    //camera
    private fun checkCameraPermission(): Boolean{
        val result: Boolean = ContextCompat.checkSelfPermission(this@EditGroupActivity,
            Manifest.permission.CAMERA)==(PackageManager.PERMISSION_GRANTED)
        val result1: Boolean = ContextCompat.checkSelfPermission(this@EditGroupActivity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE)==(PackageManager.PERMISSION_GRANTED)
        return result&&result1
    }
    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this@EditGroupActivity,camerapermissions,CAMERA_REQUEST_CODE)
    }

    //
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
                        Toast.makeText(this,"Chưa đủ quyền Gallery", Toast.LENGTH_LONG).show()
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
                ic_group_edit.setImageURI(img_uri)

            } else if (requestCode == IMAGE_PICK_CAMERA_CODE){
                //gallery
                //set to img
                ic_group_edit.setImageURI(img_uri)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}