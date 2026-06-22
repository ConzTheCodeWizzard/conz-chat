package com.conzchat.app.ui.vault

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.conzchat.app.db.ConzDatabase
import com.conzchat.app.db.VaultMessage
import com.conzchat.app.util.ConzMods
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SecretVaultFragment : Fragment() {
    private lateinit var pinLayout: LinearLayout
    private lateinit var vaultLayout: LinearLayout
    private lateinit var etPin: TextInputEditText
    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView
    private val messages = mutableListOf<VaultMessage>()

    companion object {
        fun newInstance() = SecretVaultFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF121212.toInt())
            setPadding(48, 48, 48, 48)
        }

        // Title
        root.addView(TextView(requireContext()).apply {
            text = "\uD83D\uDD12 Secret Vault"
            textSize = 22f
            setTextColor(0xFFFF1744.toInt())
            setPadding(0, 0, 0, 32)
        })

        // PIN layout
        pinLayout = LinearLayout(requireContext()).apply { orientation = LinearLayout.VERTICAL }
        etPin = TextInputEditText(requireContext()).apply {
            hint = "Enter PIN"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD
            setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF999999.toInt())
        }
        pinLayout.addView(etPin)

        pinLayout.addView(MaterialButton(requireContext()).apply {
            text = "Unlock Vault"
            setBackgroundColor(0xFFCC0000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { checkPin() }
        })

        pinLayout.addView(MaterialButton(requireContext()).apply {
            text = "Use Fingerprint"
            setBackgroundColor(0xFF222222.toInt())
            setTextColor(0xFFAAAAAA.toInt())
            setOnClickListener { useBiometric() }
        })

        pinLayout.addView(MaterialButton(requireContext()).apply {
            text = "Set New PIN"
            setBackgroundColor(0xFF333333.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { setNewPin() }
        })

        root.addView(pinLayout)

        // Vault layout (hidden until unlocked)
        vaultLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }
        tvEmpty = TextView(requireContext()).apply {
            text = "Your vault is empty.\nLong-press any message in chat and tap 'Save to Vault'."
            setTextColor(0xFF777777.toInt())
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 64, 0, 0)
        }
        vaultLayout.addView(tvEmpty)
        recycler = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
        vaultLayout.addView(recycler)
        vaultLayout.addView(MaterialButton(requireContext()).apply {
            text = "Clear Vault"
            setBackgroundColor(0xFF990000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            setOnClickListener { clearVault() }
        })
        root.addView(vaultLayout)

        return root
    }

    private fun checkPin() {
        val pin = etPin.text.toString()
        val savedPin = ConzMods.getVaultPin(requireContext())
        if (savedPin.isEmpty()) {
            Toast.makeText(requireContext(), "Set a PIN first", Toast.LENGTH_SHORT).show()
            return
        }
        if (pin == savedPin) unlock() else Toast.makeText(requireContext(), "Wrong PIN", Toast.LENGTH_SHORT).show()
    }

    private fun setNewPin() {
        val pin = etPin.text.toString()
        if (pin.length < 4) {
            Toast.makeText(requireContext(), "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show()
            return
        }
        ConzMods.setVaultPin(requireContext(), pin)
        Toast.makeText(requireContext(), "PIN set!", Toast.LENGTH_SHORT).show()
    }

    private fun useBiometric() {
        val executor = ContextCompat.getMainExecutor(requireContext())
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { unlock() }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                Toast.makeText(requireContext(), "Auth failed: $errString", Toast.LENGTH_SHORT).show()
            }
        })
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Vault")
            .setSubtitle("Use fingerprint to access your vault")
            .setNegativeButtonText("Cancel")
            .build()
        prompt.authenticate(info)
    }

    private fun unlock() {
        pinLayout.visibility = View.GONE
        vaultLayout.visibility = View.VISIBLE
        loadMessages()
    }

    private fun loadMessages() {
        lifecycleScope.launch {
            val msgs = ConzDatabase.get(requireContext()).vaultMessageDao().getAll()
            messages.clear()
            messages.addAll(msgs)
            tvEmpty.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            recycler.adapter = VaultAdapter(messages)
        }
    }

    private fun clearVault() {
        lifecycleScope.launch {
            ConzDatabase.get(requireContext()).vaultMessageDao().deleteAll()
            messages.clear()
            recycler.adapter?.notifyDataSetChanged()
            tvEmpty.visibility = View.VISIBLE
        }
    }
}

class VaultAdapter(private val items: List<VaultMessage>) : RecyclerView.Adapter<VaultAdapter.VH>() {
    private val df = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    inner class VH(val tv: TextView) : RecyclerView.ViewHolder(tv)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val tv = TextView(parent.context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            setPadding(24, 16, 24, 16)
        }
        return VH(tv)
    }
    override fun getItemCount() = items.size
    override fun onBindViewHolder(holder: VH, position: Int) {
        val m = items[position]
        holder.tv.text = "${m.senderName}: ${m.content}\n${df.format(Date(m.timestamp))}"
    }
}
