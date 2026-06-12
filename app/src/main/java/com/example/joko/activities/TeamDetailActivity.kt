package com.example.joko.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R
import com.example.joko.data.remote.response.TeamResponse
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.button.MaterialButton
import android.content.Intent

class TeamDetailActivity : AppCompatActivity() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var tvTeamName: TextView
    private lateinit var tvEventName: TextView
    private lateinit var tvDescription: TextView
    private lateinit var tvRoleNeed: TextView
    private lateinit var tvSlotsDetail: TextView
    private lateinit var tvProgressPercent: TextView
    private lateinit var pbRecruitment: ProgressBar
    private lateinit var badgeVerified: View
    private lateinit var btnApplyNow: MaterialButton
    private lateinit var btnContact: View

    private var currentTeamId: String? = null
    private var currentTeam: TeamResponse? = null
    private var isBookmarked: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_team_detail)

        currentTeamId = intent.getStringExtra(EXTRA_TEAM_ID)
        if (currentTeamId == null) {
            Toast.makeText(this, "Team ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        observeViewModel()

        viewModel.loadTeamById(currentTeamId!!)
        viewModel.loadTeamMembers(currentTeamId!!)
        
        // Observe Bookmark Status from Room
        viewModel.isBookmarked(currentTeamId!!).observe(this) { bookmarked ->
            isBookmarked = bookmarked
            findViewById<ImageView>(R.id.btn_bookmark_top).isSelected = bookmarked
        }
    }

    private fun initViews() {
        tvTeamName = findViewById(R.id.tv_team_name)
        tvEventName = findViewById(R.id.tv_event_name)
        tvDescription = findViewById(R.id.tv_description)
        tvRoleNeed = findViewById(R.id.tv_role_need)
        tvSlotsDetail = findViewById(R.id.tv_slots_detail)
        tvProgressPercent = findViewById(R.id.tv_progress_percent)
        pbRecruitment = findViewById(R.id.pb_recruitment)
        badgeVerified = findViewById(R.id.badge_verified)
        btnApplyNow = findViewById(R.id.btn_apply_now)
        btnContact = findViewById(R.id.btn_contact)

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        findViewById<ImageView>(R.id.btn_bookmark_top).setOnClickListener {
            val team = currentTeam
            if (team != null) {
                viewModel.toggleBookmark(team, isBookmarked)
                val message = if (!isBookmarked) "Tim di-bookmark" else "Bookmark dihapus"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Data tim belum siap", Toast.LENGTH_SHORT).show()
            }
        }

        btnContact.setOnClickListener {
            val contact = viewModel.teamDetail.value?.ownerContact
            if (contact.isNullOrEmpty()) {
                Toast.makeText(this, "Kontak tidak tersedia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = if (contact.startsWith("http")) {
                Intent(Intent.ACTION_VIEW, android.net.Uri.parse(contact))
            } else if (android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()) {
                Intent(Intent.ACTION_SENDTO, android.net.Uri.parse("mailto:$contact"))
            } else {
                null
            }

            if (intent != null) {
                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(this, "Tidak ada aplikasi untuk menangani aksi ini", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Format kontak tidak dikenali", Toast.LENGTH_SHORT).show()
            }
        }

        btnApplyNow.setOnClickListener {
            val status = viewModel.userRoleStatus.value ?: UserRoleStatus.NONE
            if (status == UserRoleStatus.NONE) {
                showApplyConfirmationDialog()
            } else if (status == UserRoleStatus.OWNER) {
                currentTeamId?.let { id ->
                    val intent = Intent(this, ManageApplicantsActivity::class.java).apply {
                        putExtra(ManageApplicantsActivity.EXTRA_TEAM_ID, id)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    private fun showApplyConfirmationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_apply_confirmation, null)
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogView.findViewById<MaterialButton>(R.id.btnConfirm).setOnClickListener {
            currentTeamId?.let { id ->
                viewModel.applyToTeam(id)
            }
            dialog.dismiss()
        }

        dialogView.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        viewModel.teamDetail.observe(this) { team ->
            team?.let {
                currentTeam = it
                tvTeamName.text = it.teamName
                tvEventName.text = it.eventName
                tvDescription.text = it.description ?: "No description available."
                tvRoleNeed.text = it.roleNeed?.joinToString(", ") ?: "Not specified"
                
                val currentMembers = it.currentMembersCount
                val max = it.maxCapacity
                tvSlotsDetail.text = "$currentMembers/$max Slots Filled"
                
                val progress = if (max > 0) (currentMembers.toFloat() / max.toFloat() * 100).toInt() else 0
                tvProgressPercent.text = "$progress%"
                pbRecruitment.progress = progress
                
                updateButtonState(viewModel.userRoleStatus.value ?: UserRoleStatus.NONE, it)

                badgeVerified.visibility = View.GONE
            }
        }

        viewModel.userRoleStatus.observe(this) { status ->
            updateButtonState(status, viewModel.teamDetail.value)
        }

        viewModel.isLoading.observe(this) { isLoading ->
            updateButtonState(viewModel.userRoleStatus.value ?: UserRoleStatus.NONE, viewModel.teamDetail.value)
        }

        viewModel.actionSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Berhasil!", Toast.LENGTH_SHORT).show()
                viewModel.resetActionSuccess()
                currentTeamId?.let { 
                    viewModel.loadTeamById(it)
                    viewModel.loadTeamMembers(it)
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun updateButtonState(status: UserRoleStatus, team: TeamResponse?) {
        val isLoading = viewModel.isLoading.value ?: false
        if (isLoading) {
            btnApplyNow.isEnabled = false
            btnApplyNow.text = "Processing..."
            btnApplyNow.alpha = 1.0f
            return
        }

        when (status) {
            UserRoleStatus.OWNER -> {
                btnApplyNow.isEnabled = true
                btnApplyNow.text = "Manage Team"
                btnApplyNow.alpha = 1.0f
            }
            UserRoleStatus.APPROVED -> {
                btnApplyNow.isEnabled = false
                btnApplyNow.text = "Joined"
                btnApplyNow.alpha = 0.6f
            }
            UserRoleStatus.PENDING -> {
                btnApplyNow.isEnabled = false
                btnApplyNow.text = "Applied"
                btnApplyNow.alpha = 0.6f
            }
            UserRoleStatus.REJECTED -> {
                btnApplyNow.isEnabled = false
                btnApplyNow.text = "Rejected"
                btnApplyNow.alpha = 0.6f
            }
            UserRoleStatus.NONE -> {
                if (viewModel.isTeamFull(team)) {
                    btnApplyNow.isEnabled = false
                    btnApplyNow.text = "Team Full"
                    btnApplyNow.alpha = 0.6f
                } else {
                    btnApplyNow.isEnabled = true
                    btnApplyNow.text = "Apply Now"
                    btnApplyNow.alpha = 1.0f
                }
            }
        }
    }

    companion object {
        const val EXTRA_TEAM_ID = "extra_team_id"
    }
}
