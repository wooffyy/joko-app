package com.example.joko.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CreateTeamActivity : AppCompatActivity() {

    private val viewModel: TeamViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var etTeamName: EditText
    private lateinit var etContact: EditText
    private lateinit var etMission: EditText
    private lateinit var etSlots: EditText
    private lateinit var etEventName: EditText
    private lateinit var chipGroupStack: ChipGroup
    private lateinit var etAddStack: EditText
    private lateinit var btnAddStack: MaterialButton
    private lateinit var btnCreateTeam: MaterialButton
    private lateinit var btnCancel: MaterialButton

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
        chipGroupStack = findViewById(R.id.chip_group_stack)
        etAddStack = findViewById(R.id.et_add_stack)
        btnAddStack = findViewById(R.id.btn_add_stack)
        btnCreateTeam = findViewById(R.id.btn_create_team)
        btnCancel = findViewById(R.id.btn_cancel)
    }

    private fun setupListeners() {
        findViewById<ImageView>(R.id.btn_back).setOnClickListener { finish() }
        btnCancel.setOnClickListener { finish() }

        btnAddStack.setOnClickListener {
            val tech = etAddStack.text.toString().trim()
            if (tech.isNotEmpty()) {
                if (isDuplicateRole(tech)) {
                    etAddStack.error = "Role sudah ada di daftar"
                } else {
                    addChipToGroup(tech)
                    etAddStack.text.clear()
                    etAddStack.error = null
                }
            }
        }

        btnCreateTeam.setOnClickListener {
            if (validateInput()) {
                val teamName = etTeamName.text.toString().trim()
                val eventName = etEventName.text.toString().trim()
                val slots = etSlots.text.toString().trim().toInt()
                val mission = etMission.text.toString().trim()
                val contact = etContact.text.toString().trim()
                
                val roles = mutableListOf<String>()
                for (i in 0 until chipGroupStack.childCount) {
                    val chip = chipGroupStack.getChildAt(i) as Chip
                    roles.add(chip.text.toString())
                }

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

    private fun isDuplicateRole(role: String): Boolean {
        for (i in 0 until chipGroupStack.childCount) {
            val chip = chipGroupStack.getChildAt(i) as Chip
            if (chip.text.toString().equals(role, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun validateInput(): Boolean {
        val teamName = etTeamName.text.toString().trim()
        val eventName = etEventName.text.toString().trim()
        val slotsStr = etSlots.text.toString().trim()
        val mission = etMission.text.toString().trim()
        val contact = etContact.text.toString().trim()

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
        if (slots > 50) {
            etSlots.error = "Maksimal 50 slot"
            etSlots.requestFocus()
            return false
        }

        if (mission.isNotEmpty() && mission.length < 10) {
            etMission.error = "Misi minimal 10 karakter untuk kejelasan"
            etMission.requestFocus()
            return false
        }

        if (contact.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches() && contact.length < 5) {
            etContact.error = "Kontak tidak valid (gunakan email atau link)"
            etContact.requestFocus()
            return false
        }

        return true
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            btnCreateTeam.isEnabled = !isLoading
            btnCreateTeam.text = if (isLoading) "Creating..." else "Create Team"
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
                viewModel.resetActionSuccess()
                finish()
            }
        }
    }

    private fun addChipToGroup(tech: String) {
        val chip = LayoutInflater.from(this).inflate(R.layout.layout_chip_entry, chipGroupStack, false) as Chip
        chip.text = tech
        chip.setOnCloseIconClickListener {
            chipGroupStack.removeView(chip)
        }
        chipGroupStack.addView(chip)
    }
}
