package com.conzchat.app.ui.dev

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.ConzChatApp
import com.conzchat.app.databinding.FragmentDevMenuBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.firebase.firestore.FieldValue

class DevMenuFragment : Fragment() {

    private var _binding: FragmentDevMenuBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDevMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hard guard — only DEV_UID can see this screen
        if (uid != ConzChatApp.DEV_UID) {
            context?.toast("Access denied")
            parentFragmentManager.popBackStack()
            return
        }

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        loadStats()

        // ── BROADCAST ──────────────────────────────────────────────────────────
        // Sends a real DM message from the dev to every registered user
        binding.btnBroadcast.setOnClickListener {
            val msg = binding.etBroadcast.text.toString().trim()
            if (msg.isEmpty()) { context?.toast("Enter a message first"); return@setOnClickListener }

            AlertDialog.Builder(requireContext())
                .setTitle("📬 Send Broadcast")
                .setMessage("This will send a DM to every user:\n\n\"$msg\"\n\nContinue?")
                .setPositiveButton("Send to All") { _, _ ->
                    sendBroadcastToAllUsers(msg)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ── ANNOUNCEMENT ───────────────────────────────────────────────────────
        // Writes to appConfig/announcement — HomeFragment listens and shows dialog
        binding.btnSetAnnouncement.setOnClickListener {
            val text = binding.etAnnouncement.text.toString().trim()
            if (text.isEmpty()) {
                // Clear announcement
                FirebaseManager.db.collection("appConfig").document("announcement")
                    .set(mapOf("text" to "", "time" to System.currentTimeMillis()))
                    .addOnSuccessListener { context?.toast("Announcement cleared") }
                return@setOnClickListener
            }
            FirebaseManager.db.collection("appConfig").document("announcement")
                .set(mapOf(
                    "text" to text,
                    "time" to System.currentTimeMillis(),
                    "from" to uid
                ))
                .addOnSuccessListener { context?.toast("✅ Announcement set for all users!") }
                .addOnFailureListener { context?.toast("Failed: ${it.message}") }
        }

        // ── GIVE PREMIUM ───────────────────────────────────────────────────────
        binding.btnGivePremium.setOnClickListener {
            val username = binding.etPremiumUsername.text.toString().trim().removePrefix("@")
            if (username.isEmpty()) { context?.toast("Enter a username"); return@setOnClickListener }
            FirebaseManager.usersRef.whereEqualTo("username", username).get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) { context?.toast("User @$username not found"); return@addOnSuccessListener }
                    val doc = snap.documents.first()
                    doc.reference.update(mapOf(
                        "premium" to true,
                        "premiumPopup" to "🎉 You have been given Premium by the ConzChat Dev!"
                    )).addOnSuccessListener { context?.toast("✅ Premium given to @$username!") }
                }
                .addOnFailureListener { context?.toast("Error: ${it.message}") }
        }

