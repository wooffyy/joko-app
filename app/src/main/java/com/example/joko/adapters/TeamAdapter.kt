package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.data.remote.response.TeamResponse
import com.google.android.material.button.MaterialButton

class TeamAdapter(
    private val onApplyClick: (TeamResponse) -> Unit,
    private val onDetailClick: (TeamResponse) -> Unit
) : ListAdapter<TeamResponse, TeamAdapter.TeamViewHolder>(DiffCallback) {

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tv_team_name)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvSlots: TextView = view.findViewById(R.id.tv_slots)
        val btnApply: MaterialButton = view.findViewById(R.id.btn_apply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_card, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.tvTeamName.text = team.teamName
        holder.tvEventName.text = team.eventName
        // Dummy slots display logic for now: 1 member (owner) / max capacity
        holder.tvSlots.text = "1/${team.maxCapacity} SLOTS"
        
        holder.btnApply.setOnClickListener { onApplyClick(team) }
        holder.itemView.setOnClickListener { onDetailClick(team) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TeamResponse>() {
        override fun areItemsTheSame(oldItem: TeamResponse, newItem: TeamResponse): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: TeamResponse, newItem: TeamResponse): Boolean {
            return oldItem == newItem
        }
    }
}
