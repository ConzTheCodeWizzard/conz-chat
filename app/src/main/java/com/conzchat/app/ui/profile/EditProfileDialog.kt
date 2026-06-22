package com.conzchat.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.conzchat.app.databinding.DialogEditProfileBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EditProfileDialog : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(displayName: String, bio: String) = EditProfileDialog().apply {
            arguments = Bundle().apply {
                putString("displayName", displayName)
                putString("bio", bio)
            }
        }
    }

    private var _binding: DialogEditProfileBinding? = null
    private val binding get() = _binding!!
    private var onSave: ((String, String) -> Unit)? = null

    fun setOnSave(callback: (String, String) -> Unit) { onSave = callback }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.etDisplayName.setText(arguments?.getString("displayName") ?: "")
        binding.etBio.setText(arguments?.getString("bio") ?: "")

        binding.btnSave.setOnClickListener {
            val dn = binding.etDisplayName.text.toString().trim()
            val bio = binding.etBio.text.toString().trim()
            if (dn.isNotEmpty()) {
                onSave?.invoke(dn, bio)
                dismiss()
            }
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
