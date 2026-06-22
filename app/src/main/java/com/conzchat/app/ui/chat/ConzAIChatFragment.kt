package com.conzchat.app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.conzchat.app.databinding.FragmentConzAiChatBinding
import com.conzchat.app.model.Message
import com.conzchat.app.util.FirebaseManager
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ConzAIChatFragment : Fragment() {

    companion object {
        // Use sandbox pre-configured OpenAI-compatible endpoint
        private const val OPENAI_BASE = "https://api.manus.im/api/llm-proxy/v1"
        private const val OPENAI_KEY = "sk-QxFRdwBsYSkswPsokEaLBy"
        private const val SYSTEM_PROMPT = """You are Conz AI, the official AI assistant built into ConzChat — a modern messaging app. 
You are helpful, friendly, and a little edgy/cool to match the app's vibe. 
Keep responses concise and conversational — this is a chat app, not an essay. 
You can answer questions, help with ideas, tell jokes, give advice, and chat casually. 
Never reveal you are built on OpenAI. You are Conz AI, made by ConzChat."""
    }

    private var _binding: FragmentConzAiChatBinding? = null
    private val binding get() = _binding!!

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ConzAIMessageAdapter
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val uid get() = FirebaseManager.currentUid

    // Conversation history for context
    private val conversationHistory = mutableListOf<Pair<String, String>>() // role, content

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentConzAiChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ConzAIMessageAdapter(messages)
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@ConzAIChatFragment.adapter
        }

        binding.ivBack.setOnClickListener { parentFragmentManager.popBackStack() }
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        // Welcome message
        addAIMessage("👋 Hey! I'm Conz AI. Ask me anything — I'm always here 🤖")
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return
        binding.etMessage.setText("")

        // Add user message to UI
        messages.add(Message(
            id = System.currentTimeMillis().toString(),
            from = uid, to = "conz_ai",
            time = System.currentTimeMillis(),
            text = text, type = "text"
        ))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)

        // Add to history
        conversationHistory.add(Pair("user", text))

        // Show typing indicator
        binding.tvTyping.visibility = View.VISIBLE
        binding.btnSend.isEnabled = false

        // Call AI
        scope.launch {
            val response = withContext(Dispatchers.IO) { callAI() }
            if (_binding == null) return@launch
            binding.tvTyping.visibility = View.GONE
            binding.btnSend.isEnabled = true
            conversationHistory.add(Pair("assistant", response))
            addAIMessage(response)
        }
    }

    private fun addAIMessage(text: String) {
        messages.add(Message(
            id = System.currentTimeMillis().toString(),
            from = "conz_ai", to = uid,
            time = System.currentTimeMillis(),
            text = text, type = "text"
        ))
        adapter.notifyItemInserted(messages.size - 1)
        binding.rvMessages.scrollToPosition(messages.size - 1)
    }

    private fun callAI(): String {
        return try {
            val url = URL("$OPENAI_BASE/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $OPENAI_KEY")
            conn.doOutput = true
            conn.connectTimeout = 15000
            conn.readTimeout = 30000

            val messagesArray = JSONArray()
            // System message
            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", SYSTEM_PROMPT)
            })
            // Conversation history (last 10 exchanges to avoid token limit)
            val historyToSend = if (conversationHistory.size > 20) conversationHistory.takeLast(20) else conversationHistory
            for ((role, content) in historyToSend) {
                messagesArray.put(JSONObject().apply {
                    put("role", role)
                    put("content", content)
                })
            }

            val body = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", messagesArray)
                put("max_tokens", 300)
                put("temperature", 0.8)
            }

            conn.outputStream.write(body.toString().toByteArray())

            val responseCode = conn.responseCode
            val responseText = if (responseCode == 200) {
                conn.inputStream.bufferedReader().readText()
            } else {
                conn.errorStream?.bufferedReader()?.readText() ?: ""
            }

            if (responseCode != 200) {
                return "I hit a snag on my end. Give me a sec and try again! 🤖"
            }

            val json = JSONObject(responseText)
            json.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        } catch (e: Exception) {
            "Connection issue on my end. Try again! 🤖"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }
}
