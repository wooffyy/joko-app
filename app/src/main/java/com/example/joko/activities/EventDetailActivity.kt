package com.example.joko.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioGroup
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
import com.example.joko.utils.ReportType
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.radiobutton.MaterialRadioButton

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private val viewModel: EventViewModel by viewModels {
        ViewModelFactory(this)
    }
    private var currentEvent: EventEntity? = null
    private var isBookmarked: Boolean = false
    private var reportDialog: android.app.AlertDialog? = null

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
            // Observe Event Data
            viewModel.getEventById(eventId).observe(this) { event ->
                event?.let {
                    currentEvent = it
                    setupUI(it)
                }
            }

            // Observe Bookmark Status
            viewModel.isBookmarked(eventId).observe(this) { bookmarked ->
                isBookmarked = bookmarked
                binding.btnBookmark.isSelected = bookmarked
            }
        } else {
            Toast.makeText(this, "Event ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }

        binding.btnBack.setOnClickListener { finish() }

        binding.btnBookmark.setOnClickListener {
            val event = currentEvent
            if (event != null) {
                viewModel.toggleBookmark(event, isBookmarked)
                if (!isBookmarked) {
                    Toast.makeText(this, "Event di-bookmark", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Data event belum siap", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnReport.setOnClickListener {
            showReportDialog()
            viewModel.reportSuccess.observe(this) { success ->
                if (success) {
                    Toast.makeText(this, "Laporan berhasil dikirim. Terima kasih!", Toast.LENGTH_LONG).show()
                    reportDialog?.dismiss()
                    reportDialog = null
                }
            }

            viewModel.errorMessage.observe(this) { message ->
                if (message != null) {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showReportDialog() {
        val reportDialog = layoutInflater.inflate(R.layout.dialog_report, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(reportDialog)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val rgCategories = reportDialog.findViewById<RadioGroup>(R.id.rgReportCategories)
        val btnSubmit = reportDialog.findViewById<MaterialButton>(R.id.btnSubmitReport)
        val btnCancel = reportDialog.findViewById<MaterialButton>(R.id.btnCancel)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSubmit.setOnClickListener {
            val selected =rgCategories.checkedRadioButtonId
            if (selected == -1) {
                Toast.makeText(this, "Silakan pilih kategori terlebih dahulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val category = when (selected) {
                R.id.rbSpam -> ReportType.SPAM
                R.id.rbScam -> ReportType.SCAM
                R.id.rbHoax -> ReportType.HOAX
                R.id.rbInappropriate -> ReportType.INAPPROPRIATE_CONTENT
                R.id.rbHarassment -> ReportType.HARASSMENT
                R.id.rbIllegal -> ReportType.ILLEGAL_ACTIVITY
                else -> null
            }

            if (category != null && currentEvent != null) {
                viewModel.sendReport(currentEvent!!, category)
                this.reportDialog = dialog
            }
        }

        dialog.show()
    }

    private fun setupUI(event: EventEntity) {
        binding.apply {
            tvEventTitle.text = event.title
            tvOrganizerName.text = event.organizer
            tvEventCategory.text = event.category
            tvEventLocation.text = event.location
            tvEventDeadline.text = event.startDate
            tvEventDescription.text = event.description

            // Setup Badge Verifikasi
            icVerifiedOrganizer.visibility = if (event.isVerified) android.view.View.VISIBLE else android.view.View.GONE

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
