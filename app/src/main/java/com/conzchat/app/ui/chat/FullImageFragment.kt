package com.conzchat.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.conzchat.app.databinding.FragmentFullImageBinding

class FullImageFragment : Fragment() {

    companion object {
        fun newInstance(url: String) = FullImageFragment().apply {
            arguments = Bundle().apply { putString("url", url) }
        }
    }

    private var _binding: FragmentFullImageBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFullImageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = arguments?.getString("url") ?: return
        Glide.with(this).load(url).into(binding.ivFull)
        binding.ivFull.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.ivClose.setOnClickListener { parentFragmentManager.popBackStack() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
