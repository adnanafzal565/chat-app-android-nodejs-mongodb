package com.adnantech.chatapp_free_version.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.adnantech.chatapp_free_version.ChatActivity
import com.adnantech.chatapp_free_version.R
import com.adnantech.chatapp_free_version.models.Message
import com.adnantech.chatapp_free_version.models.User
import com.adnantech.chatapp_free_version.utils.MySharedPreference
import com.adnantech.chatapp_free_version.utils.Utility
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import java.nio.charset.Charset

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.i("myLog", "Refreshed token: " + token)
        saveToken(token)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(
                "CHANNEL_ID",
                "CHANNEL_NAME",
                importance
            )
            mChannel.description = "CHANNEL_DESC"
            mNotificationManager.createNotificationChannel(mChannel)
        }

        val context: Context = this

        if (message.data.isNotEmpty()) {
            message.data.let {
                val title = it["title"].toString()
                val messageBody = it["message"].toString()
                val name = it["name"].toString()
                val _id = it["_id"].toString()

                /*if (type == "private") {
                    val messageObject: Message = Message()
                    messageObject._id = _id
                    messageObject.message = messageText
                    messageObject.iv = iv
                    messageObject._id = _id
                    messageObject.sender = sender
                    messageObject.receiver = receiver
                    messageObject.createdAt = createdAt
                    messageObject.message = Utility.decryptMessage(messageObject)
                }*/

                val intent: Intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("_id", _id)
                intent.putExtra("name", name)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                val pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT
                )
                var defaultSoundUri: Uri? = null
                defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val importance = NotificationManager.IMPORTANCE_HIGH
                    val channel =
                        NotificationChannel(
                            "CHANNEL_ID",
                            "CHANNEL_NAME",
                            importance
                        )
                    channel.description = "CHANNEL_DESC"
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    val notificationManager = this.getSystemService(
                        NotificationManager::class.java
                    )
                    notificationManager.createNotificationChannel(channel)
                }
                val notificationBuilder: NotificationCompat.Builder =
                    NotificationCompat.Builder(this, "CHANNEL_ID")
                        .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                        .setContentTitle(title)
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setPriority(NotificationManager.IMPORTANCE_MAX)
                        .setChannelId("CHANNEL_ID")
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                notificationBuilder.setSmallIcon(R.drawable.ic_launcher_foreground)
                notificationBuilder.color = ContextCompat.getColor(this, R.color.purple_200)
                val notificationManager =
                    this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, notificationBuilder.build())
            }
        }
    }

    fun saveToken(token: String) {
        val mySharedPreference: MySharedPreference = MySharedPreference()
        val request: RequestQueue = Volley.newRequestQueue(this)
        val url = mySharedPreference.getAPIURL(this) + "/saveFCMToken"
        val requestBody: String = "token=" + token

        val queue: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
//                Log.i("myLog", response)
//                val generalResponse: GeneralResponse = Gson().fromJson(response, GeneralResponse::class.java)
            },

            Response.ErrorListener { error ->
                Log.i("myLog", "error => " + error)
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] =
                    "Bearer " + mySharedPreference.getAccessToken(applicationContext)
                return headers
            }

            override fun getBody(): ByteArray {
                return requestBody.toByteArray(Charset.defaultCharset())
            }
        }
        request.add(queue)
    }
}
