package com.ensa.gestionpersonnel.ui.diplome.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ensa.gestionpersonnel.databinding.ItemDiplomeBinding
import com.ensa.gestionpersonnel.domain.model.Diplome
import java.text.SimpleDateFormat
import java.util.*

class DiplomeAdapter(
    private val onItemClick: (Diplome) -> Unit,
    private val onDeleteClick: (Diplome) -> Unit
) : ListAdapter<Diplome, DiplomeAdapter.DiplomeViewHolder>(DiplomeDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiplomeViewHolder {
        val binding = ItemDiplomeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DiplomeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiplomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DiplomeViewHolder(
        private val binding: ItemDiplomeBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(diplome: Diplome) {
            binding.apply {
                textIntitule.text = diplome.intitule
                textSpecialite.text = diplome.specialite
                textNiveau.text = diplome.niveau.name.replace("_", "+")
                textEtablissement.text = diplome.etablissement
                textDateObtention.text = dateFormat.format(diplome.dateObtention)

                root.setOnClickListener { onItemClick(diplome) }

                buttonDelete.setOnClickListener {
                    onDeleteClick(diplome)
                }
            }
        }
    }

    private class DiplomeDiffCallback : DiffUtil.ItemCallback<Diplome>() {
        override fun areItemsTheSame(oldItem: Diplome, newItem: Diplome): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Diplome, newItem: Diplome): Boolean {
            return oldItem == newItem
        }
    }
}