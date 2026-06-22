package com.conzchat.app.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentPublicGroupsBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast

class PublicGroupsFragment : Fragment() {

    private var _binding: FragmentPublicGroupsBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPublicGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnCreate.setOnClickListener { showCreatePublicGroup() }
        loadPublicGroups()
    }

    private fun loadPublicGroups() {
        FirebaseManager.publicGroupsRef.get().addOnSuccessListener { snap ->
            binding.llGroups.removeAllViews()
            snap.documents.forEach { doc ->
                val d = doc.data ?: return@forEach
                val name = d["name"] as? String ?: ""
                val tag = d["tag"] as? String ?: ""
                val members = (d["members"] as? List<*>)?.size ?: 0
                val isMember = (d["members"] as? List<*>)?.contains(uid) == true

                val row = LayoutInflater.from(context).inflate(R.layout.item_public_group, binding.llGroups, false)
                row.findViewById<TextView>(R.id.tvGroupName).text = name
                row.findViewById<TextView>(R.id.tvGroupTag).text = "#$tag · $members members"
                val btnJoin = row.findViewById<TextView>(R.id.btnJoin)
                btnJoin.text = if (isMember) "Open" else "Join"
                btnJoin.setOnClickListener {
                    if (!isMember) {
                        FirebaseManager.publicGroupsRef.document(doc.id)
                            .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
                            .addOnSuccessListener { openGroup(doc.id, name, "", tag) }
                    } else {
                        openGroup(doc.id, name, "", tag)
                    }
                }
                binding.llGroups.addView(row)
            }
        }
    }

    private fun showCreatePublicGroup() {
        val name = binding.etGroupName.text.toString().trim()
        val tag = binding.etGroupTag.text.toString().trim().lowercase()
        if (name.isEmpty() || tag.isEmpty()) { context?.toast("Fill in name and tag"); return }

        // Check tag uniqueness
        FirebaseManager.publicGroupsRef.whereEqualTo("tag", tag).get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) { context?.toast("Tag already taken"); return@addOnSuccessListener }
                val data = hashMapOf(
                    "name" to name, "tag" to tag, "photo" to "",
                    "members" to listOf(uid), "admins" to listOf(uid),
                    "createdBy" to uid, "created" to System.currentTimeMillis(),
                    "lastMessage" to "", "lastTime" to System.currentTimeMillis()
                )
                FirebaseManager.publicGroupsRef.add(data)
                    .addOnSuccessListener { docRef ->
                        context?.toast("Public group created!")
                        openGroup(docRef.id, name, "", tag)
                    }
            }
    }

    private fun openGroup(groupId: String, name: String, photo: String, tag: String) {
        val fragment = PublicGroupChatFragment.newInstance(groupId, "#$tag", photo, tag)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
