package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.databinding.ItemEventBinding
import com.google.android.material.chip.Chip
import java.util.Locale

class EventAdapter(private val onItemClick: (EventEntity) -> Unit) :
    ListAdapter<EventEntity, EventAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: EventEntity) {
            binding.apply {
                tvEventTitle.text = event.title
                tvOrganizer.text = event.organizer
                tvLocation.text = "📍 ${event.location}"
                tvDate.text = event.startDate
                tvCategoryLabel.text = event.category.uppercase()

                // Verified Badge Visibility
                ivVerifiedBadge.visibility = if (event.isVerified) View.VISIBLE else View.GONE

                // Langkah 2: UI Integration - Mengatur Trust Score
                if (event.trustScore > 0.0) {
                    tvTrustScore.visibility = View.VISIBLE
                    tvTrustScore.text = String.format(Locale.US, "⭐ %.1f", event.trustScore)
                } else {
                    // Hide jika score 0.0 agar UI tetap clean (subtle approach)
                    tvTrustScore.visibility = View.GONE
                }

                Glide.with(itemView.context)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.login_screen_overlay)
                    .into(ivEventBanner)

                cgItemTags.removeAllViews()
                event.tags?.split(",")?.forEach { tag ->
                    if (tag.isNotBlank()) {
                        val chip = Chip(itemView.context).apply {
                            text = "#${tag.trim()}"
                            textSize = 10f
                            setChipBackgroundColorResource(R.color.bg_elevated)
                            setTextColor(itemView.context.getColor(R.color.white))
                        }
                        cgItemTags.addView(chip)
                    }
                }

                root.setOnClickListener { onItemClick(event) }
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<EventEntity>() {
        override fun areItemsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EventEntity, newItem: EventEntity): Boolean {
            return oldItem == newItem
        }
    }
}
