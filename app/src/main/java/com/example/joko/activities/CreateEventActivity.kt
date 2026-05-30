package com.example.joko.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.joko.R
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.Calendar
import com.example.joko.fragments.ChipInputFragment

class CreateEventActivity : AppCompatActivity() {

    private lateinit var chipInputFragment: ChipInputFragment
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var etJudulEvent: EditText
    private lateinit var etPenyelenggara: EditText
    private lateinit var etLokasi: EditText
    private lateinit var etLinkPendaftaran: EditText
    private lateinit var etDeskripsiEvent: EditText
    private lateinit var layoutRequirementsContainer: android.widget.LinearLayout
    private lateinit var etRequirementInput: EditText
    private lateinit var btnAddRequirement: ImageView
    private lateinit var spinnerKategori: Spinner
    private lateinit var btnPublish: Button

    private val viewModel: EventViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.createEventRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupListeners()
        observeViewModel()
    }

    private fun initViews() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }

        // Inisialisasi Fragment melalui SupportFragmentManager
        chipInputFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerTags) as ChipInputFragment

        tvStartDate = findViewById(R.id.tvStartDate)
        tvEndDate = findViewById(R.id.tvEndDate)
        etJudulEvent = findViewById(R.id.etJudulEvent)
        etPenyelenggara = findViewById(R.id.etPenyelenggara)
        etLokasi = findViewById(R.id.etLokasi)
        etLinkPendaftaran = findViewById(R.id.etLinkPendaftaran)
        etDeskripsiEvent = findViewById(R.id.etDeskripsiEvent)
        layoutRequirementsContainer = findViewById(R.id.layoutRequirementsContainer)
        etRequirementInput = findViewById(R.id.etRequirementInput)
        btnAddRequirement = findViewById(R.id.btnAddRequirement)
        spinnerKategori = findViewById(R.id.spinnerKategori)
        btnPublish = findViewById(R.id.btnPublish)

        val categories = arrayOf("Hackathon", "Competition", "Seminar", "Workshop")
        val adapter = ArrayAdapter(this, R.layout.item_spinner, categories)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinnerKategori.adapter = adapter
    }

    private fun setupListeners() {
        tvStartDate.setOnClickListener {
            showDatePicker { date ->
                tvStartDate.text = date
                tvStartDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        tvEndDate.setOnClickListener {
            showDatePicker { date ->
                tvEndDate.text = date
                tvEndDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        btnAddRequirement.setOnClickListener {
            val reqText = etRequirementInput.text.toString().trim()
            if (reqText.isNotEmpty()) {
                addRequirementItem(reqText)
                etRequirementInput.text.clear()
            }
        }

        btnPublish.setOnClickListener {
            validateAndPublish()
        }
    }

    private fun validateAndPublish() {
        val title = etJudulEvent.text.toString().trim()
        val organizer = etPenyelenggara.text.toString().trim()
        val location = etLokasi.text.toString().trim()
        val category = spinnerKategori.selectedItem.toString()
        val description = etDeskripsiEvent.text.toString().trim()
        val startDate = tvStartDate.text.toString()
        val endDate = tvEndDate.text.toString()
        val regUrl = etLinkPendaftaran.text.toString().trim()
        val tagsList = chipInputFragment.getTags()
        
        val requirementsList = mutableListOf<String>()
        for (i in 0 until layoutRequirementsContainer.childCount) {
            val itemView = layoutRequirementsContainer.getChildAt(i)
            val tvText = itemView.findViewById<TextView>(R.id.tvRequirementText)
            requirementsList.add(tvText.text.toString())
        }

        if (title.isEmpty() || organizer.isEmpty() || location.isEmpty() ||
            description.isEmpty() || startDate == "mm/dd/yyyy" || endDate == "mm/dd/yyyy") {
            Toast.makeText(this, "Mohon lengkapi data wajib", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.publishEvent(
            title = title,
            category = category,
            location = location,
            startDate = startDate,
            endDate = endDate,
            description = description,
            organizer = organizer,
            imageUrl = "https://via.placeholder.com/180",
            registrationUrl = if (regUrl.isEmpty()) null else regUrl,
            requirements = requirementsList,
            tags = if (tagsList.isEmpty()) null else tagsList
        )
    }

    private fun addRequirementItem(text: String) {
        val itemView = LayoutInflater.from(this).inflate(R.layout.item_input_requirement, layoutRequirementsContainer, false)
        val tvText = itemView.findViewById<TextView>(R.id.tvRequirementText)
        val btnRemove = itemView.findViewById<ImageView>(R.id.btnRemoveRequirement)

        tvText.text = text
        btnRemove.setOnClickListener {
            layoutRequirementsContainer.removeView(itemView)
        }

        layoutRequirementsContainer.addView(itemView)
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            btnPublish.isEnabled = !isLoading
            btnPublish.text = if (isLoading) "Memproses..." else "Publikasikan"
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.publishSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Event berhasil dipublikasikan!", Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val formattedDate = String.format("%04d-%02d-%02d", y, m + 1, d)
            onDateSelected(formattedDate)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
