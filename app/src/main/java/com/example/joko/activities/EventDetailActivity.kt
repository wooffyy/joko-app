package com.example.joko.activities

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.joko.R

class EventDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_detail)

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
