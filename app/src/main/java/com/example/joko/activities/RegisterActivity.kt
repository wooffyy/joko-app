package com.example.joko.activities

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R

class RegisterActivity : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etPassword = findViewById<EditText>(R.id.etPassword)
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

        // Tombol Daftar Sekarang
        btnDaftar.setOnClickListener {
            // Logika pendaftaran bisa ditambahkan di sini
            Toast.makeText(this, "Pendaftaran Berhasil!", Toast.LENGTH_SHORT).show()
            finish() // Kembali ke LoginActivity
        }

        // Klik "Masuk" jika sudah punya akun
        tvMasuk.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }
}
