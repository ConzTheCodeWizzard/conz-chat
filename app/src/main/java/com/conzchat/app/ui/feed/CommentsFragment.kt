package com.conzchat.app.ui.feed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentCommentsBinding
import com.conzchat.app.model.FeedComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*
import com.conzchat.app.util.HarleyThemeHelper

class CommentsFragment : Fragment() {
    private var _binding: FragmentCommentsBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val comments = mutableListOf<FeedComment>()
    private lateinit var adapter: CommentAdapter
    private var postId = ""
    private var listener: ListenerRegistration? = null

    companion object {
        fun newInstance(postId: String): CommentsFragment {
            return CommentsFragment().apply {
                arguments = Bundle().apply { putString("postId", postId) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        postId = arguments?.getString("postId") ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCommentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        adapter = CommentAdapter(comments, requireContext())
        binding.rvComments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvComments.adapter = adapter
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnSendComment.setOnClickListener { sendComment() }
        loadComments()
    }

    private fun loadComments() {
        listener = db.collection("posts").document(postId).collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (_binding == null || !isAdded) return@addSnapshotListener
                if (snap == null) return@addSnapshotListener
                comments.clear()
                for (doc in snap.documents) {
                    comments.add(FeedComment(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        username = doc.getString("username") ?: "",
                        avatarUrl = doc.getString("avatarUrl") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    ))
                }
                adapter.notifyDataSetChanged()
            }
    }

    private fun sendComment() {
        val text = binding.etComment.text.toString().trim()
        if (text.isEmpty()) return
        val user = auth.currentUser ?: return
        binding.etComment.setText("")
        db.collection("users").document(user.uid).get().addOnSuccessListener { doc ->
            val username = doc?.getString("username") ?: "Anonymous"
            val avatarUrl = doc?.getString("photoUrl") ?: ""
            val commentData = hashMapOf(
                "uid" to user.uid,
                "username" to username,
                "avatarUrl" to avatarUrl,
                "text" to text,
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("posts").document(postId).collection("comments").add(commentData)
            db.collection("posts").document(postId).update("commentCount", FieldValue.increment(1))
        }
    }

    override fun onDestroyView() {
        listener?.remove()
        listener = null
        _binding = null
        super.onDestroyView()
    }
}

class CommentAdapter(
    private val comments: List<FeedComment>,
    private val ctx: Context
) : androidx.recyclerview.widget.RecyclerView.Adapter<CommentAdapter.VH>() {
    private val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    inner class VH(val binding: com.conzchat.app.databinding.ItemCommentBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = com.conzchat.app.databinding.ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = comments.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val comment = comments[position]
        val b = holder.binding
        b.tvUsername.text = comment.username.ifEmpty { "Anonymous" }
        b.tvCommentText.text = comment.text
        b.tvTime.text = if (comment.timestamp > 0) dateFormat.format(Date(comment.timestamp)) else ""
        if (comment.avatarUrl.isNotEmpty()) {
            Glide.with(ctx).load(comment.avatarUrl).placeholder(R.drawable.ic_default_avatar).into(b.ivAvatar)
        } else {
            b.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
        }
    }
}
