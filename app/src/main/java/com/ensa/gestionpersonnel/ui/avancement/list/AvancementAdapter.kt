package com.ensa.gestionpersonnel.ui.avancement.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ensa.gestionpersonnel.databinding.ItemAvancementBinding
import com.ensa.gestionpersonnel.domain.model.Avancement
import java.text.SimpleDateFormat
import java.util.*

class AvancementAdapter(
    private val onItemClick: (Avancement) -> Unit,
    private val onDeleteClick: (Avancement) -> Unit
) : ListAdapter<Avancement, AvancementAdapter.AvancementViewHolder>(AvancementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvancementViewHolder {
        val binding = ItemAvancementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AvancementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvancementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AvancementViewHolder(
        private val binding: ItemAvancementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        fun bind(avancement: Avancement) {
            binding.apply {
                textSummary.text = avancement.getSummary()
                textDateDecision.text = "Décision: ${dateFormat.format(avancement.dateDecision)}"
                textDateEffet.text = "Effet: ${dateFormat.format(avancement.dateEffet)}"

                if (avancement.description.isNotBlank()) {
                    textDescription.text = avancement.description
                } else {
                    textDescription.text = "Aucune description"
                }

                root.setOnClickListener { onItemClick(avancement) }

                buttonDelete.setOnClickListener {
                    onDeleteClick(avancement)
                }
            }
        }
    }

    private class AvancementDiffCallback : DiffUtil.ItemCallback<Avancement>() {
        override fun areItemsTheSame(oldItem: Avancement, newItem: Avancement): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Avancement, newItem: Avancement): Boolean {
            return oldItem == newItem
        }
    }
}