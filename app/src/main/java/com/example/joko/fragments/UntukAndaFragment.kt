package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.joko.R
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.activities.TeamViewModel
import com.example.joko.adapters.TeamAdapter
import com.example.joko.utils.ViewModelFactory

class UntukAndaFragment : Fragment() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var teamAdapter: TeamAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_untuk_anda, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView(view)
        observeViewModel()

        viewModel.loadTeams()
    }

    private fun initViews(view: View) {
        swipeRefresh = view.findViewById(R.id.swipe_refresh)
        progressBar = view.findViewById(R.id.progress_bar)

        swipeRefresh.setOnRefreshListener {
            viewModel.loadTeams()
        }
    }

    private fun setupRecyclerView(view: View) {
        val rvTeams = view.findViewById<RecyclerView>(R.id.rv_teams)
        teamAdapter = TeamAdapter(
            onApplyClick = { team ->
                val intent = Intent(requireContext(), TeamDetailActivity::class.java).apply {
                    putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
                }
                startActivity(intent)
            },
            onDetailClick = { team ->
                val intent = Intent(requireContext(), TeamDetailActivity::class.java).apply {
                    putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
                }
                startActivity(intent)
            }
        )
        rvTeams.adapter = teamAdapter
    }

    private fun observeViewModel() {
        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.submitList(teams)
            swipeRefresh.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!swipeRefresh.isRefreshing) {
                progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearErrorMessage()
                swipeRefresh.isRefreshing = false
            }
        }
    }
}
