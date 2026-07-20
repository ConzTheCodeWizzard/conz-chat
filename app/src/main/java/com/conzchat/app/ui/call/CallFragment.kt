package com.conzchat.app.ui.call

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentCallBinding
import com.conzchat.app.util.ApiManager
import com.conzchat.app.util.HarleyThemeHelper
import com.conzchat.app.util.OneSignalNotifier
import com.conzchat.app.util.toast
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

class CallFragment : Fragment() {

    private var _binding: FragmentCallBinding? = null
    private val binding get() = _binding!!

    // Call parameters
    private var toUid = ""
    private var toName = ""
    private var toPhoto = ""
    private var callType = "voice"   // "voice" or "video"
    private var isIncoming = false
    private var callId = ""
    private var channelName = ""
    private var agoraToken = ""

    // Agora
    private var rtcEngine: RtcEngine? = null
    private var isMuted = false
    private var isSpeakerOn = false
    private var isCallConnected = false
    private var remoteUid = 0

    // Timer
    private val timerHandler = Handler(Looper.getMainLooper())
    private var callSeconds = 0
    private val timerRunnable = object : Runnable {
        override fun run() {
            callSeconds++
            val h = callSeconds / 3600
            val m = (callSeconds % 3600) / 60
            val s = callSeconds % 60
            val timeStr = if (h > 0) String.format("%d:%02d:%02d", h, m, s)
                          else String.format("%02d:%02d", m, s)
            _binding?.tvCallStatus?.text = timeStr
            _binding?.tvCallStatusVideo?.text = timeStr
            timerHandler.postDelayed(this, 1000)
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allGranted = grants.values.all { it }
        if (allGranted) {
            initAgoraAndJoin()
        } else {
            context?.toast("Microphone/camera permission required for calls")
            endCallAndExit()
        }
    }

    companion object {
        private const val TAG = "CallFragment"

        fun newInstance(
            toUid: String,
            toName: String,
            toPhoto: String,
            callType: String,
            isIncoming: Boolean,
            callId: String = "",
            channelName: String = "",
            agoraToken: String = ""
        ) = CallFragment().apply {
            arguments = Bundle().apply {
                putString("toUid", toUid)
                putString("toName", toName)
                putString("toPhoto", toPhoto)
                putString("callType", callType)
                putBoolean("isIncoming", isIncoming)
                putString("callId", callId)
                putString("channelName", channelName)
                putString("agoraToken", agoraToken)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCallBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HarleyThemeHelper.applyTheme(requireContext(), view)

        // Extract args
        toUid = arguments?.getString("toUid") ?: ""
        toName = arguments?.getString("toName") ?: ""
        toPhoto = arguments?.getString("toPhoto") ?: ""
        callType = arguments?.getString("callType") ?: "voice"
        isIncoming = arguments?.getBoolean("isIncoming") ?: false
        callId = arguments?.getString("callId") ?: ""
        channelName = arguments?.getString("channelName") ?: ""
        agoraToken = arguments?.getString("agoraToken") ?: ""

        setupUI()
        setupButtons()

        if (isIncoming) {
            // Show incoming call UI — wait for user to accept
            showIncomingUI()
        } else {
            // Outgoing call — fetch token then join
            showOutgoingUI()
            fetchTokenAndJoin()
        }
    }

    // ─── UI Setup ─────────────────────────────────────────────────────────────

    private fun setupUI() {
        binding.tvCallerName.text = toName
        binding.tvCallerNameVideo.text = toName
        binding.tvCallType.text = if (callType == "video") "Video Call" else "Voice Call"
        binding.tvCallStatus.text = if (isIncoming) "Incoming call…" else "Calling…"
        binding.tvCallStatusVideo.text = if (isIncoming) "Incoming call…" else "Calling…"

        Glide.with(this)
            .load(ApiManager.normalizeUrl(toPhoto))
            .circleCrop()
            .placeholder(R.drawable.ic_default_avatar)
            .into(binding.ivCallerAvatar)
    }

    private fun setupButtons() {
        // Mute
        binding.btnMute.setOnClickListener {
            isMuted = !isMuted
            rtcEngine?.muteLocalAudioStream(isMuted)
            binding.btnMute.text = if (isMuted) "🔇" else "🎤"
        }

        // Speaker
        binding.btnSpeaker.setOnClickListener {
            isSpeakerOn = !isSpeakerOn
            val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.isSpeakerphoneOn = isSpeakerOn
            binding.btnSpeaker.text = if (isSpeakerOn) "🔈" else "🔊"
        }

        // Flip camera
        binding.btnFlipCamera.setOnClickListener {
            rtcEngine?.switchCamera()
        }

        // End call (active)
        binding.btnEndCall.setOnClickListener {
            endCallAndExit()
        }

        // Cancel (outgoing, not yet connected)
        binding.btnCancelCall.setOnClickListener {
            endCallAndExit()
        }

        // Decline (incoming)
        binding.btnDecline.setOnClickListener {
            endCallAndExit()
        }

        // Accept (incoming)
        binding.btnAccept.setOnClickListener {
            acceptCall()
        }
    }

    private fun showIncomingUI() {
        binding.llVoiceBackground.visibility = View.VISIBLE
        binding.llIncomingControls.visibility = View.VISIBLE
        binding.llOutgoingControls.visibility = View.GONE
        binding.llActiveControls.visibility = View.GONE
    }

    private fun showOutgoingUI() {
        binding.llVoiceBackground.visibility = View.VISIBLE
        binding.llOutgoingControls.visibility = View.VISIBLE
        binding.llIncomingControls.visibility = View.GONE
        binding.llActiveControls.visibility = View.GONE
    }

    private fun showActiveCallUI() {
        isCallConnected = true
        binding.llActiveControls.visibility = View.VISIBLE
        binding.llIncomingControls.visibility = View.GONE
        binding.llOutgoingControls.visibility = View.GONE
        // Show flip camera button for video calls
        if (callType == "video") {
            binding.llFlipCamera.visibility = View.VISIBLE
        }
        // Start timer
        timerHandler.post(timerRunnable)
    }

    // ─── Token & Join ─────────────────────────────────────────────────────────

    private fun fetchTokenAndJoin() {
        if (channelName.isEmpty()) {
            // Generate channel name from sorted UIDs to ensure both sides use same channel
            val myUid = ApiManager.currentUserId
            val sorted = listOf(myUid, toUid).sorted()
            channelName = "dm_${sorted[0]}_${sorted[1]}_${System.currentTimeMillis() / 1000}"
        }

        if (agoraToken.isNotEmpty()) {
            checkPermissionsAndJoin()
            return
        }

        // Fetch token from VPS sidecar
        val myUid = ApiManager.currentUserId
        val body = mapOf(
            "channel" to channelName,
            "uid" to 0,
            "token" to (ApiManager.getToken() ?: "")
        )
        val req = ApiManager.buildPublicRequest("/api/calls/token", "POST", body, port = 8081)
        ApiManager.executeRaw(req) { responseBody, err ->
            if (err != null || responseBody == null) {
                Log.e(TAG, "Token fetch failed: $err")
                activity?.runOnUiThread {
                    context?.toast("Could not start call — network error")
                    endCallAndExit()
                }
                return@executeRaw
            }
            try {
                val json = ApiManager.gson.fromJson(responseBody, Map::class.java)
                agoraToken = json["token"] as? String ?: ""
                // Send call signal to the other user
                sendCallSignal()
                activity?.runOnUiThread { checkPermissionsAndJoin() }
            } catch (e: Exception) {
                Log.e(TAG, "Token parse error: ${e.message}")
                activity?.runOnUiThread {
                    context?.toast("Call setup failed")
                    endCallAndExit()
                }
            }
        }
    }

    private fun sendCallSignal() {
        // Send push notification to the callee so they see incoming call
        val myUid = ApiManager.currentUserId
        val myName = ApiManager.currentUser?.displayName ?: ApiManager.currentUsername
        val myPhoto = ApiManager.currentUser?.photo ?: ""
        OneSignalNotifier.sendCallNotification(
            toUid = toUid,
            fromUid = myUid,
            fromName = myName,
            fromPhoto = myPhoto,
            callType = callType,
            callId = callId,
            channelName = channelName,
            agoraToken = agoraToken
        )
    }

    private fun acceptCall() {
        // If we have a channel and token (passed via notification), join directly
        if (channelName.isNotEmpty() && agoraToken.isNotEmpty()) {
            checkPermissionsAndJoin()
        } else if (channelName.isNotEmpty()) {
            // Fetch token for this channel
            val body = mapOf(
                "channel" to channelName,
                "uid" to 0,
                "token" to (ApiManager.getToken() ?: "")
            )
            val req = ApiManager.buildPublicRequest("/api/calls/token", "POST", body, port = 8081)
            ApiManager.executeRaw(req) { responseBody, err ->
                if (responseBody != null) {
                    try {
                        val json = ApiManager.gson.fromJson(responseBody, Map::class.java)
                        agoraToken = json["token"] as? String ?: ""
                    } catch (e: Exception) { /* use empty token */ }
                }
                activity?.runOnUiThread { checkPermissionsAndJoin() }
            }
        } else {
            context?.toast("Call data missing — cannot accept")
        }
    }

    private fun checkPermissionsAndJoin() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (callType == "video") perms.add(Manifest.permission.CAMERA)

        val missing = perms.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            initAgoraAndJoin()
        } else {
            permissionLauncher.launch(missing.toTypedArray())
        }
    }

    // ─── Agora Engine ─────────────────────────────────────────────────────────

    private fun initAgoraAndJoin() {
        try {
            val config = RtcEngineConfig().apply {
                mContext = requireContext().applicationContext
                mAppId = com.conzchat.app.util.SecureConfig.agoraAppId()
                mEventHandler = rtcEventHandler
            }
            rtcEngine = RtcEngine.create(config)

            if (callType == "video") {
                rtcEngine?.enableVideo()
                rtcEngine?.setVideoEncoderConfiguration(
                    VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_1280x720,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_ADAPTIVE
                    )
                )
                setupLocalVideo()
            } else {
                rtcEngine?.disableVideo()
                // Enable speakerphone for voice calls by default
                val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                audioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
            }

            val options = ChannelMediaOptions().apply {
                clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                publishMicrophoneTrack = true
                publishCameraTrack = callType == "video"
                autoSubscribeAudio = true
                autoSubscribeVideo = callType == "video"
            }

            val uid = 0 // Let Agora assign a UID
            val result = rtcEngine?.joinChannel(agoraToken, channelName, uid, options)
            Log.d(TAG, "joinChannel result: $result, channel: $channelName")

            // Show outgoing UI while waiting for remote user
            if (!isIncoming) {
                binding.tvCallStatus.text = "Ringing…"
                binding.tvCallStatusVideo.text = "Ringing…"
            }

        } catch (e: Exception) {
            Log.e(TAG, "Agora init failed: ${e.message}")
            context?.toast("Call failed: ${e.message}")
            endCallAndExit()
        }
    }

