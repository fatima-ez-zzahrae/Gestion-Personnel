package com.ensa.gestionpersonnel.ui.avancement.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentAvancementListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AvancementListFragment : Fragment() {

    private var _binding: FragmentAvancementListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AvancementListViewModel by viewModels()
    private lateinit var adapter: AvancementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAvancementListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadAvancements()
    }

    private fun setupRecyclerView() {
        adapter = AvancementAdapter(
            onItemClick = { avancement ->
                val action = AvancementListFragmentDirections
                    .actionAvancementListFragmentToAvancementDetailFragment(avancement.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { avancement ->
                viewModel.deleteAvancement(avancement.id)
            }
        )

        binding.recyclerViewAvancements.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AvancementListFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.avancements.observe(viewLifecycleOwner) { avancements ->
            adapter.submitList(avancements)
            binding.emptyView.visibility = if (avancements.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.fabAddAvancement.setOnClickListener {
            findNavController().navigate(
                R.id.action_avancementListFragment_to_avancementFormFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}