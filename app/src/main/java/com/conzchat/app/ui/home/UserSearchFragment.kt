package com.conzchat.app.ui.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentUserSearchBinding
import com.conzchat.app.ui.chat.ChatFragment
import com.conzchat.app.ui.profile.ProfileFragment
import com.conzchat.app.util.FirebaseManager
import de.hdodenhof.circleimageview.CircleImageView

class UserSearchFragment : Fragment() {

    private var _binding: FragmentUserSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim().lowercase()
                if (query.isNotEmpty()) searchUsers(query)
                else binding.llResults.removeAllViews()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun searchUsers(query: String) {
        FirebaseManager.usersRef.get().addOnSuccessListener { snap ->
            binding.llResults.removeAllViews()
            snap.documents.forEach { doc ->
                val u = doc.data ?: return@forEach
                val banned = u["banned"] as? Boolean ?: false
                if (banned) return@forEach
                val username = (u["username"] as? String ?: "").lowercase()
                if (username != query) return@forEach

                val name = u["displayName"] as? String ?: u["username"] as? String ?: ""
                val photo = u["photo"] as? String ?: ""

                val row = LayoutInflater.from(context).inflate(R.layout.item_user_search, binding.llResults, false)
                val ivAvatar = row.findViewById<CircleImageView>(R.id.ivAvatar)
                val tvName = row.findViewById<TextView>(R.id.tvName)
                val tvUsername = row.findViewById<TextView>(R.id.tvUsername)

                if (photo.isNotEmpty()) {
                    Glide.with(this).load(photo).apply(RequestOptions.circleCropTransform()).into(ivAvatar)
                }
                tvName.text = name
                tvUsername.text = "@${u["username"]}"

                row.setOnClickListener {
                    val fragment = ChatFragment.newInstance(doc.id, name, photo)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                row.setOnLongClickListener {
                    val fragment = ProfileFragment.newInstance(doc.id)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                    true
                }

                binding.llResults.addView(row)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
