package com.ensa.gestionpersonnel.ui.absence.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ensa.gestionpersonnel.R
import com.ensa.gestionpersonnel.databinding.FragmentAbsenceMenuBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AbsenceMenuFragment : Fragment() {

    private var _binding: FragmentAbsenceMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAbsenceMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.toolbar.title = "Gestion des Absences"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.action_absenceMenu_to_dashboard)
        }

        // Option 1: Gérer les congés
        binding.cardConges.setOnClickListener {
            findNavController().navigate(R.id.action_absenceMenu_to_congeList)
        }

        // Option 2: Gérer les absences exceptionnelles
        binding.cardAbsencesExceptionnelles.setOnClickListener {
            findNavController().navigate(R.id.action_absenceMenu_to_absenceExceptionnelleList)
        }

        // Option 3: Gérer les absences non justifiées
        binding.cardAbsencesNonJustifiees.setOnClickListener {
            findNavController().navigate(R.id.action_absenceMenu_to_absenceNonJustifieeList)
        }

        // Option 4: Gérer les absences maladie (remplace ordres de mission)
        binding.cardOrdresMission.setOnClickListener {
            findNavController().navigate(R.id.action_absenceMenu_to_absenceMaladieList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}