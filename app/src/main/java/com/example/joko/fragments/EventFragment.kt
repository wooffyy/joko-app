package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.example.joko.activities.EventDetailActivity

class EventFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_event, container, false)

        // Hubungkan CardView Impact Hackathon
        val cvImpactHackathon = view.findViewById<CardView>(R.id.cvImpactHackathon)
        
        // Klik untuk pindah ke halaman detail
        cvImpactHackathon.setOnClickListener {
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
