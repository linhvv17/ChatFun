package com.example.chatfun.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.andrognito.pinlockview.PinLockView
import com.example.chatfun.R


class LockScreenActivity : AppCompatActivity() {


    private var pass: String? = null

    private var mPinLockView: PinLockView? = null
    private var mIndicatorDots: IndicatorDots? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        //get SharedPreferences from getSharedPreferences("name_file", MODE_PRIVATE)
        val shared : SharedPreferences = getSharedPreferences("lock",MODE_PRIVATE)
//Using getXXX- with XX is type date you wrote to file "name_file"
        pass = shared.getString("pass","123456")

        mPinLockView =  findViewById(R.id.pin_lock_view)

        mIndicatorDots = findViewById(R.id.indicator_dots) as IndicatorDots

        mPinLockView!!.setPinLockListener(mPinLockListener)

        mPinLockView!!.attachIndicatorDots(mIndicatorDots)
        mPinLockView!!.setPinLockListener(mPinLockListener)

        mPinLockView!!.customKeySet = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
//        mPinLockView!!.enableLayoutShuffling()

        //mPinLockView.setCustomKeySet(new int[]{2, 3, 1, 5, 9, 6, 7, 0, 8, 4});
        //mPinLockView.enableLayoutShuffling();
        mPinLockView!!.pinLength = 6
        mPinLockView!!.textColor = ContextCompat.getColor(this, R.color.white)

        mIndicatorDots!!.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION
    }

    private val mPinLockListener: PinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {
            Log.d("TAG", "Pin complete: $pin")
            if (pin == pass){
//                val intent = Intent()
//                intent.setClass(this@LockScreenActivity, WelcomeActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
                finish()
            } else{
                Toast.makeText(this@LockScreenActivity,"WRONG PASSWORD ",Toast.LENGTH_LONG).show()
            }
//            finish()
        }


        override fun onEmpty() {
            Log.d("TAG", "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
            Log.d(
                "TAG",
                "Pin changed, new length $pinLength with intermediate pin $intermediatePin"
            )
        }
    }
}