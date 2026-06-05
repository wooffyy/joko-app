package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.joko.R
import com.example.joko.data.remote.response.TeamMemberResponse
import com.google.android.material.button.MaterialButton

class PendingApplicationAdapter(
    private val onViewDetailClick: (TeamMemberResponse) -> Unit,
    private val onCancelApplyClick: (TeamMemberResponse) -> Unit
) : ListAdapter<TeamMemberResponse, PendingApplicationAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTeamName: TextView = view.findViewById(R.id.tv_team_name)
        val tvEventName: TextView = view.findViewById(R.id.tv_event_name)
        val tvStatus: TextView = view.findViewById(R.id.tv_status)
        val btnViewDetail: MaterialButton = view.findViewById(R.id.btn_view_detail)
        val btnCancelApply: MaterialButton = view.findViewById(R.id.btn_cancel_apply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_application_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = getItem(position)
        val team = application.teamDetails

        holder.tvTeamName.text = team?.teamName ?: "Unknown Team"
        holder.tvEventName.text = team?.eventName ?: "Unknown Event"
        holder.tvStatus.text = application.status.uppercase()

        holder.btnViewDetail.setOnClickListener { onViewDetailClick(application) }
        holder.btnCancelApply.setOnClickListener { onCancelApplyClick(application) }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TeamMemberResponse>() {
        override fun areItemsTheSame(oldItem: TeamMemberResponse, newItem: TeamMemberResponse): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: TeamMemberResponse, newItem: TeamMemberResponse): Boolean {
            return oldItem == newItem
        }
    }
}
