package com.conzchat.app.ui.feed

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.conzchat.app.databinding.FragmentCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class CreatePostFragment : Fragment() {
    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private var selectedMediaUri: Uri? = null
    private var selectedMediaType = ""
    private var isPosting = false

    private val mediaPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedMediaUri = uri
            val type = requireContext().contentResolver.getType(uri) ?: ""
            selectedMediaType = if (type.startsWith("video")) "video" else "image"
            _binding?.let {
                Glide.with(this).load(uri).into(it.ivPreview)
                it.ivPreview.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        fun newInstance() = CreatePostFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBack.setOnClickListener {
            if (!isPosting) parentFragmentManager.popBackStack()
        }
        binding.btnAttachMedia.setOnClickListener {
            mediaPicker.launch("*/*")
        }
        binding.btnPost.setOnClickListener { submitPost() }
    }

    private fun submitPost() {
        val text = binding.etPostText.text.toString().trim()
        if (text.isEmpty() && selectedMediaUri == null) {
            Toast.makeText(requireContext(), "Write something or attach media", Toast.LENGTH_SHORT).show()
            return
        }
        isPosting = true
        _binding?.progressBar?.visibility = View.VISIBLE
        _binding?.btnPost?.isEnabled = false

        val user = auth.currentUser ?: return
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val username = doc?.getString("username") ?: "Anonymous"
            val avatarUrl = doc?.getString("photoUrl") ?: ""
            if (selectedMediaUri != null) {
                uploadMediaThenPost(text, username, avatarUrl, user.uid)
            } else {
                savePost(text, "", "", username, avatarUrl, user.uid)
            }
        }.addOnFailureListener {
            savePost(text, "", "", "Anonymous", "", user.uid)
        }
    }

    private fun uploadMediaThenPost(text: String, username: String, avatarUrl: String, uid: String) {
        val ref = storage.reference.child("feed_media/${UUID.randomUUID()}")
        ref.putFile(selectedMediaUri!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener { url ->
                savePost(text, url.toString(), selectedMediaType, username, avatarUrl, uid)
            }
        }.addOnFailureListener {
            if (_binding != null && isAdded) {
                Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
                _binding?.progressBar?.visibility = View.GONE
                _binding?.btnPost?.isEnabled = true
                isPosting = false
            }
        }
    }

    private fun savePost(text: String, mediaUrl: String, mediaType: String, username: String, avatarUrl: String, uid: String) {
        val postData = hashMapOf(
            "uid" to uid,
            "username" to username,
            "avatarUrl" to avatarUrl,
            "text" to text,
            "mediaUrl" to mediaUrl,
            "mediaType" to mediaType,
            "timestamp" to System.currentTimeMillis(),
            "likes" to emptyList<String>(),
            "commentCount" to 0
        )
        db.collection("posts").add(postData).addOnCompleteListener {
            if (_binding != null && isAdded) {
                isPosting = false
                _binding?.progressBar?.visibility = View.GONE
                parentFragmentManager.popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
