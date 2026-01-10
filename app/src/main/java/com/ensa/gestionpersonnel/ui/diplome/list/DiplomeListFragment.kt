package com.ensa.gestionpersonnel.ui.diplome.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentDiplomeListBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiplomeListFragment : Fragment() {

    private var _binding: FragmentDiplomeListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DiplomeListViewModel by viewModels()
    private lateinit var adapter: DiplomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiplomeListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadDiplomes()
    }

    private fun setupRecyclerView() {
        adapter = DiplomeAdapter(
            onItemClick = { diplome ->
                val action = DiplomeListFragmentDirections
                    .actionDiplomeListFragmentToDiplomeDetailFragment(diplome.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { diplome ->
                viewModel.deleteDiplome(diplome.id)
            }
        )

        binding.recyclerViewDiplomes.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@DiplomeListFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.diplomes.observe(viewLifecycleOwner) { diplomes ->
            adapter.submitList(diplomes)
            binding.emptyView.visibility = if (diplomes.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupListeners() {
        binding.fabAddDiplome.setOnClickListener {
            findNavController().navigate(
                R.id.action_diplomeListFragment_to_diplomeFormFragment
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}