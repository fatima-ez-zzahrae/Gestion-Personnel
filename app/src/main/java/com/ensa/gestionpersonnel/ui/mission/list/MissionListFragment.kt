package com.ensa.gestionpersonnel.ui.mission.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentMissionListBinding
import com.ensa.gestionpersonnel.domain.model.Mission
import com.ensa.gestionpersonnel.ui.mission.MissionViewModel
import com.ensa.gestionpersonnel.utils.NetworkResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MissionListFragment : Fragment() {

    private var _binding: FragmentMissionListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MissionViewModel by viewModels()
    private lateinit var missionAdapter: MissionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMissionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        setupFab()
        observeViewModel()

        viewModel.loadAllMissions()
    }

    private fun setupRecyclerView() {
        missionAdapter = MissionAdapter(
            onMissionClick = { mission ->
                navigateToMissionDetail(mission)
            },
            onMissionLongClick = { mission ->
                showMissionOptionsDialog(mission)
            }
        )

        binding.recyclerViewMissions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = missionAdapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { viewModel.searchMissions(it) }
                return true
            }
        })
    }

    private fun setupFab() {
        binding.fabAddMission.setOnClickListener {
            findNavController().navigate(
                R.id.action_missionListFragment_to_missionFormFragment
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.missionsState.collect { result ->
                when (result) {
                    is NetworkResult.Loading -> {
                        showLoading(true)
                    }
                    is NetworkResult.Success -> {
                        showLoading(false)
                        result.data?.let { missions ->
                            missionAdapter.submitList(missions)
                            binding.tvEmptyState.visibility =
                                if (missions.isEmpty()) View.VISIBLE else View.GONE
                        }
                    }
                    is NetworkResult.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            result.message ?: "Erreur de chargement",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.deleteState.collect { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            "Mission supprimée avec succès",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetDeleteState()
                    }
                    is NetworkResult.Error -> {
                        Toast.makeText(
                            requireContext(),
                            result.message ?: "Erreur de suppression",
                            Toast.LENGTH_SHORT
                        ).show()
                        viewModel.resetDeleteState()
                    }
                    else -> {}
                }
            }
        }
    }

    private fun navigateToMissionDetail(mission: Mission) {
        val action = MissionListFragmentDirections
            .actionMissionListFragmentToMissionDetailFragment(mission.id)
        findNavController().navigate(action)
    }

    private fun showMissionOptionsDialog(mission: Mission) {
        val options = arrayOf(
            "Voir détails",
            "Modifier",
            "Clôturer",
            "Supprimer"
        )

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mission - ${mission.destination}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> navigateToMissionDetail(mission)
                    1 -> navigateToMissionForm(mission)
                    2 -> showCloturerConfirmation(mission)
                    3 -> showDeleteConfirmation(mission)
                }
            }
            .show()
    }

    private fun navigateToMissionForm(mission: Mission) {
        val action = MissionListFragmentDirections
            .actionMissionListFragmentToMissionFormFragment(mission.id)
        findNavController().navigate(action)
    }

    private fun showCloturerConfirmation(mission: Mission) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Clôturer la mission")
            .setMessage("Voulez-vous vraiment clôturer cette mission ?")
            .setPositiveButton("Oui") { _, _ ->
                viewModel.cloturerMission(mission.id)
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun showDeleteConfirmation(mission: Mission) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Supprimer la mission")
            .setMessage("Voulez-vous vraiment supprimer cette mission ? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deleteMission(mission.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.recyclerViewMissions.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}