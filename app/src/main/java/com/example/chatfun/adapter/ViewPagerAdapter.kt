package com.example.chatfun.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ViewPagerAdapter(fragmentManager: FragmentManager):
    FragmentPagerAdapter(fragmentManager) {
    //tao list fragment
    private val fragments: ArrayList<Fragment>
    private val titles: ArrayList<String>
    init {
        fragments = ArrayList<Fragment>()
        titles = ArrayList<String>()
    }
    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }
    fun addFragment(fragment: Fragment,title: String){
        fragments.add(fragment)
        titles.add(title)
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return titles[position]
//    }
}