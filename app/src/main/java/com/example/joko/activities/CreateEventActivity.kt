package com.example.joko.activities

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.utils.ViewModelFactory
import java.util.Calendar
import com.example.joko.fragments.ChipInputFragment
import com.example.joko.utils.InputFieldValidator
import com.example.joko.utils.InputFieldValidator.Companion.validateRequiredField
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateEventActivity : AppCompatActivity() {

    private lateinit var fragmentTags: ChipInputFragment
    private lateinit var layoutRequirementsContainer: android.widget.LinearLayout
    private lateinit var etRequirementInput: EditText
    private lateinit var btnAddRequirement: ImageView
    
    private lateinit var tvStartDate: TextView
    private lateinit var tvEndDate: TextView
    private lateinit var etJudulEvent: EditText
    private lateinit var etPenyelenggara: EditText
    private lateinit var etLokasi: EditText
    private lateinit var etLinkPendaftaran: EditText
    private lateinit var etDeskripsiEvent: EditText
    private lateinit var spinnerKategori: Spinner
    private lateinit var btnPublish: Button

    // Image Upload UX: Views for Preview & Control
    private lateinit var ivEventBannerPreview: ImageView
    private lateinit var layoutPreviewContainer: View
    private lateinit var layoutUploadPlaceholder: View
    private lateinit var btnRemoveImage: ImageView
    private var selectedImageUri: Uri? = null

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Image Upload Launcher
    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showPreview(uri)
            viewModel.processImage(this, uri)
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

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

        fragmentTags = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerTags) as ChipInputFragment

        fragmentTags.setHint("Tambah Tag...")

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

        // Image Upload UX: Inisialisasi View Control
        ivEventBannerPreview = findViewById(R.id.ivEventBannerPreview)
        layoutPreviewContainer = findViewById(R.id.layoutPreviewContainer)
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)

        val categories = arrayOf("Hackathon", "Competition", "Seminar", "Workshop")
        val adapter = ArrayAdapter(this, R.layout.item_spinner, categories)
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown)
        spinnerKategori.adapter = adapter
    }

    private fun setupListeners() {
        val layoutStartDate = findViewById<View>(R.id.layoutStartDateContainer)
        val layoutEndDate = findViewById<View>(R.id.layoutEndDateContainer)

        layoutStartDate.setOnClickListener {
            showDatePicker { date ->
                tvStartDate.text = date
                tvStartDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        layoutEndDate.setOnClickListener {
            showDatePicker { date ->
                tvEndDate.text = date
                tvEndDate.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            }
        }

        // Trigger Photo Picker
        findViewById<View>(R.id.btnUploadBanner).setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        // Logic Reset Image (Tombol X)
        btnRemoveImage.setOnClickListener {
            resetImageSelection()
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

    private fun showPreview(uri: Uri) {
        layoutPreviewContainer.visibility = View.VISIBLE
        layoutUploadPlaceholder.visibility = View.GONE
        
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(ivEventBannerPreview)
    }

    private fun resetImageSelection() {
        selectedImageUri = null
        viewModel.resetImageByteArray()
        layoutPreviewContainer.visibility = View.GONE
        layoutUploadPlaceholder.visibility = View.VISIBLE
        ivEventBannerPreview.setImageDrawable(null)
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
        
        val tagsList = if (::fragmentTags.isInitialized && fragmentTags.isAdded) {
            fragmentTags.getTags()
        } else {
            emptyList()
        }

        val requirementsList = mutableListOf<String>()
        for (i in 0 until layoutRequirementsContainer.childCount) {
            val itemView = layoutRequirementsContainer.getChildAt(i)
            val tvText = itemView.findViewById<TextView>(R.id.tvRequirementText)
            requirementsList.add(tvText.text.toString())
        }

        val layoutStartDate = findViewById<View>(R.id.layoutStartDateContainer)
        val layoutEndDate = findViewById<View>(R.id.layoutEndDateContainer)

        val isTitleValid = validateRequiredField(title, etJudulEvent, "Judul event tidak boleh kosong")
        val isDescValid = validateRequiredField(description, etDeskripsiEvent, "Deskripsi event wajib diisi")
        val isOrganizerValid = validateRequiredField(organizer, etPenyelenggara, "Nama penyelenggara harus diisi")
        val isLocationValid = validateRequiredField(location, etLokasi, "Lokasi event tidak boleh kosong")

        val startDateValue = try { LocalDate.parse(startDate, formatter) } catch (e: Exception) { null }
        val endDateValue = try { LocalDate.parse(endDate, formatter) } catch (e: Exception) { null }

        val endDateError = when {
            endDate.isEmpty() || endDate == "mm/dd/yyyy" -> "Pilih tanggal selesai"
            endDateValue != null && endDateValue.isBefore(LocalDate.now()) -> "Tanggal selesai invalid"
            startDateValue != null && endDateValue != null && endDateValue.isBefore(startDateValue) -> "Tanggal invalid"
            else -> null
        }
        val isEndDateValid = InputFieldValidator.validateField(endDateError != null, layoutEndDate, endDateError ?: "")

        val regUrlError = when {
            regUrl.isEmpty() -> "Link pendaftaran wajib diisi"
            !android.util.Patterns.WEB_URL.matcher(regUrl).matches() -> "Format link tidak valid (gunakan http/https)"
            else -> null
        }
        val isRegUrlValid = InputFieldValidator.validateField(regUrlError != null, etLinkPendaftaran, regUrlError ?: "")

        // Cek hasil akhir validasi, kalo lolos validasi kirim ke viewModel
        if (isTitleValid && isDescValid && isOrganizerValid && isLocationValid && isEndDateValid && isRegUrlValid) {
            viewModel.publishEvent(
                title = title,
                category = category,
                location = location,
                startDate = startDate,
                endDate = endDate,
                description = description,
                organizer = organizer,
                registrationUrl = regUrl,
                requirements = requirementsList,
                tags = tagsList
            )
        } else {
            Toast.makeText(this, "Mohon lengkapi data wajib", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addRequirementItem(text: String) {
        val itemView = android.view.LayoutInflater.from(this).inflate(R.layout.item_input_requirement, layoutRequirementsContainer, false)
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
            
            // Disable reset button saat sedang loading/processing
            btnRemoveImage.isEnabled = !isLoading
            btnRemoveImage.alpha = if (isLoading) 0.5f else 1.0f
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
