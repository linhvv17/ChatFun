package com.example.chatfun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.example.chatfun.fragment.*
import com.example.chatfun.model.User
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
//    private lateinit var auth: FirebaseAuth
//    private lateinit var viewPagerAdapter: ViewPagerAdapter
//    private lateinit var viewPager: ViewPager
    private lateinit var mFirebaseAuth:FirebaseAuth
    private var refUsers: DatabaseReference? = null
    private  var firebaseUser:FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //==========
        mFirebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        //========
        initToolbar()
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)
        viewPagerAdapter.addFragment(HomeFragment(),"Home")
        viewPagerAdapter.addFragment(FriendFragment(),"Friend")
        viewPagerAdapter.addFragment(ChatFragment(),"Chat")
        viewPagerAdapter.addFragment(PersonalFragment(),"Personal")
        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        //        thay thế văn bản bằng biểu tượng.
        tabLayout.getTabAt(0)!!.setIcon(R.drawable.ic_home_page)
        tabLayout.getTabAt(1)!!.setIcon(R.drawable.ic_friend_page)
        tabLayout.getTabAt(2)!!.setIcon(R.drawable.ic_chat_page)
        tabLayout.getTabAt(3)!!.setIcon(R.drawable.ic_personal_page)
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