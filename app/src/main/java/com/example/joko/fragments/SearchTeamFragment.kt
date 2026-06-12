package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.activities.TeamViewModel
import com.example.joko.adapters.TeamAdapter
import com.example.joko.utils.ViewModelFactory

class SearchTeamFragment : Fragment() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var teamAdapter: TeamAdapter
    private lateinit var rvTeams: RecyclerView
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var etSearch: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_team, container, false)

        // Initialize Views
        rvTeams = view.findViewById(R.id.rv_teams)
        pbLoading = view.findViewById(R.id.pb_loading)
        tvError = view.findViewById(R.id.tv_error)
        etSearch = view.findViewById(R.id.et_search)
        val btnBack: ImageView = view.findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecyclerView()
        setupSearchListener()
        observeViewModel()

        viewModel.loadTeams()

        return view
    }

    private fun setupRecyclerView() {
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
            },
            onBookmarkClick = { team ->
                val isBookmarked = viewModel.bookmarkedTeamIds.value?.contains(team.id) == true
                viewModel.toggleBookmark(team, isBookmarked)
                val message = if (!isBookmarked) "Tim di-bookmark" else "Bookmark dihapus"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        )
        rvTeams.adapter = teamAdapter
    }

    private fun setupSearchListener() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.searchTeams(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun observeViewModel() {
        viewModel.filteredTeams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.submitList(teams)
            tvError.visibility = if (teams.isEmpty() && !etSearch.text.isNullOrBlank()) View.VISIBLE else View.GONE
            if (teams.isEmpty()) tvError.text = "Tidak ada tim ditemukan"
        }

        viewModel.bookmarkedTeamIds.observe(viewLifecycleOwner) { ids ->
            teamAdapter.setBookmarkedIds(ids)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) tvError.visibility = View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (message != null) {
                tvError.text = message
                tvError.visibility = View.VISIBLE
                viewModel.clearErrorMessage()
            }
        }
    }
}
