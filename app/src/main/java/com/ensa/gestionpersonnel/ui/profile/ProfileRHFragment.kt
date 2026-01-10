package com.ensa.gestionpersonnel.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.databinding.FragmentProfileRhBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileRHFragment : Fragment() {

    private var _binding: FragmentProfileRhBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()
    private var hasChanges = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileRhBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupObservers()
        setupTextChangeListeners()

        // Charger le profil
        viewModel.loadProfile()
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnUpdateProfile.setOnClickListener {
            if (validateInputs()) {
                showSaveConfirmation()
            }
        }

        binding.btnReset.setOnClickListener {
            showResetConfirmation()
        }
    }

    private fun setupObservers() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile?.let {
                binding.edtNom.editText?.setText(it.nom)
                binding.edtPrenom.editText?.setText(it.prenom)
                binding.edtUsername.editText?.setText(it.username)
                binding.edtEmail.editText?.setText(it.email)
                binding.tvFullName.text = "${it.prenom} ${it.nom}"
                hasChanges = false
                updateSaveButtonState()
            }
        }

        // Observer isLoading avec observe (pas collect)
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            isLoading?.let {
                binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
                binding.btnUpdateProfile.isEnabled = !it && hasChanges
                binding.btnReset.isEnabled = !it
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            success?.let {
                if (it == true) {
                    Toast.makeText(
                        requireContext(),
                        "Profil mis à jour avec succès",
                        Toast.LENGTH_SHORT
                    ).show()
                    hasChanges = false
                    updateSaveButtonState()
                } else if (it == false) {
                    Toast.makeText(
                        requireContext(),
                        "Erreur lors de la mise à jour",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun setupTextChangeListeners() {
        listOf(
            binding.edtNom,
            binding.edtPrenom,
            binding.edtUsername,
            binding.edtEmail
        ).forEach { textInputLayout ->
            textInputLayout.editText?.doAfterTextChanged {
                hasChanges = true
                updateSaveButtonState()
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.edtNom.editText?.text.toString().trim().isEmpty()) {
            binding.edtNom.error = "Le nom est requis"
            isValid = false
        } else {
            binding.edtNom.error = null
        }

        if (binding.edtPrenom.editText?.text.toString().trim().isEmpty()) {
            binding.edtPrenom.error = "Le prénom est requis"
            isValid = false
        } else {
            binding.edtPrenom.error = null
        }

        return isValid
    }

    private fun updateSaveButtonState() {
        val isLoading = viewModel.isLoading.value ?: false
        binding.btnUpdateProfile.isEnabled = hasChanges && !isLoading
    }

    private fun showSaveConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmer la mise à jour")
            .setMessage("Voulez-vous sauvegarder les modifications ?")
            .setPositiveButton("Sauvegarder") { _, _ ->
                saveProfile()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showResetConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Réinitialiser")
            .setMessage("Voulez-vous annuler toutes les modifications ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.loadProfile()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun saveProfile() {
        val newProfile = com.ensa.gestionpersonnel.domain.model.ResponsableRH(
            id = 1,
            nom = binding.edtNom.editText?.text.toString().trim(),
            prenom = binding.edtPrenom.editText?.text.toString().trim(),
            username = binding.edtUsername.editText?.text.toString().trim(),
            email = binding.edtEmail.editText?.text.toString().trim()
        )
        viewModel.updateProfile(newProfile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}