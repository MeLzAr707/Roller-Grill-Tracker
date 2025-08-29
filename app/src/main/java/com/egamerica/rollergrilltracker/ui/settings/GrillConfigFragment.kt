package com.egamerica.rollergrilltracker.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.egamerica.rollergrilltracker.databinding.FragmentGrillConfigBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GrillConfigFragment : Fragment() {

    private var _binding: FragmentGrillConfigBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: GrillConfigViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrillConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewGrillConfigs.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set up adapter when layout is ready
    }

    private fun observeViewModel() {
        viewModel.grillConfigs.observe(viewLifecycleOwner, Observer { grillConfigs ->
            // TODO: Update adapter when layout is ready
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is GrillConfigViewModel.SaveStatus.SUCCESS -> {
                    // TODO: Show success message
                }
                is GrillConfigViewModel.SaveStatus.ERROR -> {
                    // TODO: Show error message
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}