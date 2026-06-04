package com.example.joko.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class CreateTeamActivity : AppCompatActivity() {

    private lateinit var chipGroupStack: ChipGroup
    private lateinit var etAddStack: EditText
    private lateinit var btnAddStack: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_team)

        val btnBack: ImageView = findViewById(R.id.btn_back)
        btnBack.setOnClickListener { finish() }

        val btnCancel: MaterialButton = findViewById(R.id.btn_cancel)
        btnCancel.setOnClickListener { finish() }

        chipGroupStack = findViewById(R.id.chip_group_stack)
        etAddStack = findViewById(R.id.et_add_stack)
        btnAddStack = findViewById(R.id.btn_add_stack)

        btnAddStack.setOnClickListener {
            val tech = etAddStack.text.toString().trim()
            if (tech.isNotEmpty()) {
                addChipToGroup(tech)
                etAddStack.text.clear()
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
