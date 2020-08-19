package com.example.chatfun.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContextWrapper
import android.icu.text.CaseMap
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION_CODES.O
import androidx.annotation.RequiresApi
import com.google.firebase.database.core.Context

class OreoNotification(base: android.content.Context):ContextWrapper(base) {

    private var notificationManager:NotificationManager? = null

    @TargetApi(O)

    companion object{
        private const val CHANNEL_ID = "com.example.chatfun"
        private const val CHANNEL_NAME = "Message Chat Fun"

    }

    init {
        if (Build.VERSION.SDK_INT >= O){
            createChannel()
        }
    }
    @TargetApi(O)
    private fun createChannel(){
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        channel.enableLights(false)
        channel.enableVibration(true)
        channel.lockscreenVisibility=Notification.VISIBILITY_PRIVATE
        getManager!!.createNotificationChannel(channel)
    }
    val getManager:NotificationManager? get(){
        if (notificationManager == null){
            notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager
    }
    @TargetApi(O)
    fun getOreoNotification(
        title: String?,
        body: String?,
        pendingIntent: PendingIntent,
        soundUri: Uri?,
        icon: String?
    ): Notification.Builder{
        return Notification.Builder(applicationContext, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(icon!!.toInt())
            .setSound(soundUri)
            .setAutoCancel(true)

    }


}