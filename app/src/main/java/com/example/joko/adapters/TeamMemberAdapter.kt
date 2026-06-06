package com.example.joko.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.joko.R
import com.example.joko.data.remote.response.TeamMemberResponse
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView

class TeamMemberAdapter(
    private val isApplicant: Boolean,
    private val onAcceptClick: ((String) -> Unit)? = null,
    private val onRejectClick: ((String) -> Unit)? = null,
    private val onProfileClick: ((String) -> Unit)? = null,
    private val isProcessing: Boolean = false
) : ListAdapter<TeamMemberResponse, TeamMemberAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = if (isApplicant) R.layout.item_applicant_card else R.layout.item_member_card
        val view = LayoutInflater.from(parent.context).inflate(layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivAvatar: ShapeableImageView = itemView.findViewById(R.id.iv_avatar)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)

        fun bind(member: TeamMemberResponse) {
            tvName.text = member.userDetails?.name ?: "Anonymous"

            itemView.setOnClickListener {
                member.userId?.let { id -> onProfileClick?.invoke(id) }
            }
            
            val pfpUrl = member.userDetails?.pfpUrl
            Glide.with(itemView.context)
                .load(pfpUrl)
                .placeholder(R.drawable.default_avatar)
                .into(ivAvatar)

            if (isApplicant) {
                val btnAccept = itemView.findViewById<MaterialButton>(R.id.btn_accept)
                val btnReject = itemView.findViewById<MaterialButton>(R.id.btn_reject)
                
                btnAccept.isEnabled = !isProcessing
                btnReject.isEnabled = !isProcessing
                
                btnAccept.setOnClickListener { onAcceptClick?.invoke(member.id) }
                btnReject.setOnClickListener { onRejectClick?.invoke(member.id) }
            } else {
                itemView.findViewById<TextView>(R.id.tv_role)?.text = itemView.context.getString(R.string.label_member_role_default)
            }
        }
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
