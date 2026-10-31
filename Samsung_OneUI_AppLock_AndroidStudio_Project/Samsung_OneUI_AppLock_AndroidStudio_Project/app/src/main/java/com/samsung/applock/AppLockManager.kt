package com.samsung.applock

import android.content.Context
import android.content.SharedPreferences

class AppLockManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("samsung_app_lock_prefs", Context.MODE_PRIVATE)
    private val tempUnlocked = mutableSetOf<String>()

    fun setAppLocked(packageName: String, isLocked: Boolean) {
        prefs.edit().putBoolean("locked_$packageName", isLocked).apply()
    }

    fun isAppLocked(packageName: String): Boolean {
        return prefs.getBoolean("locked_$packageName", false)
    }

    fun setTemporarilyUnlocked(packageName: String, unlocked: Boolean) {
        if (unlocked) {
            tempUnlocked.add(packageName)
        } else {
            tempUnlocked.remove(packageName)
        }
    }

    fun isTemporarilyUnlocked(packageName: String): Boolean {
        return tempUnlocked.contains(packageName)
    }
}
