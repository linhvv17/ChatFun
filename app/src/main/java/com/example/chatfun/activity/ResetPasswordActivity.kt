package com.example.chatfun.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import com.example.chatfun.R
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {
    private var inputEmail: EditText? = null
    private var btnReset: Button? = null
    private var btnBack: Button? = null
    private var auth: FirebaseAuth? = null
    private var progressBar: ProgressBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        inputEmail = findViewById<EditText>(R.id.email)
        btnReset = findViewById<Button>(R.id.btn_reset_password)
        btnBack = findViewById<Button>(R.id.btn_back)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        auth = FirebaseAuth.getInstance()
        btnBack!!.setOnClickListener { finish() }
        btnReset!!.setOnClickListener(View.OnClickListener {
            val email: String = inputEmail?.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(email)) {
                Toast.makeText(
                    application,
                    "Enter your registered email id",
                    Toast.LENGTH_SHORT
                ).show()
                return@OnClickListener
            }
            progressBar!!.visibility = View.VISIBLE
            auth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "We have sent you instructions to reset your password!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ResetPasswordActivity,
                            "Failed to send reset email!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    progressBar!!.visibility = View.GONE
                }
        })
    }
}