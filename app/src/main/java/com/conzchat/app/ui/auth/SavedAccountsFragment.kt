package com.conzchat.app.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentSavedAccountsBinding
import com.conzchat.app.databinding.ItemSavedAccountBinding
import com.conzchat.app.db.ConzDatabase
import com.conzchat.app.db.SavedAccount
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import com.conzchat.app.util.HarleyThemeHelper

class SavedAccountsFragment : Fragment() {
    private var _binding: FragmentSavedAccountsBinding? = null
    private val binding get() = _binding!!
    private val accounts = mutableListOf<SavedAccount>()
    private lateinit var adapter: SavedAccountAdapter

    companion object {
        fun newInstance() = SavedAccountsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSavedAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)
        adapter = SavedAccountAdapter(accounts,
            onTap = { account -> autoSignIn(account) },
            onDelete = { account -> deleteAccount(account) }
        )
        binding.rvSavedAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSavedAccounts.adapter = adapter
        binding.btnBack.setOnClickListener { parentFragmentManager.popBackStack() }
        loadAccounts()
    }

    private fun loadAccounts() {
        lifecycleScope.launch {
            val list = ConzDatabase.get(requireContext()).savedAccountDao().getAll()
            accounts.clear()
            accounts.addAll(list)
            adapter.notifyDataSetChanged()
            binding.tvEmpty.visibility = if (accounts.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun autoSignIn(account: SavedAccount) {
        Toast.makeText(requireContext(), "Signing in as ${account.username}...", Toast.LENGTH_SHORT).show()
        FirebaseAuth.getInstance().signInWithEmailAndPassword(account.email, account.password)
            .addOnSuccessListener {
                if (!isAdded) return@addOnSuccessListener
                // Navigate to home
                (activity as? com.conzchat.app.MainActivity)?.showHome()
            }
            .addOnFailureListener { e ->
                if (!isAdded) return@addOnFailureListener
                Toast.makeText(requireContext(), "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteAccount(account: SavedAccount) {
        lifecycleScope.launch {
            ConzDatabase.get(requireContext()).savedAccountDao().delete(account.id)
            loadAccounts()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}

class SavedAccountAdapter(
    private val accounts: List<SavedAccount>,
    private val onTap: (SavedAccount) -> Unit,
    private val onDelete: (SavedAccount) -> Unit
) : androidx.recyclerview.widget.RecyclerView.Adapter<SavedAccountAdapter.VH>() {

    inner class VH(val binding: ItemSavedAccountBinding) : androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSavedAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun getItemCount() = accounts.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val account = accounts[position]
        holder.binding.tvUsername.text = account.username.ifEmpty { "User" }
        holder.binding.tvEmail.text = account.email
        holder.itemView.setOnClickListener { onTap(account) }
        holder.binding.btnDelete.setOnClickListener { onDelete(account) }
    }
}
