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
import com.example.joko.MainActivity
import com.example.joko.R
import com.example.joko.activities.LoginActivity
import com.example.joko.utils.SessionManager

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        val btnLogout = view.findViewById<ImageView>(R.id.btnLogout)
        val btnLihatSemuaEvents = view.findViewById<TextView>(R.id.btnLihatSemuaEvents)
        val btnLihatTim = view.findViewById<TextView>(R.id.btnLihatTim)
        val btnAturMinat = view.findViewById<Button>(R.id.btnAturMinat)
        val btnExploreBanner = view.findViewById<Button>(R.id.btnExploreBanner)

        // Fungsi Logout
        btnLogout.setOnClickListener {
            val sessionManager = SessionManager(requireContext())
            sessionManager.clearSession()
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
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

        return view
    }
}