    private fun setupLocalVideo() {
        val localView = RtcEngine.CreateRendererView(requireContext())
        localView.setZOrderMediaOverlay(true)
        binding.flLocalVideo.removeAllViews()
        binding.flLocalVideo.addView(localView)
        binding.flLocalVideo.visibility = View.VISIBLE
        rtcEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0))
        rtcEngine?.startPreview()
    }

    private fun setupRemoteVideo(uid: Int) {
        activity?.runOnUiThread {
            if (_binding == null) return@runOnUiThread
            val remoteView = RtcEngine.CreateRendererView(requireContext())
            binding.flRemoteVideo.removeAllViews()
            binding.flRemoteVideo.addView(remoteView)
            binding.flRemoteVideo.visibility = View.VISIBLE
            rtcEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid))

            // Switch to video overlay UI
            binding.llVoiceBackground.visibility = View.GONE
            binding.llVideoOverlay.visibility = View.VISIBLE
            binding.llControls.setBackgroundColor(0x88000000.toInt())
        }
    }

    // ─── Agora Event Handler ──────────────────────────────────────────────────

    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            Log.d(TAG, "Joined channel: $channel, uid: $uid")
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                if (!isIncoming) {
                    binding.tvCallStatus.text = "Ringing…"
                    binding.tvCallStatusVideo.text = "Ringing…"
                }
            }
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.d(TAG, "Remote user joined: $uid")
            remoteUid = uid
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                showActiveCallUI()
                if (callType == "video") {
                    setupRemoteVideo(uid)
                } else {
                    binding.tvCallStatus.text = "00:00"
                }
            }
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.d(TAG, "Remote user offline: $uid, reason: $reason")
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                context?.toast("Call ended")
                endCallAndExit()
            }
        }

        override fun onLeaveChannel(stats: RtcStats) {
            Log.d(TAG, "Left channel")
        }

        override fun onError(err: Int) {
            Log.e(TAG, "Agora error: $err")
            activity?.runOnUiThread {
                if (_binding == null) return@runOnUiThread
                val msg = when (err) {
                    Constants.ERR_INVALID_TOKEN -> "Invalid call token"
                    Constants.ERR_TOKEN_EXPIRED -> "Call token expired"
                    Constants.ERR_NOT_READY -> "Engine not ready"
                    else -> "Call error ($err)"
                }
                context?.toast(msg)
                endCallAndExit()
            }
        }

        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
            if (state == Constants.REMOTE_VIDEO_STATE_DECODING) {
                setupRemoteVideo(uid)
            }
        }
    }

    // ─── End Call ─────────────────────────────────────────────────────────────

    private fun endCallAndExit() {
        timerHandler.removeCallbacks(timerRunnable)
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
        } catch (e: Exception) {
            Log.e(TAG, "Agora cleanup error: ${e.message}")
        }
        // Restore audio mode
        val audioManager = context?.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        audioManager?.mode = AudioManager.MODE_NORMAL
        audioManager?.isSpeakerphoneOn = false

        if (isAdded) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerHandler.removeCallbacks(timerRunnable)
        try {
            rtcEngine?.leaveChannel()
            RtcEngine.destroy()
            rtcEngine = null
        } catch (e: Exception) {
            Log.e(TAG, "Agora destroy error: ${e.message}")
        }
        _binding = null
    }
}
