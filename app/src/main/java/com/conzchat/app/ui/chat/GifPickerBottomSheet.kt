package com.conzchat.app.ui.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.conzchat.app.R
import com.conzchat.app.databinding.BottomSheetGifPickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.*
import org.json.JSONObject
import java.net.URL

class GifPickerBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private val GIPHY_KEY get() = com.conzchat.app.util.SecureConfig.giphyApiKey()
        private const val GIPHY_BASE = "https://api.giphy.com/v1/gifs"

        fun newInstance() = GifPickerBottomSheet()

        /** Convenience factory that wires the callback before showing */
        fun newInstance(
            chatId: String,
            isGroup: Boolean,
            isPublicGroup: Boolean,
            onGifSelected: (String) -> Unit
        ): GifPickerBottomSheet = GifPickerBottomSheet().also { it.setOnGifSelected(onGifSelected) }
    }

    private var _binding: BottomSheetGifPickerBinding? = null
    private val binding get() = _binding!!
    private var onGifSelected: ((String) -> Unit)? = null

    internal data class GifItem(val previewUrl: String, val fullUrl: String)

    private val gifItems = mutableListOf<GifItem>()
    private lateinit var gifAdapter: GifAdapter
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    fun setOnGifSelected(callback: (String) -> Unit) {
        onGifSelected = callback
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetGifPickerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gifAdapter = GifAdapter(gifItems) { item ->
            onGifSelected?.invoke(item.fullUrl)
            dismiss()
        }

        binding.rvGifs.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = gifAdapter
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                searchJob?.cancel()
                searchJob = scope.launch {
                    delay(400)
                    if (q.isNotEmpty()) fetchGifs(q) else fetchTrending()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        fetchTrending()
    }

    private fun fetchTrending() {
        scope.launch {
            try {
                showLoading(true)
                val result = withContext(Dispatchers.IO) {
                    URL("$GIPHY_BASE/trending?api_key=$GIPHY_KEY&limit=18&rating=pg-13").readText()
                }
                parseAndDisplay(result)
            } catch (e: Exception) {
                showError()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun fetchGifs(query: String) {
        scope.launch {
            try {
                showLoading(true)
                val result = withContext(Dispatchers.IO) {
                    val encoded = java.net.URLEncoder.encode(query, "UTF-8")
                    URL("$GIPHY_BASE/search?api_key=$GIPHY_KEY&q=$encoded&limit=18&rating=pg-13").readText()
                }
                parseAndDisplay(result)
            } catch (e: Exception) {
                showError()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun parseAndDisplay(json: String) {
        val obj = JSONObject(json)
        val data = obj.getJSONArray("data")
        gifItems.clear()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val images = item.getJSONObject("images")
            val previewUrl = try {
                images.getJSONObject("fixed_height_small").getString("url")
            } catch (e: Exception) {
                images.getJSONObject("original").getString("url")
            }
            val fullUrl = images.getJSONObject("original").getString("url")
            gifItems.add(GifItem(previewUrl, fullUrl))
        }
        if (gifItems.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvGifs.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.rvGifs.visibility = View.VISIBLE
        }
        gifAdapter.notifyDataSetChanged()
    }

    private fun showLoading(loading: Boolean) {
        if (_binding != null) {
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
    }

    private fun showError() {
        gifItems.clear()
        gifAdapter.notifyDataSetChanged()
        if (_binding != null) {
            binding.tvEmpty.text = "Could not load GIFs"
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvGifs.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scope.cancel()
        _binding = null
    }

    private inner class GifAdapter(
        private val items: List<GifItem>,
        private val onClick: (GifItem) -> Unit
    ) : RecyclerView.Adapter<GifAdapter.GifVH>() {

        inner class GifVH(view: View) : RecyclerView.ViewHolder(view) {
            val iv: ImageView = view.findViewById(R.id.ivGif)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GifVH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gif, parent, false)
            return GifVH(view)
        }

        override fun onBindViewHolder(holder: GifVH, position: Int) {
            val item = items[position]
            Glide.with(holder.itemView.context)
                .asGif()
                .load(item.previewUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_placeholder)
                .into(holder.iv)
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount() = items.size
    }
}
