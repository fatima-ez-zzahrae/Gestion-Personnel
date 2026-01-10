package com.ensa.gestionpersonnel.ui.diplome_avancement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentDiplomeAvancementMenuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DiplomeAvancementMenuFragment : Fragment() {

    private var _binding: FragmentDiplomeAvancementMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiplomeAvancementMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
    }

    private fun setupListeners() {
        binding.apply {
            // Navigation vers la liste des diplômes
            cardDiplomes.setOnClickListener {
                findNavController().navigate(
                    R.id.action_diplomeAvancementMenuFragment_to_diplomeListFragment
                )
            }

            // Navigation vers la liste des avancements
            cardAvancements.setOnClickListener {
                findNavController().navigate(
                    R.id.action_diplomeAvancementMenuFragment_to_avancementListFragment
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}