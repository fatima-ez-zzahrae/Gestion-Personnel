package com.ensa.gestionpersonnel.ui.personnel.form

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.data.remote.dto.PersonnelDto
import com.ensa.gestionpersonnel.databinding.FragmentPersonnelFormBinding
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class PersonnelFormFragment : Fragment() {

    private var _binding: FragmentPersonnelFormBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonnelFormViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_person_placeholder)
                    .circleCrop()
                    .into(binding.ivPhoto)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonnelFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupDropdowns()
        setupDatePickers()
        setupClickListeners()
        setupObservers()
    }

    private fun setupUI() {
        // Titre dynamique
        binding.tvTitle.text = if (viewModel.isEditMode) {
            "Modifier le Personnel"
        } else {
            "Ajouter un Personnel"
        }
    }

    private fun setupDropdowns() {
        // Sexe
        val sexeItems = arrayOf("Masculin", "Féminin")
        val sexeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, sexeItems)
        binding.actvSexe.setAdapter(sexeAdapter)

        // Type d'employé
        val typeEmployeItems = arrayOf("Pédagogique", "Administratif", "Technique")
        val typeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, typeEmployeItems)
        binding.actvTypeEmploye.setAdapter(typeAdapter)

        // Grade
        val gradeItems = arrayOf(
            "Professeur de l'Enseignement Supérieur",
            "Professeur Habilité",
            "Professeur Agrégé",
            "Professeur Assistant",
            "Ingénieur en Chef",
            "Ingénieur d'État",
            "Technicien",
            "Administrateur",
            "Adjoint Administratif"
        )
        val gradeAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, gradeItems)
        binding.actvGrade.setAdapter(gradeAdapter)
    }

    private fun setupDatePickers() {
        // Date de naissance - Permet saisie manuelle ET calendrier
        // L'utilisateur peut soit taper directement (format YYYY-MM-DD) soit utiliser le calendrier
        binding.tilDateNaissance.setEndIconOnClickListener {
            showDatePicker { date ->
                binding.etDateNaissance.setText(date)
            }
        }
        
        // Date recrutement Ministère
        binding.etDateRecrutementMinistere.setOnClickListener {
            showDatePicker { date ->
                binding.etDateRecrutementMinistere.setText(date)
            }
        }

        // Date recrutement ENSA
        binding.etDateRecrutementENSA.setOnClickListener {
            showDatePicker { date ->
                binding.etDateRecrutementENSA.setText(date)
            }
        }
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        
        datePickerDialog.show()
    }

    private fun setupClickListeners() {
        // Bouton retour
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Sélection de photo
        binding.btnSelectPhoto.setOnClickListener {
            openImagePicker()
        }

        // Bouton Annuler
        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        // Bouton Enregistrer
        binding.btnSave.setOnClickListener {
            savePersonnel()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun setupObservers() {
        // Observer pour charger les données en mode édition
        viewModel.personnelState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    result.data?.let { personnel ->
                        fillFormWithData(personnel)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observer pour la sauvegarde
        viewModel.saveState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val message = if (viewModel.isEditMode) {
                        "Personnel modifié avec succès"
                    } else {
                        "Personnel ajouté avec succès"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fillFormWithData(personnel: com.ensa.gestionpersonnel.domain.model.Personnel) {
        binding.apply {
            etPpr.setText(personnel.ppr)
            etCin.setText(personnel.cin)
            etNomFr.setText(personnel.nomFr)
            etPrenomFr.setText(personnel.prenomFr)
            etNomAr.setText(personnel.nomAr)
            etPrenomAr.setText(personnel.prenomAr)
            actvSexe.setText(personnel.sexe, false)
            etDateNaissance.setText(personnel.dateNaissance)
            etLieuNaissance.setText(personnel.lieuNaissance)
            etEmail.setText(personnel.email)
            etTelephone.setText(personnel.telephone)
            etAdresse.setText(personnel.adresse)
            actvTypeEmploye.setText(personnel.typeEmploye, false)
            etDateRecrutementMinistere.setText(personnel.dateRecrutementMinistere)
            etDateRecrutementENSA.setText(personnel.dateRecrutementENSA)
            actvGrade.setText(personnel.gradeActuel, false)
            etEchelle.setText(personnel.echelleActuelle.toString())
            etEchelon.setText(personnel.echelonActuel.toString())
            etSoldeConges.setText(personnel.soldeConges.toString())
            switchActif.isChecked = personnel.estActif

            // Charger la photo
            personnel.photoUrl?.let { url ->
                try {
                    val uri = Uri.parse(url)
                    Glide.with(requireContext())
                        .load(uri)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivPhoto)
                    selectedImageUri = uri
                } catch (e: Exception) {
                    // Si ce n'est pas un URI valide, essayer comme URL
                    Glide.with(requireContext())
                        .load(url)
                        .placeholder(R.drawable.ic_person_placeholder)
                        .circleCrop()
                        .into(ivPhoto)
                }
            }
        }
    }

    private fun savePersonnel() {
        binding.apply {
            val ppr = etPpr.text.toString().trim()
            val cin = etCin.text.toString().trim()
            val nomFr = etNomFr.text.toString().trim()
            val prenomFr = etPrenomFr.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val telephone = etTelephone.text.toString().trim()

            // Validation
            val (isValid, errorMessage) = viewModel.validateForm(
                ppr, cin, nomFr, prenomFr, email, telephone
            )

            if (!isValid) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                return
            }

            // Vérifier les autres champs obligatoires
            if (actvSexe.text?.toString()?.isBlank() != false || 
                etDateNaissance.text?.toString()?.isBlank() != false ||
                etLieuNaissance.text?.toString()?.isBlank() != false || 
                etAdresse.text?.toString()?.isBlank() != false ||
                actvTypeEmploye.text?.toString()?.isBlank() != false || 
                etDateRecrutementMinistere.text?.toString()?.isBlank() != false ||
                etDateRecrutementENSA.text?.toString()?.isBlank() != false || 
                actvGrade.text?.toString()?.isBlank() != false ||
                etEchelle.text?.toString()?.isBlank() != false || 
                etEchelon.text?.toString()?.isBlank() != false) {
                
                Toast.makeText(context, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_LONG).show()
                return
            }

            // Créer le DTO
            val personnelDto = PersonnelDto(
                ppr = ppr,
                cin = cin,
                nomFr = nomFr,
                nomAr = etNomAr.text?.toString()?.trim() ?: "",
                prenomFr = prenomFr,
                prenomAr = etPrenomAr.text?.toString()?.trim() ?: "",
                email = email,
                telephone = telephone,
                dateNaissance = etDateNaissance.text?.toString() ?: "",
                lieuNaissance = etLieuNaissance.text?.toString()?.trim() ?: "",
                sexe = actvSexe.text?.toString() ?: "",
                adresse = etAdresse.text?.toString()?.trim() ?: "",
                photoUrl = selectedImageUri?.toString(), // URL de la photo sélectionnée
                typeEmploye = actvTypeEmploye.text?.toString() ?: "",
                dateRecrutementMinistere = etDateRecrutementMinistere.text?.toString() ?: "",
                dateRecrutementENSA = etDateRecrutementENSA.text?.toString() ?: "",
                gradeActuel = actvGrade.text?.toString() ?: "",
                echelleActuelle = etEchelle.text?.toString()?.toIntOrNull() ?: 1,
                echelonActuel = etEchelon.text?.toString()?.toIntOrNull() ?: 1,
                soldeConges = etSoldeConges.text?.toString()?.toIntOrNull() ?: 30,
                estActif = switchActif.isChecked
            )

            // Sauvegarder
            viewModel.savePersonnel(personnelDto)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
