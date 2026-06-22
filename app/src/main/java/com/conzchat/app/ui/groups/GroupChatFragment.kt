package com.conzchat.app.ui.groups

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentGroupChatBinding
import com.conzchat.app.model.GroupMessage
import com.conzchat.app.model.ReplyTo
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.ImageUtils
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.io.File

class GroupChatFragment : Fragment() {

    companion object {
        fun newInstance(groupId: String, name: String, photo: String) = GroupChatFragment().apply {
            arguments = Bundle().apply {
                putString("groupId", groupId)
                putString("name", name)
                putString("photo", photo)
            }
        }
    }

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    private var groupId = ""
    private var groupName = ""
    private var groupPhoto = ""
    private val messages = mutableListOf<GroupMessage>()
    private lateinit var adapter: GroupMessageAdapter
    private var messagesListener: ListenerRegistration? = null
    private var groupListener: ListenerRegistration? = null
    private var cameraUri: Uri? = null
    private var replyTo: ReplyTo? = null
    private val uid get() = FirebaseManager.currentUid

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { sendImage(it) }
    }
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) cameraUri?.let { sendImage(it) }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupId = arguments?.getString("groupId") ?: ""
        groupName = arguments?.getString("name") ?: ""
        groupPhoto = arguments?.getString("photo") ?: ""

        setupUI()
        loadMessages()
        loadGroupInfo()
    }

    private fun setupUI() {
        binding.tvGroupName.text = groupName
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.ivGroupInfo.setOnClickListener { openGroupInfo() }

        if (groupPhoto.isNotEmpty()) {
            com.bumptech.glide.Glide.with(this).load(groupPhoto)
                .apply(com.bumptech.glide.request.RequestOptions.circleCropTransform())
                .into(binding.ivGroupAvatar)
        }

        adapter = GroupMessageAdapter(messages, uid) { uid -> openProfile(uid) }
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@GroupChatFragment.adapter
        }

        // Swipe-to-reply
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(rv: androidx.recyclerview.widget.RecyclerView, vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, target: androidx.recyclerview.widget.RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                val pos = vh.adapterPosition
                if (pos >= 0 && pos < messages.size) {
                    val msg = messages[pos]
                    replyTo = ReplyTo(id = msg.id, text = msg.text.ifEmpty { "[media]" }, sender = msg.senderName)
                    binding.replyBar.visibility = View.VISIBLE
                    binding.tvReplySender.text = msg.senderName
                    binding.tvReplyText.text = msg.text.ifEmpty { "[media]" }
                }
                adapter.notifyItemChanged(pos)
            }
            override fun getSwipeThreshold(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder) = 0.3f
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.rvMessages)

        // Reply bar cancel
        binding.btnCancelReply.setOnClickListener {
            replyTo = null
            binding.replyBar.visibility = View.GONE
        }

        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = s?.isNotEmpty() == true
                binding.btnSend.visibility = if (hasText) View.VISIBLE else View.GONE
                binding.btnMedia.visibility = if (hasText) View.GONE else View.VISIBLE
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnSend.setOnClickListener { sendText() }
        binding.btnMedia.setOnClickListener { toggleMediaBar() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnGallery.setOnClickListener { galleryLauncher.launch("image/*") }
    }

    private fun loadMessages() {
        messagesListener = FirebaseManager.groupMessagesRef(groupId)
            .orderBy("time", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, _ ->
                if (snap == null) return@addSnapshotListener
                messages.clear()
                snap.documents.forEach { doc ->
                    val d = doc.data ?: return@forEach
                    messages.add(GroupMessage(
                        id = doc.id,
                        from = d["from"] as? String ?: "",
                        senderName = d["senderName"] as? String ?: "",
                        senderPhoto = d["senderPhoto"] as? String ?: "",
                        time = d["time"] as? Long ?: 0L,
                        text = d["text"] as? String ?: "",
                        type = d["type"] as? String ?: "text",
                        url = d["url"] as? String ?: "",
                        deleted = d["deleted"] as? Boolean ?: false
                    ))
                }
                adapter.notifyDataSetChanged()
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
    }

    private fun loadGroupInfo() {
        groupListener = FirebaseManager.groupsRef.document(groupId)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                val d = snap.data ?: return@addSnapshotListener
                val name = d["name"] as? String ?: groupName
                binding.tvGroupName.text = name
                val memberCount = (d["members"] as? List<*>)?.size ?: 0
                binding.tvMemberCount.text = "$memberCount members"
            }
    }

    private fun sendText() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        // Get current user info
        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { doc ->
            val senderName = doc.getString("displayName") ?: doc.getString("username") ?: ""
            val senderPhoto = doc.getString("photo") ?: ""

            val msgData = hashMapOf<String, Any>(
                "from" to uid, "senderName" to senderName, "senderPhoto" to senderPhoto,
                "time" to System.currentTimeMillis(),
                "text" to text, "type" to "text"
            )
            FirebaseManager.groupMessagesRef(groupId).add(msgData)
            FirebaseManager.groupsRef.document(groupId).update(
                mapOf("lastMessage" to text, "lastTime" to System.currentTimeMillis())
            )
            binding.etMessage.setText("")
        }
    }

    private fun sendImage(uri: Uri) {
        val base64 = ImageUtils.compressImageToBase64ForChat(requireContext(), uri) ?: return
        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { doc ->
            val senderName = doc.getString("displayName") ?: doc.getString("username") ?: ""
            val senderPhoto = doc.getString("photo") ?: ""
            val msgData = hashMapOf<String, Any>(
                "from" to uid, "senderName" to senderName, "senderPhoto" to senderPhoto,
                "time" to System.currentTimeMillis(),
                "type" to "image", "url" to base64, "text" to ""
            )
            FirebaseManager.groupMessagesRef(groupId).add(msgData)
            FirebaseManager.groupsRef.document(groupId).update(
                mapOf("lastMessage" to "📷 Photo", "lastTime" to System.currentTimeMillis())
            )
        }
        closeMediaBar()
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 101)
            return
        }
        val photoFile = File.createTempFile("photo_", ".jpg", requireContext().cacheDir)
        cameraUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.provider", photoFile)
        cameraLauncher.launch(cameraUri!!)
    }

    private var mediaBarOpen = false
    private fun toggleMediaBar() {
        mediaBarOpen = !mediaBarOpen
        binding.mediaBar.visibility = if (mediaBarOpen) View.VISIBLE else View.GONE
    }
    private fun closeMediaBar() {
        mediaBarOpen = false
        binding.mediaBar.visibility = View.GONE
    }

    private fun openGroupInfo() {
        val fragment = GroupInfoFragment.newInstance(groupId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openProfile(uid: String) {
        val fragment = com.conzchat.app.ui.profile.ProfileFragment.newInstance(uid)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        groupListener?.remove()
        _binding = null
    }
}
