package com.conzchat.app.ui.call

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentCallBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.firebase.firestore.ListenerRegistration
import com.conzchat.app.util.HarleyThemeHelper

class CallFragment : Fragment() {

    companion object {
        fun newInstance(
            toUid: String, toName: String, toPhoto: String,
            callType: String, isIncoming: Boolean, callId: String
        ) = CallFragment().apply {
            arguments = Bundle().apply {
                putString("toUid", toUid)
                putString("toName", toName)
                putString("toPhoto", toPhoto)
                putString("callType", callType)
                putBoolean("isIncoming", isIncoming)
                putString("callId", callId)
            }
        }
    }

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    private var toUid = ""
    private var toName = ""
    private var toPhoto = ""
    private var callType = "voice"
    private var isIncoming = false
    private var callId = ""
    private var callStatus = "ringing"
    private var callListener: ListenerRegistration? = null
    private var ringtonePlayer: MediaPlayer? = null
    private var callSeconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private val uid get() = FirebaseManager.currentUid
    private var isMuted = false
    private var isSpeaker = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        toUid = arguments?.getString("toUid") ?: ""
        toName = arguments?.getString("toName") ?: ""
        toPhoto = arguments?.getString("toPhoto") ?: ""
        callType = arguments?.getString("callType") ?: "voice"
        isIncoming = arguments?.getBoolean("isIncoming") ?: false
        callId = arguments?.getString("callId") ?: ""

        setupUI()

        if (isIncoming) {
            showIncomingUI()
            playRingtone()
        } else {
            initiateCall()
        }

        listenForCallChanges()
    }

    private fun setupUI() {
        binding.tvCallerName.text = toName
        binding.tvCallType.text = if (callType == "video") "Video Call" else "Voice Call"

        if (toPhoto.isNotEmpty()) {
            Glide.with(this).load(toPhoto)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_default_avatar)
                .into(binding.ivCallerAvatar)
        }

        binding.btnEndCall.setOnClickListener { endCall() }
        binding.btnAccept.setOnClickListener { acceptCall() }
        binding.btnDecline.setOnClickListener { declineCall() }
        binding.btnMute.setOnClickListener { toggleMute() }
        binding.btnSpeaker.setOnClickListener { toggleSpeaker() }
    }

    private fun showIncomingUI() {
        binding.tvCallStatus.text = "Incoming ${if (callType == "video") "Video" else "Voice"} Call"
        binding.btnAccept.visibility = View.VISIBLE
        binding.btnDecline.visibility = View.VISIBLE
        binding.btnEndCall.visibility = View.GONE
        binding.btnMute.visibility = View.GONE
        binding.btnSpeaker.visibility = View.GONE
    }

    private fun showActiveUI() {
        binding.btnAccept.visibility = View.GONE
        binding.btnDecline.visibility = View.GONE
        binding.btnEndCall.visibility = View.VISIBLE
        binding.btnMute.visibility = View.VISIBLE
        binding.btnSpeaker.visibility = View.VISIBLE
        startTimer()
    }

    private fun initiateCall() {
        binding.tvCallStatus.text = "Calling..."
        binding.btnAccept.visibility = View.GONE
        binding.btnDecline.visibility = View.GONE
        binding.btnEndCall.visibility = View.VISIBLE
        binding.btnMute.visibility = View.GONE
        binding.btnSpeaker.visibility = View.GONE

        val callData = hashMapOf(
            "from" to uid, "to" to toUid,
            "type" to callType, "status" to "ringing",
            "time" to System.currentTimeMillis()
        )
        if (callId.isEmpty()) {
            FirebaseManager.callsRef.add(callData).addOnSuccessListener { docRef ->
                callId = docRef.id
            }
        }
    }

    private fun acceptCall() {
        stopRingtone()
        FirebaseManager.callsRef.document(callId).update("status", "active")
        showActiveUI()
        callStatus = "active"
        binding.tvCallStatus.text = "Connected"
    }

    private fun declineCall() {
        stopRingtone()
        FirebaseManager.callsRef.document(callId).update("status", "declined")
        parentFragmentManager.popBackStack()
    }

    private fun endCall() {
        stopTimer()
        FirebaseManager.callsRef.document(callId).update("status", "ended")
        parentFragmentManager.popBackStack()
    }

    private fun listenForCallChanges() {
        if (callId.isEmpty()) return
        callListener = FirebaseManager.callsRef.document(callId)
            .addSnapshotListener { snap, _ ->
                if (snap == null || !snap.exists()) return@addSnapshotListener
                val status = snap.getString("status") ?: return@addSnapshotListener
                when (status) {
                    "active" -> {
                        if (callStatus != "active") {
                            callStatus = "active"
                            stopRingtone()
                            showActiveUI()
                            binding.tvCallStatus.text = "Connected"
                        }
                    }
                    "declined" -> {
                        stopRingtone()
                        binding.tvCallStatus.text = "Call Declined"
                        handler.postDelayed({ parentFragmentManager.popBackStack() }, 1500)
                    }
                    "ended" -> {
                        stopRingtone()
                        stopTimer()
                        binding.tvCallStatus.text = "Call Ended"
                        handler.postDelayed({ parentFragmentManager.popBackStack() }, 1500)
                    }
                    "missed" -> {
                        stopRingtone()
                        binding.tvCallStatus.text = "Missed Call"
                        handler.postDelayed({ parentFragmentManager.popBackStack() }, 1500)
                    }
                }
            }
    }

    private fun playRingtone() {
        try {
            ringtonePlayer = MediaPlayer().apply {
                setAudioStreamType(AudioManager.STREAM_RING)
                isLooping = true
                val uri = android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_RINGTONE)
                setDataSource(requireContext(), uri)
                prepare()
                start()
            }
        } catch (e: Exception) { }
    }

    private fun stopRingtone() {
        ringtonePlayer?.stop()
        ringtonePlayer?.release()
        ringtonePlayer = null
    }

    private fun startTimer() {
        callSeconds = 0
        timerRunnable = object : Runnable {
            override fun run() {
                callSeconds++
                binding.tvCallStatus.text = String.format("%d:%02d", callSeconds / 60, callSeconds % 60)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let { handler.removeCallbacks(it) }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        binding.btnMute.alpha = if (isMuted) 0.5f else 1.0f
        binding.btnMute.text = if (isMuted) "🔇" else "🎤"
    }

    private fun toggleSpeaker() {
        isSpeaker = !isSpeaker
        val am = requireContext().getSystemService(android.content.Context.AUDIO_SERVICE) as AudioManager
        am.isSpeakerphoneOn = isSpeaker
        binding.btnSpeaker.alpha = if (isSpeaker) 1.0f else 0.5f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopRingtone()
        stopTimer()
        callListener?.remove()
        _binding = null
    }
}
