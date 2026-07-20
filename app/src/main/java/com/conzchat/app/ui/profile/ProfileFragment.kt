package com.conzchat.app.ui.profile

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentProfileBinding
import com.conzchat.app.model.User
import com.conzchat.app.ui.settings.AccountSettingsFragment
import com.conzchat.app.ui.settings.CreditsFragment
import com.conzchat.app.ui.settings.ModsFragment
import com.conzchat.app.ui.settings.SettingsFragment
import com.conzchat.app.util.ApiManager
import com.conzchat.app.util.HarleyThemeHelper
import com.conzchat.app.util.toast
import java.io.File
import java.util.concurrent.TimeUnit

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance(uid: String) = ProfileFragment().apply {
            arguments = Bundle().apply { putString("uid", uid) }
        }
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val profileUid by lazy { arguments?.getString("uid") ?: ApiManager.currentUserId }
    private val myUid get() = ApiManager.currentUserId
    private val isMyProfile get() = profileUid.isEmpty() || profileUid == myUid
    private var profileUser: User? = null

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadAvatar(it) }
    }
    private val coverPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { uploadCover(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Show cached user immediately if viewing own profile
        if (isMyProfile && ApiManager.currentUser != null) {
            displayProfile(ApiManager.currentUser!!)
        }
        loadProfile()
    }

    private fun loadProfile() {
        val uid = if (isMyProfile) myUid else profileUid
        if (uid.isEmpty()) return
        ApiManager.getUser(uid) { user, _ ->
            activity?.runOnUiThread {
                if (_binding == null || user == null) return@runOnUiThread
                profileUser = user
                if (isMyProfile) ApiManager.currentUser = user
                displayProfile(user)
            }
        }
    }

    private fun displayProfile(user: User) {
        binding.tvDisplayName.text = user.displayName.ifEmpty { user.username }
        binding.tvUsername.text = "@${user.username}"

        // ── Status display ─────────────────────────────────────────────────
        if (user.status.isNotEmpty()) {
            binding.tvStatus.text = user.status
            binding.tvStatus.visibility = View.VISIBLE
        } else {
            binding.tvStatus.visibility = View.GONE
        }

        // Days on ConzChat
        if (user.created > 0) {
            val days = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - user.created)
            binding.tvDaysOnApp.text = when {
                days == 0L -> "Joined today"
                days == 1L -> "1 day on ConzChat"
                else -> "$days days on ConzChat"
            }
            binding.tvDaysOnApp.visibility = View.VISIBLE
        } else {
            binding.tvDaysOnApp.visibility = View.GONE
        }

        // Badges
        binding.tvDevBadge.visibility = if (user.role == "admin") View.VISIBLE else View.GONE
        binding.tvPremiumBadge.visibility = if (user.premium) View.VISIBLE else View.GONE

        // Photos
        Glide.with(this).load(ApiManager.normalizeUrl(user.photo.takeIf { it.isNotEmpty() }))
            .circleCrop().placeholder(R.drawable.ic_default_avatar).into(binding.ivAvatar)
        Glide.with(this).load(ApiManager.normalizeUrl(user.coverPhoto.takeIf { it.isNotEmpty() }))
            .placeholder(R.color.bgMain).into(binding.ivCover)

        if (isMyProfile) {
            binding.layoutOwnActions.visibility = View.VISIBLE
            binding.layoutOtherActions.visibility = View.GONE
            binding.tvStatusEdit.visibility = View.VISIBLE
            binding.btnEditCover.visibility = View.VISIBLE
            binding.btnChangeAvatar.visibility = View.VISIBLE

            // ── Edit status ────────────────────────────────────────────────
            binding.tvStatusEdit.setOnClickListener {
                val input = EditText(requireContext()).apply {
                    hint = "Set a status..."
                    setText(user.status)
                    setSelection(user.status.length)
                }
                val container = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.VERTICAL
                    setPadding(48, 16, 48, 0)
                    addView(input)
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Update Status")
                    .setView(container)
                    .setPositiveButton("Save") { _, _ ->
                        val newStatus = input.text.toString().trim()
                        val currentUser = profileUser ?: ApiManager.currentUser ?: return@setPositiveButton
                        ApiManager.updateProfile(
                            currentUser.displayName,
                            currentUser.photo,
                            currentUser.coverPhoto,
                            newStatus
                        ) { ok, err ->
                            activity?.runOnUiThread {
                                if (ok) {
                                    // Update cached user and refresh display immediately
                                    val updated = currentUser.copy(status = newStatus)
                                    profileUser = updated
                                    ApiManager.currentUser = updated
                                    displayProfile(updated)
                                    context?.toast("Status updated!")
                                } else {
                                    context?.toast(err ?: "Failed to update status")
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel", null).show()
            }

            // Photo pickers
            binding.ivAvatar.setOnClickListener { avatarPicker.launch("image/*") }
            binding.btnChangeAvatar.setOnClickListener { avatarPicker.launch("image/*") }
            binding.ivCover.setOnClickListener { coverPicker.launch("image/*") }
            binding.btnEditCover.setOnClickListener { coverPicker.launch("image/*") }

            // Navigation
            binding.btnEdit.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AccountSettingsFragment())
                    .addToBackStack(null).commit()
            }
            binding.btnAccountSettings.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, AccountSettingsFragment())
                    .addToBackStack(null).commit()
            }
            binding.btnMods.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, ModsFragment())
                    .addToBackStack(null).commit()
            }
            binding.btnCredits.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, CreditsFragment())
                    .addToBackStack(null).commit()
            }

            // ── About ConzChat ─────────────────────────────────────────────
            binding.btnAbout.setOnClickListener {
                showAboutDialog()
            }

            binding.btnCheckUpdates.setOnClickListener {
                context?.toast("Checking for updates...")
                ApiManager.checkForUpdate { info, _ ->
                    activity?.runOnUiThread {
                        if (info != null && info.versionCode > 0)
                            context?.toast("Version ${info.versionName} available!")
                        else
                            context?.toast("You are up to date!")
                    }
                }
            }
        } else {
            binding.layoutOwnActions.visibility = View.GONE
            binding.layoutOtherActions.visibility = View.VISIBLE
            binding.tvStatusEdit.visibility = View.GONE
            binding.btnEditCover.visibility = View.GONE
            binding.btnChangeAvatar.visibility = View.GONE

            binding.btnMessage.setOnClickListener {
                val fragment = com.conzchat.app.ui.chat.ChatFragment.newInstance(
                    user.uid, user.displayName.ifEmpty { user.username }, user.photo
                )
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null).commit()
            }
            binding.btnAddFriend.setOnClickListener {
                ApiManager.sendFriendRequest(user.uid) { ok, err ->
                    activity?.runOnUiThread {
                        if (ok) context?.toast("Friend request sent!")
                        else context?.toast(err ?: "Failed to send request")
                    }
                }
            }
            binding.btnBlock.setOnClickListener {
                val ctx = context ?: return@setOnClickListener
                val targetUid = user.uid
                val targetName = user.displayName.ifEmpty { user.username }
                val options = arrayOf("Block $targetName", "Report $targetName", "Block & Report")
                AlertDialog.Builder(ctx)
                    .setTitle("Block / Report")
                    .setItems(options) { _, which ->
                        when (which) {
                            0 -> AlertDialog.Builder(ctx)
                                .setTitle("Block $targetName?")
                                .setMessage("They won't be able to message you.")
                                .setPositiveButton("Block") { _, _ ->
                                    ApiManager.blockUser(targetUid) { ok, err ->
                                        activity?.runOnUiThread {
                                            if (ok) { ctx.toast("$targetName blocked"); parentFragmentManager.popBackStack() }
                                            else ctx.toast(err ?: "Failed to block")
                                        }
                                    }
                                }.setNegativeButton("Cancel", null).show()
                            1 -> {
                                val input = android.widget.EditText(ctx).apply { hint = "Reason for report" }
                                AlertDialog.Builder(ctx)
                                    .setTitle("Report $targetName")
                                    .setView(input)
                                    .setPositiveButton("Report") { _, _ ->
                                        val reason = input.text.toString().trim().ifEmpty { "No reason given" }
                                        ApiManager.reportUser(targetUid, reason) { ok, err ->
                                            activity?.runOnUiThread {
                                                if (ok) ctx.toast("Report submitted. Thank you.")
                                                else ctx.toast(err ?: "Failed to report")
                                            }
                                        }
                                    }.setNegativeButton("Cancel", null).show()
                            }
                            2 -> {
                                val input = android.widget.EditText(ctx).apply { hint = "Reason for report" }
                                AlertDialog.Builder(ctx)
                                    .setTitle("Block & Report $targetName")
                                    .setView(input)
                                    .setPositiveButton("Submit") { _, _ ->
                                        val reason = input.text.toString().trim().ifEmpty { "No reason given" }
                                        ApiManager.blockUser(targetUid) { _, _ -> }
                                        ApiManager.reportUser(targetUid, reason) { ok, err ->
                                            activity?.runOnUiThread {
                                                if (ok) { ctx.toast("$targetName blocked and reported"); parentFragmentManager.popBackStack() }
                                                else ctx.toast(err ?: "Failed")
                                            }
                                        }
                                    }.setNegativeButton("Cancel", null).show()
                            }
                        }
                    }.show()
            }
        }
    }

    private fun showAboutDialog() {
        val ctx = context ?: return

        // Build the dialog layout
        val scrollView = ScrollView(ctx)
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        // Story text
        val tvStory = TextView(ctx).apply {
            text = """ConzChat is a social media platform brought to you by Conz — he wanted to bring a fresh, new place for people to connect.

ConzChat started off as a web app with basic HTML, CSS, and JS with a Firebase backend, but as it grew popular Conz knew it couldn't survive as a basic web app anymore, so he rewrote the app in native Android Kotlin and wired up Firebase. It ran well for a month or so until it started running slow due to many users — this is when he realised the last thing holding ConzChat back was the basic crappy Firebase backend. So he ripped out all of Firebase and wrote a brand new custom Golang backend which he's hosting on a VPS, and now the app is running smooth as butter and is bulletproof in terms of user safety.

Does the app look a lot like Kik? YES. Does this mean it's a modded Kik? NO. Does this mean stuff is directly taken from Kik? NO. Conz simply really likes how Kik looks — it's been his favourite social media since childhood, so yes it has a lot of Kik vibes but it's intentionally done. ConzChat has nothing to do with Kik or Kik's servers."""
            textSize = 14f
            setTextColor(0xFFDDDDDD.toInt())
            setLineSpacing(0f, 1.4f)
        }
        container.addView(tvStory)

        // Spacer
        val spacer = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 32)
        }
        container.addView(spacer)

        // Divider
        val divider = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).also {
                it.bottomMargin = 24
            }
            setBackgroundColor(0x33FFFFFF)
        }
        container.addView(divider)

        // Contact on ConzChat button
        val btnConzChat = TextView(ctx).apply {
            text = "💬  Contact Conz on ConzChat"
            textSize = 14f
            setTextColor(0xFFCC0000.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 20, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                // Navigate to Conz's profile
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, newInstance("Conz"))
                    .addToBackStack(null).commit()
            }
        }
        container.addView(btnConzChat)

        // Divider 2
        val divider2 = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1).also {
                it.topMargin = 4
                it.bottomMargin = 4
            }
            setBackgroundColor(0x22FFFFFF)
        }
        container.addView(divider2)

        // Contact on Kik button
        val btnKik = TextView(ctx).apply {
            text = "📱  Contact Conz on KiK"
            textSize = 14f
            setTextColor(0xFF4CAF50.toInt())
            gravity = android.view.Gravity.CENTER
            setPadding(0, 20, 0, 20)
            setTypeface(null, android.graphics.Typeface.BOLD)
            isClickable = true
            isFocusable = true
            setOnClickListener {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse("https://kik.me/ConzNew"))
                    startActivity(intent)
                } catch (_: Exception) { }
            }
        }
        container.addView(btnKik)

        scrollView.addView(container)

        AlertDialog.Builder(ctx)
            .setTitle("ℹ️ About ConzChat")
            .setView(scrollView)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun uploadAvatar(uri: Uri) {
        val ctx = context ?: return
        val file = uriToFile(ctx, uri) ?: return
        context?.toast("Uploading photo...")
        ApiManager.uploadFile(file, "image/jpeg") { url, err ->
            activity?.runOnUiThread {
                if (url != null) {
                    val user = profileUser ?: ApiManager.currentUser ?: return@runOnUiThread
                    ApiManager.updateProfile(user.displayName, url, user.coverPhoto, user.status) { ok, _ ->
                        if (ok) activity?.runOnUiThread { loadProfile() }
                    }
                } else {
                    context?.toast("Upload failed: $err")
                }
            }
        }
    }

    private fun uploadCover(uri: Uri) {
        val ctx = context ?: return
        val file = uriToFile(ctx, uri) ?: return
        context?.toast("Uploading cover photo...")
        ApiManager.uploadFile(file, "image/jpeg") { url, err ->
            activity?.runOnUiThread {
                if (url != null) {
                    val user = profileUser ?: ApiManager.currentUser ?: return@runOnUiThread
                    ApiManager.updateProfile(user.displayName, user.photo, url, user.status) { ok, _ ->
                        if (ok) activity?.runOnUiThread { loadProfile() }
                    }
                } else {
                    context?.toast("Upload failed: $err")
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}

private fun uriToFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val file = File.createTempFile("upload_", ".jpg", context.cacheDir)
        file.outputStream().use { inputStream.copyTo(it) }
        file
    } catch (e: Exception) { null }
}
