package com.ensa.gestionpersonnel.ui.absence.list

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensa.gestionpersonnel.databinding.FragmentAbsenceListBinding
import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.domain.model.AbsenceType
import com.ensa.gestionpersonnel.ui.absence.AbsenceViewModel
import com.ensa.gestionpersonnel.utils.NetworkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AbsenceListFragment : Fragment() {

    private var _binding: FragmentAbsenceListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AbsenceViewModel by viewModels()
    private lateinit var absenceAdapter: AbsenceAdapter
    private var currentAbsenceType: AbsenceType? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsenceListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupérer le type d'absence depuis les arguments
        arguments?.getString("absenceType")?.let { typeString ->
            if (typeString.isNotEmpty()) {
                currentAbsenceType = AbsenceType.valueOf(typeString)
            }
        }

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupSearch()

        // Charger les données
        loadInitialData()
    }

    override fun onResume() {
        super.onResume()
        // Recharger les données à chaque fois qu'on revient sur cet écran
        loadInitialData()
    }

    private fun loadInitialData() {
        val personnelId = arguments?.getLong("personnelId", 0L) ?: 0L

        if (personnelId != 0L) {
            // Charger les absences d'un personnel spécifique
            viewModel.loadAbsencesByPersonnel(personnelId)
        } else {
            // Charger toutes les absences
            viewModel.loadAllAbsences()
        }
    }

    private fun setupUI() {
        // Configuration du titre selon le type
        binding.toolbar.title = when (currentAbsenceType) {
            AbsenceType.CONGE_ANNUEL -> "Gestion des Congés"
            AbsenceType.MALADIE -> "Gestion des Absences Maladie"
            AbsenceType.EXCEPTIONNELLE -> "Absences Exceptionnelles"
            AbsenceType.NON_JUSTIFIEE -> "Absences Non Justifiées"
            null -> "Toutes les Absences"
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Bouton d'ajout - passe le type d'absence
        binding.fabAddAbsence.setOnClickListener {
            val personnelId = arguments?.getLong("personnelId", 0L) ?: 0L
            val action = AbsenceListFragmentDirections.actionAbsenceListToAbsenceForm(
                personnelId = personnelId,
                absenceType = currentAbsenceType?.name ?: ""
            )
            findNavController().navigate(action)
        }

        // Masquer le spinner de filtre si on a déjà un type
        if (currentAbsenceType != null) {
            binding.spinnerFilterType.visibility = View.GONE
            binding.btnFilter.visibility = View.GONE
        }

        binding.btnClearFilter.setOnClickListener {
            viewModel.clearFilters()
            // Réappliquer le filtre de type si on est dans une vue filtrée
            currentAbsenceType?.let { type ->
                viewModel.filterAbsencesByType(type.name)
            }
            binding.btnClearFilter.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        absenceAdapter = AbsenceAdapter(
            onItemClick = { absence: Absence ->
                absence.id?.let { id ->
                    val action = AbsenceListFragmentDirections
                        .actionAbsenceListToAbsenceDetail(id)
                    findNavController().navigate(action)
                }
            },
            onValidateClick = { absence: Absence ->
                showValidationDialog(absence)
            },
            onDeleteClick = { absence: Absence ->
                showDeleteConfirmation(absence)
            }
        )

        binding.recyclerViewAbsences.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = absenceAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observer les absences
        viewModel.absences.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE

                    // Appliquer le filtre par type si nécessaire
                    val absences = result.data ?: emptyList()
                    val filteredAbsences = if (currentAbsenceType != null) {
                        absences.filter { it.type == currentAbsenceType }
                    } else {
                        absences
                    }

                    // Forcer le rafraîchissement complet
                    absenceAdapter.submitList(null)
                    absenceAdapter.submitList(filteredAbsences.toList())

                    binding.tvEmptyState.visibility = if (filteredAbsences.isEmpty()) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }
                    updateStatistics(filteredAbsences)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                    showErrorDialog(result.message ?: "Erreur inconnue")
                }
            }
        }

        // Observer les opérations sur les absences
        viewModel.absenceOperation.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Snackbar.make(
                        binding.root,
                        "Opération réussie",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Recharger après un court délai
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadInitialData()
                        absenceAdapter.notifyDataSetChanged()
                    }, 300)
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    showErrorDialog(result.message ?: "Erreur inconnue")
                }
                else -> {}
            }
        }
    }

    private fun updateStatistics(absences: List<Absence>) {
        val total = absences.size
        val pending = absences.count { !it.estValideeParAdmin }

        binding.tvTotalAbsences.text = total.toString()
        binding.tvPendingAbsences.text = pending.toString()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchAbsences(newText ?: "")
                return true
            }
        })
    }

    private fun showValidationDialog(absence: Absence) {
        val action = if (absence.estValideeParAdmin) "Invalider" else "Valider"
        val message = "Voulez-vous $action cette absence de " +
                "${absence.personnelPrenom} ${absence.personnelNom} ?"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("$action l'absence")
            .setMessage(message)
            .setPositiveButton(action) { _, _ ->
                absence.id?.let { id ->
                    viewModel.validateAbsence(id, !absence.estValideeParAdmin)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showDeleteConfirmation(absence: Absence) {
        val message = "Voulez-vous vraiment supprimer cette absence de " +
                "${absence.personnelPrenom} ${absence.personnelNom} ?"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer l'absence")
            .setMessage(message)
            .setPositiveButton("Supprimer") { _, _ ->
                absence.id?.let { id ->
                    viewModel.deleteAbsence(id)
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showErrorDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Erreur")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}