package com.conzchat.app.ui.auth

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.fragment.app.Fragment
import com.conzchat.app.R
import com.conzchat.app.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    private val rotatingMessages = listOf(
        "Built by ~Conz~ ❤️",
        "v4.0.0 — The ultimate update 🚀",
        "Voice & Video Calls in DMs and Groups 📞",
        "Conz AI is always here to chat 🤖",
        "Public Groups — find your people 🌍",
        "ConzMods — your chat, your rules ⚡",
        "Stories disappear after 24 hours 👻",
        "Report any bugs to @Borg on ConzChat 🔧",
        "Thank you for using ConzChat 🙏"
    )

    private var rotatingIndex = 0
    private val handler = Handler(Looper.getMainLooper())
    private var rotatingRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start everything invisible for entrance animation
        binding.tvAppName.alpha = 0f
        binding.tvAppName.translationY = -40f
        binding.tvRotating.alpha = 0f
        binding.btnSavedAccounts.alpha = 0f
        binding.btnLogin.alpha = 0f
        binding.btnLogin.translationY = 30f
        binding.btnRegister.alpha = 0f
        binding.btnRegister.translationY = 30f

        // Staggered entrance animations
        handler.postDelayed({ animateIn() }, 100)

        binding.btnSavedAccounts.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, SavedAccountsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }

        binding.btnLogin.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }.start()
            handler.postDelayed({
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, LoginFragment())
                    .addToBackStack(null)
                    .commit()
            }, 120)
        }

        binding.btnRegister.setOnClickListener {
            it.animate().scaleX(0.95f).scaleY(0.95f).setDuration(80).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
            }.start()
            handler.postDelayed({
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, RegisterFragment())
                    .addToBackStack(null)
                    .commit()
            }, 120)
        }

        startRotatingText()
    }

    private fun animateIn() {
        if (_binding == null) return

        // Logo slides down and fades in
        binding.tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(OvershootInterpolator(1.2f))
            .start()

        // Rotating text fades in
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            binding.tvRotating.animate().alpha(1f).setDuration(500).start()
        }, 400)

        // Saved accounts button fades in
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            binding.btnSavedAccounts.animate().alpha(1f).setDuration(400).start()
        }, 500)

        // Buttons slide up and fade in
        handler.postDelayed({
            if (_binding == null) return@postDelayed
            binding.btnLogin.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 600)

        handler.postDelayed({
            if (_binding == null) return@postDelayed
            binding.btnRegister.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(500)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 750)
    }

    private fun startRotatingText() {
        if (_binding == null) return
        binding.tvRotating.text = rotatingMessages[0]

        rotatingRunnable = object : Runnable {
            override fun run() {
                if (_binding == null) return
                binding.tvRotating.animate()
                    .alpha(0f)
                    .translationY(-8f)
                    .setDuration(350)
                    .withEndAction {
                        if (_binding == null) return@withEndAction
                        rotatingIndex = (rotatingIndex + 1) % rotatingMessages.size
                        binding.tvRotating.text = rotatingMessages[rotatingIndex]
                        binding.tvRotating.translationY = 8f
                        binding.tvRotating.animate()
                            .alpha(0.8f)
                            .translationY(0f)
                            .setDuration(350)
                            .start()
                    }
                    .start()
                handler.postDelayed(this, 3500)
            }
        }
        handler.postDelayed(rotatingRunnable!!, 3500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rotatingRunnable?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
