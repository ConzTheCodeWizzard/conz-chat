package com.conzchat.app.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.conzchat.app.ConzChatApp
import com.conzchat.app.MainActivity
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentSettingsBinding
import com.conzchat.app.db.ConzDatabase
import com.conzchat.app.db.SavedAccount
import com.conzchat.app.util.AppPreferences
import com.conzchat.app.util.ConzMods
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.firebase.auth.EmailAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.conzchat.app.util.HarleyThemeHelper

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // ===== BRIGHTNESS =====
        val brightness = AppPreferences.getBrightness(requireContext())
        binding.seekBrightness.progress = brightness
        binding.tvBrightnessValue.text = "$brightness%"
        binding.seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvBrightnessValue.text = "$progress%"
                AppPreferences.setBrightness(requireContext(), progress)
                (activity as? MainActivity)?.applyBrightness(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // ===== CONZMODS =====
        binding.switchDisableReceipts.isChecked = ConzMods.isDisableReceipts(requireContext())
        binding.switchDisableTyping.isChecked = ConzMods.isDisableTyping(requireContext())
        binding.switchFakeCamera.isChecked = ConzMods.isFakeCamera(requireContext())
        binding.switchScreenshotProtection.isChecked = ConzMods.isScreenshotProtection(requireContext())

        binding.switchDisableReceipts.setOnCheckedChangeListener { _, checked ->
            ConzMods.setDisableReceipts(requireContext(), checked)
        }
        binding.switchDisableTyping.setOnCheckedChangeListener { _, checked ->
            ConzMods.setDisableTyping(requireContext(), checked)
        }
        binding.switchFakeCamera.setOnCheckedChangeListener { _, checked ->
            ConzMods.setFakeCamera(requireContext(), checked)
        }
        binding.switchScreenshotProtection.setOnCheckedChangeListener { _, checked ->
            ConzMods.setScreenshotProtection(requireContext(), checked)
            if (checked) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                context?.toast("Screenshot protection enabled")
            } else {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                context?.toast("Screenshot protection disabled")
            }
        }

        // Apply screenshot protection on open if enabled
        if (ConzMods.isScreenshotProtection(requireContext())) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        // ===== ACCOUNT ACTIONS =====
        binding.btnChangeDisplayName.setOnClickListener { showChangeDisplayName() }
        binding.btnChangePassword.setOnClickListener { showChangePassword() }

        // ===== CREDITS =====
        binding.btnCredits.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreditsFragment())
                .addToBackStack(null).commit()
        }

        // ===== DEV MENU =====
        if (uid == ConzChatApp.DEV_UID) {
            binding.btnDevMenu.visibility = View.VISIBLE
            binding.btnDevMenu.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, com.conzchat.app.ui.dev.DevMenuFragment())
                    .addToBackStack(null).commit()
            }
        }

        // ===== LOGOUT =====
        binding.btnLogout.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Would you like to save this account to Saved Accounts before logging out?")
                .setPositiveButton("Save & Logout") { _, _ ->
                    saveAccountThenLogout()
                }
                .setNegativeButton("Just Logout") { _, _ ->
                    FirebaseManager.auth.signOut()
                }
                .setNeutralButton("Cancel", null)
                .show()
        }

        // ===== DELETE ACCOUNT =====
        binding.btnDeleteAccount.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("This will permanently delete your account and all your data. This cannot be undone.")
                .setPositiveButton("Delete") { _, _ -> deleteAccount() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showChangeDisplayName() {
        val input = EditText(requireContext()).apply {
            hint = "Enter new display name"
            maxLines = 1
            filters = arrayOf(android.text.InputFilter.LengthFilter(30))
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { snap ->
            input.setText(snap.getString("displayName") ?: "")
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(input)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Change Display Name")
            .setView(container)
            .setPositiveButton("Set") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isEmpty()) { context?.toast("Name cannot be empty"); return@setPositiveButton }
                FirebaseManager.usersRef.document(uid).update("displayName", newName)
                    .addOnSuccessListener { context?.toast("Display name updated!") }
                    .addOnFailureListener { context?.toast("Failed to update name") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePassword() {
        val currentPwInput = EditText(requireContext()).apply {
            hint = "Current Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        val newPwInput = EditText(requireContext()).apply {
            hint = "New Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(currentPwInput)
            addView(View(context).apply { minimumHeight = 24 })
            addView(newPwInput)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("ConzChat pw changer")
            .setView(container)
            .setPositiveButton("Change") { _, _ ->
                val currentPw = currentPwInput.text.toString()
                val newPw = newPwInput.text.toString()
                if (currentPw.isEmpty() || newPw.isEmpty()) { context?.toast("Please fill in both fields"); return@setPositiveButton }
                if (newPw.length < 6) { context?.toast("Password must be at least 6 characters"); return@setPositiveButton }
                val user = FirebaseManager.auth.currentUser ?: return@setPositiveButton
                val email = user.email ?: return@setPositiveButton
                val credential = EmailAuthProvider.getCredential(email, currentPw)
                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(newPw)
                            .addOnSuccessListener { context?.toast("Password changed successfully!") }
                            .addOnFailureListener { context?.toast("Failed to change password") }
                    }
                    .addOnFailureListener { context?.toast("Current password is incorrect") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val user = FirebaseManager.auth.currentUser ?: return
        FirebaseManager.usersRef.document(uid).delete()
        user.delete()
            .addOnSuccessListener { context?.toast("Account deleted") }
            .addOnFailureListener { context?.toast("Failed to delete account. Please re-login and try again.") }
    }

    private fun saveAccountThenLogout() {
        // Use applicationContext to avoid issues when fragment detaches
        val appContext = requireContext().applicationContext
        val prefs = appContext.getSharedPreferences("conz_creds", android.content.Context.MODE_PRIVATE)
        val email = prefs.getString("lastEmail", null)
        val password = prefs.getString("lastPassword", null)
        val user = FirebaseManager.auth.currentUser

        if (email.isNullOrEmpty() || password.isNullOrEmpty() || user == null) {
            Toast.makeText(appContext, "No credentials found — logging out", Toast.LENGTH_SHORT).show()
            FirebaseManager.auth.signOut()
            return
        }

        // Get username from Firestore then save to Room DB
        FirebaseManager.usersRef.document(user.uid).get().addOnSuccessListener { doc ->
            val username = doc.getString("displayName")
                ?: doc.getString("username")
                ?: email.substringBefore("@")
            val db = ConzDatabase.get(appContext)
            CoroutineScope(Dispatchers.IO).launch {
                // Check if already saved
                val existing = db.savedAccountDao().findByEmail(email)
                if (existing != null) {
                    // Update existing entry
                    db.savedAccountDao().insert(
                        existing.copy(password = password, username = username, savedAt = System.currentTimeMillis())
                    )
                } else {
                    db.savedAccountDao().insert(
                        SavedAccount(
                            email = email,
                            password = password,
                            username = username,
                            savedAt = System.currentTimeMillis()
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Account saved!", Toast.LENGTH_SHORT).show()
                    FirebaseManager.auth.signOut()
                }
            }
        }.addOnFailureListener {
            // Still save with email as username fallback
            val db = ConzDatabase.get(appContext)
            CoroutineScope(Dispatchers.IO).launch {
                db.savedAccountDao().insert(
                    SavedAccount(
                        email = email,
                        password = password,
                        username = email.substringBefore("@"),
                        savedAt = System.currentTimeMillis()
                    )
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(appContext, "Account saved!", Toast.LENGTH_SHORT).show()
                    FirebaseManager.auth.signOut()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
