package com.example.joko.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.joko.activities.BookmarkViewModel
import com.example.joko.activities.EventDetailActivity
import com.example.joko.adapters.EventAdapter
import com.example.joko.databinding.FragmentBookmarkEventBinding
import com.example.joko.utils.ViewModelFactory

class BookmarkEventFragment : Fragment() {

    private var _binding: FragmentBookmarkEventBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BookmarkViewModel by viewModels {
        ViewModelFactory(requireContext())
    }

    private lateinit var adapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        setupSwipeRefresh()
    }

    private fun setupRecyclerView() {
        adapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id)
            startActivity(intent)
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@BookmarkEventFragment.adapter
        }
    }

    private fun observeViewModel() {
        viewModel.bookmarkedEvents.observe(viewLifecycleOwner) { events ->
            adapter.submitList(events)
            binding.tvEmptyState.visibility = if (events.isEmpty()) View.VISIBLE else View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Because Room Flow is reactive, we don't strictly need a "refresh" 
            // but we stop the animation to provide feedback.
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
