package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.activities.CreateTeamActivity
import com.example.joko.activities.ManageApplicantsActivity
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.activities.TeamViewModel
import com.example.joko.adapters.MyTeamAdapter
import com.example.joko.adapters.PendingApplicationAdapter
import com.example.joko.utils.ViewModelFactory

class TimAndaFragment : Fragment() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var myTeamAdapter: MyTeamAdapter
    private lateinit var pendingAdapter: PendingApplicationAdapter
    
    private lateinit var tvEmptyMyTeams: TextView
    private lateinit var tvEmptyApplications: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tim_anda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerViews(view)
        observeViewModel()

        loadData()
    }

    private fun loadData() {
        viewModel.loadMyTeams()
        viewModel.loadMyApplications()
    }

    private fun initViews(view: View) {
        tvEmptyMyTeams = view.findViewById(R.id.tv_empty_my_teams)
        tvEmptyApplications = view.findViewById(R.id.tv_empty_applications)

        // Tombol Buat Tim di Header
        view.findViewById<View>(R.id.btn_create_team_header).setOnClickListener {
            startActivity(Intent(requireContext(), CreateTeamActivity::class.java))
        }

        // Card Create New Team di bagian bawah
        view.findViewById<View>(R.id.btn_create_team_bottom).setOnClickListener {
            startActivity(Intent(requireContext(), CreateTeamActivity::class.java))
        }
    }

    private fun setupRecyclerViews(view: View) {
        // My Teams RV
        val rvMyTeams = view.findViewById<RecyclerView>(R.id.rv_my_teams)
        myTeamAdapter = MyTeamAdapter(
            onManageClick = { team ->
                val intent = Intent(requireContext(), ManageApplicantsActivity::class.java).apply {
                    putExtra(ManageApplicantsActivity.EXTRA_TEAM_ID, team.id)
                }
                startActivity(intent)
            }
        )
        rvMyTeams.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myTeamAdapter
        }

        // Pending Applications RV
        val rvPending = view.findViewById<RecyclerView>(R.id.rv_pending_applications)
        pendingAdapter = PendingApplicationAdapter(
            onViewDetailClick = { application ->
                val intent = Intent(requireContext(), TeamDetailActivity::class.java).apply {
                    putExtra(TeamDetailActivity.EXTRA_TEAM_ID, application.teamId)
                }
                startActivity(intent)
            },
            onCancelApplyClick = { application ->
                viewModel.cancelApplication(application.id)
            }
        )
        rvPending.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pendingAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.myTeams.observe(viewLifecycleOwner) { teams ->
            myTeamAdapter.submitList(teams)
            tvEmptyMyTeams.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.myApplications.observe(viewLifecycleOwner) { applications ->
            pendingAdapter.submitList(applications)
            tvEmptyApplications.visibility = if (applications.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.actionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Aksi berhasil!", Toast.LENGTH_SHORT).show()
                viewModel.resetActionSuccess()
                loadData() // Refresh both sections
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
            }
        }
    }
}
