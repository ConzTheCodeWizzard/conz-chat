package com.conzchat.app.ui.stories

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.conzchat.app.databinding.FragmentStoriesBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.ImageUtils
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.conzchat.app.util.HarleyThemeHelper

class StoriesFragment : Fragment() {

    companion object {
        fun newInstance(viewUid: String? = null) = StoriesFragment().apply {
            arguments = Bundle().apply { putString("viewUid", viewUid) }
        }
    }

    private var _binding: FragmentStoriesBinding? = null
    private val binding get() = _binding!!

    private var viewUid: String? = null
    private var storyUrls = mutableListOf<String>()
    private var currentIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null
    private var storyListener: ListenerRegistration? = null
    private val uid get() = FirebaseManager.currentUid

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { postStory(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        viewUid = arguments?.getString("viewUid")

        binding.ivClose.setOnClickListener { parentFragmentManager.popBackStack() }

        if (viewUid == null || viewUid == uid) {
            // My story - show post option
            binding.btnPostStory.visibility = View.VISIBLE
            binding.btnPostStory.setOnClickListener { galleryLauncher.launch("image/*") }
            loadMyStory()
        } else {
            // Viewing someone else's story
            binding.btnPostStory.visibility = View.GONE
            loadUserStory(viewUid!!)
        }

        // Tap to advance
        binding.ivStory.setOnClickListener { advanceStory() }
        binding.tvLeft.setOnClickListener { previousStory() }
        binding.tvRight.setOnClickListener { advanceStory() }
    }

    private fun loadMyStory() {
        storyListener = FirebaseManager.storiesRef.document(uid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) {
                    showEmpty()
                    return@addSnapshotListener
                }
                val urls = (snap.get("urls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (urls.isEmpty()) { showEmpty(); return@addSnapshotListener }
                storyUrls.clear()
                storyUrls.addAll(urls)
                currentIndex = 0
                showStory()
            }
    }

    private fun loadUserStory(targetUid: String) {
        storyListener = FirebaseManager.storiesRef.document(targetUid)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) {
                    showEmpty()
                    return@addSnapshotListener
                }
                val urls = (snap.get("urls") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (urls.isEmpty()) { showEmpty(); return@addSnapshotListener }
                storyUrls.clear()
                storyUrls.addAll(urls)
                currentIndex = 0
                showStory()

                // Mark as viewed
                FirebaseManager.storiesRef.document(targetUid)
                    .update("viewers", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
            }
    }

    private fun showStory() {
        if (storyUrls.isEmpty()) { showEmpty(); return }
        binding.ivStory.visibility = View.VISIBLE
        binding.tvEmpty.visibility = View.GONE
        Glide.with(this).load(storyUrls[currentIndex]).into(binding.ivStory)
        binding.tvProgress.text = "${currentIndex + 1}/${storyUrls.size}"
        startAutoAdvance()
    }

    private fun showEmpty() {
        binding.ivStory.visibility = View.GONE
        binding.tvEmpty.visibility = View.VISIBLE
        stopAutoAdvance()
    }

    private fun startAutoAdvance() {
        stopAutoAdvance()
        progressRunnable = Runnable { advanceStory() }
        handler.postDelayed(progressRunnable!!, 5000)
    }

    private fun stopAutoAdvance() {
        progressRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun advanceStory() {
        if (currentIndex < storyUrls.size - 1) {
            currentIndex++
            showStory()
        } else {
            parentFragmentManager.popBackStack()
        }
    }

    private fun previousStory() {
        if (currentIndex > 0) {
            currentIndex--
            showStory()
        }
    }

    private fun postStory(uri: Uri) {
        val base64 = ImageUtils.compressImageToBase64(requireContext(), uri, maxSize = 800, quality = 70) ?: return
        val now = System.currentTimeMillis()
        FirebaseManager.storiesRef.document(uid).get().addOnSuccessListener { doc ->
            val existing = (doc.get("urls") as? List<*>)?.filterIsInstance<String>()?.toMutableList() ?: mutableListOf()
            existing.add(base64)
            val data = hashMapOf(
                "uid" to uid,
                "urls" to existing,
                "time" to now,
                "expires" to now + 24 * 60 * 60 * 1000L, // 24 hours
                "viewers" to listOf<String>()
            )
            FirebaseManager.storiesRef.document(uid).set(data)
                .addOnSuccessListener { context?.toast("Story posted!") }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoAdvance()
        storyListener?.remove()
        _binding = null
    }
}
