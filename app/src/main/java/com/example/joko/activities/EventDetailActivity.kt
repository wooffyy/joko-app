package com.example.joko.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.databinding.ActivityEventDetailBinding
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private val viewModel: EventViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.detailRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val eventId = intent.getStringExtra("EVENT_ID")
        if (eventId != null) {
            viewModel.getEventById(eventId).observe(this) { event ->
                event?.let {
                    setupUI(it)
                    viewModel.incrementClickCount(it.id)
                }
            }
        } else {
            Toast.makeText(this, "Event ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupUI(event: EventEntity) {
        binding.apply {
            tvEventTitle.text = event.title
            tvOrganizerName.text = event.organizer
            tvEventCategory.text = event.category
            tvEventLocation.text = event.location
            tvEventDeadline.text = event.startDate
            tvEventDescription.text = event.description

            Glide.with(this@EventDetailActivity)
                .load(event.imageUrl)
                .placeholder(R.drawable.login_screen_overlay)
                .into(ivEventThumbnail)

            // Setup Requirements
            layoutRequirements.removeAllViews()
            event.requirements?.split(",")?.forEach { req ->
                if (req.isNotBlank()) {
                    val reqView = LayoutInflater.from(this@EventDetailActivity)
                        .inflate(R.layout.item_requirements, layoutRequirements, false)
                    reqView.findViewById<TextView>(R.id.tvRequirementItem).text = req.trim()
                    layoutRequirements.addView(reqView)
                }
            }

            // Setup Tags
            cgEventTags.removeAllViews()
            event.tags?.split(",")?.forEach { tag ->
                if (tag.isNotBlank()) {
                    val chip = Chip(this@EventDetailActivity).apply {
                        text = "#${tag.trim()}"
                        setChipBackgroundColorResource(R.color.bg_elevated)
                        setTextColor(getColor(R.color.white))
                    }
                    cgEventTags.addView(chip)
                }
            }


            val location = event.location
            if (isPhysicalLocation(location)) {
                btnMap.visibility = android.view.View.VISIBLE
                btnMap.setOnClickListener {
                    openGoogleMaps(location!!)
                }
            } else {
                btnMap.visibility = android.view.View.GONE
            }


            btnDaftarSekarang.setOnClickListener {
                val url = event.registrationUrl
                if (!url.isNullOrBlank()) {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse(if (url.startsWith("http")) url else "https://$url")
                    startActivity(intent)
                } else {
                    Toast.makeText(this@EventDetailActivity, "Link pendaftaran tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isPhysicalLocation(location: String?): Boolean {
        if (location.isNullOrBlank()) return false
        val onlineKeywords = listOf("online", "virtual", "zoom", "google meet", "gmeet", "link", "daring", "live")
        return !onlineKeywords.any { location.contains(it, ignoreCase = true) }
    }

    private fun openGoogleMaps(location: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(location)}")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(this, "Aplikasi peta tidak ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}
