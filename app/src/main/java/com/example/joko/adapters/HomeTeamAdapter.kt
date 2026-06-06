package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.data.remote.response.TeamResponse

class HomeTeamAdapter(
    private val onDetailClick: (TeamResponse) -> Unit
) : ListAdapter<TeamResponse, HomeTeamAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tv_team_name)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvSlots: TextView = view.findViewById(R.id.tv_slots)
        val pbRecruitment: ProgressBar = view.findViewById(R.id.pb_recruitment)
        val layoutRoles: ViewGroup = view.findViewById(R.id.layout_roles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_home_team_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val team = getItem(position)
        holder.tvTeamName.text = team.teamName
        holder.tvEventName.text = team.eventName
        
        val current = team.currentMembersCount
        val max = team.maxCapacity
        holder.tvSlots.text = "$current/$max"
        
        val progress = if (max > 0) (current.toFloat() / max.toFloat() * 100).toInt() else 0
        holder.pbRecruitment.progress = progress

        // Handle roles (simplified for home) - Updated to match TeamAdapter display style
        holder.layoutRoles.removeAllViews()
        team.roleNeed?.take(2)?.forEach { role ->
            val roleView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.layout_tag_display, holder.layoutRoles, false) as TextView
            roleView.text = role
            holder.layoutRoles.addView(roleView)
        }

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
