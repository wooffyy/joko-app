package com.example.joko.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.example.joko.fragments.BookmarkEventFragment
import com.example.joko.fragments.BookmarkTeamFragment

class BookmarkActivity : AppCompatActivity() {

    private lateinit var btnEventBookmark: LinearLayout
    private lateinit var btnTeamBookmark: LinearLayout
    private lateinit var tvEventBookmark: TextView
    private lateinit var tvTeamBookmark: TextView
    private lateinit var indicatorEventBookmark: View
    private lateinit var indicatorTeamBookmark: View
    private lateinit var btnBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bookmark)

        btnEventBookmark = findViewById(R.id.btn_event_bookmark)
        btnTeamBookmark = findViewById(R.id.btn_team_bookmark)
        tvEventBookmark = findViewById(R.id.tv_event_bookmark)
        tvTeamBookmark = findViewById(R.id.tv_team_bookmark)
        indicatorEventBookmark = findViewById(R.id.indicator_event_bookmark)
        indicatorTeamBookmark = findViewById(R.id.indicator_team_bookmark)
        btnBack = findViewById(R.id.btnBack)

        // Default Fragment
        replaceFragment(BookmarkEventFragment())

        btnEventBookmark.setOnClickListener {
            setActiveTab(true)
            replaceFragment(BookmarkEventFragment())
        }

        btnTeamBookmark.setOnClickListener {
            setActiveTab(false)
            replaceFragment(BookmarkTeamFragment())
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setActiveTab(isEvent: Boolean) {
        if (isEvent) {
            tvEventBookmark.setTextColor(resources.getColor(R.color.white, null))
            indicatorEventBookmark.visibility = View.VISIBLE
            
            tvTeamBookmark.setTextColor(resources.getColor(R.color.text_secondary, null))
            indicatorTeamBookmark.visibility = View.INVISIBLE
        } else {
            tvEventBookmark.setTextColor(resources.getColor(R.color.text_secondary, null))
            indicatorEventBookmark.visibility = View.INVISIBLE
            
            tvTeamBookmark.setTextColor(resources.getColor(R.color.white, null))
            indicatorTeamBookmark.visibility = View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
