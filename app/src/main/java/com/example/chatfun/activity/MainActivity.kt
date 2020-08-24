package com.example.chatfun.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.chatfun.R
import com.example.chatfun.adapter.ViewPagerAdapter
import com.example.chatfun.fragment.*
import com.example.chatfun.model.Chat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var viewPagerAdapter: ViewPagerAdapter
//    private lateinit var viewPager: ViewPager
    private lateinit var mFirebaseAuth:FirebaseAuth
    private var refUsers: DatabaseReference? = null
    private  var firebaseUser:FirebaseUser? = null

//    private val PermissionsRequestCode = 438
//    private lateinit var managePermissions: ManagePermissions


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        reference!!.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter =
                    ViewPagerAdapter(
                        supportFragmentManager
                    )
                var countUnReadMessage = 0
                for (dataSnapshot in p0.children) {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid)|| !chat.isIsSeen()!!){
                        countUnReadMessage+=1
                    }
                }
                viewPagerAdapter.addFragment(HomeFragment(),"Home")
                viewPagerAdapter.addFragment(FriendFragment(),"Friend")
                if (countUnReadMessage == 0){
                    viewPagerAdapter.addFragment(ChatFragment(),"Chat")
                }
                else{
                    viewPagerAdapter.addFragment(ChatFragment(),"($countUnReadMessage) Chat")
                }
                viewPagerAdapter.addFragment(GroupFragment(),"Group")
                viewPagerAdapter.addFragment(PersonalFragment(),"Personal")
                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)
                tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_home_page)
                tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_friend_page)
                tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_chat_page)
                tabLayout.getTabAt(3)!!.setIcon(R.drawable.ic_group)
                tabLayout.getTabAt(4)!!.setIcon(R.drawable.ic_personal_page)
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

        refUsers!!.addValueEventListener(object : ValueEventListener{
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
                FirebaseAuth.getInstance().signOut()
                val intent = Intent()
                intent.setClass(this, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
        }
        return false
    }
    private fun initToolbar(){
        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""
    }

}