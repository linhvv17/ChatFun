package com.example.chatfun.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatfun.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setSupportActionBar(toolbar_login)
        supportActionBar!!.title= "Login"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar_login.setNavigationOnClickListener {
//            onBackPressed()
            val intent = Intent()
            intent.setClass(this, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }
//        val intent = intent
//        val test = intent.getStringArrayListExtra("test")
//        edt_email_login.setText(test[0])
//        edt_password_login.setText(test[1])
        mAuth = FirebaseAuth.getInstance()
        //========
        btn_login.setOnClickListener {
            loginUser()
        }
    }

    private fun loginUser() {
        val email = edt_email_login.text.toString().trim()
        val password = edt_password_login.text.toString().trim()
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