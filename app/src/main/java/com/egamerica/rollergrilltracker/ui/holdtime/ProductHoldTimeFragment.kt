package com.egamerica.rollergrilltracker.ui.holdtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.egamerica.rollergrilltracker.databinding.FragmentProductHoldTimeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductHoldTimeFragment : Fragment() {

    private var _binding: FragmentProductHoldTimeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductHoldTimeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductHoldTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.recyclerViewHoldTimes.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set up adapter when layout is ready
    }

    private fun observeViewModel() {
        viewModel.slotsWithProducts.observe(viewLifecycleOwner, Observer { slotsWithProducts ->
            // TODO: Update adapter when layout is ready
        })

        viewModel.productHoldTimes.observe(viewLifecycleOwner, Observer { holdTimes ->
            // TODO: Update adapter when layout is ready
        })

        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}