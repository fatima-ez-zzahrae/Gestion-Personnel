package com.ensa.gestionpersonnel.ui.dashboard

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ensa.gestionpersonnel.databinding.FragmentDashboardBinding
import com.ensa.gestionpersonnel.data.remote.dto.DashboardStats
import com.ensa.gestionpersonnel.utils.NetworkResult
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupObservers()
    }

    private fun setupClickListeners() {
        binding.btnRefresh.setOnClickListener {
            viewModel.loadStats()
        }
    }

    private fun setupObservers() {
        viewModel.statsState.observe(viewLifecycleOwner) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnRefresh.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRefresh.isEnabled = true
                    result.data?.let { stats ->
                        updateUI(stats)
                    }
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRefresh.isEnabled = true
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateUI(stats: DashboardStats) {
        binding.tvTotalPersonnel.text = stats.totalPersonnel.toString()
        binding.tvPersonnelActif.text = stats.personnelActif.toString()
        binding.tvPersonnelInactif.text = stats.personnelInactif.toString()
        binding.tvMoyenneAnciennete.text = stats.moyenneAnciennete.toString()
        binding.tvTotalConges.text = stats.totalConges.toString()
        
        setupPieChart(stats.repartitionParType)
    }

    private fun setupPieChart(data: Map<String, Int>) {
        if (data.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        
        val entries = data.map { (type, count) ->
            PieEntry(count.toFloat(), type)
        }

        val dataSet = PieDataSet(entries, "Répartition").apply {
            colors = listOf(
                Color.parseColor("#4CAF50"), // Vert - Pédagogique
                Color.parseColor("#2196F3"), // Bleu - Administratif
                Color.parseColor("#FF9800")  // Orange - Technique
            )
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            setDrawValues(true)
        }

        val pieData = PieData(dataSet)
        
        binding.pieChart.apply {
            this.data = pieData
            description.isEnabled = false
            isRotationEnabled = true
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000)
            invalidate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

