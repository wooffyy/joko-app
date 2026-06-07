package com.example.joko.activities

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
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
import com.example.joko.utils.InputFieldValidator

class RegisterActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private lateinit var skillInputFragment: ChipInputFragment
    private val viewModel: AuthViewModel by viewModels {
        ViewModelFactory(this)
    }

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etName: EditText
    private lateinit var layoutPassword: android.widget.LinearLayout
    private lateinit var tvErrorNama: TextView
    private lateinit var tvErrorEmail: TextView
    private lateinit var tvErrorPassword: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerRoot = findViewById<ScrollView>(R.id.registerRoot)
        
        // Keyboard-aware scrolling logic using WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(registerRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val isImeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            
            // Adjust padding to accommodate system bars and keyboard
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                if (isImeVisible) ime.bottom else systemBars.bottom
            )
            
            // Scroll to focused view when keyboard opens
            if (isImeVisible) {
                currentFocus?.let { focused ->
                    v.post {
                        val rect = Rect()
                        focused.getGlobalVisibleRect(rect)
                        val screenHeight = v.rootView.height
                        val visibleBottom = screenHeight - ime.bottom
                        
                        // If focused input is covered by keyboard, scroll up
                        if (rect.bottom > visibleBottom) {
                            val scrollAmount = rect.bottom - visibleBottom + 100
                            registerRoot.smoothScrollBy(0, scrollAmount)
                        }
                    }
                }
            }
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etName = findViewById(R.id.etNama)
        val etUniversity = findViewById<EditText>(R.id.etPilihUniv)
        val etPortfolio = findViewById<EditText>(R.id.etPortfolio)
        val cgInterest = findViewById<ChipGroup>(R.id.cgInterest)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)
        val btnDaftar = findViewById<Button>(R.id.btnDaftar)
        val tvMasuk = findViewById<TextView>(R.id.tvMasuk)
        
        tvErrorNama = findViewById(R.id.tvErrorNama)
        tvErrorEmail = findViewById(R.id.tvErrorEmail)
        tvErrorPassword = findViewById(R.id.tvErrorPassword)
        layoutPassword = findViewById(R.id.layoutPassword)

        setupValidation()

        // Additional focus listeners for bottom fields to ensure visibility
        val bottomFields = listOf(etUniversity, etPortfolio)
        bottomFields.forEach { et ->
            et.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    registerRoot.postDelayed({
                        val imeInsets = ViewCompat.getRootWindowInsets(registerRoot)?.getInsets(WindowInsetsCompat.Type.ime())
                        if (imeInsets != null && imeInsets.bottom > 0) {
                            val rect = Rect()
                            v.getGlobalVisibleRect(rect)
                            val screenHeight = registerRoot.rootView.height
                            val visibleBottom = screenHeight - imeInsets.bottom
                            if (rect.bottom > visibleBottom) {
                                registerRoot.smoothScrollBy(0, rect.bottom - visibleBottom + 100)
                            }
                        }
                    }, 200)
                }
            }
        }

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

            // Validasi
            val isNameValid = InputFieldValidator.validateRequiredField(name, etName, "Nama tidak boleh kosong", errorView = tvErrorNama)
            val isEmailValid = InputFieldValidator.validateRequiredField(email, etEmail, "Email tidak boleh kosong", errorView = tvErrorEmail)
            
            val passwordError = when {
                password.isEmpty() -> "Password tidak boleh kosong"
                password.length < 8 -> "Password minimal 8 karakter"
                else -> null
            }
            val isPasswordValid = InputFieldValidator.validateField(passwordError != null, layoutPassword, passwordError ?: "", errorView = tvErrorPassword)

            if (isNameValid && isEmailValid && isPasswordValid) {
                // Mengambil list interest yang dipilih
                val selectedInterests = cgInterest.checkedChipIds.map { id ->
                    findViewById<Chip>(id).text.toString()
                }

                val selectedSkills = skillInputFragment.getTags()
                
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
                Toast.makeText(this, "Mohon lengkapi data dengan benar", Toast.LENGTH_SHORT).show()
            }
        }

        // Klik "Masuk" jika sudah punya akun
        tvMasuk.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }

        observeViewModel()
    }

    private fun setupValidation() {
        InputFieldValidator.setupLiveValidation(etName, "Nama tidak boleh kosong", errorView = tvErrorNama)
        InputFieldValidator.setupLiveValidation(etEmail, "Email tidak boleh kosong", errorView = tvErrorEmail)
        InputFieldValidator.setupLiveValidation(etPassword, "Password minimal 8 karakter", errorView = tvErrorPassword, tintView = layoutPassword) { input ->
            input.isNotEmpty() && input.length >= 8
        }
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
