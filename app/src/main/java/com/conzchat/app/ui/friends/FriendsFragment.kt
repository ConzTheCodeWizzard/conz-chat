package com.conzchat.app.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentFriendsBinding
import com.conzchat.app.ui.chat.ChatFragment
import com.conzchat.app.ui.profile.ProfileFragment
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.conzchat.app.util.HarleyThemeHelper

class FriendsFragment : Fragment() {

    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        loadFriendRequests()
        loadFriends()
    }

    private fun loadFriendRequests() {
        FirebaseManager.friendRequestsRef
            .whereEqualTo("to", uid)
            .whereEqualTo("status", "pending")
            .get().addOnSuccessListener { snap ->
                binding.llRequests.removeAllViews()
                if (snap.isEmpty) {
                    binding.tvRequestsLabel.visibility = View.GONE
                    return@addOnSuccessListener
                }
                binding.tvRequestsLabel.visibility = View.VISIBLE
                snap.documents.forEach { doc ->
                    val fromUid = doc.getString("from") ?: return@forEach
                    FirebaseManager.usersRef.document(fromUid).get().addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("displayName") ?: userDoc.getString("username") ?: fromUid
                        val row = LayoutInflater.from(context).inflate(R.layout.item_friend_request, binding.llRequests, false)
                        row.findViewById<TextView>(R.id.tvName).text = name
                        row.findViewById<TextView>(R.id.btnAccept).setOnClickListener {
                            acceptRequest(doc.id, fromUid)
                            binding.llRequests.removeView(row)
                        }
                        row.findViewById<TextView>(R.id.btnDecline).setOnClickListener {
                            doc.reference.update("status", "declined")
                            binding.llRequests.removeView(row)
                        }
                        binding.llRequests.addView(row)
                    }
                }
            }
    }

    private fun acceptRequest(docId: String, fromUid: String) {
        FirebaseManager.friendRequestsRef.document(docId).update("status", "accepted")
        FirebaseManager.usersRef.document(uid)
            .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(fromUid))
        FirebaseManager.usersRef.document(fromUid)
            .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(uid))
        context?.toast("Friend added!")
        loadFriends()
    }

    private fun loadFriends() {
        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { doc ->
            val friends = (doc.get("friends") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            binding.llFriends.removeAllViews()
            if (friends.isEmpty()) {
                binding.tvNoFriends.visibility = View.VISIBLE
                return@addOnSuccessListener
            }
            binding.tvNoFriends.visibility = View.GONE
            friends.forEach { friendUid ->
                FirebaseManager.usersRef.document(friendUid).get().addOnSuccessListener { userDoc ->
                    val name = userDoc.getString("displayName") ?: userDoc.getString("username") ?: friendUid
                    val row = LayoutInflater.from(context).inflate(R.layout.item_friend, binding.llFriends, false)
                    row.findViewById<TextView>(R.id.tvName).text = name
                    row.setOnClickListener {
                        val fragment = ProfileFragment.newInstance(friendUid)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    row.findViewById<TextView>(R.id.btnMessage).setOnClickListener {
                        val fragment = ChatFragment.newInstance(friendUid, name, "")
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    binding.llFriends.addView(row)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
