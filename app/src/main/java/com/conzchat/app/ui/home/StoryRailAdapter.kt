package com.conzchat.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.model.Story
import com.conzchat.app.util.FirebaseManager

class StoryRailAdapter(
    private val stories: List<Story>,
    private val onAddStory: () -> Unit,
    private val onStoryClick: (Story) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_ADD = 0
        private const val TYPE_STORY = 1
    }

    // Group stories by user (show one item per user)
    private val groupedStories: List<Story>
        get() {
            val seen = mutableSetOf<String>()
            return stories.filter { seen.add(it.uid) }
        }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_ADD else TYPE_STORY
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_ADD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story_add, parent, false)
            AddStoryViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_story, parent, false)
            StoryViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is AddStoryViewHolder) {
            holder.itemView.setOnClickListener { onAddStory() }
        } else if (holder is StoryViewHolder) {
            val story = groupedStories[position - 1]
            val myUid = FirebaseManager.currentUid
            val isSeen = story.seenBy.contains(myUid)

            // Load avatar
            if (story.photo.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(story.photo)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_default_avatar)
                    .into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }

            // Story ring color
            holder.ivRing.setImageResource(
                if (isSeen) R.drawable.bg_story_ring_seen else R.drawable.bg_story_ring_unseen
            )

            holder.tvName.text = story.name.take(8)
            holder.itemView.setOnClickListener { onStoryClick(story) }
        }
    }

    override fun getItemCount() = groupedStories.size + 1 // +1 for add button

    inner class AddStoryViewHolder(view: View) : RecyclerView.ViewHolder(view)

    inner class StoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val ivRing: ImageView = view.findViewById(R.id.ivRing)
        val tvName: TextView = view.findViewById(R.id.tvName)
    }
}
