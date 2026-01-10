package com.ensa.gestionpersonnel.ui.diplome.form

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
import com.ensa.gestionpersonnel.databinding.FragmentDiplomeFormBinding
import com.ensa.gestionpersonnel.domain.model.NiveauDiplome
import com.ensa.gestionpersonnel.domain.model.Personnel
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class DiplomeFormFragment : Fragment() {

    private var _binding: FragmentDiplomeFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiplomeFormViewModel by viewModels()
    private val args: DiplomeFormFragmentArgs by navArgs()

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var selectedDate: Date = Date()
    private var selectedPersonnelId: Long = 0L
    private var personnelList: List<Personnel> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiplomeFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Si personnelId est passé en argument, on le présélectionne
        if (args.personnelId != 0L) {
            selectedPersonnelId = args.personnelId
        }

        setupNiveauSpinner()
        setupDatePicker()
        setupObservers()
        setupListeners()

        // ✅ CHARGER LA LISTE DES PERSONNELS
        viewModel.loadPersonnelList()

        if (args.diplomeId != 0L) {
            viewModel.loadDiplome(args.diplomeId)
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

    private fun setupNiveauSpinner() {
        val niveaux = NiveauDiplome.values().map {
            it.name.replace("_", "+")
        }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            niveaux
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerNiveau.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.editTextDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = calendar.time
                    binding.editTextDate.setText(dateFormat.format(selectedDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupObservers() {
        // ✅ OBSERVER POUR LA LISTE DES PERSONNELS
        viewModel.personnelList.observe(viewLifecycleOwner) { list ->
            personnelList = list
            setupPersonnelSpinner()
        }

        viewModel.diplome.observe(viewLifecycleOwner) { diplome ->
            diplome?.let {
                binding.apply {
                    editTextIntitule.setText(it.intitule)
                    editTextSpecialite.setText(it.specialite)
                    editTextEtablissement.setText(it.etablissement)
                    editTextFichierPreuve.setText(it.fichierPreuve)

                    val niveauPosition = NiveauDiplome.values().indexOf(it.niveau)
                    spinnerNiveau.setSelection(niveauPosition)

                    selectedDate = it.dateObtention
                    editTextDate.setText(dateFormat.format(selectedDate))

                    selectedPersonnelId = it.personnelId
                    setupPersonnelSpinner() // Rafraîchir pour présélectionner
                }
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Diplôme enregistré", Toast.LENGTH_SHORT).show()
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
            saveDiplome()
        }

        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveDiplome() {
        val intitule = binding.editTextIntitule.text.toString()
        val specialite = binding.editTextSpecialite.text.toString()
        val etablissement = binding.editTextEtablissement.text.toString()
        val fichierPreuve = binding.editTextFichierPreuve.text.toString()
        val niveau = NiveauDiplome.values()[binding.spinnerNiveau.selectedItemPosition]

        // ✅ RÉCUPÉRER LE PERSONNEL SÉLECTIONNÉ
        val personnelPosition = binding.spinnerPersonnel.selectedItemPosition
        if (personnelPosition < 0 || personnelPosition >= personnelList.size) {
            Toast.makeText(requireContext(), "Veuillez sélectionner un personnel", Toast.LENGTH_SHORT).show()
            return
        }
        val personnelId = personnelList[personnelPosition].id

        viewModel.saveDiplome(
            id = args.diplomeId,
            personnelId = personnelId,
            intitule = intitule,
            specialite = specialite,
            niveau = niveau,
            etablissement = etablissement,
            dateObtention = selectedDate,
            fichierPreuve = fichierPreuve
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}