        // ── REVOKE PREMIUM ─────────────────────────────────────────────────────
        binding.btnRevokePremium.setOnClickListener {
            val username = binding.etPremiumUsername.text.toString().trim().removePrefix("@")
            if (username.isEmpty()) { context?.toast("Enter a username"); return@setOnClickListener }
            FirebaseManager.usersRef.whereEqualTo("username", username).get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) { context?.toast("User @$username not found"); return@addOnSuccessListener }
                    snap.documents.first().reference.update("premium", false)
                        .addOnSuccessListener { context?.toast("Premium revoked from @$username") }
                }
        }

        // ── BOOT USER ──────────────────────────────────────────────────────────
        binding.btnBoot.setOnClickListener {
            val username = binding.etBootUsername.text.toString().trim().removePrefix("@")
            val reason = binding.etBootReason.text.toString().trim().ifEmpty { "You have been logged out by the admin." }
            if (username.isEmpty()) { context?.toast("Enter a username"); return@setOnClickListener }
            FirebaseManager.usersRef.whereEqualTo("username", username).get()
                .addOnSuccessListener { snap ->
                    if (snap.isEmpty) { context?.toast("User @$username not found"); return@addOnSuccessListener }
                    snap.documents.first().reference.update(mapOf(
                        "forceLogout" to true,
                        "logoutMessage" to reason
                    )).addOnSuccessListener { context?.toast("✅ @$username booted!") }
                }
        }

        // ── BAN USER ───────────────────────────────────────────────────────────
        binding.btnBan.setOnClickListener {
            val username = binding.etBootUsername.text.toString().trim().removePrefix("@")
            if (username.isEmpty()) { context?.toast("Enter a username"); return@setOnClickListener }
            AlertDialog.Builder(requireContext())
                .setTitle("⛔ Ban @$username?")
                .setMessage("This will permanently ban this user.")
                .setPositiveButton("Ban") { _, _ ->
                    FirebaseManager.usersRef.whereEqualTo("username", username).get()
                        .addOnSuccessListener { snap ->
                            if (snap.isEmpty) { context?.toast("User not found"); return@addOnSuccessListener }
                            snap.documents.first().reference.update(mapOf(
                                "banned" to true,
                                "forceLogout" to true,
                                "logoutMessage" to "Your account has been permanently banned."
                            )).addOnSuccessListener { context?.toast("✅ @$username banned!") }
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ── PUSH VERSION UPDATE ────────────────────────────────────────────────
        binding.btnPushVersion.setOnClickListener {
            val version = binding.etNewVersion.text.toString().trim()
            val apkUrl = binding.etApkUrl.text.toString().trim()
            if (version.isEmpty()) { context?.toast("Enter version number"); return@setOnClickListener }
            FirebaseManager.db.collection("appConfig").document("version")
                .set(mapOf(
                    "latestVersion" to version,
                    "apkUrl" to apkUrl,
                    "pushedAt" to System.currentTimeMillis()
                ))
                .addOnSuccessListener { context?.toast("✅ Version $version pushed!") }
                .addOnFailureListener { context?.toast("Failed: ${it.message}") }
        }

        // ACCOUNT COLLECTOR
        binding.btnAccountCollector.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.conzchat.app.R.id.fragmentContainer, AccountCollectorFragment())
                .addToBackStack(null).commit()
        }

        // CONZFEED
        binding.btnConzFeed.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.conzchat.app.R.id.fragmentContainer, com.conzchat.app.ui.feed.FeedFragment.newInstance())
                .addToBackStack(null).commit()
        }
    }

    private fun sendBroadcastToAllUsers(message: String) {
        binding.tvBroadcastStatus.visibility = View.VISIBLE
        binding.tvBroadcastStatus.text = "Sending..."

        FirebaseManager.usersRef.get().addOnSuccessListener { snap ->
            val users = snap.documents.filter { it.id != uid }
            if (users.isEmpty()) {
                binding.tvBroadcastStatus.text = "No users found."
                return@addOnSuccessListener
            }

            val total = users.size
            var sent = 0
            var failed = 0

            users.forEach { userDoc ->
                val targetUid = userDoc.id
                // Build a chat key (sorted UIDs joined by _)
                val chatKey = listOf(uid, targetUid).sorted().joinToString("_")
                val msgData = hashMapOf(
                    "from" to uid,
                    "to" to targetUid,
                    "text" to message,
                    "type" to "text",
                    "time" to System.currentTimeMillis(),
                    "read" to false,
                    "deleted" to false,
                    "broadcast" to true
                )
                FirebaseManager.messagesRef.add(msgData)
                    .addOnSuccessListener {
                        sent++
                        if (sent + failed == total) {
                            if (_binding != null) {
                                binding.tvBroadcastStatus.text = "✅ Sent to $sent/$total users"
                                binding.etBroadcast.setText("")
                            }
                        }
                    }
                    .addOnFailureListener {
                        failed++
                        if (sent + failed == total) {
                            if (_binding != null) {
                                binding.tvBroadcastStatus.text = "Sent: $sent, Failed: $failed (total: $total)"
                            }
                        }
                    }
            }
        }.addOnFailureListener {
            binding.tvBroadcastStatus.text = "Failed to fetch users: ${it.message}"
        }
    }

    private fun loadStats() {
        FirebaseManager.usersRef.get().addOnSuccessListener { snap ->
            if (_binding != null) binding.tvUserCount.text = "👥 Total users: ${snap.size()}"
        }
        FirebaseManager.messagesRef.get().addOnSuccessListener { snap ->
            if (_binding != null) binding.tvMessageCount.text = "💬 Total messages: ${snap.size()}"
        }
        FirebaseManager.groupsRef.get().addOnSuccessListener { snap ->
            if (_binding != null) binding.tvGroupCount.text = "👥 Private groups: ${snap.size()}"
        }
        FirebaseManager.publicGroupsRef.get().addOnSuccessListener { snap ->
            if (_binding != null) binding.tvPublicGroupCount.text = "🌐 Public groups: ${snap.size()}"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
