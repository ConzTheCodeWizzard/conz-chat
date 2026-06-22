package com.conzchat.app.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentCreateGroupBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast

class CreateGroupFragment : Fragment() {

    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        binding.btnCreate.setOnClickListener {
            val name = binding.etGroupName.text.toString().trim()
            if (name.isEmpty()) { context?.toast("Enter a group name"); return@setOnClickListener }
            createGroup(name)
        }
    }

    private fun createGroup(name: String) {
        binding.btnCreate.isEnabled = false
        val groupData = hashMapOf(
            "name" to name,
            "photo" to "",
            "members" to listOf(uid),
            "admins" to listOf(uid),
            "createdBy" to uid,
            "created" to System.currentTimeMillis(),
            "lastMessage" to "",
            "lastTime" to System.currentTimeMillis()
        )
        FirebaseManager.groupsRef.add(groupData)
            .addOnSuccessListener { docRef ->
                context?.toast("Group created!")
                val fragment = GroupChatFragment.newInstance(docRef.id, name, "")
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit()
            }
            .addOnFailureListener {
                binding.btnCreate.isEnabled = true
                context?.toast("Failed to create group")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
