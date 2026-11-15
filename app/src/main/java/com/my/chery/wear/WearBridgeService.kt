package com.my.chery.wear

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.my.chery.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** 前台服务：常驻穿戴桥，保证手表随时能通过蓝牙拿到车况。 */
class WearBridgeService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, notification(WearBridge.state.value))
        WearBridge.attach(this)
        scope.launch {
            WearBridge.state.collect { text ->
                notification(text)?.let { manager().notify(NOTIFICATION_ID, it) }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "手表互联",
            NotificationManager.IMPORTANCE_LOW
        )
        channel.description = "保持与小米手表的蓝牙数据通道"
        manager().createNotificationChannel(channel)
    }

    private fun notification(text: String): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_lock)
        .setContentTitle("手表互联")
        .setContentText(text)
        .setOngoing(true)
        .setSilent(true)
        .build()

    private fun manager() = getSystemService(NotificationManager::class.java)

    companion object {
        private const val CHANNEL_ID = "wear_bridge"
        private const val NOTIFICATION_ID = 1001

        fun ensure(context: Context) {
            runCatching {
                ContextCompat.startForegroundService(
                    context,
                    Intent(context, WearBridgeService::class.java)
                )
            }
        }
    }
}
