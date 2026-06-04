package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.example.joko.activities.TeamDetailActivity

class SearchTeamFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_team, container, false)

        val btnBack: ImageView = view.findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Set click listeners for cards
        val card1: View = view.findViewById(R.id.card_team_1)
        val card2: View = view.findViewById(R.id.card_team_2)
        val card3: View = view.findViewById(R.id.card_team_3)

        val intent = Intent(requireContext(), TeamDetailActivity::class.java)

        card1.setOnClickListener { startActivity(intent) }
        card2.setOnClickListener { startActivity(intent) }
        card3.setOnClickListener { startActivity(intent) }

        return view
    }
}
