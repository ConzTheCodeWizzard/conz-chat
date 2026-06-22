package com.conzchat.app.ui.profile

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.ConzChatApp
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentProfileBinding
import com.conzchat.app.ui.settings.AccountSettingsFragment
import com.conzchat.app.ui.settings.ModsFragment
import com.conzchat.app.ui.settings.CreditsFragment
import com.conzchat.app.ui.settings.CheckUpdatesFragment
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.ImageUtils
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.conzchat.app.util.HarleyThemeHelper

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance(uid: String) = ProfileFragment().apply {
            arguments = Bundle().apply { putString("uid", uid) }
        }
    }

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileUid by lazy { arguments?.getString("uid") ?: FirebaseManager.currentUid }
    private val myUid get() = FirebaseManager.currentUid
    private val isMyProfile get() = profileUid == myUid

    private var profileData: Map<String, Any> = emptyMap()
    private var profileListener: ListenerRegistration? = null

    // Pickers
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

        if (isMyProfile) {
            binding.layoutOwnActions.visibility = View.VISIBLE
            binding.layoutOtherActions.visibility = View.GONE
            binding.btnAccountSettings.setOnClickListener { openFragment(AccountSettingsFragment()) }
            binding.btnMods.setOnClickListener { openFragment(ModsFragment()) }
            binding.btnCredits.setOnClickListener { openFragment(CreditsFragment()) }
            binding.btnCheckUpdates.setOnClickListener { openFragment(CheckUpdatesFragment()) }

            // Tap avatar or cover to change — no camera icon, just tap
            binding.ivAvatar.setOnClickListener { avatarPicker.launch("image/*") }
            binding.ivCover.setOnClickListener { coverPicker.launch("image/*") }
            binding.tvStatusEdit.setOnClickListener { showEditStatus() }
        } else {
            binding.layoutOwnActions.visibility = View.GONE
            binding.layoutOtherActions.visibility = View.VISIBLE
            binding.btnMessage.setOnClickListener {
                val name = profileData["displayName"] as? String
                    ?: profileData["username"] as? String ?: ""
                val photo = profileData["photo"] as? String ?: ""
                parentFragmentManager.popBackStack()
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer,
                        com.conzchat.app.ui.chat.ChatFragment.newInstance(profileUid, name, photo))
                    .addToBackStack(null).commit()
            }
            binding.btnAddFriend.setOnClickListener { sendFriendRequest() }
            binding.btnBlock.setOnClickListener { blockUser() }

            // Dev options — shown as extra button for dev viewing other users
            if (myUid == ConzChatApp.DEV_UID) {
                binding.btnBlock.text = "🔧 Dev Options"
                binding.btnBlock.setTextColor(0xFFFFD700.toInt())
                binding.btnBlock.setOnClickListener { showDevOptions() }
            }
        }

        loadProfile()
    }

    private fun loadProfile() {
        profileListener = FirebaseManager.usersRef.document(profileUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                profileData = snap.data ?: emptyMap()
                renderProfile()
            }
    }

    private fun renderProfile() {
        val username = profileData["username"] as? String ?: ""
        val displayName = profileData["displayName"] as? String ?: username
        val bio = profileData["bio"] as? String ?: ""
        val status = profileData["status"] as? String ?: ""
        val photo = profileData["photo"] as? String ?: ""
        val cover = profileData["coverPhoto"] as? String ?: ""
        val premium = profileData["premium"] as? Boolean ?: false
        val isDev = profileUid == ConzChatApp.DEV_UID
        val created = profileData["created"] as? Long ?: System.currentTimeMillis()
        val friends = (profileData["friendCount"] as? Long)?.toInt()
            ?: (profileData["friends"] as? List<*>)?.size
            ?: 0
        val daysOnApp = ((System.currentTimeMillis() - created) / 86400000L).toInt()

        binding.tvDisplayName.text = displayName
        binding.tvUsername.text = "@$username"

        // Dev badge
        binding.tvDevBadge.visibility = if (isDev) View.VISIBLE else View.GONE

        // Premium badge
        binding.tvPremiumBadge.visibility = if (premium && !isDev) View.VISIBLE else View.GONE

        // Status / bio
        val statusText = status.ifEmpty { bio }
        if (statusText.isNotEmpty()) {
            binding.tvStatus.text = statusText
            binding.tvStatus.visibility = View.VISIBLE
        } else {
            binding.tvStatus.visibility = View.GONE
        }

        // Edit status (own profile)
        if (isMyProfile) {
            binding.tvStatusEdit.text = if (statusText.isNotEmpty()) "✏️ Edit status" else "+ Add a status"
            binding.tvStatusEdit.visibility = View.VISIBLE
        } else {
            binding.tvStatusEdit.visibility = View.GONE
        }

        binding.tvFriendCount.text = "$friends friends"
        binding.tvDaysOnApp.text = "$daysOnApp days on ConzChat"

        // Avatar
        if (photo.isNotEmpty()) {
            Glide.with(this).load(photo)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.ivAvatar)
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }

        // Cover photo
        if (cover.isNotEmpty()) {
            Glide.with(this).load(cover).centerCrop().into(binding.ivCover)
        } else {
            binding.ivCover.setImageDrawable(null)
            binding.ivCover.setBackgroundColor(0xFF1A1A1A.toInt())
        }
    }

    private fun showEditStatus() {
        val current = profileData["status"] as? String ?: ""
        val input = EditText(requireContext()).apply {
            setText(current)
            hint = "What's on your mind..."
            maxLines = 2
            filters = arrayOf(android.text.InputFilter.LengthFilter(60))
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF888888.toInt())
        }
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 16, 48, 0)
            addView(input)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Set your status")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                val newStatus = input.text.toString().trim()
                FirebaseManager.usersRef.document(myUid).update("status", newStatus)
                    .addOnSuccessListener { context?.toast("Status updated!") }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditProfile() {
        val dialog = EditProfileDialog.newInstance(
            displayName = profileData["displayName"] as? String ?: "",
            bio = profileData["bio"] as? String ?: ""
        )
        dialog.setOnSave { displayName, bio ->
            FirebaseManager.usersRef.document(myUid).update(
                mapOf("displayName" to displayName, "bio" to bio)
            ).addOnSuccessListener { context?.toast("Profile updated!") }
        }
        dialog.show(childFragmentManager, "edit_profile")
    }

    private fun uploadAvatar(uri: Uri) {
        context?.toast("Uploading photo...")
        val base64 = ImageUtils.compressImageToBase64(requireContext(), uri, maxSize = 400, quality = 80) ?: return
        FirebaseManager.usersRef.document(myUid).update("photo", base64)
            .addOnSuccessListener { context?.toast("Profile photo updated!") }
            .addOnFailureListener { context?.toast("Upload failed") }
    }

    private fun uploadCover(uri: Uri) {
        context?.toast("Uploading cover...")
        val base64 = ImageUtils.compressCoverPhotoToBase64(requireContext(), uri) ?: return
        FirebaseManager.usersRef.document(myUid).update("coverPhoto", base64)
            .addOnSuccessListener { context?.toast("Cover photo updated!") }
            .addOnFailureListener { context?.toast("Upload failed") }
    }

    private fun sendFriendRequest() {
        val reqData = hashMapOf(
            "from" to myUid,
            "to" to profileUid,
            "status" to "pending",
            "time" to System.currentTimeMillis()
        )
        FirebaseManager.friendRequestsRef.add(reqData)
            .addOnSuccessListener { context?.toast("Friend request sent!") }
            .addOnFailureListener { context?.toast("Failed to send request") }
    }

    private fun blockUser() {
        AlertDialog.Builder(requireContext())
            .setTitle("Block User?")
            .setMessage("They won't be able to message you.")
            .setPositiveButton("Block") { _, _ ->
                FirebaseManager.usersRef.document(myUid)
                    .update("blockedUsers", com.google.firebase.firestore.FieldValue.arrayUnion(profileUid))
                    .addOnSuccessListener {
                        context?.toast("User blocked")
                        parentFragmentManager.popBackStack()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDevOptions() {
        val options = arrayOf("🚪 Boot User", "🔨 Ban User", "💎 Give Premium", "❌ Remove Premium", "🔓 Unban User")
        AlertDialog.Builder(requireContext())
            .setTitle("🔧 Dev Options — ${profileData["username"] ?: ""}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> FirebaseManager.usersRef.document(profileUid).update(
                        mapOf("forceLogout" to true, "logoutMessage" to "You have been booted by the developer. ~Conz~")
                    ).addOnSuccessListener { context?.toast("Booted!") }
                    1 -> FirebaseManager.usersRef.document(profileUid).update(
                        mapOf("banned" to true, "forceLogout" to true, "logoutMessage" to "This account has been permanently BANNED ~Conz~")
                    ).addOnSuccessListener { context?.toast("Banned!") }
                    2 -> FirebaseManager.usersRef.document(profileUid).update(
                        mapOf("premium" to true, "premiumPopup" to "Premium has been successfully added to your account, ENJOY! Please refresh the app to activate premium features.")
                    ).addOnSuccessListener { context?.toast("Premium granted!") }
                    3 -> FirebaseManager.usersRef.document(profileUid).update("premium", false)
                        .addOnSuccessListener { context?.toast("Premium removed") }
                    4 -> FirebaseManager.usersRef.document(profileUid).update(
                        mapOf("banned" to false, "forceLogout" to false)
                    ).addOnSuccessListener { context?.toast("Unbanned!") }
                }
            }
            .show()
    }

    private fun openFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        profileListener?.remove()
        _binding = null
    }
}
