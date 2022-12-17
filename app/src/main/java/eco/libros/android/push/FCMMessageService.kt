package eco.libros.android.push

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import eco.libros.android.R
import eco.libros.android.common.variable.GlobalVariable

class FCMMessageService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = remoteMessage.data["channelId"]
        val ticker = remoteMessage.data["ticker"]
        val title = remoteMessage.data["title"]
        val msg = remoteMessage.data["msg"]
        val isNotify = remoteMessage.data["pushType"]


        if (msg != null && msg.trim { it <= ' ' }.isNotEmpty()) {
            val notificationIntent = Intent(this, PushDialogActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("ticker", title)
                putExtra("msg", msg)
            }
//                notificationIntent.putExtra("ticker", title)
//                notificationIntent.putExtra("msg", msg)

            val contentIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT
            )

            val builder = NotificationCompat.Builder(this, channelId!!)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(title)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)

            builder.setDefaults(Notification.DEFAULT_ALL)
            builder.setContentIntent(contentIntent)

            // Add as notification

            // Add as notification
            manager.notify(GlobalVariable.NOTIFICATION_NO, builder.build())
        }
    }

}