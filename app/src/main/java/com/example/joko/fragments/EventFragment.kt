package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
        setupListeners() // PENTING: Restore listener agar filter bekerja
        observeViewModel()

        // Pemicu sinkronisasi data saat halaman dibuka
        viewModel.fetchEvents()
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
        // Menangani perubahan filter kategori
        binding.cgCategories.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<Chip>(checkedIds[0])
                val category = chip.text.toString()
                viewModel.setFilter(category)
            } else {
                // Jika tidak ada yang dipilih, paksa kembali ke "Semua"
                binding.chipSemua.isChecked = true
                viewModel.setFilter("Semua")
            }
        }
    }

    private fun observeViewModel() {
        // Observe allEvents yang sudah menangani logic filtering secara reaktif
        viewModel.allEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            binding.tvEmptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
