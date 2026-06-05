package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.joko.R
import com.example.joko.activities.CreateTeamActivity
import com.example.joko.activities.ManageApplicantsActivity
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.activities.TeamViewModel
import com.example.joko.adapters.MyTeamAdapter
import com.example.joko.adapters.PendingApplicationAdapter
import com.example.joko.databinding.FragmentTimAndaBinding
import com.example.joko.utils.ViewModelFactory

class TimAndaFragment : Fragment() {

    private var _binding: FragmentTimAndaBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var myTeamAdapter: MyTeamAdapter
    private lateinit var pendingAdapter: PendingApplicationAdapter
    private var currentFilter = "created"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimAndaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setupRecyclerView must be called BEFORE initViews because initViews calls refreshList()
        // which uses the adapters initialized in setupRecyclerView.
        setupRecyclerView()
        initViews()
        observeViewModel()

        loadData()
    }

    private fun loadData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadMyTeams()
        viewModel.loadMyApplications()
    }

    private fun initViews() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }

        binding.btnFilterCreated.setOnClickListener { updateFilter("created") }
        binding.btnFilterJoined.setOnClickListener { updateFilter("joined") }
        binding.btnFilterPending.setOnClickListener { updateFilter("pending") }

        binding.fabCreateTeam.setOnClickListener {
            startActivity(Intent(requireContext(), CreateTeamActivity::class.java))
        }
        
        // Initialize default filter state
        updateFilter("created")
    }

    private fun setupRecyclerView() {
        myTeamAdapter = MyTeamAdapter(
            onManageClick = { team ->
                val intent = Intent(requireContext(), ManageApplicantsActivity::class.java).apply {
                    putExtra(ManageApplicantsActivity.EXTRA_TEAM_ID, team.id)
                }
                startActivity(intent)
            }
        )

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

        binding.rvTeamsAnda.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = myTeamAdapter
        }
    }

    private fun updateFilter(filter: String) {
        currentFilter = filter
        
        // UI updates for filter buttons
        val activeBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tag)
        val inactiveBg = ContextCompat.getDrawable(requireContext(), R.drawable.bg_tag_inactive)
        val textColor = ContextCompat.getColor(requireContext(), R.color.white)

        binding.btnFilterCreated.apply {
            background = if (filter == "created") activeBg else inactiveBg
            setTextColor(textColor)
        }
        binding.btnFilterJoined.apply {
            background = if (filter == "joined") activeBg else inactiveBg
            setTextColor(textColor)
        }
        binding.btnFilterPending.apply {
            background = if (filter == "pending") activeBg else inactiveBg
            setTextColor(textColor)
        }

        refreshList()
    }

    private fun observeViewModel() {
        viewModel.myTeams.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = false
            if (currentFilter != "pending") refreshList()
        }

        viewModel.myApplications.observe(viewLifecycleOwner) {
            binding.swipeRefresh.isRefreshing = false
            if (currentFilter == "pending") refreshList()
        }

        viewModel.actionSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Aksi berhasil!", Toast.LENGTH_SHORT).show()
                viewModel.resetActionSuccess()
                loadData()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun refreshList() {
        when (currentFilter) {
            "created", "joined" -> {
                binding.rvTeamsAnda.adapter = myTeamAdapter
                val teams = viewModel.myTeams.value ?: emptyList()
                myTeamAdapter.submitList(teams)
                binding.tvEmptyState.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
            }
            "pending" -> {
                binding.rvTeamsAnda.adapter = pendingAdapter
                val applications = viewModel.myApplications.value ?: emptyList()
                pendingAdapter.submitList(applications)
                binding.tvEmptyState.visibility = if (applications.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
