package com.yogeshpaliyal.deepr.server

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.yogeshpaliyal.deepr.MainActivity
import com.yogeshpaliyal.deepr.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class LocalServerService : Service() {
    private val localServerRepository: LocalServerRepository by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_START -> {
                // Start foreground immediately to avoid ANR
                startForeground(NOTIFICATION_ID, createNotification(null))
                serviceScope.launch {
                    localServerRepository.startServer()
                    observeServerState()
                }
            }

            ACTION_STOP -> {
                serviceScope.launch {
                    localServerRepository.stopServer()
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        }
        return START_STICKY
    }

    private fun observeServerState() {
        serviceScope.launch {
            localServerRepository.isRunning.collect { isRunning ->
                if (isRunning) {
                    val serverUrl = localServerRepository.serverUrl.first()
                    val notificationManager =
                        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, createNotification(serverUrl))
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.local_server_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW,
                ).apply {
                    description = getString(R.string.local_server_notification_channel_description)
                    setShowBadge(false)
                }

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(serverUrl: String?): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                0,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        val stopIntent =
            Intent(this, LocalServerService::class.java).apply {
                action = ACTION_STOP
            }
        val stopPendingIntent =
            PendingIntent.getService(
                this,
                1,
                stopIntent,
                PendingIntent.FLAG_IMMUTABLE,
            )

        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.local_server_running))
            .setContentText(
                if (serverUrl != null) {
                    getString(R.string.local_server_notification_text, serverUrl)
                } else {
                    getString(R.string.local_server_starting)
                },
            ).setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(
                0,
                getString(R.string.stop),
                stopPendingIntent,
            ).setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val CHANNEL_ID = "local_server_channel"
        private const val NOTIFICATION_ID = 1001
        const val ACTION_START = "com.yogeshpaliyal.deepr.ACTION_START_SERVER"
        const val ACTION_STOP = "com.yogeshpaliyal.deepr.ACTION_STOP_SERVER"

        fun startService(context: Context) {
            val intent =
                Intent(context, LocalServerService::class.java).apply {
                    action = ACTION_START
                }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent =
                Intent(context, LocalServerService::class.java).apply {
                    action = ACTION_STOP
                }
            context.startService(intent)
        }
    }
}
