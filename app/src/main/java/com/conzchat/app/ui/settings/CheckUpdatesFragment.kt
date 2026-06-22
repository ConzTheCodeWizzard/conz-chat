package com.conzchat.app.ui.settings

import android.app.AlertDialog
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.BuildConfig
import com.conzchat.app.databinding.FragmentCheckUpdatesBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.conzchat.app.util.HarleyThemeHelper

class CheckUpdatesFragment : Fragment() {

    companion object {
        fun newInstance() = CheckUpdatesFragment()
        // Current app version — bump this each release
        const val CURRENT_VERSION = "4.0.6"
    }

    private var _binding: FragmentCheckUpdatesBinding? = null
    private val binding get() = _binding!!
    private var downloadId = -1L

    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (id == downloadId) {
                context?.toast("Download complete! Install from your notifications.")
                if (_binding != null) {
                    binding.tvStatus.text = "✅ Download complete — check your notifications to install."
                    binding.btnUpdate.visibility = View.GONE
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCheckUpdatesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.tvCurrentVersion.text = "Current version: $CURRENT_VERSION"
        binding.btnUpdate.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
        binding.tvStatus.text = "Checking for updates..."

        // Register download receiver (Android 14+ requires RECEIVER_NOT_EXPORTED for non-system broadcasts)
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                requireContext().registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED
                )
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                requireContext().registerReceiver(
                    downloadReceiver,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        } catch (_: Exception) {}

        checkForUpdate()
    }

    private fun checkForUpdate() {
        // Firebase stores latest version info at: appConfig/version
        // Fields: { latestVersion: "1.8", apkUrl: "https://..." }
        FirebaseManager.db.collection("appConfig").document("version")
            .get()
            .addOnSuccessListener { snap ->
                if (_binding == null) return@addOnSuccessListener
                binding.progressBar.visibility = View.GONE
                val latest = snap.getString("latestVersion") ?: CURRENT_VERSION
                val apkUrl = snap.getString("apkUrl") ?: ""

                if (isNewerVersion(latest, CURRENT_VERSION)) {
                    binding.tvStatus.text = "🎉 An update is available!\nLatest version: $latest"
                    binding.tvLatestVersion.text = "Latest: $latest"
                    binding.tvLatestVersion.visibility = View.VISIBLE
                    if (apkUrl.isNotEmpty()) {
                        binding.btnUpdate.visibility = View.VISIBLE
                        binding.btnUpdate.setOnClickListener {
                            confirmAndDownload(apkUrl, latest)
                        }
                    }
                } else {
                    binding.tvStatus.text = "✅ You are already on the latest update."
                    binding.tvLatestVersion.text = "Latest: $latest"
                    binding.tvLatestVersion.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.progressBar.visibility = View.GONE
                binding.tvStatus.text = "⚠️ Could not check for updates. Please try again later."
            }
    }

    private fun confirmAndDownload(apkUrl: String, version: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Update Available")
            .setMessage("An update is available (v$version). Do you wish to update ConzChat?\n\nThe APK will be downloaded automatically.")
            .setPositiveButton("Update") { _, _ ->
                downloadApk(apkUrl, version)
            }
            .setNegativeButton("Not Now", null)
            .show()
    }

    private fun downloadApk(url: String, version: String) {
        try {
            binding.tvStatus.text = "⬇️ Downloading update v$version..."
            binding.btnUpdate.isEnabled = false

            val request = DownloadManager.Request(Uri.parse(url)).apply {
                setTitle("ConzChat v$version")
                setDescription("Downloading update...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "ConzChat-v$version.apk")
                setMimeType("application/vnd.android.package-archive")
            }

            val dm = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadId = dm.enqueue(request)
            context?.toast("Download started — check your notifications")
        } catch (e: Exception) {
            binding.tvStatus.text = "❌ Download failed: ${e.message}"
            binding.btnUpdate.isEnabled = true
        }
    }

    /** Returns true if `latest` is a newer version than `current` */
    private fun isNewerVersion(latest: String, current: String): Boolean {
        return try {
            val l = latest.split(".").map { it.toInt() }
            val c = current.split(".").map { it.toInt() }
            for (i in 0 until maxOf(l.size, c.size)) {
                val lv = l.getOrElse(i) { 0 }
                val cv = c.getOrElse(i) { 0 }
                if (lv > cv) return true
                if (lv < cv) return false
            }
            false
        } catch (_: Exception) { false }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try { requireContext().unregisterReceiver(downloadReceiver) } catch (_: Exception) {}
        _binding = null
    }
}
