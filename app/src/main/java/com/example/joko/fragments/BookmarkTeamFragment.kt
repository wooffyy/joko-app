package com.example.joko.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.joko.R

class BookmarkTeamFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Menggunakan layout khusus bookmark yang sudah dihapus filter dan FAB-nya
        return inflater.inflate(R.layout.fragment_bookmark_team, container, false)
    }
}
