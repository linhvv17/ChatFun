package com.example.chatfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import com.example.chatfun.fragment.PersonalFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity: AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private  var firebaseUserId:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
//        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
        setSupportActionBar(toolbar_register)
        supportActionBar!!.title= "Register"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_register.setNavigationOnClickListener {
            val intent = Intent()
            intent.setClass(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        //---------------
        mAuth = FirebaseAuth.getInstance()
        btn_register.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        //tao bien luu gia tri
        val username = edt_username_register.text.toString()
        val email = edt_email_register.text.toString()
        val password = edt_password_register.text.toString()
        if (username == ""){
            Toast.makeText(this,"Bạn chưa nhập Username !",Toast.LENGTH_LONG).show()
        }
        else if (email == ""){
            Toast.makeText(this,"Bạn chưa nhập Email !",Toast.LENGTH_LONG).show()
        }
        else if (password == ""){
            Toast.makeText(this,"Bạn chưa nhập Password !",Toast.LENGTH_LONG).show()
        }
        else{
            //tạo tài khoản với email và password
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { it ->
                    if (it.isSuccessful){
                        //tạp userId
                        firebaseUserId = mAuth.currentUser!!.uid
                        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUserId)
                        //tạo Hashmap truyền dữ liệu
                        val userHashMap = HashMap<String, Any>()
                        userHashMap["uid"] = firebaseUserId
                        userHashMap["username"] = username
                        userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/chatfun-92f3b.appspot.com/o/profile.png?alt=media&token=7b154fa8-93bd-4ca4-93fc-35c130160ba3"
                        userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/chatfun-92f3b.appspot.com/o/cover.jpg?alt=media&token=6bbc2a48-99ae-4a1a-b9b8-fd430654ff83"
                        userHashMap["status"] = "offline"
                        userHashMap["search"] = username.toLowerCase()
                        userHashMap["facebook"] = "https://m.facebook.com"
                        userHashMap["instagram"] = "https://m.instagram.com"
                        userHashMap["website"] = "https://m.google.com"
                        //===========
//                        startActivity(Intent(this@RegisterActivity,WelcomeActivity::class.java))
//                        finish()
                        refUsers.updateChildren(userHashMap)
                            .addOnCompleteListener {
                                if (it.isSuccessful){
                                    Toast.makeText(this,"Dang ky thanh cong!",Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this@RegisterActivity,LoginActivity::class.java))
                                    finish()
                                }
                                else{
                                    Toast.makeText(this,"Error Message: "+ it.exception?.message.toString(),Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                    else{
                        Toast.makeText(this,"Error Message: "+ it.exception?.message.toString(),Toast.LENGTH_LONG).show()

                    }
                }
        }
    }

    private fun loginUser() {
        val email = edt_email_register.text.toString()
        val password = edt_password_register.text.toString()
        if (email == ""){
            Toast.makeText(this,"Bạn chưa nhập Email !", Toast.LENGTH_LONG).show()
        }
        else if (password == ""){
            Toast.makeText(this,"Bạn chưa nhập Password !", Toast.LENGTH_LONG).show()
        }
        else{
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent()
                        intent.setClass(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        Toast.makeText(this,"Error Message: "+ it.exception?.message.toString(),Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}