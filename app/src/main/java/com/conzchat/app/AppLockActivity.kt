package com.conzchat.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.conzchat.app.util.ConzMods

/**
 * Full-screen lock screen that appears when App Lock is enabled.
 * User must enter their password to proceed to MainActivity.
 */
class AppLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If app lock is not enabled or already unlocked, skip
        if (!ConzMods.isAppLockEnabled(this) || ConzMods.isAppUnlocked(this)) {
            proceedToMain()
            return
        }

        // Build the lock screen UI programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF0D0D0D.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(80, 0, 80, 0)
        }

        val icon = TextView(this).apply {
            text = "\uD83D\uDD12"
            textSize = 48f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 32)
        }
        layout.addView(icon)

        val title = TextView(this).apply {
            text = "ConzChat Locked"
            textSize = 24f
            setTextColor(0xFFFFFFFF.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 8)
        }
        layout.addView(title)

        val subtitle = TextView(this).apply {
            text = "Enter your password to unlock"
            textSize = 14f
            setTextColor(0xFFAAAAAA.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 0, 0, 48)
        }
        layout.addView(subtitle)

        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF666666.toInt())
            setBackgroundColor(0xFF222222.toInt())
            setPadding(32, 24, 32, 24)
            textSize = 16f
        }
        layout.addView(passwordInput, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 32 })

        val unlockBtn = android.widget.Button(this).apply {
            text = "UNLOCK"
            setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFFCC0022.toInt())
            textSize = 16f
            setPadding(0, 24, 0, 24)
            setOnClickListener {
                val entered = passwordInput.text.toString()
                val correct = ConzMods.getAppLockPassword(this@AppLockActivity)
                if (entered == correct) {
                    ConzMods.setAppUnlocked(this@AppLockActivity, true)
                    proceedToMain()
                } else {
                    Toast.makeText(this@AppLockActivity, "Wrong password", Toast.LENGTH_SHORT).show()
                    passwordInput.setText("")
                }
            }
        }
        layout.addView(unlockBtn, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ))

        setContentView(layout)
    }

    private fun proceedToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        // Don't allow back to bypass lock
        finishAffinity()
    }
}
