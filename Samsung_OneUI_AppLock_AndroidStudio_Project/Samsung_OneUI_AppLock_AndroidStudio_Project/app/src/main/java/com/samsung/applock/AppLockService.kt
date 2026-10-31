package com.samsung.applock

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class AppLockService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var appLockManager: AppLockManager
    private var lastForegroundPackage: String? = null
    private var isChecking = true

    private val monitorRunnable = object : Runnable {
        override fun run() {
            if (isChecking) {
                checkForegroundApp()
                handler.postDelayed(this, 300) // Poll every 300ms
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        appLockManager = AppLockManager(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        handler.post(monitorRunnable)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY // Ensures service is restarted if Android OS kills it
    }

    override fun onDestroy() {
        isChecking = false
        handler.removeCallbacks(monitorRunnable)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun checkForegroundApp() {
        val currentPackage = getForegroundPackageName() ?: return

        // If package changed and it is protected
        if (currentPackage != packageName && currentPackage != lastForegroundPackage) {
            lastForegroundPackage = currentPackage

            if (appLockManager.isAppLocked(currentPackage) && !appLockManager.isTemporarilyUnlocked(currentPackage)) {
                // Launch Lock Overlay immediately
                val lockIntent = Intent(this, LockOverlayActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    putExtra("TARGET_PACKAGE", currentPackage)
                }
                startActivity(lockIntent)
            }
        }
    }

    private fun getForegroundPackageName(): String? {
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val events = usm.queryEvents(time - 1000 * 5, time)
        val event = UsageEvents.Event()
        var lastPkg: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPkg = event.packageName
            }
        }
        return lastPkg
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "App Lock Protection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Samsung App Lock active in the background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App Lock Active")
            .setContentText("Protected apps are secured")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "samsung_app_lock_channel"
        const val NOTIFICATION_ID = 1001
    }
}
