package com.ensa.gestionpersonnel.ui.absence.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.databinding.FragmentAbsenceFormBinding
import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.domain.model.AbsenceType
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.ui.absence.AbsenceViewModel
import com.ensa.gestionpersonnel.ui.personnel.list.PersonnelListViewModel
import com.ensa.gestionpersonnel.utils.NetworkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AbsenceFormFragment : Fragment() {

    private var _binding: FragmentAbsenceFormBinding? = null
    private val binding get() = _binding!!
    private val absenceViewModel: AbsenceViewModel by viewModels()
    private val personnelViewModel: PersonnelListViewModel by viewModels()

    private var selectedAbsenceType: AbsenceType? = null
    private var selectedPersonnel: Personnel? = null
    private var personnelList: List<Personnel> = emptyList()
    private var dateDebut: Date? = null
    private var dateFin: Date? = null
    private var absenceId: Long = 0L
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsenceFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Récupérer les arguments
        arguments?.let {
            absenceId = it.getLong("absenceId", 0L)
            val personnelId = it.getLong("personnelId", 0L)

            // Type d'absence prédéfini
            it.getString("absenceType")?.let { typeString ->
                if (typeString.isNotEmpty()) {
                    selectedAbsenceType = AbsenceType.valueOf(typeString)
                }
            }

            // Si un personnel est spécifié, le charger
            if (personnelId != 0L) {
                loadSpecificPersonnel(personnelId)
            }
        }

        setupUI()
        setupObservers()
        loadPersonnelList()

        if (absenceId != 0L) {
            loadAbsenceData()
        }
    }

    private fun setupUI() {
        // Configuration du titre
        binding.toolbar.title = if (absenceId != 0L) {
            "Modifier l'absence"
        } else {
            when (selectedAbsenceType) {
                AbsenceType.CONGE_ANNUEL -> "Nouveau Congé"
                AbsenceType.MALADIE -> "Nouvelle Absence Maladie"
                AbsenceType.EXCEPTIONNELLE -> "Nouvelle Absence Exceptionnelle"
                AbsenceType.NON_JUSTIFIEE -> "Nouvelle Absence Non Justifiée"
                null -> "Nouvelle Absence"
            }
        }

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Configuration du spinner de type
        val types = AbsenceType.values().map { type ->
            when (type) {
                AbsenceType.CONGE_ANNUEL -> "Congé Annuel"
                AbsenceType.MALADIE -> "Absence Maladie"
                AbsenceType.EXCEPTIONNELLE -> "Absence Exceptionnelle"
                AbsenceType.NON_JUSTIFIEE -> "Absence Non Justifiée"
            }
        }
        val typeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            types
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerType.adapter = typeAdapter

        // Sélectionner le type par défaut si spécifié
        selectedAbsenceType?.let { type ->
            val position = AbsenceType.values().indexOf(type)
            binding.spinnerType.setSelection(position)
            binding.spinnerType.isEnabled = false
        }

        // Sélection du personnel
        binding.spinnerPersonnel.setOnItemSelectedListener(object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Position 0 est "Sélectionner un personnel"
                    selectedPersonnel = personnelList[position - 1]
                    updatePersonnelInfo()
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedPersonnel = null
            }
        })

        // Sélection des dates
        binding.etDateDebut.setOnClickListener {
            showDatePicker { date ->
                dateDebut = date
                binding.etDateDebut.setText(dateFormat.format(date))
                updateDuration()
            }
        }

        binding.etDateFin.setOnClickListener {
            showDatePicker { date ->
                dateFin = date
                binding.etDateFin.setText(dateFormat.format(date))
                updateDuration()
            }
        }

        // Bouton soumettre
        binding.btnSubmit.setOnClickListener {
            if (validateForm()) {
                submitForm()
            }
        }

        // Gestion du justificatif
        binding.btnUploadJustificatif.setOnClickListener {
            // TODO: Implémenter l'upload de fichier
            Snackbar.make(
                binding.root,
                "Fonctionnalité d'upload à implémenter",
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupObservers() {
        // Observer la liste du personnel
        personnelViewModel.personnelListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    personnelList = result.data ?: emptyList()
                    updatePersonnelSpinner()
                }
                is NetworkResult.Error -> {
                    showError("Erreur lors du chargement du personnel: ${result.message}")
                }
                is NetworkResult.Loading -> {
                    // Afficher un indicateur de chargement si nécessaire
                }
            }
        }

        // Observer les opérations sur les absences
        absenceViewModel.absenceOperation.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true

                    Snackbar.make(
                        binding.root,
                        "Absence enregistrée avec succès",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    // Retourner à la liste
                    findNavController().navigateUp()
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true

                    showError(result.message ?: "Erreur lors de l'enregistrement")
                }
                else -> {}
            }
        }
    }

    private fun loadPersonnelList() {
        personnelViewModel.loadPersonnelList()
    }

    private fun loadSpecificPersonnel(personnelId: Long) {
        // Charger le personnel spécifique et le présélectionner
        personnelViewModel.loadPersonnelById(personnelId)
    }

    private fun updatePersonnelSpinner() {
        val personnelNames = mutableListOf("Sélectionner un personnel")
        personnelNames.addAll(personnelList.map { "${it.nomFr} ${it.prenomFr} (${it.ppr})" })

        val personnelAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            personnelNames
        )
        personnelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPersonnel.adapter = personnelAdapter

        // Si un personnel a été préchargé, le sélectionner
        arguments?.getLong("personnelId", 0L)?.let { personnelId ->
            if (personnelId != 0L) {
                val position = personnelList.indexOfFirst { it.id == personnelId }
                if (position >= 0) {
                    binding.spinnerPersonnel.setSelection(position + 1) // +1 pour "Sélectionner..."
                    binding.spinnerPersonnel.isEnabled = false // Désactiver la modification
                }
            }
        }
    }

    private fun updatePersonnelInfo() {
        selectedPersonnel?.let { personnel ->
            binding.cardPersonnelInfo.visibility = View.VISIBLE
            binding.tvPersonnelNom.text = "${personnel.nomFr} ${personnel.prenomFr}"
            binding.tvPersonnelPpr.text = "PPR: ${personnel.ppr}"
            binding.tvPersonnelType.text = "Type: ${personnel.typeEmploye}"

            // Afficher le solde de congés
            binding.tvSoldeConges.text = "Solde de congés: ${personnel.soldeConges} jours"
            binding.tvSoldeConges.visibility = if (selectedAbsenceType == AbsenceType.CONGE_ANNUEL) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun loadAbsenceData() {
        // TODO: Implémenter le chargement d'une absence existante
        // Nécessite d'ajouter une méthode dans le repository
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDuration() {
        if (dateDebut != null && dateFin != null) {
            val diff = dateFin!!.time - dateDebut!!.time
            val days = (diff / (24 * 60 * 60 * 1000)).toInt() + 1
            binding.tvDuree.text = "Durée: $days jour(s)"
            binding.tvDuree.visibility = View.VISIBLE

            // Vérifier le solde de congés
            if (selectedAbsenceType == AbsenceType.CONGE_ANNUEL && selectedPersonnel != null) {
                if (days > selectedPersonnel!!.soldeConges) {
                    binding.tvWarning.visibility = View.VISIBLE
                    binding.tvWarning.text = "⚠️ Attention: Le solde de congés est insuffisant!"
                } else {
                    binding.tvWarning.visibility = View.GONE
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        // Validation du personnel
        if (selectedPersonnel == null) {
            showError("Veuillez sélectionner un personnel")
            return false
        }

        // Validation du type
        val typePosition = binding.spinnerType.selectedItemPosition
        if (typePosition < 0) {
            showError("Veuillez sélectionner un type d'absence")
            return false
        }
        selectedAbsenceType = AbsenceType.values()[typePosition]

        // Validation des dates
        if (dateDebut == null) {
            showError("Veuillez sélectionner une date de début")
            return false
        }

        if (dateFin == null) {
            showError("Veuillez sélectionner une date de fin")
            return false
        }

        if (dateFin!!.before(dateDebut)) {
            showError("La date de fin doit être après la date de début")
            return false
        }

        // Validation du motif pour certains types
        val motif = binding.etMotif.text.toString().trim()
        if (selectedAbsenceType in listOf(
                AbsenceType.EXCEPTIONNELLE,
                AbsenceType.NON_JUSTIFIEE
            ) && motif.isEmpty()) {
            showError("Le motif est obligatoire pour ce type d'absence")
            return false
        }

        // Validation du solde de congés
        if (selectedAbsenceType == AbsenceType.CONGE_ANNUEL) {
            val diff = dateFin!!.time - dateDebut!!.time
            val days = (diff / (24 * 60 * 60 * 1000)).toInt() + 1
            if (days > selectedPersonnel!!.soldeConges) {
                showError("Solde de congés insuffisant (disponible: ${selectedPersonnel!!.soldeConges} jours)")
                return false
            }
        }

        return true
    }

    private fun submitForm() {
        val absence = Absence(
            id = if (absenceId != 0L) absenceId else null,
            personnelId = selectedPersonnel!!.id,
            personnelNom = selectedPersonnel!!.nomFr,
            personnelPrenom = selectedPersonnel!!.prenomFr,
            personnelPpr = selectedPersonnel!!.ppr,
            dateDebut = dateDebut!!,
            dateFin = dateFin!!,
            type = selectedAbsenceType!!,
            motif = binding.etMotif.text.toString().trim().ifEmpty { null },
            justificatifUrl = null, // TODO: Gérer l'upload
            estValideeParAdmin = false
        )

        if (absenceId != 0L) {
            absenceViewModel.updateAbsence(absence)
        } else {
            absenceViewModel.createAbsence(absence)
        }
    }

    private fun showError(message: String) {
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