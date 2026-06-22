package com.conzchat.app.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.conzchat.app.databinding.BottomSheetVoiceRecorderBinding
import com.conzchat.app.util.FirebaseManager
import com.conzchat.app.util.toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File

class VoiceRecorderBottomSheet : BottomSheetDialogFragment() {

    companion object {
        fun newInstance(toUid: String) = VoiceRecorderBottomSheet().apply {
            arguments = Bundle().apply { putString("toUid", toUid) }
        }
    }

    private var _binding: BottomSheetVoiceRecorderBinding? = null
    private val binding get() = _binding!!

    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var seconds = 0
    private val handler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private val uid get() = FirebaseManager.currentUid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetVoiceRecorderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRecord.setOnClickListener {
            if (!isRecording) startRecording() else stopRecording()
        }
        binding.btnCancel.setOnClickListener { dismiss() }
    }

    private fun startRecording() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 200)
            return
        }
        audioFile = File.createTempFile("voice_", ".m4a", requireContext().cacheDir)
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioFile!!.absolutePath)
            prepare()
            start()
        }
        isRecording = true
        binding.btnRecord.text = "⏹ Stop"
        binding.tvStatus.text = "Recording..."
        seconds = 0
        timerRunnable = object : Runnable {
            override fun run() {
                seconds++
                binding.tvTimer.text = String.format("%d:%02d", seconds / 60, seconds % 60)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timerRunnable!!)
    }

    private fun stopRecording() {
        timerRunnable?.let { handler.removeCallbacks(it) }
        try {
            recorder?.stop()
            recorder?.release()
        } catch (e: Exception) { }
        recorder = null
        isRecording = false
        binding.btnRecord.text = "🎤 Record"
        binding.tvStatus.text = "Sending..."

        // Send voice note
        val toUid = arguments?.getString("toUid") ?: return
        val bytes = audioFile?.readBytes() ?: return
        val base64 = "data:audio/mp4;base64," + android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val msgData = hashMapOf<String, Any>(
            "from" to uid, "to" to toUid,
            "time" to System.currentTimeMillis(),
            "type" to "voice", "url" to base64,
            "text" to "", "receipt" to "S"
        )
        FirebaseManager.messagesRef.add(msgData)
            .addOnSuccessListener { dismiss() }
            .addOnFailureListener { context?.toast("Failed to send voice note") }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timerRunnable?.let { handler.removeCallbacks(it) }
        recorder?.release()
        _binding = null
    }
}
