package com.ensa.gestionpersonnel.ui.mission.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentMissionFormBinding
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.ensa.gestionpersonnel.ui.mission.MissionViewModel
import com.ensa.gestionpersonnel.ui.personnel.list.PersonnelListViewModel
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MissionFormFragment : Fragment() {

    private var _binding: FragmentMissionFormBinding? = null
    private val binding get() = _binding!!

    private val missionViewModel: MissionViewModel by viewModels()
    private val personnelViewModel: PersonnelListViewModel by viewModels()
    private val args: MissionFormFragmentArgs by navArgs()

    private var personnelList: List<Personnel> = emptyList()
    private var selectedPersonnel: Personnel? = null
    private var currentMission: Mission? = null

    private var dateDepart: Date? = null
    private var dateRetour: Date? = null

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupStatutDropdown()
        setupDatePickers()
        setupButtons()
        observeViewModel()

        // CORRECTION: Utiliser le bon nom de méthode
        personnelViewModel.loadPersonnelList()

        if (args.missionId != -1L) {
            binding.toolbar.title = "Modifier Mission"
            missionViewModel.loadMissionDetail(args.missionId)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupStatutDropdown() {
        val statuts = arrayOf("Planifiée", "En cours", "Terminée", "Annulée")
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, statuts)
        binding.actvStatut.setAdapter(adapter)
        binding.actvStatut.setText(statuts[0], false)
    }

    private fun setupPersonnelDropdown(personnel: List<Personnel>) {
        personnelList = personnel
        val personnelNames = personnel.map { "${it.prenomFr} ${it.nomFr}" }.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, personnelNames)
        binding.actvPersonnel.setAdapter(adapter)

        binding.actvPersonnel.setOnItemClickListener { _, _, position, _ ->
            selectedPersonnel = personnel[position]
        }
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        binding.etDateDepart.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateDepart = calendar.time
                    binding.etDateDepart.setText(dateFormat.format(dateDepart!!))
                    calculateDuree()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.tilDateDepart.setEndIconOnClickListener {
            binding.etDateDepart.performClick()
        }

        binding.etDateRetour.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dateRetour = calendar.time
                    binding.etDateRetour.setText(dateFormat.format(dateRetour!!))
                    calculateDuree()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.tilDateRetour.setEndIconOnClickListener {
            binding.etDateRetour.performClick()
        }
    }

    private fun calculateDuree() {
        if (dateDepart != null && dateRetour != null) {
            val diff = dateRetour!!.time - dateDepart!!.time
            val days = (diff / (1000 * 60 * 60 * 24)).toInt() + 1

            if (days > 0) {
                binding.tvDureeEstimee.visibility = View.VISIBLE
                binding.tvDureeEstimee.text = "Durée estimée: $days jour${if (days > 1) "s" else ""}"
            } else {
                binding.tvDureeEstimee.visibility = View.GONE
                Toast.makeText(requireContext(), "La date de retour doit être après la date de départ", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            if (validateForm()) {
                saveMission()
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true

        if (binding.etDestination.text.isNullOrBlank()) {
            binding.tilDestination.error = "La destination est requise"
            isValid = false
        } else {
            binding.tilDestination.error = null
        }

        if (binding.etObjetMission.text.isNullOrBlank()) {
            binding.tilObjetMission.error = "L'objet de la mission est requis"
            isValid = false
        } else {
            binding.tilObjetMission.error = null
        }

        if (selectedPersonnel == null) {
            binding.tilPersonnel.error = "Veuillez sélectionner un personnel"
            isValid = false
        } else {
            binding.tilPersonnel.error = null
        }

        if (dateDepart == null) {
            binding.tilDateDepart.error = "La date de départ est requise"
            isValid = false
        } else {
            binding.tilDateDepart.error = null
        }

        if (dateRetour == null) {
            binding.tilDateRetour.error = "La date de retour est requise"
            isValid = false
        } else {
            binding.tilDateRetour.error = null
        }

        if (dateDepart != null && dateRetour != null && dateRetour!!.before(dateDepart)) {
            binding.tilDateRetour.error = "La date de retour doit être après la date de départ"
            isValid = false
        }

        return isValid
    }

    private fun saveMission() {
        val destination = binding.etDestination.text.toString()
        val objetMission = binding.etObjetMission.text.toString()
        val personnel = selectedPersonnel!!

        val statut = when (binding.actvStatut.text.toString()) {
            "Planifiée" -> StatutMission.PLANIFIEE
            "En cours" -> StatutMission.EN_COURS
            "Terminée" -> StatutMission.TERMINEE
            "Annulée" -> StatutMission.ANNULEE
            else -> StatutMission.PLANIFIEE
        }

        val mission = Mission(
            id = currentMission?.id ?: 0,
            destination = destination,
            objetMission = objetMission,
            dateDepart = dateDepart!!,
            dateRetour = dateRetour!!,
            statut = statut,
            rapportUrl = currentMission?.rapportUrl,
            personnelId = personnel.id,
            personnelNom = personnel.nomFr,
            personnelPrenom = personnel.prenomFr
        )

        if (currentMission == null) {
            missionViewModel.createMission(mission)
        } else {
            missionViewModel.updateMission(mission)
        }
    }

    private fun observeViewModel() {
        // CORRECTION: Observer LiveData au lieu de StateFlow
        personnelViewModel.personnelListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let { setupPersonnelDropdown(it) }
                }
                is NetworkResult.Error -> {
                    Toast.makeText(requireContext(), "Erreur lors du chargement du personnel", Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            missionViewModel.missionDetailState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let { fillFormWithMission(it) }
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), "Erreur lors du chargement de la mission", Toast.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            missionViewModel.operationState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading(true)
                    }
                    is NetworkResult.Success -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), "Mission enregistrée avec succès", Toast.LENGTH_SHORT).show()
                        missionViewModel.resetOperationState()
                        findNavController().navigateUp()
                    }
                    is NetworkResult.Error -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), result.message ?: "Erreur lors de l'enregistrement", Toast.LENGTH_SHORT).show()
                        missionViewModel.resetOperationState()
                    }
                    null -> {}
                }
            }
        }
    }

    private fun fillFormWithMission(mission: Mission) {
        currentMission = mission

        binding.etDestination.setText(mission.destination)
        binding.etObjetMission.setText(mission.objetMission)

        dateDepart = mission.dateDepart
        dateRetour = mission.dateRetour

        binding.etDateDepart.setText(dateFormat.format(mission.dateDepart))
        binding.etDateRetour.setText(dateFormat.format(mission.dateRetour))

        calculateDuree()

        val personnelIndex = personnelList.indexOfFirst { it.id == mission.personnelId }
        if (personnelIndex != -1) {
            selectedPersonnel = personnelList[personnelIndex]
            binding.actvPersonnel.setText("${mission.personnelPrenom} ${mission.personnelNom}", false)
        }

        val statutText = when (mission.statut) {
            StatutMission.PLANIFIEE -> "Planifiée"
            StatutMission.EN_COURS -> "En cours"
            StatutMission.TERMINEE -> "Terminée"
            StatutMission.ANNULEE -> "Annulée"
        }
        binding.actvStatut.setText(statutText, false)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !isLoading
        binding.btnCancel.isEnabled = !isLoading
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}