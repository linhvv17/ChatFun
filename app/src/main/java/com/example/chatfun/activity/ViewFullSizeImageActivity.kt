package com.example.chatfun.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.chatfun.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_full_size_image.*

class ViewFullSizeImageActivity : AppCompatActivity() {
    private lateinit var img_url: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_full_size_image)
        img_url = intent.getStringExtra("url")
        Picasso.get().load(img_url).placeholder(R.drawable.bellerin).into(image_view_full)
    }
}