package com.conzchat.app.ui.groups

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.conzchat.app.databinding.FragmentGroupInfoBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.conzchat.app.util.HarleyThemeHelper

class PublicGroupInfoFragment : Fragment() {

    companion object {
        fun newInstance(groupId: String) = PublicGroupInfoFragment().apply {
            arguments = Bundle().apply { putString("groupId", groupId) }
        }
    }

    private var _binding: FragmentGroupInfoBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentGroupInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        val groupId = arguments?.getString("groupId") ?: ""
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnAddMember.visibility = View.GONE
        binding.etAddMember.visibility = View.GONE

        FirebaseManager.publicGroupsRef.document(groupId).get().addOnSuccessListener { doc ->
            val d = doc.data ?: return@addOnSuccessListener
            binding.tvGroupName.text = d["name"] as? String ?: ""
            val members = (d["members"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            binding.tvMemberCount.text = "${members.size} members"

            members.forEach { memberId ->
                FirebaseManager.usersRef.document(memberId).get().addOnSuccessListener { userDoc ->
                    val name = userDoc.getString("displayName") ?: userDoc.getString("username") ?: memberId
                    val tv = TextView(requireContext()).apply {
                        text = "• $name"
                        textSize = 15f
                        setTextColor(0xFFCCCCCC.toInt())
                        setPadding(0, 8, 0, 8)
                    }
                    binding.llMembers.addView(tv)
                }
            }
        }

        binding.btnLeave.setOnClickListener {
            FirebaseManager.publicGroupsRef.document(groupId)
                .update("members", com.google.firebase.firestore.FieldValue.arrayRemove(uid))
                .addOnSuccessListener {
                    context?.toast("Left group")
                    parentFragmentManager.popBackStack()
                    parentFragmentManager.popBackStack()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
