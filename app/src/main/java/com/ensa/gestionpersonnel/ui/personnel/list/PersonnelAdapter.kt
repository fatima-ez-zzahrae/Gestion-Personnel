package com.ensa.gestionpersonnel.ui.personnel.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.ItemPersonnelBinding
import com.ensa.gestionpersonnel.domain.model.Personnel

class PersonnelAdapter(
    private val onItemClick: (Personnel) -> Unit,
    private val onEditClick: (Personnel) -> Unit,
    private val onDeleteClick: (Personnel) -> Unit
) : ListAdapter<Personnel, PersonnelAdapter.PersonnelViewHolder>(PersonnelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PersonnelViewHolder {
        val binding = ItemPersonnelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PersonnelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PersonnelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PersonnelViewHolder(
        private val binding: ItemPersonnelBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(personnel: Personnel) {
            binding.apply {
                tvNomComplet.text = personnel.getNomComplet()
                tvPpr.text = "PPR: ${personnel.ppr}"
                tvGradeType.text = "${personnel.gradeActuel} • ${personnel.typeEmploye}"
                
                // Statut
                chipStatus.text = if (personnel.estActif) "Actif" else "Inactif"
                chipStatus.setChipBackgroundColorResource(
                    if (personnel.estActif) R.color.green else R.color.red
                )
                
                // Charger photo avec Glide
                Glide.with(ivPhoto.context)
                    .load(personnel.photoUrl)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .error(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(ivPhoto)
                
                // Click sur la carte
                root.setOnClickListener {
                    onItemClick(personnel)
                }
                
                // Menu contextuel
                btnMore.setOnClickListener { view ->
                    showPopupMenu(view, personnel)
                }
            }
        }

        private fun showPopupMenu(view: View, personnel: Personnel) {
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.menu_personnel_item, popup.menu)
            
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        onEditClick(personnel)
                        true
                    }
                    R.id.action_delete -> {
                        onDeleteClick(personnel)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }
    }

    class PersonnelDiffCallback : DiffUtil.ItemCallback<Personnel>() {
        override fun areItemsTheSame(oldItem: Personnel, newItem: Personnel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Personnel, newItem: Personnel): Boolean {
            return oldItem == newItem
        }
    }
}

