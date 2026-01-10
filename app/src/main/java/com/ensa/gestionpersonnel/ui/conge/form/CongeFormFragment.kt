package com.ensa.gestionpersonnel.ui.conge.form

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentCongeFormBinding
import com.ensa.gestionpersonnel.domain.model.Absence
import com.ensa.gestionpersonnel.domain.model.AbsenceType
import com.ensa.gestionpersonnel.ui.absence.AbsenceViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class CongeFormFragment : Fragment() {

    private var _binding: FragmentCongeFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AbsenceViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var selectedDateDebut: Date? = null
    private var selectedDateFin: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCongeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupDatePickers()
    }

    private fun setupUI() {
        binding.toolbar.title = "Nouveau Congé Annuel"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSubmit.setOnClickListener {
            submitConge()
        }
    }

    private fun setupObservers() {
        viewModel.absenceOperation.observe(viewLifecycleOwner) { result ->
            when (result) {
                is com.ensa.gestionpersonnel.utils.NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSubmit.isEnabled = false
                }
                is com.ensa.gestionpersonnel.utils.NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Snackbar.make(binding.root, "Congé annuel créé avec succès", Snackbar.LENGTH_LONG).show()
                    findNavController().navigateUp()
                }
                is com.ensa.gestionpersonnel.utils.NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Snackbar.make(binding.root, "Erreur: ${result.message}", Snackbar.LENGTH_LONG).show()
                }
                else -> {}
            }
        }
    }

    private fun setupDatePickers() {
        binding.etDateDebut.setOnClickListener {
            showDatePickerDialog(true)
        }

        binding.etDateFin.setOnClickListener {
            showDatePickerDialog(false)
        }
    }

    private fun showDatePickerDialog(isStartDate: Boolean) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val selectedDate = calendar.time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

                if (isStartDate) {
                    selectedDateDebut = selectedDate
                    binding.etDateDebut.setText(dateFormat.format(selectedDate))
                } else {
                    selectedDateFin = selectedDate
                    binding.etDateFin.setText(dateFormat.format(selectedDate))
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun submitConge() {
        val personnelId = arguments?.getLong("personnelId") ?: 1L
        val dateDebut = selectedDateDebut ?: return
        val dateFin = selectedDateFin ?: return
        val motif = binding.etMotif.text.toString().trim()

        if (motif.isEmpty()) {
            binding.etMotif.error = "Veuillez saisir un motif"
            return
        }

        val absence = Absence(
            personnelId = personnelId,
            personnelNom = "Nom",
            personnelPrenom = "Prénom",
            personnelPpr = "PPR",
            dateDebut = dateDebut,
            dateFin = dateFin,
            type = AbsenceType.CONGE_ANNUEL,
            motif = motif,
            estValideeParAdmin = false
        )

        viewModel.createAbsence(absence)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}