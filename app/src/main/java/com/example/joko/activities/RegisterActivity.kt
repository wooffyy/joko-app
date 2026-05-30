package com.example.joko.activities

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.joko.R
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.example.joko.fragments.ChipInputFragment

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var skillInputFragment: ChipInputFragment
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.registerRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etName = findViewById<EditText>(R.id.etNama)
        val etUniversity = findViewById<EditText>(R.id.etPilihUniv)
        val etPortfolio = findViewById<EditText>(R.id.etPortfolio)
        val cgInterest = findViewById<ChipGroup>(R.id.cgInterest)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)
        val tvMasuk = findViewById<TextView>(R.id.tvMasuk)

        // Tombol Back
        btnBack.setOnClickListener {
            onBackPressed()
        }

        // Fitur Show/Hide Password
        ivShowPassword.setOnClickListener {
            if (isPasswordVisible) {
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isPasswordVisible = false
            } else {
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isPasswordVisible = true
            }
            etPassword.setSelection(etPassword.text.length)
        }

        skillInputFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerSkills) as ChipInputFragment

        skillInputFragment.setHint("+ Tambah Skill...")

        // Tombol Daftar Sekarang
        btnDaftar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val name = etName.text.toString().trim()
            val university = etUniversity.text.toString().trim().ifEmpty { null }
            val portfolio = etPortfolio.text.toString().trim().ifEmpty { null }

            // Mengambil list interest yang dipilih
            val selectedInterests = cgInterest.checkedChipIds.map { id ->
                findViewById<Chip>(id).text.toString()
            }

            val selectedSkills = skillInputFragment.getTags()
            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty()) {
                if (password.length >= 6) {
                    viewModel.register(
                        email = email,
                        password = password,
                        name = name,
                        university = university,
                        interests = selectedInterests,
                        skills = selectedSkills,
                        portfolioLink = portfolio
                    )
                } else {
                    Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Nama, Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik "Masuk" jika sudah punya akun
        tvMasuk.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { isLoading ->
            findViewById<Button>(R.id.btnDaftar).isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.authSuccess.observe(this) { success ->
            if (success) {
                val email = findViewById<EditText>(R.id.etEmail).text.toString().trim()
                val intent = Intent(this, VerifPendingActivity::class.java).apply {
                    putExtra("USER_EMAIL", email)
                }
                startActivity(intent)
                finish()
            }
        }
    }
}
