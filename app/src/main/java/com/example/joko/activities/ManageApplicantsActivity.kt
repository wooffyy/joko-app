package com.example.joko.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.adapters.TeamMemberAdapter
import com.example.joko.utils.ViewModelFactory

class ManageApplicantsActivity : AppCompatActivity() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var tabApplicants: LinearLayout
    private lateinit var tabMembers: LinearLayout
    private lateinit var tvTabApplicants: TextView
    private lateinit var tvTabMembers: TextView
    private lateinit var indicatorApplicants: View
    private lateinit var indicatorMembers: View
    private lateinit var rvTeamMembers: RecyclerView
    private lateinit var tvEmptyState: TextView
    private lateinit var tvTeamNameHeader: TextView
    private lateinit var pbManage: android.widget.ProgressBar
    private lateinit var svManageApplicants: androidx.core.widget.NestedScrollView

    private var currentTeamId: String? = null
    private var isShowingApplicants = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_applicants)

        currentTeamId = intent.getStringExtra(EXTRA_TEAM_ID)
        if (currentTeamId == null) {
            Toast.makeText(this, "Team ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        observeViewModel()

        // Fetch data real
        viewModel.loadTeamMembers(currentTeamId!!)
        viewModel.loadTeamById(currentTeamId!!)

        tabApplicants.setOnClickListener {
            isShowingApplicants = true
            updateTabUI()
            updateList()
        }
        tabMembers.setOnClickListener {
            isShowingApplicants = false
            updateTabUI()
            updateList()
        }
    }

    private fun initViews() {
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        tabApplicants = findViewById(R.id.tab_applicants)
        tabMembers = findViewById(R.id.tab_members)
        tvTabApplicants = findViewById(R.id.tv_tab_applicants)
        tvTabMembers = findViewById(R.id.tv_tab_members)
        indicatorApplicants = findViewById(R.id.indicator_applicants)
        indicatorMembers = findViewById(R.id.indicator_members)
        rvTeamMembers = findViewById(R.id.rv_team_members)
        tvEmptyState = findViewById(R.id.tv_empty_state)
        tvTeamNameHeader = findViewById(R.id.tv_team_name_header)
        pbManage = findViewById(R.id.pbManage)
        svManageApplicants = findViewById(R.id.svManageApplicants)
    }

    private fun setupRecyclerView() {
        rvTeamMembers.layoutManager = LinearLayoutManager(this)
    }

    private fun observeViewModel() {
        viewModel.teamDetail.observe(this) { team ->
            tvTeamNameHeader.text = team?.teamName ?: "Nama Tim"
        }

        viewModel.teamMembers.observe(this) { members ->
            val total = members.size
            // Sync with DB Contract: PENDING, APPROVED, REJECTED
            val pending = members.count { it.status == "PENDING" }
            val approved = members.count { it.status == "APPROVED" }
            val rejected = members.count { it.status == "REJECTED" }

            Log.d("ManageApplicants", """
                --- Runtime Statistics ---
                Total Members: $total
                PENDING: $pending
                APPROVED: $approved
                REJECTED: $rejected
                Raw Statuses: ${members.map { it.status }}
                ----------------------------
            """.trimIndent())

            tvTabApplicants.text = "${getString(R.string.label_tab_applicants)} ($pending)"
            updateList()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            pbManage.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (!isLoading) {
                svManageApplicants.visibility = View.VISIBLE
            } else {
                svManageApplicants.visibility = View.INVISIBLE
            }
            updateList() // Refresh list to update button states (enabled/disabled)
        }

        viewModel.actionSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Berhasil memperbarui status!", Toast.LENGTH_SHORT).show()
                viewModel.resetActionSuccess()
                // Refresh data pendaftar & detail tim (untuk member count)
                currentTeamId?.let {
                    viewModel.loadTeamMembers(it)
                    viewModel.loadTeamById(it)
                }
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }

    private fun updateTabUI() {
        val soraBold = ResourcesCompat.getFont(this, R.font.sora_bold)
        val soraRegular = ResourcesCompat.getFont(this, R.font.sora_regular)

        if (isShowingApplicants) {
            tvTabApplicants.setTextColor(ContextCompat.getColor(this, R.color.primary))
            tvTabApplicants.typeface = soraBold
            indicatorApplicants.visibility = View.VISIBLE
            tvTabMembers.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            tvTabMembers.typeface = soraRegular
            indicatorMembers.visibility = View.INVISIBLE
        } else {
            tvTabApplicants.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            tvTabApplicants.typeface = soraRegular
            indicatorApplicants.visibility = View.INVISIBLE
            tvTabMembers.setTextColor(ContextCompat.getColor(this, R.color.primary))
            tvTabMembers.typeface = soraBold
            indicatorMembers.visibility = View.VISIBLE
        }
    }

    private fun updateList() {
        val members = viewModel.teamMembers.value ?: emptyList()
        val filteredList = if (isShowingApplicants) {
            members.filter { it.status == "PENDING" }
        } else {
            members.filter { it.status == "APPROVED" }
        }

        val adapter = TeamMemberAdapter(
            isApplicant = isShowingApplicants,
            onAcceptClick = { id -> viewModel.updateMemberStatus(id, "APPROVED") },
            onRejectClick = { id -> viewModel.updateMemberStatus(id, "REJECTED") },
            onProfileClick = { userId ->
                val intent = Intent(this, PublicProfileActivity::class.java).apply {
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            },
            isProcessing = viewModel.isLoading.value ?: false
        )
        rvTeamMembers.adapter = adapter
        adapter.submitList(filteredList)

        if (filteredList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
            tvEmptyState.text = if (isShowingApplicants) {
                getString(R.string.label_no_applicants)
            } else {
                getString(R.string.label_no_members)
            }
            rvTeamMembers.visibility = View.GONE
        } else {
            tvEmptyState.visibility = View.GONE
            rvTeamMembers.visibility = View.VISIBLE
        }
    }

    companion object {
        const val EXTRA_TEAM_ID = "extra_team_id"
    }
}
