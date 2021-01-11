package com.example.chatfun.activity

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.chatfun.R
import com.example.chatfun.adapter.ViewPagerAdapter
import com.example.chatfun.fragment.*
import com.example.chatfun.model.Chat
import com.example.chatfun.notifications.Token
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var viewPagerAdapter: ViewPagerAdapter
//    private lateinit var viewPager: ViewPager
    private lateinit var mFirebaseAuth:FirebaseAuth
    private var refUsers: DatabaseReference? = null
    private  var firebaseUser:FirebaseUser? = null

    private var isLockScreen: Boolean? = null
//    private val PermissionsRequestCode = 438
//    private lateinit var managePermissions: ManagePermissions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //get SharedPreferences from getSharedPreferences("name_file", MODE_PRIVATE)
        val shared : SharedPreferences = this.getSharedPreferences("lock", MODE_PRIVATE)
//Using getXXX- with XX is type date you wrote to file "name_file"
        isLockScreen = shared.getBoolean("lock", false)
//        Log.d("ENCRYPT", ASE().encrypt("ABCD"))
//        Log.d("ENCRYPT", ASE().decrypt("Gg787RF91GZIXp/GIHHfew=="))

        //check permission
        // Initialize a list of required permissions to request runtime
//        val list = listOf<String>(
//            Manifest.permission.CAMERA,
//            Manifest.permission.READ_CONTACTS,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.SEND_SMS,
//            Manifest.permission.READ_CALENDAR
//        )

//        // Initialize a new instance of ManagePermissions class
//        managePermissions = ManagePermissions(this,list,PermissionsRequestCode)

        // Button to check permissions states
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
//            managePermissions.checkPermissions()
//        }

        //==========
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        //========
        initToolbar()
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
//        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
//        viewPagerAdapter.addFragment(HomeFragment(),"Home")
//        viewPagerAdapter.addFragment(FriendFragment(),"Friend")
//        viewPagerAdapter.addFragment(ChatFragment(),"Chat")
//        viewPagerAdapter.addFragment(PersonalFragment(),"Personal")
//        viewPager.adapter = viewPagerAdapter
//        tabLayout.setupWithViewPager(viewPager)

        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter =
                    ViewPagerAdapter(
                        supportFragmentManager
                    )
                var countUnReadMessage = 0
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) || !chat.isIsSeen()!!) {
                        countUnReadMessage += 1
                    }
                }
                viewPagerAdapter.addFragment(HomeFragment(), "Home")
                viewPagerAdapter.addFragment(FriendFragment(), "Friend")
                if (countUnReadMessage == 0) {
                    viewPagerAdapter.addFragment(ChatFragment(), "Chat")
                } else {
                    viewPagerAdapter.addFragment(ChatFragment(), "($countUnReadMessage) Chat")
                }
                viewPagerAdapter.addFragment(GroupFragment(), "Group")
                viewPagerAdapter.addFragment(PersonalFragment(), "Personal")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
                tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_home_24)
                tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_people_24)
                tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_baseline_message_24)
                tabLayout.getTabAt(3)!!.setIcon(R.drawable.ic_baseline_group_work_24)
                tabLayout.getTabAt(4)!!.setIcon(R.drawable.ic_baseline_person_24)
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
        //        thay thế văn bản bằng biểu tượng.

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                //được gọi khi một tab chuyển sang trạng thái được chọn.

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                //được gọi khi một tab không còn được chọn.

            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                //được gọi khi một tab đã được chọn được chọn lại bởi người dùng.

            }
        })

        refUsers!!.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
//                if (p0.exists()){
//                    val user: User? = p0.getValue(User::class.java)
//                    tv_username_profile.text = user?.username
////                    Log.d("AAA", tv_username_profile.text.toString())
//                    Picasso.get().load(user?.profile).into(img_view_profile)
//                }
                tv_username_profile.text = p0.child("username").value.toString()
                Picasso.get().load(p0.child("profile").value.toString()).into(img_view_profile)
            }

        })

