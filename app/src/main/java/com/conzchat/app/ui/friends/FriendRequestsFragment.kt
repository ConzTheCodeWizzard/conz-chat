package com.conzchat.app.ui.friends

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.conzchat.app.R
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.conzchat.app.util.HarleyThemeHelper

class FriendRequestsFragment : Fragment() {

    companion object {
        fun newInstance() = FriendRequestsFragment()
    }

    private val uid get() = FirebaseManager.currentUid
    private var listener: ListenerRegistration? = null
    private val requests = mutableListOf<RequestItem>()
    private lateinit var adapter: RequestAdapter

    data class RequestItem(val docId: String, val fromUid: String, val name: String, val photo: String, val username: String)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_friend_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        view.findViewById<View>(R.id.ivBack).setOnClickListener { parentFragmentManager.popBackStack() }

        val rv = view.findViewById<RecyclerView>(R.id.rvRequests)
        val tvEmpty = view.findViewById<TextView>(R.id.tvEmpty)

        adapter = RequestAdapter(requests,
            onAccept = { item ->
                acceptRequest(item)
            },
            onDecline = { item ->
                declineRequest(item)
            }
        )
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        listener = FirebaseManager.friendRequestsRef
            .whereEqualTo("to", uid)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snap, _ ->
                requests.clear()
                if (snap == null || snap.isEmpty) {
                    tvEmpty.visibility = View.VISIBLE
                    rv.visibility = View.GONE
                    adapter.notifyDataSetChanged()
                    return@addSnapshotListener
                }
                tvEmpty.visibility = View.GONE
                rv.visibility = View.VISIBLE
                var loaded = 0
                val docs = snap.documents
                docs.forEach { doc ->
                    val fromUid = doc.getString("from") ?: return@forEach
                    FirebaseManager.usersRef.document(fromUid).get().addOnSuccessListener { userDoc ->
                        val name = userDoc.getString("displayName") ?: userDoc.getString("username") ?: "Unknown"
                        val photo = userDoc.getString("photoUrl") ?: ""
                        val username = userDoc.getString("username") ?: ""
                        requests.add(RequestItem(doc.id, fromUid, name, photo, username))
                        loaded++
                        if (loaded == docs.size) {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
    }

    private fun acceptRequest(item: RequestItem) {
        FirebaseManager.friendRequestsRef.document(item.docId).update("status", "accepted")
        // Add to both users' friends sub-collection
        FirebaseManager.usersRef.document(uid).collection("friends").document(item.fromUid)
            .set(mapOf("uid" to item.fromUid, "since" to System.currentTimeMillis()))
        FirebaseManager.usersRef.document(item.fromUid).collection("friends").document(uid)
            .set(mapOf("uid" to uid, "since" to System.currentTimeMillis()))
        // Atomically increment friendCount for both users
        FirebaseManager.usersRef.document(uid)
            .update("friendCount", com.google.firebase.firestore.FieldValue.increment(1))
        FirebaseManager.usersRef.document(item.fromUid)
            .update("friendCount", com.google.firebase.firestore.FieldValue.increment(1))
        context?.toast("✅ You and ${item.name} are now friends!")
    }

    private fun declineRequest(item: RequestItem) {
        FirebaseManager.friendRequestsRef.document(item.docId).update("status", "declined")
        context?.toast("Request declined")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listener?.remove()
    }

    // ── Adapter ──────────────────────────────────────────────────────────────
    inner class RequestAdapter(
        private val items: MutableList<RequestItem>,
        private val onAccept: (RequestItem) -> Unit,
        private val onDecline: (RequestItem) -> Unit
    ) : RecyclerView.Adapter<RequestAdapter.VH>() {

        inner class VH(v: View) : RecyclerView.ViewHolder(v) {
            val ivAvatar: ImageView = v.findViewById(R.id.ivAvatar)
            val tvName: TextView = v.findViewById(R.id.tvName)
            val tvUsername: TextView = v.findViewById(R.id.tvUsername)
            val btnAccept: TextView = v.findViewById(R.id.btnAccept)
            val btnDecline: TextView = v.findViewById(R.id.btnDecline)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request_card, parent, false)
            return VH(v)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = items[position]
            holder.tvName.text = item.name
            holder.tvUsername.text = if (item.username.isNotEmpty()) "@${item.username}" else ""
            if (item.photo.isNotEmpty()) {
                Glide.with(holder.ivAvatar).load(item.photo).circleCrop()
                    .placeholder(R.drawable.ic_default_avatar).into(holder.ivAvatar)
            } else {
                holder.ivAvatar.setImageResource(R.drawable.ic_default_avatar)
            }
            holder.btnAccept.setOnClickListener {
                onAccept(item)
                items.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
            holder.btnDecline.setOnClickListener {
                onDecline(item)
                items.removeAt(holder.adapterPosition)
                notifyItemRemoved(holder.adapterPosition)
            }
        }
    }
}
