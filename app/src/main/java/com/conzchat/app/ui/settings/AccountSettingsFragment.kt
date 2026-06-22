package com.conzchat.app.ui.settings

import android.app.AlertDialog
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentAccountSettingsBinding
import com.conzchat.app.util.AppPreferences
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.conzchat.app.util.HarleyThemeHelper

class AccountSettingsFragment : Fragment() {

    companion object {
        fun newInstance() = AccountSettingsFragment()
    }

    private var _binding: FragmentAccountSettingsBinding? = null
    private val binding get() = _binding!!
    private val auth get() = FirebaseAuth.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Brightness slider
        val currentBrightness = AppPreferences.getBrightness(requireContext())
        binding.seekBrightness.progress = currentBrightness
        binding.tvBrightnessValue.text = "$currentBrightness%"
        binding.seekBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                val brightness = progress / 100f
                binding.tvBrightnessValue.text = "$progress%"
                AppPreferences.setBrightness(requireContext(), progress)
                try {
                    val window = activity?.window ?: return
                    val lp = window.attributes
                    lp.screenBrightness = if (brightness <= 0f) 0.01f else brightness
                    window.attributes = lp
                } catch (_: Exception) {}
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        // Change display name
        binding.rowChangeName.setOnClickListener { showChangeNameDialog() }

        // Change password
        binding.rowChangePassword.setOnClickListener { showChangePasswordDialog() }

        // Logout
        binding.rowLogout.setOnClickListener { confirmLogout() }

        // Deactivate account
        binding.rowDeactivate.setOnClickListener { confirmDeactivate() }
    }

    private fun showChangeNameDialog() {
        val input = EditText(requireContext()).apply {
            hint = "New display name"
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            val uid = FirebaseManager.currentUid
            FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { snap ->
                setText(snap.getString("displayName") ?: snap.getString("username") ?: "")
            }
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(input)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Change Display Name")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) { context?.toast("Please enter a name"); return@setPositiveButton }
                FirebaseManager.usersRef.document(FirebaseManager.currentUid)
                    .update("displayName", name)
                    .addOnSuccessListener { context?.toast("Display name updated!") }
                    .addOnFailureListener { context?.toast("Failed: ${it.message}") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showChangePasswordDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
        }
        val etCurrent = EditText(requireContext()).apply {
            hint = "Current password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        val etNew = EditText(requireContext()).apply {
            hint = "New password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        layout.addView(etCurrent)
        layout.addView(etNew)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(layout)
            .setPositiveButton("Update") { _, _ ->
                val current = etCurrent.text.toString()
                val newPass = etNew.text.toString()
                if (current.isEmpty() || newPass.isEmpty()) { context?.toast("Fill in both fields"); return@setPositiveButton }
                if (newPass.length < 6) { context?.toast("Password must be at least 6 characters"); return@setPositiveButton }
                val user = auth.currentUser ?: return@setPositiveButton
                val credential = EmailAuthProvider.getCredential(user.email ?: "", current)
                user.reauthenticate(credential).addOnSuccessListener {
                    user.updatePassword(newPass).addOnSuccessListener {
                        context?.toast("Password updated!")
                    }.addOnFailureListener { context?.toast("Failed: ${it.message}") }
                }.addOnFailureListener { context?.toast("Current password incorrect") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmLogout() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                val uid = FirebaseManager.currentUid
                FirebaseManager.usersRef.document(uid).update("fcmToken", "")
                FirebaseAuth.getInstance().signOut()
                // Navigate to welcome
                requireActivity().recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeactivate() {
        AlertDialog.Builder(requireContext())
            .setTitle("Deactivate Account")
            .setMessage("This will permanently deactivate your account. This cannot be undone. Are you absolutely sure?")
            .setPositiveButton("Deactivate") { _, _ ->
                val uid = FirebaseManager.currentUid
                FirebaseManager.usersRef.document(uid).update(
                    mapOf("deactivated" to true, "forceLogout" to true, "logoutMessage" to "Your account has been deactivated.")
                ).addOnSuccessListener {
                    FirebaseAuth.getInstance().signOut()
                    requireActivity().recreate()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
