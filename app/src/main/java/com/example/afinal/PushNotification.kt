package com.example.afinal

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotification: FirebaseMessagingService() {

    override fun onNewToken(token: String){
        super.onNewToken(token)
        Log.d("INFO", "FCM registration new token: $token")
        setToken(token)
    }
    @SuppressLint("ServiceCast")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
            Log.d("custominfo","received")
            val message = if(remoteMessage.notification!!.body == "normal") "Your heart rate is normal !" else "Your heart rate is abnormal"
            createNotification(this, "Result is ready!!!", message)
        }


    fun createNotification(context: Context, title: String?, message: String?) {
        // Create a Notification Manager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a Notification Channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Create a Notification Builder
        val builder = NotificationCompat.Builder(context, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // Show the notification
        notificationManager.notify(0, builder.build())
    }
}