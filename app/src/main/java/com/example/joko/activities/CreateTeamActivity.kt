package com.example.joko.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R
import com.example.joko.fragments.ChipInputFragment
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.button.MaterialButton

class CreateTeamActivity : AppCompatActivity() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var etTeamName: EditText
    private lateinit var etContact: EditText
    private lateinit var etMission: EditText
    private lateinit var etSlots: EditText
    private lateinit var etEventName: EditText
    private lateinit var btnCreateTeam: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        etTeamName = findViewById(R.id.et_team_name)
        etContact = findViewById(R.id.et_contact)
        etMission = findViewById(R.id.et_mission)
        etSlots = findViewById(R.id.et_slots)
        etEventName = findViewById(R.id.et_event_name)
        btnCreateTeam = findViewById(R.id.btn_create_team)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }

        btnCreateTeam.setOnClickListener {
            if (validateInput()) {
                val teamName = etTeamName.text.toString().trim()
                val eventName = etEventName.text.toString().trim()
                // Gunakan toIntOrNull untuk keamanan ekstra
                val slots = etSlots.text.toString().trim().toIntOrNull() ?: 2
                val mission = etMission.text.toString().trim()
                val contact = etContact.text.toString().trim()

                // Perbaikan: Ganti getSelectedTags() menjadi getTags() sesuai ChipInputFragment.kt
                val fragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerStack) as? ChipInputFragment
                val roles = fragment?.getTags() ?: emptyList()

                viewModel.createTeam(
                    name = teamName,
                    eventName = eventName,
                    maxCapacity = slots,
                    description = mission.ifBlank { null },
                    roleNeed = if (roles.isEmpty()) null else roles,
                    ownerContact = contact.ifBlank { null }
                )
            }
        }
    }

    private fun validateInput(): Boolean {
        val teamName = etTeamName.text.toString().trim()
        val eventName = etEventName.text.toString().trim()
        val slotsStr = etSlots.text.toString().trim()

        if (teamName.isEmpty()) {
            etTeamName.error = "Nama tim wajib diisi"
            etTeamName.requestFocus()
            return false
        }
        if (eventName.isEmpty()) {
            etEventName.error = "Nama event wajib diisi"
            etEventName.requestFocus()
            return false
        }
        if (slotsStr.isEmpty()) {
            etSlots.error = "Jumlah slot wajib diisi"
            etSlots.requestFocus()
            return false
        }

        val slots = slotsStr.toIntOrNull()
        if (slots == null) {
            etSlots.error = "Format angka tidak valid"
            etSlots.requestFocus()
            return false
        }
        if (slots < 2) {
            etSlots.error = "Minimal 2 slot (termasuk Anda)"
            etSlots.requestFocus()
            return false
        }

        return true
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            btnCreateTeam.isEnabled = !isLoading
            btnCreateTeam.text = if (isLoading) "Memproses..." else "Buat Tim"
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.clearErrorMessage()
            }
        }

        viewModel.actionSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Tim berhasil dibuat!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}