package com.example.joko.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.joko.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar

class CreateEventActivity : AppCompatActivity() {

    private lateinit var chipGroupTags: ChipGroup
    private lateinit var btnAddTag: Chip
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createEventRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        chipGroupTags = findViewById(R.id.chipGroupTags)
        btnAddTag = findViewById(R.id.btnAddTag)
        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        val spinnerKategori = findViewById<Spinner>(R.id.spinnerKategori)
        val btnPublish = findViewById<Button>(R.id.btnPublish)

        // Setup Spinner Kategori
        val categories = arrayOf("Hackathon", "Competition", "Seminar", "Workshop")
        
        // item_spinner.xml (warna font putih) untuk tampilan saat dipilih (di form)
        val adapter = ArrayAdapter(this, R.layout.item_spinner, categories)
        
        // item_spinner_dropdown.xml (warna font hitam) untuk tampilan list pilihan dropdown agar kontras
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)

        spinnerKategori.adapter = adapter

        // Setup Date Pickers
        tvStartDate.setOnClickListener { showDatePicker { date -> tvStartDate.text = date } }
        tvEndDate.setOnClickListener { showDatePicker { date -> tvEndDate.text = date } }

        // Setup Date Pickers
        tvStartDate.setOnClickListener {
            showDatePicker { date ->
                tvStartDate.text = date
                // Ganti warna ke putih setelah tanggal masuk
                tvStartDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        tvEndDate.setOnClickListener {
            showDatePicker { date ->
                tvEndDate.text = date
                // Ganti warna ke putih setelah tanggal masuk
                tvEndDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        // Setup Tag Addition
        btnAddTag.setOnClickListener {
            showAddTagDialog()
        }

        // Setup Publish Button
        btnPublish.setOnClickListener {
            Toast.makeText(this, getString(R.string.event_published), Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, y, m, d ->
            val formattedDate = String.format("%02d/%02d/%04d", m + 1, d, y)
            onDateSelected(formattedDate)
        }, year, month, day).show()
    }

    private fun showAddTagDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tambah Tag")
        
        val input = EditText(this)
        input.setPadding(32, 32, 32, 32)
        builder.setView(input)

        builder.setPositiveButton("Tambah") { _, _ ->
            val tagText = input.text.toString().trim()
            if (tagText.isNotEmpty()) {
                addTagToGroup(tagText)
            }
        }
        builder.setNegativeButton("Batal") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun addTagToGroup(tagText: String) {
        val chip = Chip(this)
        chip.text = "#$tagText"
        chip.isCloseIconVisible = true
        chip.setChipBackgroundColorResource(R.color.primary)
        chip.setTextColor(resources.getColor(R.color.bg_base, null))
        chip.setCloseIconTintResource(R.color.bg_base)
        
        chip.setOnCloseIconClickListener {
            chipGroupTags.removeView(chip)
        }

        // Add before the "Add Tag" button
        val index = chipGroupTags.indexOfChild(btnAddTag)
        chipGroupTags.addView(chip, index)
    }
}
