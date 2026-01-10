
package com.ensa.gestionpersonnel.ui.mission.detail

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentMissionDetailBinding
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.domain.model.StatutMission
import com.ensa.gestionpersonnel.ui.mission.MissionViewModel
import com.ensa.gestionpersonnel.utils.NetworkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class MissionDetailFragment : Fragment() {

    private var _binding: FragmentMissionDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MissionViewModel by viewModels()
    private val args: MissionDetailFragmentArgs by navArgs()

    private var currentMission: Mission? = null
    private val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.FRENCH)

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                uploadRapport(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupButtons()
        observeViewModel()

        viewModel.loadMissionDetail(args.missionId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_edit -> {
                    navigateToEdit()
                    true
                }
                R.id.action_delete -> {
                    showDeleteConfirmation()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupButtons() {
        binding.btnUploadRapport.setOnClickListener {
            openFilePicker()
        }

        binding.btnCloturer.setOnClickListener {
            showCloturerConfirmation()
        }

        binding.btnViewRapport.setOnClickListener {
            currentMission?.rapportUrl?.let { url ->
                openRapport(url)
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.missionDetailState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading(true)
                    }
                    is NetworkResult.Success -> {
                        showLoading(false)
                        result.data?.let { displayMissionDetails(it) }
                    }
                    is NetworkResult.Error -> {
                        showLoading(false)
                        Toast.makeText(requireContext(), result.message ?: "Erreur de chargement", Toast.LENGTH_SHORT).show()
                    }
                    null -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Toast.makeText(requireContext(), "Opération réussie", Toast.LENGTH_SHORT).show()
                        viewModel.resetOperationState()
                        viewModel.loadMissionDetail(args.missionId)
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), result.message ?: "Erreur d'opération", Toast.LENGTH_SHORT).show()
                        viewModel.resetOperationState()
                    }
                    else -> {}
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Toast.makeText(requireContext(), "Mission supprimée", Toast.LENGTH_SHORT).show()
                        viewModel.resetDeleteState()
                        findNavController().navigateUp()
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(requireContext(), result.message ?: "Erreur de suppression", Toast.LENGTH_SHORT).show()
                        viewModel.resetDeleteState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun displayMissionDetails(mission: Mission) {
        currentMission = mission

        binding.tvDestination.text = mission.destination
        binding.tvObjetMission.text = mission.objetMission

        val statutText = when (mission.statut) {
            StatutMission.PLANIFIEE -> "PLANIFIÉE"
            StatutMission.EN_COURS -> "EN COURS"
            StatutMission.TERMINEE -> "TERMINÉE"
            StatutMission.ANNULEE -> "ANNULÉE"
        }
        binding.tvStatut.text = statutText

        when (mission.statut) {
            StatutMission.PLANIFIEE -> {
                binding.tvStatut.setBackgroundResource(R.drawable.bg_status_pending)
            }
            StatutMission.EN_COURS, StatutMission.TERMINEE -> {
                binding.tvStatut.setBackgroundResource(R.drawable.bg_status_validated)
            }
            StatutMission.ANNULEE -> {
                binding.tvStatut.setBackgroundResource(R.drawable.bg_status_pending)
            }
        }

        val personnelName = "${mission.personnelPrenom} ${mission.personnelNom}"
        binding.tvPersonnelNom.text = personnelName
        binding.tvPersonnelPpr.text = "PPR: ${mission.personnelId}"

        binding.tvDateDepart.text = dateFormat.format(mission.dateDepart)
        binding.tvDateRetour.text = dateFormat.format(mission.dateRetour)

        val duree = mission.getDureeJours()
        binding.tvDuree.text = "Durée: $duree jour${if (duree > 1) "s" else ""}"

        if (mission.rapportUrl != null) {
            binding.cardRapport.visibility = View.VISIBLE
        } else {
            binding.cardRapport.visibility = View.GONE
        }

        if (mission.statut == StatutMission.TERMINEE || mission.statut == StatutMission.ANNULEE) {
            binding.layoutActions.visibility = View.GONE
        } else {
            binding.layoutActions.visibility = View.VISIBLE
            binding.btnCloturer.isEnabled = mission.statut != StatutMission.TERMINEE
        }
    }

    private fun navigateToEdit() {
        val action = MissionDetailFragmentDirections
            .actionMissionDetailFragmentToMissionFormFragment(args.missionId)
        findNavController().navigate(action)
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer la mission")
            .setMessage("Voulez-vous vraiment supprimer cette mission ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteMission(args.missionId)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showCloturerConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clôturer la mission")
            .setMessage("Voulez-vous marquer cette mission comme terminée ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.cloturerMission(args.missionId)
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        filePickerLauncher.launch(intent)
    }

    private fun uploadRapport(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val file = File(requireContext().cacheDir, "rapport_${System.currentTimeMillis()}.pdf")

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("rapport", file.name, requestFile)

            viewModel.uploadRapport(args.missionId, body)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de la préparation du fichier", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRapport(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Impossible d'ouvrir le rapport", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
