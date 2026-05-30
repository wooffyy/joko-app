package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.local.entity.EventEntity
import com.example.joko.databinding.ItemHomeEventBinding

class HomeEventAdapter(private val onItemClick: (EventEntity) -> Unit) :
    ListAdapter<EventEntity, HomeEventAdapter.HomeEventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeEventViewHolder {
        val binding = ItemHomeEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeEventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HomeEventViewHolder(private val binding: ItemHomeEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: EventEntity) {
            binding.apply {
                tvEventTitle.text = event.title
                tvEventDate.text = "📅 ${event.startDate}"
                tvEventLocation.text = "📍 ${event.location}"
                tvCategory.text = event.category

                Glide.with(itemView.context)
                    .load(event.imageUrl)
                    .placeholder(R.drawable.login_screen_overlay)
                    .into(ivEventBanner)

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
