package com.example.joko.activities

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.remote.request.UpdateProfileRequest
import com.example.joko.fragments.ChipInputFragment
import com.example.joko.utils.ViewModelFactory

class EditProfileActivity : AppCompatActivity() {
    private lateinit var btnBack: ImageView
    private lateinit var btnUploadPfp: RelativeLayout
    private lateinit var ivPfpPreview: ImageView
    private lateinit var btnRemoveImage: ImageView
    private lateinit var layoutUploadPlaceholder: View
    private lateinit var etEditUsername: EditText
    private lateinit var etEditUniversity: EditText
    private lateinit var etEditBio: EditText
    private lateinit var fragmentSkills: ChipInputFragment
    private lateinit var etEditPortfolio: EditText
    private lateinit var etEditEmail: EditText
    private lateinit var etEditLinkedin: EditText
    private lateinit var btnPublish: Button

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            showPreview(uri)
            viewModel.processImage(this, uri)
        }
    }

    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        initViews()
        setupListeners()
        observeViewModel()

        viewModel.getUserProfile()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnUploadPfp = findViewById(R.id.btnUploadPfp)
        ivPfpPreview = findViewById(R.id.ivPfpPreview)
        btnRemoveImage = findViewById(R.id.btnRemoveImage)
        layoutUploadPlaceholder = findViewById(R.id.layoutUploadPlaceholder)
        etEditUsername = findViewById(R.id.etEditUsername)
        etEditUniversity = findViewById(R.id.etEditUniversity)
        etEditBio = findViewById(R.id.etEditBio)
        
        fragmentSkills = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerSkills) as ChipInputFragment
        fragmentSkills.setHint("Tambah Skill...")

        etEditPortfolio = findViewById(R.id.etEditPortfolio)
        etEditEmail = findViewById(R.id.etEditEmail)
        etEditLinkedin = findViewById(R.id.etEditLinkedin)
        btnPublish = findViewById(R.id.btnPublish)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnPublish.setOnClickListener {
            saveProfile()
        }

        btnUploadPfp.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        btnRemoveImage.setOnClickListener {
            resetImageSelection()
        }
    }

    private fun showPreview(uri: Any) {
        ivPfpPreview.visibility = View.VISIBLE
        btnRemoveImage.visibility = View.VISIBLE
        layoutUploadPlaceholder.visibility = View.GONE
        
        Glide.with(this)
            .load(uri)
            .centerCrop()
            .into(ivPfpPreview)
    }

    private fun resetImageSelection() {
        viewModel.resetImageByteArray()
        ivPfpPreview.visibility = View.GONE
        btnRemoveImage.visibility = View.GONE
        layoutUploadPlaceholder.visibility = View.VISIBLE
        ivPfpPreview.setImageDrawable(null)
        
        // If there was an original profile pic, we might want to show it again 
        // OR just keep it cleared if the user wants to remove it.
        // For now, let's just clear the selection.
    }

    private fun observeViewModel() {
        viewModel.userProfile.observe(this) { profile ->
            profile?.let {
                etEditUsername.setText(it.name)
                etEditUniversity.setText(it.university)
                etEditBio.setText(it.bio)
                etEditPortfolio.setText(it.portfolioLink)
                etEditEmail.setText(it.email)
                etEditLinkedin.setText(it.linkedin)
                
                it.skills?.let { skills ->
                    fragmentSkills.setTags(skills)
                }
                
                if (!it.pfpUrl.isNullOrEmpty()) {
                    showPreview(it.pfpUrl)
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            btnPublish.isEnabled = !isLoading
            btnPublish.text = if (isLoading) "Menyimpan..." else "Simpan Perubahan"
        }

        viewModel.updateSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfile() {
        val name = etEditUsername.text.toString().trim()
        val university = etEditUniversity.text.toString().trim()
        val bio = etEditBio.text.toString().trim()
        val portfolio = etEditPortfolio.text.toString().trim()
        val email = etEditEmail.text.toString().trim()
        val linkedin = etEditLinkedin.text.toString().trim()
        val skills = fragmentSkills.getTags()

        if (name.isEmpty()) {
            etEditUsername.error = "Nama tidak boleh kosong"
            return
        }

        viewModel.updateProfile(
            name = name,
            university = university,
            bio = bio,
            portfolioLink = portfolio,
            email = email,
            linkedin = linkedin,
            skills = skills
        )
    }
}
