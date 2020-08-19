package com.example.chatfun.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chatfun.R
import com.example.chatfun.adapter.PostAdapter
import com.example.chatfun.model.Post
import com.example.chatfun.model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.personal_fragment.view.*

class PersonalFragment: Fragment() {
    //rc view
    private lateinit var mPostAdapter: PostAdapter
    private lateinit var mPosts: ArrayList<Post>
    private lateinit var recyclerView: RecyclerView
    //
    private var userReference: DatabaseReference? = null
    private var firebaseUser: FirebaseUser? = null
    private var RequestCode = 100
    private var imgUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.personal_fragment,container,false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        userReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")

        userReference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user = p0.getValue(User::class.java)
                    if (context != null){
                        view.tv_username_setting.text = user!!.getUsername()
                        Picasso.get().load(user!!.getProfile()).into(view.img_avatar)
                        Picasso.get().load(user!!.getCover()).into(view.img_view_cover)
                    }
                }
            }

        })


        view.img_avatar.setOnClickListener {
            pickUpImage()
        }
        view.img_view_cover.setOnClickListener {
            coverChecker = "cover"
            pickUpImage()
        }
        view.ic_set_facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLink()
        }
        view.ic_set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLink()
        }
        view.ic_set_youtobe.setOnClickListener {
            socialChecker = "youtobe"
            setSocialLink()
        }

        //
        //load post
        //set recyclerview
        recyclerView = view.findViewById(R.id.rc_my_personal_post)
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        recyclerView.layoutManager = layoutManager
        //init post list
        mPosts = arrayListOf()
        loadAllMyPost()

        return view
    }

    private fun loadAllMyPost() {
        //path cua all post
        val ref : DatabaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        //get all data
        ref.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(context, ""+p0.message, Toast.LENGTH_LONG).show()

            }

            override fun onDataChange(p0: DataSnapshot) {
                mPosts.clear()
                for (ds in p0.children){
                    val post: Post? = ds.getValue(Post::class.java)
                    if (post!!.uid == firebaseUser!!.uid){
                        mPosts.add(post!!)

                        mPostAdapter = PostAdapter(context, mPosts)

                        //set adapter
                        recyclerView.adapter = mPostAdapter
                    }

                }
            }

        })
    }

    private fun setSocialLink() {
        val builder = AlertDialog.Builder(
            context!!, R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )
        if (socialChecker == "website"){
            builder.setTitle("Write URL: ")
        }
        else{
            builder.setTitle("Write Username: ")
        }
        val editText = EditText(context)
        if (socialChecker == "website"){
            editText.hint = "e.g www.google.com"
        }
        else{
            editText.hint = "e.g Linh.khoanh.1997"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create",DialogInterface.OnClickListener {
                dialog, which ->
            val str = editText.text.toString()
            if (str == ""){
                Toast.makeText(context,"Please write something ....", Toast.LENGTH_LONG).show()
            }
            else{
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener {
                dialog, which ->
            dialog.cancel()
        })
        builder.show()

    }

    private fun saveSocialLink(str: String) {
        val mapSocialLink = HashMap<String,Any>()
//        mapSocialLink["profile"] = url
//        userReference!!.updateChildren(mapSocialLink)
        when(socialChecker){
            "facebook" ->{
                mapSocialLink["facebook"] = "https://www.facebook.com/$str"
            }
            "instagram" ->{
                mapSocialLink["instagram"] = "https://www.instagram.com/$str"
            }
            "youtobe" ->{
                mapSocialLink["youtobe"] = "https://www.youtobe.com/$str"
            }
        }
        userReference!!.updateChildren(mapSocialLink)
            .addOnCompleteListener { 
                task ->
                if (task.isSuccessful){
                    Toast.makeText(context,"Saved successfully", Toast.LENGTH_LONG).show()
                }
                else{

                }
            }
    }

    private fun pickUpImage() {
        val intent = Intent()
        intent.type = "image/"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }
    

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode &&
                resultCode == Activity.RESULT_OK &&
                data!!.data != null){
            imgUri = data.data
            Toast.makeText(context,"Uploading....", Toast.LENGTH_LONG).show()
            upLoadImageToDatabase()
        }
    }

    private fun upLoadImageToDatabase() {

        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Uploading, please wait...")
        progressBar.show()

        if (imgUri != null){
            val fileRef = storageRef!!.child(
                System.currentTimeMillis().toString() + ".jpg"
            )
            var uploadTask : StorageTask<*>
            uploadTask = fileRef.putFile(imgUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> {task ->
                if (task.isSuccessful){
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl

            }).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover"){
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        userReference!!.updateChildren(mapCoverImg)
                        coverChecker = ""
                    }
                    else{
                        val mapProfileImg = HashMap<String,Any>()
                        mapProfileImg["profile"] = url
                        userReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }

            }

        }
    }
}