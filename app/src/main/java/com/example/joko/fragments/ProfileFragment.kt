package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.activities.AuthViewModel
import com.example.joko.activities.EditProfileActivity
import com.example.joko.activities.LoginActivity
import com.example.joko.data.remote.response.ProfileResponse
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ProfileFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private var isContactExpanded = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupContactDropdown(view)
        setupClickListeners(view)
        observeViewModel(view)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getUserProfile()
    }

    private fun setupContactDropdown(view: View) {
        val contactSection = view.findViewById<LinearLayout>(R.id.ContactSection)
        val contactHeader = view.findViewById<LinearLayout>(R.id.ContactHeader)
        val contactContent = view.findViewById<LinearLayout>(R.id.ContactContentContainer)
        val contactArrow = view.findViewById<ImageView>(R.id.ivContactArrow)

        // Set initial state
        contactContent.visibility = View.GONE

        contactHeader.setOnClickListener {
            isContactExpanded = !isContactExpanded
            
            // Professional smooth expansion animation
            TransitionManager.beginDelayedTransition(contactSection)
            
            if (isContactExpanded) {
                contactContent.visibility = View.VISIBLE
                contactArrow.animate().rotation(180f).setDuration(300).start()
            } else {
                contactContent.visibility = View.GONE
                contactArrow.animate().rotation(0f).setDuration(300).start()
            }
        }
    }

    private fun setupClickListeners(view: View) {
        val btnLogout = view.findViewById<CardView>(R.id.btnLogout)
        val btnEditProfile = view.findViewById<CardView>(R.id.btnEditProfile)
        val btnMyBookmark = view.findViewById<CardView>(R.id.btnMyBookmark)

        btnLogout.setOnClickListener {
            viewModel.checkSession() // Double check if actually logged in or session expired
            val sessionManager = com.example.joko.utils.SessionManager(requireContext())
            sessionManager.clearSession()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        btnEditProfile.setOnClickListener {
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        btnMyBookmark.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Bookmark...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel(view: View) {
        viewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            profile?.let { updateUI(view, it) }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You can add a shimmer or progress bar here if needed
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(view: View, profile: ProfileResponse) {
        val ivProfilePicture = view.findViewById<ImageView>(R.id.ivProfilePicture)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvUniversity = view.findViewById<TextView>(R.id.tvUniversity)
        val tvTrustScore = view.findViewById<TextView>(R.id.tvTrustScore)
        val tvBio = view.findViewById<TextView>(R.id.tvBio)
        val cgSkills = view.findViewById<ChipGroup>(R.id.cgSkills)
        val ivVerifiedBadge = view.findViewById<ImageView>(R.id.ivVerifiedBadge)

        // Profile Details
        tvUsername.text = profile.name
        tvUniversity.text = profile.university ?: "Mahasiswa"
        tvTrustScore.text = "⭐ ${profile.trustScore ?: 0.0}"
        tvBio.text = profile.bio ?: "Belum ada bio."
        ivVerifiedBadge.visibility = if (profile.isVerified == true) View.VISIBLE else View.GONE

        // Profile Image
        if (profile.pfpUrl != null) {
            Glide.with(this)
                .load(profile.pfpUrl)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivProfilePicture)
        }

        // Contact Links
        view.findViewById<TextView>(R.id.tvPortfolioLink).text = profile.portfolioLink ?: "Belum diatur"
        view.findViewById<TextView>(R.id.tvEmailLink).text = profile.email ?: "Belum diatur"
        view.findViewById<TextView>(R.id.tvLinkedinLink).text = profile.linkedin ?: "Belum diatur"

        // Skills (Dynamic Chips)
        cgSkills.removeAllViews()
        profile.skills?.forEach { skill ->
            val chip = Chip(requireContext()).apply {
                text = skill
                setChipBackgroundColorResource(R.color.bg_surface)
                setTextColor(resources.getColor(R.color.white, null))
                chipStrokeWidth = 1f
                setChipStrokeColorResource(R.color.outline_variant)
            }
            cgSkills.addView(chip)
        }
    }
}
