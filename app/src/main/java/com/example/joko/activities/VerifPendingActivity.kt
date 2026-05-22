package com.example.joko.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.joko.R

class VerifPendingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verif_pending)

        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)
        val btnOpenEmail = findViewById<Button>(R.id.btnOpenEmail)

        val email = intent.getStringExtra("USER_EMAIL") ?: "email-mu"
        val fullText = "Kami telah mengirimkan tautan verifikasi ke $email. Silakan klik tautan tersebut untuk mengaktifkan akunmu."

        val spannable = SpannableString(fullText)
        val startIndex = fullText.indexOf(email)
        val endIndex = startIndex + email.length

        if (startIndex != -1) {
            spannable.setSpan(
                ForegroundColorSpan(Color.parseColor("#5B8FF9")),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            spannable.setSpan(
                StyleSpan(Typeface.BOLD),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        tvDesc.text = spannable

        btnOpenEmail.setOnClickListener {
            openEmail()
        }

        tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun openEmail() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_APP_EMAIL)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Tidak ada aplikasi email yang ditemukan", Toast.LENGTH_SHORT).show()
        }
    }
}