package com.example.joko.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.remote.response.ProfileResponse
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class PublicProfileActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(this)
    }

    private var isContactExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_profile)

        val root = findViewById<View>(R.id.pbProfile).parent as View
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userId = intent.getStringExtra("USER_ID")
        if (userId == null) {
            Toast.makeText(this, "ID Pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        observeViewModel()
        
        // Use a custom fetch method if needed, but AuthViewModel's getUserProfile 
        // currently uses getUserId() internally. Let's check AuthViewModel.
        viewModel.getUserProfileById(userId)
    }

    private fun setupUI() {
        // Show Back Button
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.visibility = View.VISIBLE
        btnBack.setOnClickListener { finish() }

        // Hide Self-Action Buttons
        findViewById<View>(R.id.btnEditProfile).visibility = View.GONE
        findViewById<View>(R.id.btnMyBookmark).visibility = View.GONE
        findViewById<View>(R.id.btnAskVerification).visibility = View.GONE
        findViewById<View>(R.id.btnLogout).visibility = View.GONE
        
        // Note: btnAddSkill should also be hidden for public profiles
        findViewById<View>(R.id.btnAddSkill).visibility = View.GONE

        setupContactDropdown()
    }

    private fun setupContactDropdown() {
        val contactSection = findViewById<LinearLayout>(R.id.ContactSection)
        val contactHeader = findViewById<LinearLayout>(R.id.ContactHeader)
        val contactContent = findViewById<LinearLayout>(R.id.ContactContentContainer)
        val contactArrow = findViewById<ImageView>(R.id.ivContactArrow)

        contactContent.visibility = View.GONE

        contactHeader.setOnClickListener {
            isContactExpanded = !isContactExpanded
            val transition = AutoTransition().apply { duration = 400 }
            TransitionManager.beginDelayedTransition(contactSection.parent as ViewGroup, transition)
            
            if (isContactExpanded) {
                contactContent.visibility = View.VISIBLE
                contactArrow.animate().rotation(180f).setDuration(400).start()
            } else {
                contactContent.visibility = View.GONE
                contactArrow.animate().rotation(0f).setDuration(400).start()
            }
        }
    }

    private fun observeViewModel() {
        val progressBar = findViewById<ProgressBar>(R.id.pbProfile)
        val scrollView = findViewById<ScrollView>(R.id.svProfile)

        viewModel.otherUserProfile.observe(this) { profile ->
            profile?.let { 
                updateUI(it)
                scrollView.visibility = View.VISIBLE
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) scrollView.visibility = View.INVISIBLE
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(profile: ProfileResponse) {
        val ivProfilePicture = findViewById<ImageView>(R.id.ivProfilePicture)
        val tvUsername = findViewById<TextView>(R.id.tvUsername)
        val tvUniversity = findViewById<TextView>(R.id.tvUniversity)
        val tvBio = findViewById<TextView>(R.id.tvBio)
        val cgSkills = findViewById<ChipGroup>(R.id.cgSkills)
        val ivVerifiedBadge = findViewById<ImageView>(R.id.ivVerifiedBadge)

        tvUsername.text = profile.name
        tvUniversity.text = profile.university ?: "Mahasiswa"
        tvBio.text = profile.bio ?: "Belum ada bio."
        ivVerifiedBadge.visibility = if (profile.isVerified == true) View.VISIBLE else View.GONE

        if (profile.pfpUrl != null) {
            Glide.with(this)
                .load(profile.pfpUrl)
                .placeholder(R.drawable.default_avatar)
                .circleCrop()
                .into(ivProfilePicture)
        }

        // Contact setup (Matching your fragment implementation)
        findViewById<TextView>(R.id.tvPortfolioLink).text = profile.portfolioLink ?: "Belum diatur"
        findViewById<TextView>(R.id.tvEmailLink).text = profile.email ?: "Belum diatur"
        findViewById<TextView>(R.id.tvLinkedinLink).text = profile.linkedin ?: "Belum diatur"

        findViewById<View>(R.id.PortfolioSection).setOnClickListener {
            profile.portfolioLink?.let { link ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (link.startsWith("http")) link else "https://$link")
                }
                startActivity(intent)
            }
        }

        findViewById<View>(R.id.EmailSection).setOnClickListener {
            profile.email?.let { email ->
                val intent = Intent(Intent.ACTION_SENDTO).apply { data = Uri.parse("mailto:$email") }
                startActivity(Intent.createChooser(intent, "Kirim Email"))
            }
        }

        findViewById<View>(R.id.LinkedinSection).setOnClickListener {
            profile.linkedin?.let { link ->
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (link.startsWith("http")) link else "https://$link")
                }
                startActivity(intent)
            }
        }

        cgSkills.removeAllViews()
        profile.skills?.forEach { skill ->
            val chip = Chip(this).apply {
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
