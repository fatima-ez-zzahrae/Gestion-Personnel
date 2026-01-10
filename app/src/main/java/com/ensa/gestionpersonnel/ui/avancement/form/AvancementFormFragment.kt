package com.ensa.gestionpersonnel.ui.avancement.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ensa.gestionpersonnel.databinding.FragmentAvancementFormBinding
import com.ensa.gestionpersonnel.domain.model.Personnel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AvancementFormFragment : Fragment() {

    private var _binding: FragmentAvancementFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AvancementFormViewModel by viewModels()
    private val args: AvancementFormFragmentArgs by navArgs()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var dateDecision: Date = Date()
    private var dateEffet: Date = Date()
    private var selectedPersonnelId: Long = 0L
    private var personnelList: List<Personnel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvancementFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si personnelId est passé en argument, on le présélectionne
        if (args.personnelId != 0L) {
            selectedPersonnelId = args.personnelId
        }

        setupDatePickers()
        setupObservers()
        setupListeners()

        // ✅ CHARGER LA LISTE DES PERSONNELS
        viewModel.loadPersonnelList()

        if (args.avancementId != 0L) {
            viewModel.loadAvancement(args.avancementId)
        }
    }

    private fun setupPersonnelSpinner() {
        val personnelNames = personnelList.map { "${it.getNomComplet()} (${it.ppr})" }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            personnelNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerPersonnel.adapter = adapter

        // Présélectionner le personnel si déjà défini
        if (selectedPersonnelId != 0L) {
            val position = personnelList.indexOfFirst { it.id == selectedPersonnelId }
            if (position >= 0) {
                binding.spinnerPersonnel.setSelection(position)
            }
        }
    }

    private fun setupDatePickers() {
        binding.editTextDateDecision.setOnClickListener {
            showDatePicker { date ->
                dateDecision = date
                binding.editTextDateDecision.setText(dateFormat.format(dateDecision))
            }
        }

        binding.editTextDateEffet.setOnClickListener {
            showDatePicker { date ->
                dateEffet = date
                binding.editTextDateEffet.setText(dateFormat.format(dateEffet))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun setupObservers() {
        // ✅ OBSERVER POUR LA LISTE DES PERSONNELS
        viewModel.personnelList.observe(viewLifecycleOwner) { list ->
            personnelList = list
            setupPersonnelSpinner()
        }

        viewModel.avancement.observe(viewLifecycleOwner) { avancement ->
            avancement?.let {
                binding.apply {
                    editTextGradePrecedent.setText(it.gradePrecedent)
                    editTextGradeNouveau.setText(it.gradeNouveau)
                    editTextEchellePrecedente.setText(it.echellePrecedente.toString())
                    editTextEchelleNouvelle.setText(it.echelleNouvelle.toString())
                    editTextEchelonPrecedent.setText(it.echelonPrecedent.toString())
                    editTextEchelonNouveau.setText(it.echelonNouveau.toString())
                    editTextDescription.setText(it.description)

                    dateDecision = it.dateDecision
                    dateEffet = it.dateEffet
                    editTextDateDecision.setText(dateFormat.format(dateDecision))
                    editTextDateEffet.setText(dateFormat.format(dateEffet))

                    selectedPersonnelId = it.personnelId
                    setupPersonnelSpinner() // Rafraîchir pour présélectionner
                }
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Avancement enregistré", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            saveAvancement()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveAvancement() {
        val gradePrecedent = binding.editTextGradePrecedent.text.toString()
        val gradeNouveau = binding.editTextGradeNouveau.text.toString()
        val echellePrecedente = binding.editTextEchellePrecedente.text.toString().toIntOrNull() ?: 0
        val echelleNouvelle = binding.editTextEchelleNouvelle.text.toString().toIntOrNull() ?: 0
        val echelonPrecedent = binding.editTextEchelonPrecedent.text.toString().toIntOrNull() ?: 0
        val echelonNouveau = binding.editTextEchelonNouveau.text.toString().toIntOrNull() ?: 0
        val description = binding.editTextDescription.text.toString()

        // ✅ RÉCUPÉRER LE PERSONNEL SÉLECTIONNÉ
        val personnelPosition = binding.spinnerPersonnel.selectedItemPosition
        if (personnelPosition < 0 || personnelPosition >= personnelList.size) {
            Toast.makeText(requireContext(), "Veuillez sélectionner un personnel", Toast.LENGTH_SHORT).show()
            return
        }
        val personnelId = personnelList[personnelPosition].id

        viewModel.saveAvancement(
            id = args.avancementId,
            personnelId = personnelId,
            dateDecision = dateDecision,
            dateEffet = dateEffet,
            gradePrecedent = gradePrecedent,
            gradeNouveau = gradeNouveau,
            echellePrecedente = echellePrecedente,
            echelleNouvelle = echelleNouvelle,
            echelonPrecedent = echelonPrecedent,
            echelonNouveau = echelonNouveau,
            description = description
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}