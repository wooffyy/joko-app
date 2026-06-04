package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.example.joko.activities.ManageApplicantsActivity

class TimAndaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tim_anda, container, false)

        // Tombol Manage di kartu pertama (Nebula Squad - Active)
        val card1 = view.findViewById<View>(R.id.card_my_team_1)
        val btnManage1 = card1?.findViewById<Button>(R.id.btn_manage)
        
        btnManage1?.setOnClickListener {
            val intent = Intent(requireContext(), ManageApplicantsActivity::class.java)
            startActivity(intent)
        }

        // Tombol Manage di kartu kedua (Cyber Knights - Pending)
        val btnManage2 = view.findViewById<Button>(R.id.btn_manage_2)
        btnManage2?.setOnClickListener {
            val intent = Intent(requireContext(), ManageApplicantsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
