package com.ensa.gestionpersonnel.ui.mission.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import java.text.SimpleDateFormat
import java.util.Locale

class MissionAdapter(
    private val onMissionClick: (Mission) -> Unit,
    private val onMissionLongClick: (Mission) -> Unit
) : ListAdapter<Mission, MissionAdapter.MissionViewHolder>(MissionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return MissionViewHolder(view, onMissionClick, onMissionLongClick)
    }

    override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MissionViewHolder(
        itemView: View,
        private val onMissionClick: (Mission) -> Unit,
        private val onMissionLongClick: (Mission) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val cardView: CardView = itemView.findViewById(R.id.cardMission)
        private val tvDestination: TextView = itemView.findViewById(R.id.tvDestination)
        private val tvObjetMission: TextView = itemView.findViewById(R.id.tvObjetMission)
        private val tvPersonnel: TextView = itemView.findViewById(R.id.tvPersonnel)
        private val tvDateDepart: TextView = itemView.findViewById(R.id.tvDateDepart)
        private val tvDateRetour: TextView = itemView.findViewById(R.id.tvDateRetour)
        private val tvDuree: TextView = itemView.findViewById(R.id.tvDuree)
        private val tvStatut: TextView = itemView.findViewById(R.id.tvStatut)
        private val ivStatutIcon: ImageView = itemView.findViewById(R.id.ivStatutIcon)

        private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun bind(mission: Mission) {
            tvDestination.text = mission.destination
            tvObjetMission.text = mission.objetMission

            val personnelName = if (mission.personnelNom != null && mission.personnelPrenom != null) {
                "${mission.personnelPrenom} ${mission.personnelNom}"
            } else {
                "Personnel #${mission.personnelId}"
            }
            tvPersonnel.text = personnelName

            tvDateDepart.text = dateFormat.format(mission.dateDepart)
            tvDateRetour.text = dateFormat.format(mission.dateRetour)

            val duree = mission.getDureeJours()
            tvDuree.text = "$duree jour${if (duree > 1) "s" else ""}"

            when (mission.statut) {
                StatutMission.PLANIFIEE -> {
                    tvStatut.text = "PLANIFIÉE"
                    tvStatut.setBackgroundResource(R.drawable.bg_status_pending)
                    ivStatutIcon.setImageResource(R.drawable.ic_calendar)
                }
                StatutMission.EN_COURS -> {
                    tvStatut.text = "EN COURS"
                    tvStatut.setBackgroundResource(R.drawable.bg_status_validated)
                    ivStatutIcon.setImageResource(R.drawable.ic_flight)
                }
                StatutMission.TERMINEE -> {
                    tvStatut.text = "TERMINÉE"
                    tvStatut.setBackgroundResource(R.drawable.bg_status_validated)
                    ivStatutIcon.setImageResource(R.drawable.ic_check)
                }
                StatutMission.ANNULEE -> {
                    tvStatut.text = "ANNULÉE"
                    tvStatut.setBackgroundResource(R.drawable.bg_status_pending)
                    ivStatutIcon.setImageResource(R.drawable.ic_baseline_close_24)
                }
            }

            cardView.setOnClickListener { onMissionClick(mission) }
            cardView.setOnLongClickListener {
                onMissionLongClick(mission)
                true
            }
        }
    }

    class MissionDiffCallback : DiffUtil.ItemCallback<Mission>() {
        override fun areItemsTheSame(oldItem: Mission, newItem: Mission): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Mission, newItem: Mission): Boolean {
            return oldItem == newItem
        }
    }
}