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
import com.example.joko.utils.InputFieldValidator
import com.example.joko.utils.InputFieldValidator.Companion.validateRequiredField

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
        setupValidation()
        observeViewModel()
    }

    private fun setupValidation() {
        InputFieldValidator.setupLiveValidation(etTeamName, "Nama tim wajib diisi")
        InputFieldValidator.setupLiveValidation(etEventName, "Nama event wajib diisi")
        InputFieldValidator.setupLiveValidation(etSlots, "Minimal 2 slot (termasuk Anda)") { input ->
            val slots = input.toIntOrNull()
            slots != null && slots >= 2
        }
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

        val isTeamNameValid = validateRequiredField(teamName, etTeamName, "Nama tim wajib diisi")
        val isEventNameValid = validateRequiredField(eventName, etEventName, "Nama event wajib diisi")
        val isSlotsStrValid = validateRequiredField(slotsStr, etSlots, "Jumlah slot wajib diisi")

        var isSlotsValid = false
        if (isSlotsStrValid) {
            val slots = slotsStr.toIntOrNull()
            val error = when {
                slots == null -> "Format angka tidak valid"
                slots < 2 -> "Minimal 2 slot (termasuk Anda)"
                else -> null
            }
            isSlotsValid = InputFieldValidator.validateField(error != null, etSlots, error ?: "")
        }

        return isTeamNameValid && isEventNameValid && isSlotsStrValid && isSlotsValid
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