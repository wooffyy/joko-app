package com.example.joko.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.joko.R

class TeamFragment : Fragment() {

    private lateinit var btnUntukAnda: LinearLayout
    private lateinit var btnTimAnda: LinearLayout
    private lateinit var tvUntukAnda: TextView
    private lateinit var tvTimAnda: TextView
    private lateinit var indicatorUntukAnda: View
    private lateinit var indicatorTimAnda: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_team, container, false)

        btnUntukAnda = view.findViewById(R.id.btn_untuk_anda)
        btnTimAnda = view.findViewById(R.id.btn_tim_anda)
        tvUntukAnda = view.findViewById(R.id.tv_untuk_anda)
        tvTimAnda = view.findViewById(R.id.tv_tim_anda)
        indicatorUntukAnda = view.findViewById(R.id.indicator_untuk_anda)
        indicatorTimAnda = view.findViewById(R.id.indicator_tim_anda)

        // Set default fragment
        replaceFragment(UntukAndaFragment())

        btnUntukAnda.setOnClickListener {
            setActiveTab(true)
            replaceFragment(UntukAndaFragment())
        }

        btnTimAnda.setOnClickListener {
            setActiveTab(false)
            replaceFragment(TimAndaFragment())
        }

        return view
    }

    private fun setActiveTab(isUntukAnda: Boolean) {
        if (isUntukAnda) {
            tvUntukAnda.setTextColor(resources.getColor(R.color.white, null))
            indicatorUntukAnda.visibility = View.VISIBLE
            
            tvTimAnda.setTextColor(resources.getColor(R.color.text_secondary, null))
            indicatorTimAnda.visibility = View.INVISIBLE
        } else {
            tvUntukAnda.setTextColor(resources.getColor(R.color.text_secondary, null))
            indicatorUntukAnda.visibility = View.INVISIBLE
            
            tvTimAnda.setTextColor(resources.getColor(R.color.white, null))
            indicatorTimAnda.visibility = View.VISIBLE
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
