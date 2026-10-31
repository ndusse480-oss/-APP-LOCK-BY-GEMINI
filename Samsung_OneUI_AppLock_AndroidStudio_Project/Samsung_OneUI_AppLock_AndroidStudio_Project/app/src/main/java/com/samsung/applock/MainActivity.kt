package com.samsung.applock

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var appLockManager: AppLockManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        appLockManager = AppLockManager(this)
        recyclerView = findViewById(R.id.recyclerViewApps)
        recyclerView.layoutManager = LinearLayoutManager(this)

        checkPermissionsAndPrompt()
        loadInstalledApps()
        startAppLockService()
    }

    private fun checkPermissionsAndPrompt() {
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Enable Usage Access for App Lock", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Enable 'Appear on Top' permission", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            startActivity(intent)
        }
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun startAppLockService() {
        val serviceIntent = Intent(this, AppLockService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun loadInstalledApps() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appList = mutableListOf<AppModel>()

        for (app in installedApps) {
            // Exclude self and non-launcher system apps
            if (app.packageName == packageName) continue
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                val name = pm.getApplicationLabel(app).toString()
                val icon = pm.getApplicationIcon(app)
                val isLocked = appLockManager.isAppLocked(app.packageName)
                appList.add(AppModel(name, app.packageName, icon, isLocked))
            }
        }
        appList.sortBy { it.name.lowercase() }

        recyclerView.adapter = AppAdapter(appList) { app, isLocked ->
            appLockManager.setAppLocked(app.packageName, isLocked)
            Toast.makeText(this, "${app.name} ${if (isLocked) "Locked" else "Unlocked"}", Toast.LENGTH_SHORT).show()
        }
    }
}

data class AppModel(
    val name: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable,
    var isLocked: Boolean
)

class AppAdapter(
    private val apps: List<AppModel>,
    private val onToggle: (AppModel, Boolean) -> Unit
) : RecyclerView.Adapter<AppAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val title: TextView = view.findViewById(R.id.appName)
        val pkg: TextView = view.findViewById(R.id.appPkg)
        val switchLock: Switch = view.findViewById(R.id.switchLock)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = apps[position]
        holder.title.text = app.name
        holder.pkg.text = app.packageName
        holder.icon.setImageDrawable(app.icon)
        holder.switchLock.setOnCheckedChangeListener(null)
        holder.switchLock.isChecked = app.isLocked

        holder.switchLock.setOnCheckedChangeListener { _, isChecked ->
            app.isLocked = isChecked
            onToggle(app, isChecked)
        }
    }

    override fun getItemCount() = apps.size
}
