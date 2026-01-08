package com.ensa.gestionpersonnel.ui.personnel.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentPersonnelDetailBinding
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class PersonnelDetailFragment : Fragment() {

    private var _binding: FragmentPersonnelDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonnelDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonnelDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        // Observer pour les détails
        viewModel.personnelState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { personnel ->
                        displayPersonnelDetails(personnel)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observer pour la suppression
        viewModel.deleteState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // Afficher loading
                }
                is NetworkResult.Success -> {
                    Toast.makeText(context, "Personnel supprimé avec succès", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClickListeners() {
        // Bouton retour
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEdit.setOnClickListener {
            // Navigation vers formulaire d'édition
            viewModel.personnelState.value?.data?.id?.let { id ->
                val action = PersonnelDetailFragmentDirections
                    .actionPersonnelDetailToPersonnelForm(id)
                findNavController().navigate(action)
            } ?: run {
                Toast.makeText(context, "Erreur: ID personnel introuvable", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    private fun displayPersonnelDetails(personnel: com.ensa.gestionpersonnel.domain.model.Personnel) {
        binding.apply {
            // Photo
            Glide.with(requireContext())
                .load(personnel.photoUrl)
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .circleCrop()
                .into(ivPhoto)

            // Informations header
            tvNomComplet.text = personnel.getNomComplet()
            tvGradeType.text = "${personnel.gradeActuel} • ${personnel.typeEmploye}"
            
            // Statut
            chipStatus.text = if (personnel.estActif) "Actif" else "Inactif"
            chipStatus.setChipBackgroundColorResource(
                if (personnel.estActif) R.color.green else R.color.red
            )

            // Informations personnelles
            tvPpr.text = personnel.ppr
            tvCin.text = personnel.cin
            tvEmail.text = personnel.email
            tvTelephone.text = personnel.telephone
            tvDateNaissance.text = formatDate(personnel.dateNaissance)
            tvAdresse.text = personnel.adresse

            // Informations professionnelles
            tvDateRecrutementENSA.text = formatDate(personnel.dateRecrutementENSA)
            tvAnciennete.text = "${personnel.getAnciennete()} ans"
            tvEchelleEchelon.text = "Échelle ${personnel.echelleActuelle} / Échelon ${personnel.echelonActuel}"
            tvSoldeConges.text = "${personnel.soldeConges} jours"
        }
    }

    private fun showDeleteConfirmation() {
        viewModel.personnelState.value?.data?.let { personnel ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Confirmer la suppression")
                .setMessage("Voulez-vous vraiment supprimer ${personnel.getNomComplet()} ?\n\nCette action est irréversible.")
                .setPositiveButton("Supprimer") { _, _ ->
                    viewModel.deletePersonnel()
                }
                .setNegativeButton("Annuler", null)
                .show()
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
