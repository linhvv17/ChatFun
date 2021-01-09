package com.example.chatfun.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.example.chatfun.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.Executor

class LoginActivity: AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

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

        btn_reset_password.setOnClickListener {
                startActivity(
                    Intent(
                        this@LoginActivity,
                        ResetPasswordActivity::class.java
                    )
                )
        }

        btn_register.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
        }


        //Biometric
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(
                        applicationContext,
                        "Authentication error: $errString", Toast.LENGTH_SHORT
                    )
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    super.onAuthenticationSucceeded(result)
                    Toast.makeText(
                        applicationContext,
                        "Authentication succeeded!", Toast.LENGTH_SHORT
                    )
                        .show()
                    loginUser()
//                    val intent = Intent()
//                    intent.setClass(this@LoginActivity, MainActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(intent)
//                    finish()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        applicationContext, "Authentication failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for my app")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use account password")
            .build()

        // Prompt appears when user clicks "Log in".
        // Consider integrating with the keystore to unlock cryptographic operations,
        // if needed by your app.
        val biometricLoginButton =
            findViewById<Button>(R.id.btn_loginFinger)
        biometricLoginButton.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun loginUser() {
        val email = edt_email_login.text.toString().trim()
        val password = edt_password_login.text.toString().trim()
        if (email == ""){
            Toast.makeText(this, "Bạn chưa nhập Email !", Toast.LENGTH_LONG).show()
        }
        else if (password == ""){
            Toast.makeText(this, "Bạn chưa nhập Password !", Toast.LENGTH_LONG).show()
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
                        Toast.makeText(
                            this,
                            "Error Message: " + it.exception?.message.toString(),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }

    override fun onPause() {
        // TODO Auto-generated method stub
        super.onPause()
        //gọi hàm lưu trạng thái ở đây
        savingPreferences()
    }

    override fun onResume() {
        // TODO Auto-generated method stub
        super.onResume()
        //gọi hàm đọc trạng thái ở đây
        restoringPreferences()
    }

    /**
     * hàm lưu trạng thái
     * Shared Preferences là một bộ nhớ in-memory (RAM) đặt trên một bộ nhớ disk-storage (External Storage).
     * Mọi thao tác đều đi qua bộ nhớ in-memory đầu tiên sau đó mới đến disk-storage trong trường hợp cần thiết.
     */
    private fun  savingPreferences() {
        //tạo đối tượng getSharedPreferences
        val pre: SharedPreferences =
            this.getSharedPreferences("prefname", MODE_PRIVATE)
        //tạo đối tượng Editor để lưu thay đổi
        val editor = pre.edit()
        val user: String = edt_email_login.text.toString()
        val pwd: String = edt_password_login.text.toString()
        //boolean bchk = chksaveaccount.isChecked();
//        if(!bchk)
//        {
//            //xóa mọi lưu trữ trước đó
//            editor.clear();
//        }
//        else
//        {
        //lưu vào editor
        editor.putString("user", user)
        editor.putString("pwd", pwd)
        //editor.putBoolean("checked", bchk);
        //}
        //chấp nhận lưu xuống file
        editor.commit()
    }

    /**
     * hàm đọc trạng thái đã lưu trước đó
     */
    private fun restoringPreferences() {
        val pre: SharedPreferences =
            this.getSharedPreferences("prefname", MODE_PRIVATE)
        //lấy giá trị checked ra, nếu không thấy thì giá trị mặc định là false
//        boolean bchk=pre.getBoolean("checked", false);
//        if(bchk)
//        {
//            //lấy user, pwd, nếu không thấy giá trị mặc định là rỗng
        val user = pre.getString("user", "")
        val pwd = pre.getString("pwd", "")
        edt_email_login.setText(user)
        edt_password_login.setText(pwd)
//        }
//        chksaveaccount.setChecked(bchk);
    }
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }
}