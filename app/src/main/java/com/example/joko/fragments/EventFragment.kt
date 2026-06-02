package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.joko.MainActivity
import com.example.joko.R
import com.example.joko.activities.EventDetailActivity
import com.example.joko.activities.EventViewModel
import com.example.joko.adapters.EventAdapter
import com.example.joko.databinding.FragmentEventBinding
import com.example.joko.utils.ViewModelFactory
import com.google.android.material.chip.Chip

class EventFragment : Fragment() {

    private var _binding: FragmentEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        setupListeners() 
        observeViewModel()

        // Pemicu sinkronisasi data saat halaman dibuka
        viewModel.fetchEvents()
        viewModel.fetchUserData()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            startActivity(intent)
        }
        binding.rvEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvents.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.fetchEvents()
        }
    }

    private fun setupListeners() {
        // Navigasi ke Profile Screen saat foto profil diklik
        binding.ivProfile.setOnClickListener {
            (activity as? MainActivity)?.navigateToTab(R.id.nav_profile)
        }

        // Langkah 2: Fragment Search Integration
        // Mengirim input user ke ViewModel secara realtime
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearch(text?.toString() ?: "")
        }

        // Langkah 3.2.2: Listener Guard Stabilization
        binding.cgCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            val currentFilterInViewModel = viewModel.currentFilter.value ?: "Semua"
            
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val selectedCategory = chip.text.toString()
                
                // Guard: Hanya panggil setFilter jika kategori yang diklik berbeda dengan filter aktif di ViewModel.
                if (!selectedCategory.equals(currentFilterInViewModel, ignoreCase = true)) {
                    viewModel.setFilter(selectedCategory)
                }
            } else {
                // Fallback: Jika tidak ada chip terpilih, kembalikan ke "Semua"
                if (!currentFilterInViewModel.equals("Semua", ignoreCase = true)) {
                    binding.chipSemua.isChecked = true
                    viewModel.setFilter("Semua")
                } else {
                    // Jika sudah di "Semua", pastikan visual tetap sinkron
                    binding.chipSemua.isChecked = true
                }
            }
        }
    }

    private fun observeViewModel() {
        // Observe allEvents yang sudah menangani logic filtering secara reaktif (DB + Category + Search)
        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            binding.tvEmptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }

        // Langkah 3.1: Dynamic Chip Rendering
        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            renderFilterChips(categories)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.pfpUrl.observe(viewLifecycleOwner) { url ->
            Glide.with(this)
                .load(url)
                .placeholder(R.drawable.default_avatar)
                .error(R.drawable.default_avatar)
                .circleCrop()
                .into(binding.ivProfile)
        }
    }

    private fun renderFilterChips(categories: List<String>) {
        // Ambil filter aktif dari ViewModel sebagai acuan sinkronisasi
        val activeFilter = viewModel.currentFilter.value ?: "Semua"

        // 1. Membersihkan chip lama kecuali "Semua" (index 0)
        while (binding.cgCategories.childCount > 1) {
            binding.cgCategories.removeViewAt(1)
        }

        // 2. Membuat Chip baru dengan sinkronisasi identitas visual (Warna + Teks)
        categories.forEach { category ->
            if (!category.equals("Semua", ignoreCase = true)) {
                // Gunakan ContextThemeWrapper untuk menyuntikkan dasar Geometri
                val themedContext = ContextThemeWrapper(requireContext(), R.style.Widget_App_Chip_Filter)
                val chip = Chip(themedContext).apply {
                    text = category.replaceFirstChar { it.uppercase() }
                    isCheckable = true
                    id = View.generateViewId()
                    
                    // Explicit Visual Identity Injection (Warna & State)
                    setChipBackgroundColorResource(R.color.chip_bg_selector)
                    setChipStrokeColorResource(R.color.chip_stroke_selector)
                    setTextColor(requireContext().getColorStateList(R.color.chip_text_selector))
                    setTextAppearance(R.style.TextAppearance_App_Chip)
                }
                binding.cgCategories.addView(chip)
            }
        }

        // Langkah 3.2.1: Re-selection Synchronization
        // Menjamin status 'checked' tetap terjaga meskipun chip di-recreate
        for (i in 0 until binding.cgCategories.childCount) {
            val chip = binding.cgCategories.getChildAt(i) as? Chip
            if (chip != null && chip.text.toString().equals(activeFilter, ignoreCase = true)) {
                chip.isChecked = true
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
