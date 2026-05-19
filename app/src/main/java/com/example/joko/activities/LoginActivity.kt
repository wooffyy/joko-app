package com.example.joko.activities

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R

class LoginActivity : AppCompatActivity() {
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etPassword = findViewById<EditText>(R.id.etPassword)
        val ivShowPassword = findViewById<ImageView>(R.id.ivShowPassword)

        ivShowPassword.setOnClickListener {
            if (isPasswordVisible) {
                // Sembunyikan Password
                etPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                isPasswordVisible = false
            } else {
                // Tampilkan Password
                etPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                isPasswordVisible = true
            }
            
            // Pindahkan kursor ke posisi paling akhir teks
            etPassword.setSelection(etPassword.text.length)
        }
    }
}
