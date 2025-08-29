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
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductHoldTimeFragment : Fragment() {

    private var _binding: FragmentProductHoldTimeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductHoldTimeViewModel by viewModels()
    
    private lateinit var pagerAdapter: GrillHoldTimePagerAdapter

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
        binding.expiredProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // TODO: Set up adapter for expired products when ready
    }

    private fun setupViewPager(grillConfigs: List<com.egamerica.rollergrilltracker.data.entities.GrillConfig>) {
        pagerAdapter = GrillHoldTimePagerAdapter(requireActivity(), grillConfigs)
        binding.grillViewPager.adapter = pagerAdapter
        
        // Setup TabLayout with ViewPager2
        TabLayoutMediator(binding.grillTabs, binding.grillViewPager) { tab, position ->
            tab.text = pagerAdapter.getGrillName(position)
        }.attach()
    }

    private fun observeViewModel() {
        viewModel.activeGrillConfigs.observe(viewLifecycleOwner, Observer { grillConfigs ->
            if (grillConfigs.isNotEmpty()) {
                setupViewPager(grillConfigs)
            }
        })

        viewModel.expiredProducts.observe(viewLifecycleOwner, Observer { expiredProducts ->
            if (expiredProducts.isEmpty()) {
                binding.noExpiredProductsText.visibility = View.VISIBLE
                binding.expiredProductsRecyclerView.visibility = View.GONE
            } else {
                binding.noExpiredProductsText.visibility = View.GONE
                binding.expiredProductsRecyclerView.visibility = View.VISIBLE
                // TODO: Update adapter when ready
            }
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