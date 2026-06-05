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

    private var currentTeamId: String? = null

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

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        btnApplyNow.setOnClickListener {
            val status = viewModel.userRoleStatus.value ?: UserRoleStatus.NONE
            if (status == UserRoleStatus.NONE) {
                currentTeamId?.let { id ->
                    viewModel.applyToTeam(id)
                }
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

    private fun observeViewModel() {
        viewModel.teamDetail.observe(this) { team ->
            team?.let {
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
