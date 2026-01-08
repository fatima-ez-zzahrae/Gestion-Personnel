package com.ensa.gestionpersonnel.ui.personnel.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentPersonnelListBinding
import com.ensa.gestionpersonnel.domain.model.Personnel
import com.ensa.gestionpersonnel.utils.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class PersonnelListFragment : Fragment() {

    private var _binding: FragmentPersonnelListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PersonnelListViewModel by viewModels()
    
    private lateinit var personnelAdapter: PersonnelAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonnelListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSearchView()
        setupClickListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        personnelAdapter = PersonnelAdapter(
            onItemClick = { personnel ->
                val action = PersonnelListFragmentDirections
                    .actionPersonnelListToPersonnelDetail(personnel.id)
                findNavController().navigate(action)
            },
            onEditClick = { personnel ->
                val action = PersonnelListFragmentDirections
                    .actionPersonnelListToPersonnelForm(personnel.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { personnel ->
                showDeleteConfirmation(personnel)
            }
        )

        binding.rvPersonnel.apply {
            adapter = personnelAdapter
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupSearchView() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchJob?.cancel()
                searchJob = lifecycleScope.launch {
                    delay(500) // Debounce 500ms
                    s?.toString()?.let { query ->
                        viewModel.searchPersonnel(query)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupClickListeners() {
        binding.fabAdd.setOnClickListener {
            // Passer 0L pour indiquer le mode création
            val action = PersonnelListFragmentDirections
                .actionPersonnelListToPersonnelForm(0L)
            findNavController().navigate(action)
        }
    }

    private fun setupObservers() {
        // Observer pour la liste
        viewModel.personnelListState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvPersonnel.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    
                    if (result.data.isNullOrEmpty()) {
                        binding.rvPersonnel.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvPersonnel.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        personnelAdapter.submitList(result.data)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observer pour la suppression
        viewModel.deleteState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // Afficher dialog de chargement si nécessaire
                }
                is NetworkResult.Success -> {
                    Toast.makeText(context, "Personnel supprimé", Toast.LENGTH_SHORT).show()
                }
                is NetworkResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDeleteConfirmation(personnel: Personnel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirmer la suppression")
            .setMessage("Voulez-vous vraiment supprimer ${personnel.getNomComplet()} ?")
            .setPositiveButton("Supprimer") { _, _ ->
                viewModel.deletePersonnel(personnel.id)
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchJob?.cancel()
        _binding = null
    }
}

