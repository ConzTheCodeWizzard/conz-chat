package com.conzchat.app.ui.feed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentFeedBinding
import com.conzchat.app.model.FeedPost
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class FeedFragment : Fragment() {
    private var _binding: FragmentFeedBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val posts = mutableListOf<FeedPost>()
    private lateinit var adapter: FeedAdapter
    private var listener: ListenerRegistration? = null

    companion object {
        fun newInstance() = FeedFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = FeedAdapter(posts, requireContext(),
            onLike = { post -> toggleLike(post) },
            onComment = { post -> openComments(post) },
            onShare = { post -> sharePost(post) },
            onProfileClick = { post -> openProfile(post.uid) }
        )
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { loadPosts() }
        binding.fabCreatePost.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreatePostFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
        loadPosts()
    }

    private fun loadPosts() {
        listener?.remove()
        listener = db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (_binding == null || !isAdded) return@addSnapshotListener
                binding.swipeRefresh.isRefreshing = false
                if (err != null || snap == null) return@addSnapshotListener
                posts.clear()
                for (doc in snap.documents) {
                    val post = FeedPost(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        username = doc.getString("username") ?: "",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        text = doc.getString("text") ?: "",
                        mediaUrl = doc.getString("mediaUrl") ?: "",
                        mediaType = doc.getString("mediaType") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        likes = (doc.get("likes") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        commentCount = (doc.getLong("commentCount") ?: 0L).toInt()
                    )
                    posts.add(post)
                }
                adapter.notifyDataSetChanged()
                binding.tvEmpty.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
            }
    }

    private fun toggleLike(post: FeedPost) {
        val uid = auth.currentUser?.uid ?: return
        val ref = db.collection("posts").document(post.id)
        if (uid in post.likes) {
            ref.update("likes", com.google.firebase.firestore.FieldValue.arrayRemove(uid))
        } else {
            ref.update("likes", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
        }
    }

    private fun openComments(post: FeedPost) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, CommentsFragment.newInstance(post.id))
            .addToBackStack(null)
            .commit()
    }

    private fun sharePost(post: FeedPost) {
        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT, "${post.username}: ${post.text}")
        }
        startActivity(android.content.Intent.createChooser(intent, "Share post"))
    }

    private fun openProfile(uid: String) {
        // Navigate to profile
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, com.conzchat.app.ui.profile.ProfileFragment.newInstance(uid))
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        listener?.remove()
        listener = null
        _binding = null
        super.onDestroyView()
    }
}
