package com.samsung.applock

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class LockOverlayActivity : AppCompatActivity() {

    private lateinit var appLockManager: AppLockManager
    private var targetPackage: String? = null
    private var enteredPin = StringBuilder()
    private val correctPin = "1234" // Default or user configured

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_overlay)

        appLockManager = AppLockManager(this)
        targetPackage = intent.getStringExtra("TARGET_PACKAGE")

        setupAppDetails()
        setupKeypad()
        setupBiometrics()
    }

    private fun setupAppDetails() {
        val appIconView = findViewById<ImageView>(R.id.lockedAppIcon)
        val appNameView = findViewById<TextView>(R.id.lockedAppName)

        if (targetPackage != null) {
            try {
                val pm = packageManager
                val appInfo = pm.getApplicationInfo(targetPackage!!, 0)
                appNameView.text = pm.getApplicationLabel(appInfo)
                appIconView.setImageDrawable(pm.getApplicationIcon(appInfo))
            } catch (e: Exception) {
                appNameView.text = "Protected App"
            }
        }
    }

    private fun setupKeypad() {
        val pinDots = listOf(
            findViewById<View>(R.id.dot0),
            findViewById<View>(R.id.dot1),
            findViewById<View>(R.id.dot2),
            findViewById<View>(R.id.dot3)
        )

        fun updateDots() {
            for (i in 0 until 4) {
                if (i < enteredPin.length) {
                    pinDots[i].setBackgroundResource(R.drawable.dot_filled)
                } else {
                    pinDots[i].setBackgroundResource(R.drawable.dot_empty)
                }
            }
        }

        val digitIds = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in digitIds) {
            findViewById<Button>(id).setOnClickListener { v ->
                if (enteredPin.length < 4) {
                    enteredPin.append((v as Button).text)
                    updateDots()

                    if (enteredPin.length == 4) {
                        if (enteredPin.toString() == correctPin) {
                            unlockSuccess()
                        } else {
                            Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show()
                            enteredPin.clear()
                            updateDots()
                        }
                    }
                }
            }
        }

        findViewById<View>(R.id.btnDelete).setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updateDots()
            }
        }

        findViewById<View>(R.id.btnCancel).setOnClickListener {
            // Go to home launcher
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(homeIntent)
            finish()
        }
    }

    private fun setupBiometrics() {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                unlockSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Samsung App Lock")
            .setSubtitle("Confirm fingerprint or face to open")
            .setNegativeButtonText("Use PIN")
            .build()

        findViewById<View>(R.id.biometricIcon).setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }

        // Auto prompt biometrics on launch
        biometricPrompt.authenticate(promptInfo)
    }

    private fun unlockSuccess() {
        targetPackage?.let {
            appLockManager.setTemporarilyUnlocked(it, true)
        }
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Prevent bypassing lock by pressing back
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}
