package com.example.joko.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.joko.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ChipInputFragment : Fragment() {

    private lateinit var chipGroupTags: ChipGroup
    private lateinit var etInputTag: EditText
    private var customHint: String? = null // Menyimpan hint sementara jika di-set sebelum view siap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chip, container, false)

        chipGroupTags = view.findViewById(R.id.chipGroupTags)
        etInputTag = view.findViewById(R.id.etInputTag)

        // Jika ada hint custom yang sudah dipasang sebelumnya, langsung terapkan di sini
        customHint?.let { etInputTag.hint = it }

        setupInputListener()
        return view
    }

    private fun setupInputListener() {
        etInputTag.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val tagText = etInputTag.text.toString().trim()
                if (tagText.isNotEmpty()) {
                    addChipToGroup(tagText)
                    etInputTag.setText("")
                }
                true
            } else {
                false
            }
        }
    }

    private fun addChipToGroup(tagText: String) {
        val chip = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_event_tag, chipGroupTags, false) as Chip

        chip.apply {
            text = tagText
            isCloseIconVisible = true
            setCloseIconTintResource(R.color.white)
            setOnCloseIconClickListener {
                chipGroupTags.removeView(this)
            }
        }

        val inputIndex = chipGroupTags.indexOfChild(etInputTag)
        chipGroupTags.addView(chip, inputIndex)
    }

    fun setHint(hintText: String) {
        customHint = hintText
        // Jika View sudah dibuat (onViewCreated), langsung ganti secara real-time
        if (::etInputTag.isInitialized) {
            etInputTag.hint = hintText
        }
    }

    fun getTags(): List<String> {
        val tagsList = mutableListOf<String>()
        for (i in 0 until chipGroupTags.childCount) {
            val view = chipGroupTags.getChildAt(i)
            if (view is Chip) {
                tagsList.add(view.text.toString())
            }
        }
        return tagsList
    }
}