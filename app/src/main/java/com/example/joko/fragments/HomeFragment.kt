package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.joko.MainActivity
import com.example.joko.R
import com.example.joko.activities.CreateEventActivity
import com.example.joko.activities.EventDetailActivity
import com.example.joko.activities.HomeViewModel
import com.example.joko.activities.LoginActivity
import com.example.joko.activities.TeamDetailActivity
import com.example.joko.activities.TeamViewModel
import com.example.joko.adapters.HomeEventAdapter
import com.example.joko.adapters.HomeTeamAdapter
import com.example.joko.utils.SessionManager
import com.example.joko.utils.ViewModelFactory

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private val teamViewModel: TeamViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var eventAdapter: HomeEventAdapter
    private lateinit var teamAdapter: HomeTeamAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI(view)
        setupRecyclerViews(view)
        observeViewModel(view)

        // Initial load
        viewModel.fetchUserData()
        teamViewModel.loadTeams()
    }

    private fun setupUI(view: View) {
        val tvUserName = view.findViewById<TextView>(R.id.tvUserName)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        val btnLogout = view.findViewById<ImageView>(R.id.btnLogout)
        val btnLihatSemuaEvents = view.findViewById<TextView>(R.id.btnLihatSemuaEvents)
        val btnLihatTim = view.findViewById<TextView>(R.id.btnLihatTim)
        val btnAturMinat = view.findViewById<Button>(R.id.btnAturMinat)
        val btnExploreBanner = view.findViewById<Button>(R.id.btnExploreBanner)
        val btnCreateEvent = view.findViewById<Button>(R.id.btnCreateEvent)

        // Set Dynamic Greeting
        val name = viewModel.userName ?: "User"
        tvUserName.text = "Halo, $name"

        // Fungsi Logout
        btnLogout.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            sessionManager.clearSession()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        ivProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
        }

        // Navigasi ke Create Event Activity
        btnCreateEvent.setOnClickListener {
            val intent = Intent(requireContext(), CreateEventActivity::class.java)
            startActivity(intent)
        }

        // Navigasi ke Event Screen (Klik "Lihat Semua" atau "Explore Event")
        val toEventScreen = View.OnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_event)
        }
        btnLihatSemuaEvents.setOnClickListener(toEventScreen)
        btnExploreBanner.setOnClickListener(toEventScreen)

        // Navigasi ke Team Screen (Klik "Lihat Tim")
        btnLihatTim.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_team)
        }

        // Navigasi ke Profile Screen (Klik "Atur Minat")
        btnAturMinat.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
        }
    }

    private fun setupRecyclerViews(view: View) {
        // Events RecyclerView
        val rvHomeEvents = view.findViewById<RecyclerView>(R.id.rvHomeEvents)
        eventAdapter = HomeEventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            startActivity(intent)
        }
        rvHomeEvents.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = eventAdapter
        }

        // Teams RecyclerView
        val rvHomeTeams = view.findViewById<RecyclerView>(R.id.rv_home_teams)
        teamAdapter = HomeTeamAdapter { team ->
            val intent = Intent(requireContext(), TeamDetailActivity::class.java)
            intent.putExtra(TeamDetailActivity.EXTRA_TEAM_ID, team.id)
            startActivity(intent)
        }
        rvHomeTeams.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = teamAdapter
        }
    }

    private fun observeViewModel(view: View) {
        val btnCreateEvent = view.findViewById<Button>(R.id.btnCreateEvent)
        val ivProfile = view.findViewById<ImageView>(R.id.ivProfile)
        
        // Observe Data Events
        viewModel.latestEvents.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
        }

        // Observe Data Teams
        teamViewModel.teams.observe(viewLifecycleOwner) { teams ->
            val filteredTeams = teams.filter { it.currentMembersCount < it.maxCapacity }.take(5)
            teamAdapter.submitList(filteredTeams)
        }

        viewModel.isVerified.observe(viewLifecycleOwner) { isVerified ->
            btnCreateEvent.visibility = if (isVerified) View.VISIBLE else View.GONE
        }

        viewModel.pfpUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(ivProfile)
        }
    }
}
