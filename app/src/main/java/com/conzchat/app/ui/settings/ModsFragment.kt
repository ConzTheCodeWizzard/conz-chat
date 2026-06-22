package com.conzchat.app.ui.settings

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.conzchat.app.databinding.FragmentModsBinding
import com.conzchat.app.util.ConzMods

class ModsFragment : Fragment() {

    companion object {
        fun newInstance() = ModsFragment()
    }

    private var _binding: FragmentModsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentModsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        val ctx = requireContext()

        // ===== Load saved states =====
        binding.switchReceipts.isChecked = ConzMods.isDisableReceipts(ctx)
        binding.switchTyping.isChecked = ConzMods.isDisableTyping(ctx)
        binding.switchFakeCamera.isChecked = ConzMods.isFakeCamera(ctx)
        binding.switchScreenshot.isChecked = ConzMods.isScreenshotProtection(ctx)
        binding.switchLightMode.isChecked = ConzMods.isLightMode(ctx)
        binding.switchAppLock.isChecked = ConzMods.isAppLockEnabled(ctx)
        binding.switchPictureBg.isChecked = ConzMods.isPictureBgEnabled(ctx)
        binding.switchVoiceChanger.isChecked = ConzMods.isVoiceChangerEnabled(ctx)
        binding.switchGhostMode.isChecked = ConzMods.isGhostMode(ctx)
        binding.switchAutoReply.isChecked = ConzMods.isAutoReply(ctx)
        binding.switchTranslate.isChecked = ConzMods.isTranslate(ctx)
        binding.switchSelfDestruct.isChecked = ConzMods.isSelfDestruct(ctx)
        binding.switchVibeSync.isChecked = ConzMods.isVibeSync(ctx)

        // Auto-reply message field
        if (ConzMods.isAutoReply(ctx)) {
            binding.etAutoReplyMsg.visibility = View.VISIBLE
        }
        binding.etAutoReplyMsg.setText(ConzMods.getAutoReplyMsg(ctx))

        // Notification sound buttons
        highlightSoundButton(ConzMods.getNotifSound(ctx))

        // ===== Listeners =====
        binding.switchReceipts.setOnCheckedChangeListener { _, checked ->
            ConzMods.setDisableReceipts(ctx, checked)
        }
        binding.switchTyping.setOnCheckedChangeListener { _, checked ->
            ConzMods.setDisableTyping(ctx, checked)
        }
        binding.switchFakeCamera.setOnCheckedChangeListener { _, checked ->
            ConzMods.setFakeCamera(ctx, checked)
        }
        binding.switchScreenshot.setOnCheckedChangeListener { _, checked ->
            ConzMods.setScreenshotProtection(ctx, checked)
            if (checked) {
                activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
        binding.switchLightMode.setOnCheckedChangeListener { _, checked ->
            ConzMods.setLightMode(ctx, checked)
            // Recreate activity to apply theme change
            activity?.recreate()
        }
        binding.switchAppLock.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                showSetPasswordDialog()
            } else {
                ConzMods.setAppLockEnabled(ctx, false)
                ConzMods.setAppLockPassword(ctx, "")
            }
        }
        binding.switchPictureBg.setOnCheckedChangeListener { _, checked ->
            ConzMods.setPictureBgEnabled(ctx, checked)
        }
        binding.switchVoiceChanger.setOnCheckedChangeListener { _, checked ->
            ConzMods.setVoiceChangerEnabled(ctx, checked)
        }
        binding.switchGhostMode.setOnCheckedChangeListener { _, checked ->
            ConzMods.setGhostMode(ctx, checked)
        }
        binding.switchAutoReply.setOnCheckedChangeListener { _, checked ->
            ConzMods.setAutoReply(ctx, checked)
            binding.etAutoReplyMsg.visibility = if (checked) View.VISIBLE else View.GONE
        }
        binding.switchTranslate.setOnCheckedChangeListener { _, checked ->
            ConzMods.setTranslate(ctx, checked)
        }
        binding.switchSelfDestruct.setOnCheckedChangeListener { _, checked ->
            ConzMods.setSelfDestruct(ctx, checked)
        }
        binding.switchVibeSync.setOnCheckedChangeListener { _, checked ->
            ConzMods.setVibeSync(ctx, checked)
        }

        // Auto-reply message save on focus lost
        binding.etAutoReplyMsg.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val msg = binding.etAutoReplyMsg.text.toString().trim()
                if (msg.isNotEmpty()) {
                    ConzMods.setAutoReplyMsg(ctx, msg)
                }
            }
        }

        // Notification sound buttons
        binding.btnSoundDefault.setOnClickListener {
            ConzMods.setNotifSound(ctx, "default")
            highlightSoundButton("default")
        }
        binding.btnSoundPop.setOnClickListener {
            ConzMods.setNotifSound(ctx, "pop")
            highlightSoundButton("pop")
        }
        binding.btnSoundChime.setOnClickListener {
            ConzMods.setNotifSound(ctx, "chime")
            highlightSoundButton("chime")
        }
        binding.btnSoundBuzz.setOnClickListener {
            ConzMods.setNotifSound(ctx, "buzz")
            highlightSoundButton("buzz")
        }
    }

    private fun highlightSoundButton(selected: String) {
        val buttons = listOf(
            "default" to binding.btnSoundDefault,
            "pop" to binding.btnSoundPop,
            "chime" to binding.btnSoundChime,
            "buzz" to binding.btnSoundBuzz
        )
        for ((key, btn) in buttons) {
            if (key == selected) {
                val bg = GradientDrawable().apply {
                    setColor(Color.parseColor("#FF3B3B"))
                    cornerRadius = 8f
                }
                btn.background = bg
                btn.setTextColor(Color.WHITE)
            } else {
                val bg = GradientDrawable().apply {
                    setColor(Color.parseColor("#2A2A2A"))
                    cornerRadius = 8f
                }
                btn.background = bg
                btn.setTextColor(Color.WHITE)
            }
        }
    }

    private fun showSetPasswordDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Set app lock password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
            setPadding(48, 24, 48, 24)
        }
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("App Lock")
            .setMessage("Set a password to lock ConzChat")
            .setView(input)
            .setPositiveButton("Set") { _, _ ->
                val pw = input.text.toString().trim()
                if (pw.length >= 4) {
                    ConzMods.setAppLockEnabled(requireContext(), true)
                    ConzMods.setAppLockPassword(requireContext(), pw)
                } else {
                    binding.switchAppLock.isChecked = false
                }
            }
            .setNegativeButton("Cancel") { _, _ ->
                binding.switchAppLock.isChecked = false
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Save auto-reply message if still in focus
        _binding?.let {
            val msg = it.etAutoReplyMsg.text.toString().trim()
            if (msg.isNotEmpty()) {
                ConzMods.setAutoReplyMsg(requireContext(), msg)
            }
        }
        _binding = null
    }
}
