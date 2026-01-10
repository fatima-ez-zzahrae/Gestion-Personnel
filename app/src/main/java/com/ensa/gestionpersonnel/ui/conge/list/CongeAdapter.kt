package com.ensa.gestionpersonnel.ui.conge.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ensa.gestionpersonnel.domain.model.Absence
import java.text.SimpleDateFormat
import java.util.Locale

class CongeAdapter(
    private val onItemClick: (Absence) -> Unit,
    private val onValidateClick: (Absence) -> Unit,
    private val onDeleteClick: (Absence) -> Unit
) : ListAdapter<Absence, CongeAdapter.CongeViewHolder>(CongeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CongeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            com.ensa.gestionpersonnel.R.layout.item_conge,
            parent,
            false
        )
        return CongeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CongeViewHolder, position: Int) {
        val absence = getItem(position)
        holder.bind(absence)
    }

    inner class CongeViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            view.findViewById<android.widget.Button>(com.ensa.gestionpersonnel.R.id.btnValidate).setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onValidateClick(getItem(position))
                }
            }

            view.findViewById<android.widget.Button>(com.ensa.gestionpersonnel.R.id.btnDelete).setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onDeleteClick(getItem(position))
                }
            }
        }

        fun bind(absence: Absence) {
            val tvPersonnelName = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvPersonnelName)
            val tvPpr = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvPpr)
            val tvPeriod = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvPeriod)
            val tvDuration = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvDuration)
            val tvMotif = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvMotif)
            val tvStatus = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvStatus)
            val btnValidate = itemView.findViewById<android.widget.Button>(com.ensa.gestionpersonnel.R.id.btnValidate)
            val tvJustificatif = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvJustificatif)
            val tvType = itemView.findViewById<android.widget.TextView>(com.ensa.gestionpersonnel.R.id.tvType)

            tvPersonnelName.text = "${absence.personnelPrenom} ${absence.personnelNom}"
            tvPpr.text = "PPR: ${absence.personnelPpr}"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
            tvPeriod.text = "Du ${dateFormat.format(absence.dateDebut)} au ${dateFormat.format(absence.dateFin)}"

            tvDuration.text = "${absence.getDureeJours()} jours"
            tvType.text = "Congé Annuel" // Toujours afficher "Congé Annuel"
            tvMotif.text = absence.motif ?: "Pas de motif"

            if (absence.estValideeParAdmin) {
                tvStatus.text = "Validé"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#388E3C"))
                btnValidate.text = "Invalider"
            } else {
                tvStatus.text = "En attente"
                tvStatus.setTextColor(android.graphics.Color.parseColor("#FFA000"))
                btnValidate.text = "Valider"
            }

            if (absence.justificatifUrl != null) {
                tvJustificatif.visibility = View.VISIBLE
                tvJustificatif.text = "Justificatif présent"
            } else {
                tvJustificatif.visibility = View.GONE
            }
        }
    }

    class CongeDiffCallback : DiffUtil.ItemCallback<Absence>() {
        override fun areItemsTheSame(oldItem: Absence, newItem: Absence): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Absence, newItem: Absence): Boolean {
            return oldItem == newItem
        }
    }
}