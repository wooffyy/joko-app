package com.example.joko.activities

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.joko.R

class ManageApplicantsActivity : AppCompatActivity() {

    private lateinit var tabApplicants: LinearLayout
    private lateinit var tabMembers: LinearLayout
    private lateinit var tvTabApplicants: TextView
    private lateinit var tvTabMembers: TextView
    private lateinit var indicatorApplicants: View
    private lateinit var indicatorMembers: View
    private lateinit var containerContent: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_applicants)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        tabApplicants = findViewById(R.id.tab_applicants)
        tabMembers = findViewById(R.id.tab_members)
        tvTabApplicants = findViewById(R.id.tv_tab_applicants)
        tvTabMembers = findViewById(R.id.tv_tab_members)
        indicatorApplicants = findViewById(R.id.indicator_applicants)
        indicatorMembers = findViewById(R.id.indicator_members)
        containerContent = findViewById(R.id.container_content)

        // Default tab
        showApplicants()

        tabApplicants.setOnClickListener { showApplicants() }
        tabMembers.setOnClickListener { showMembers() }
    }

    private fun showApplicants() {
        // UI Update
        tvTabApplicants.setTextColor(ContextCompat.getColor(this, R.color.primary))
        indicatorApplicants.visibility = View.VISIBLE
        tvTabMembers.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        indicatorMembers.visibility = View.INVISIBLE

        containerContent.removeAllViews()
        val inflater = LayoutInflater.from(this)

        // Sample Data for Applicants
        for (i in 1..3) {
            val itemView = inflater.inflate(R.layout.item_applicant_card, containerContent, false)
            itemView.findViewById<TextView>(R.id.tv_name).text = "Pelamar $i"
            
            // Set click listeners for buttons
            itemView.findViewById<Button>(R.id.btn_accept).setOnClickListener {
                // Logic for accepting
            }
            itemView.findViewById<Button>(R.id.btn_reject).setOnClickListener {
                // Logic for rejecting
            }
            
            containerContent.addView(itemView)
        }
    }

    private fun showMembers() {
        // UI Update
        tvTabApplicants.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        indicatorApplicants.visibility = View.INVISIBLE
        tvTabMembers.setTextColor(ContextCompat.getColor(this, R.color.primary))
        indicatorMembers.visibility = View.VISIBLE

        containerContent.removeAllViews()
        val inflater = LayoutInflater.from(this)

        // Owner Item
        val ownerView = inflater.inflate(R.layout.item_member_card, containerContent, false)
        ownerView.findViewById<TextView>(R.id.tv_name).text = "Felix Wong"
        ownerView.findViewById<TextView>(R.id.tv_role).text = "OWNER"
        // Owner doesn't have remove button visible usually
        containerContent.addView(ownerView)

        // Member Item
        val memberView = inflater.inflate(R.layout.item_member_card, containerContent, false)
        memberView.findViewById<TextView>(R.id.tv_name).text = "Sarah Chen"
        memberView.findViewById<TextView>(R.id.tv_role).text = "FRONTEND"
        
        val btnRemove = memberView.findViewById<Button>(R.id.btn_remove)
        btnRemove.visibility = View.VISIBLE
        btnRemove.setOnClickListener {
            // Logic for removing member
        }

        containerContent.addView(memberView)
    }
}
