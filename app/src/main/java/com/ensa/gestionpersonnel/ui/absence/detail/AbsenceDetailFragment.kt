package com.ensa.gestionpersonnel.ui.absence.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.databinding.FragmentAbsenceDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AbsenceDetailFragment : Fragment() {

    private var _binding: FragmentAbsenceDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsenceDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // TODO: Charger les détails de l'absence
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}