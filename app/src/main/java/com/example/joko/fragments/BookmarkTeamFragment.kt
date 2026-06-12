package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.joko.activities.BookmarkViewModel
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.adapters.TeamAdapter
import com.example.joko.databinding.FragmentBookmarkTeamBinding
import com.example.joko.utils.ViewModelFactory

class BookmarkTeamFragment : Fragment() {

    private var _binding: FragmentBookmarkTeamBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarkViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var teamAdapter: TeamAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkTeamBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamAdapter(
            onApplyClick = { team ->
                Toast.makeText(requireContext(), "Mendaftar ke tim ${team.teamName}", Toast.LENGTH_SHORT).show()
            },
            onDetailClick = { team ->
                val intent = Intent(requireContext(), TeamDetailActivity::class.java)
                intent.putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
                startActivity(intent)
            },
            onBookmarkClick = { team ->
                // In Bookmark page, clicking bookmark means remove it
                viewModel.toggleBookmark(team, true)
                Toast.makeText(requireContext(), "Bookmark dihapus", Toast.LENGTH_SHORT).show()
            }
        )

        binding.rvTeamsAnda.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = teamAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.bookmarkedTeams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.submitList(teams)
            binding.tvEmptyState.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
        
        // Ensure adapter knows which IDs are bookmarked for visual consistency
        viewModel.bookmarkedTeams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.setBookmarkedIds(teams.map { it.id }.toSet())
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
