package com.conzchat.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentSuggestionsBinding
import com.conzchat.app.ui.profile.ProfileFragment
import com.conzchat.app.util.FirebaseManager
import de.hdodenhof.circleimageview.CircleImageView

class SuggestionsFragment : Fragment() {

    private var _binding: FragmentSuggestionsBinding? = null
    private val binding get() = _binding!!
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSuggestionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        loadSuggestions()
    }

    private fun loadSuggestions() {
        // Get users not already friends
        FirebaseManager.usersRef.document(uid).get().addOnSuccessListener { myDoc ->
            val myFriends = (myDoc.get("friends") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
            val blocked = (myDoc.get("blockedUsers") as? List<*>)?.filterIsInstance<String>() ?: emptyList()

            FirebaseManager.usersRef.limit(50).get().addOnSuccessListener { snap ->
                binding.llSuggestions.removeAllViews()
                snap.documents.forEach { doc ->
                    if (doc.id == uid) return@forEach
                    if (doc.id in myFriends) return@forEach
                    if (doc.id in blocked) return@forEach

                    val d = doc.data ?: return@forEach
                    val name = d["displayName"] as? String ?: d["username"] as? String ?: ""
                    val photo = d["photo"] as? String ?: ""
                    val username = d["username"] as? String ?: ""

                    val row = LayoutInflater.from(context).inflate(R.layout.item_suggestion, binding.llSuggestions, false)
                    row.findViewById<TextView>(R.id.tvName).text = name
                    row.findViewById<TextView>(R.id.tvUsername).text = "@$username"
                    val iv = row.findViewById<CircleImageView>(R.id.ivAvatar)
                    if (photo.isNotEmpty()) {
                        Glide.with(this).load(photo)
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.ic_default_avatar)
                            .into(iv)
                    }
                    row.setOnClickListener {
                        val fragment = ProfileFragment.newInstance(doc.id)
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                    row.findViewById<TextView>(R.id.btnAdd).setOnClickListener {
                        val reqData = hashMapOf(
                            "from" to uid, "to" to doc.id,
                            "status" to "pending",
                            "time" to System.currentTimeMillis()
                        )
                        FirebaseManager.friendRequestsRef.add(reqData)
                        row.findViewById<TextView>(R.id.btnAdd).text = "Sent ✓"
                        row.findViewById<TextView>(R.id.btnAdd).isEnabled = false
                    }
                    binding.llSuggestions.addView(row)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
