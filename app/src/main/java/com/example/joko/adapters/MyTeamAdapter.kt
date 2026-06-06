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

class MyTeamAdapter(
    private val buttonText: String = "Manage",
    private val buttonIconRes: Int = R.drawable.ic_team,
    private val onActionClick: (TeamResponse) -> Unit
) : ListAdapter<TeamResponse, MyTeamAdapter.MyTeamViewHolder>(DiffCallback) {

    class MyTeamViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tv_team_name)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvSlots: TextView = view.findViewById(R.id.tv_slots)
        val pbRecruitment: ProgressBar = view.findViewById(R.id.pb_recruitment)
        val layoutRoles: ViewGroup = view.findViewById(R.id.layout_roles)
        val btnAction: MaterialButton = view.findViewById(R.id.btn_manage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyTeamViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_my_team_card, parent, false)
        return MyTeamViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyTeamViewHolder, position: Int) {
        val team = getItem(position)
        holder.tvTeamName.text = team.teamName
        holder.tvEventName.text = team.eventName
        
        val current = team.currentMembersCount
        val max = team.maxCapacity
        holder.tvSlots.text = "$current/$max SLOTS"
        
        val progress = if (max > 0) (current.toFloat() / max.toFloat() * 100).toInt() else 0
        holder.pbRecruitment.progress = progress

        // Handle roles dynamically using the display tag layout
        holder.layoutRoles.removeAllViews()
        team.roleNeed?.forEach { role ->
            val roleView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.layout_tag_display, holder.layoutRoles, false) as TextView
            roleView.text = role
            holder.layoutRoles.addView(roleView)
        }
        
        // Update button appearance
        holder.btnAction.text = buttonText
        holder.btnAction.setIconResource(buttonIconRes)
        
        holder.btnAction.setOnClickListener { onActionClick(team) }
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
