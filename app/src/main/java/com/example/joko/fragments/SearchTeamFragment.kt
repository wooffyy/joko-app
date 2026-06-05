package com.example.joko.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_team, container, false)

        // Initialize Views
        rvTeams = view.findViewById(R.id.rv_teams)
        pbLoading = view.findViewById(R.id.pb_loading)
        tvError = view.findViewById(R.id.tv_error)
        val btnBack: ImageView = view.findViewById(R.id.btn_back)

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        setupRecyclerView()
        observeViewModel()

        viewModel.loadTeams()

        return view
    }

    private fun setupRecyclerView() {
        teamAdapter = TeamAdapter(
            onApplyClick = { team ->
                showApplyDialog()
            },
            onDetailClick = { team ->
                val intent = Intent(requireContext(), TeamDetailActivity::class.java)
                // In a real scenario, we would pass team.id
                startActivity(intent)
            }
        )
        rvTeams.adapter = teamAdapter
    }

    private fun observeViewModel() {
        viewModel.teams.observe(viewLifecycleOwner) { teams ->
            teamAdapter.submitList(teams)
            tvError.visibility = if (teams.isEmpty()) View.VISIBLE else View.GONE
            if (teams.isEmpty()) tvError.text = "Tidak ada tim ditemukan"
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

    private fun showApplyDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_apply_confirmation)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnConfirm = dialog.findViewById<Button>(R.id.btn_confirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btn_cancel)

        btnConfirm.setOnClickListener {
            // Logic for apply will be implemented in next step
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
