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
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.example.joko.activities.TeamDetailActivity

class UntukAndaFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_untuk_anda, container, false)

        val card1: View = view.findViewById(R.id.card_team_1)
        val card2: View = view.findViewById(R.id.card_team_2)
        val card3: View = view.findViewById(R.id.card_team_3)

        // Set click listeners for the cards to go to detail
        val intent = Intent(requireContext(), TeamDetailActivity::class.java)
        card1.setOnClickListener { startActivity(intent) }
        card2.setOnClickListener { startActivity(intent) }
        card3.setOnClickListener { startActivity(intent) }

        // Set click listeners for Apply buttons inside cards
        card1.findViewById<Button>(R.id.btn_apply).setOnClickListener { showApplyDialog() }
        card2.findViewById<Button>(R.id.btn_apply).setOnClickListener { showApplyDialog() }
        card3.findViewById<Button>(R.id.btn_apply).setOnClickListener { showApplyDialog() }

        return view
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
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
