package com.example.joko.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.joko.R

class EventDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnDaftarSekarang = findViewById<Button>(R.id.btnDaftarSekarang)

        btnBack.setOnClickListener {
            finish()
        }

        btnDaftarSekarang.setOnClickListener {
            Toast.makeText(this, "Berhasil daftar ke event!", Toast.LENGTH_SHORT).show()
            // Kembali ke Event Screen setelah sukses daftar
            finish()
        }
    }
}
