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
import com.google.android.material.button.MaterialButton

class TeamAdapter(
    private val onApplyClick: (TeamResponse) -> Unit,
    private val onDetailClick: (TeamResponse) -> Unit,
    private val onBookmarkClick: (TeamResponse) -> Unit
) : ListAdapter<TeamResponse, TeamAdapter.TeamViewHolder>(DiffCallback) {

    private var bookmarkedIds: Set<String> = emptySet()

    fun setBookmarkedIds(ids: Set<String>) {
        bookmarkedIds = ids
        notifyDataSetChanged()
    }

    class TeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tv_team_name)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvSlots: TextView = view.findViewById(R.id.tv_slots)
        val tvProgressPercent: TextView = view.findViewById(R.id.tv_progress_percent)
        val pbRecruitment: ProgressBar = view.findViewById(R.id.pb_recruitment)
        val layoutRoles: ViewGroup = view.findViewById(R.id.layout_roles)
        val btnApply: MaterialButton = view.findViewById(R.id.btn_apply)
        val btnBookmark: View = view.findViewById(R.id.btn_bookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_team_card, parent, false)
        return TeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: TeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.tvTeamName.text = team.teamName
        holder.tvEventName.text = team.eventName
        
        val current = team.currentMembersCount
        val max = team.maxCapacity
        holder.tvSlots.text = "$current/$max SLOTS"
        
        val progress = if (max > 0) (current.toFloat() / max.toFloat() * 100).toInt() else 0
        holder.pbRecruitment.progress = progress
        holder.tvProgressPercent.text = "$progress%"

        // Handle roles dynamically using layout_tag_display for consistent look
        holder.layoutRoles.removeAllViews()
        team.roleNeed?.forEach { role ->
            val roleView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.layout_tag_display, holder.layoutRoles, false) as TextView
            roleView.text = role
            holder.layoutRoles.addView(roleView)
        }
        
        holder.btnApply.setOnClickListener { onApplyClick(team) }
        holder.itemView.setOnClickListener { onDetailClick(team) }

        // Sinkronisasi status bookmark dari Room
        holder.btnBookmark.isSelected = bookmarkedIds.contains(team.id)

        holder.btnBookmark.setOnClickListener {
            onBookmarkClick(team)
        }
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