//        auth = Firebase.auth
//        viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
//        viewPagerAdapter.addFragment(HomeFragment(),"Home")
//        viewPagerAdapter.addFragment(FriendFragment(),"Friend")
//        viewPagerAdapter.addFragment(ChatFragment(),"Chat")
//        viewPagerAdapter.addFragment(MoreFragment(),"More")
//        viewPager.adapter = viewPagerAdapter
//        tab_layout.setupWithViewPager(viewPager)

        updateToken(FirebaseInstanceId.getInstance().token)
    }

    // Receive the permissions request result
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
//                                            grantResults: IntArray) {
//        when (requestCode) {
//            PermissionsRequestCode ->{
//                val isPermissionsGranted = managePermissions
//                    .processPermissionsResult(requestCode,permissions,grantResults)
//
//                if(isPermissionsGranted){
//                    // Do the task now
////                    toast("Permissions granted.")
//                }else{
////                    toast("Permissions denied.")
//                }
//                return
//            }
//        }
//    }


    private fun updateToken(token: String?) {
        val ref = FirebaseDatabase.getInstance().reference.child("Tokens")
        val token1 = Token(token!!)
        ref.child(firebaseUser!!.uid).setValue(token1)
    }

    override fun onStart() {
        super.onStart()
        mFirebaseAuth!!.addAuthStateListener {
            this.mFirebaseAuth!!
        }

    }

    override fun onStop() {
        super.onStop()
        mFirebaseAuth!!.removeAuthStateListener{
            this.mFirebaseAuth!!
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logout -> {

                val options = arrayOf<CharSequence>(
                    "Log Out",
                    "Reset Password",
                    "Set Pass Lock",
                    "Enable LockScreen",
                    "Disable LockScreen"
                )
                //dialog
                var builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle("Chose Option")
                //set option
                builder.setItems(
                    options
                ) { dialog, which ->
                    if (which == 0) {
                        FirebaseAuth.getInstance().signOut()
                        val intent = Intent()
                        intent.setClass(this, WelcomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else if (which == 1) {
                        updatePassWord()
                    } else if (which == 2) {
                        setPassLockScreen()
                    } else if (which == 3) {
                        enableLockScreen()
                    } else if (which == 4) {
                        disableLockScreen()
                    }
                }
                builder.create().show()

                return true
            }
        }
        return false
    }

    private fun disableLockScreen() {
        isLockScreen = false
        //Create a object SharedPreferences from getSharedPreferences("name_file",MODE_PRIVATE) of Context
        val pref : SharedPreferences = this.getSharedPreferences("lock", MODE_PRIVATE);
        //Using putXXX - with XXX is type data you want to write like: putString, putInt...   from      Editor object
        val editor = pref.edit();
        editor.putBoolean("lock", false)
        //finally, when you are done saving the values, call the commit() method.
        editor.commit()
    }

    private fun enableLockScreen() {
        isLockScreen = true
        //Create a object SharedPreferences from getSharedPreferences("name_file",MODE_PRIVATE) of Context
        val pref : SharedPreferences = this.getSharedPreferences("lock", MODE_PRIVATE);
        //Using putXXX - with XXX is type data you want to write like: putString, putInt...   from      Editor object
        val editor = pref.edit();
        editor.putBoolean("lock", true)
        //finally, when you are done saving the values, call the commit() method.
        editor.commit()
    }

    private fun setPassLockScreen() {

        val builder = AlertDialog.Builder(
            this, R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )
        val editText = EditText(this)
        builder.setView(editText)
        builder.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(this, "Please write something ....", Toast.LENGTH_LONG)
                    .show()
            } else {
//                progressBar.visibility = View.VISIBLE

                //Create a object SharedPreferences from getSharedPreferences("name_file",MODE_PRIVATE) of Context
                val pref: SharedPreferences = this.getSharedPreferences("lock", MODE_PRIVATE);
                //Using putXXX - with XXX is type data you want to write like: putString, putInt...   from      Editor object
                val editor = pref.edit();
                editor.putString("pass", str)
                //finally, when you are done saving the values, call the commit() method.
                editor.commit()
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()

    }

    private fun updatePassWord() {
        val builder = AlertDialog.Builder(
            this, R.style.Theme_AppCompat_DayNight_Dialog_Alert
        )
        val editText = EditText(this)
        builder.setView(editText)
        builder.setPositiveButton("Update", DialogInterface.OnClickListener { dialog, which ->
            val str = editText.text.toString()
            if (str == "") {
                Toast.makeText(this, "Please write something ....", Toast.LENGTH_LONG)
                    .show()
            } else {
                progressBar.visibility = View.VISIBLE
                firebaseUser!!.updatePassword(editText.text.toString())
                    .addOnCompleteListener(OnCompleteListener<Void?> { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@MainActivity,
                                "Password is updated, sign in with new password!",
                                Toast.LENGTH_SHORT
                            ).show()
//                            signOut()
                            progressBar.visibility = View.GONE
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Failed to update password!",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = View.GONE
                        }
                    })

            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        })
        builder.show()
    }

    //sign out method
    fun signOut() {
        mFirebaseAuth.signOut()
    }
    private fun initToolbar(){
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
    }

//    private fun checkScreenOff(){
//        if (this.getSystemService(Context.KEYGUARD_SERVICE).inKeyguardRestrictedInputMode()) {
//            //locked
//        } else {
//            //not locked
//        }
//    }



    override fun onPause() {
        super.onPause()
        Log.d("onPause", "onPause")
        if (isLockScreen!!){
            val intent = Intent()
            intent.setClass(this, LockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
//        finish()
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume", "onResume")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("onRestart", "onRestart")
    }
}