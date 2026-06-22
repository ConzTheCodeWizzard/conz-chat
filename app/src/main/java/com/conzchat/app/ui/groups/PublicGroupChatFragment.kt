package com.conzchat.app.ui.groups

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentGroupChatBinding
import com.conzchat.app.model.GroupMessage
import com.conzchat.app.util.FirebaseManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.conzchat.app.util.HarleyThemeHelper

class PublicGroupChatFragment : Fragment() {

    companion object {
        fun newInstance(groupId: String, name: String, photo: String, tag: String) = PublicGroupChatFragment().apply {
            arguments = Bundle().apply {
                putString("groupId", groupId)
                putString("name", name)
                putString("photo", photo)
                putString("tag", tag)
            }
        }
    }

    private var _binding: FragmentGroupChatBinding? = null
    private val binding get() = _binding!!

    private var groupId = ""
    private var groupName = ""
    private var groupTag = ""
    private val messages = mutableListOf<GroupMessage>()
    private lateinit var adapter: GroupMessageAdapter
    private var messagesListener: ListenerRegistration? = null
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        groupId = arguments?.getString("groupId") ?: ""
        groupName = arguments?.getString("name") ?: ""
        groupTag = arguments?.getString("tag") ?: ""

        binding.tvGroupName.text = groupName
        binding.tvMemberCount.text = "#$groupTag"
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.ivGroupInfo.setOnClickListener { openGroupInfo() }

        adapter = GroupMessageAdapter(messages, uid) { uid ->
            val fragment = com.conzchat.app.ui.profile.ProfileFragment.newInstance(uid)
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@PublicGroupChatFragment.adapter
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
        binding.btnMedia.visibility = View.GONE // Simplified for public groups

        loadMessages()
    }

    private fun loadMessages() {
        messagesListener = FirebaseManager.publicGroupMessagesRef(groupId)
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

    private fun sendText() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { doc ->
            val senderName = doc.getString("displayName") ?: doc.getString("username") ?: ""
            val senderPhoto = doc.getString("photo") ?: ""
            val msgData = hashMapOf<String, Any>(
                "from" to uid, "senderName" to senderName, "senderPhoto" to senderPhoto,
                "time" to System.currentTimeMillis(),
                "text" to text, "type" to "text"
            )
            FirebaseManager.publicGroupMessagesRef(groupId).add(msgData)
            FirebaseManager.publicGroupsRef.document(groupId).update(
                mapOf("lastMessage" to text, "lastTime" to System.currentTimeMillis())
            )
            binding.etMessage.setText("")
        }
    }

    private fun openGroupInfo() {
        val fragment = PublicGroupInfoFragment.newInstance(groupId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagesListener?.remove()
        _binding = null
    }
}
