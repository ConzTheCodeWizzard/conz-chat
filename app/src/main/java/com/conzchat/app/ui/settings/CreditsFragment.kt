package com.conzchat.app.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.ConzChatApp
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentCreditsBinding
import com.conzchat.app.ui.chat.ChatFragment
import com.conzchat.app.util.HarleyThemeHelper

class CreditsFragment : Fragment() {

    companion object {
        fun newInstance() = CreditsFragment()
    }

    private var _binding: FragmentCreditsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreditsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }

        // Clicking the dev card opens a chat with @Borg
        binding.cardDev.setOnClickListener {
            val fragment = ChatFragment.newInstance(
                uid = ConzChatApp.DEV_UID,
                name = "Conz",
                photo = ""
            )
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
