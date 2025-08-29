package com.egamerica.rollergrilltracker.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.egamerica.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private val viewModel: DashboardViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up click listeners for navigation
        view.findViewById<View>(R.id.card_sales).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_sales)
        }
        
        view.findViewById<View>(R.id.card_waste).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_waste)
        }
        
        view.findViewById<View>(R.id.card_suggestions).setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_suggestions)
        }
        
        // Set up observers for the current time period
        val currentTimePeriodText = view.findViewById<TextView>(R.id.text_current_time_period)
        viewModel.currentTimePeriod.observe(viewLifecycleOwner, Observer { timePeriod ->
            currentTimePeriodText.text = timePeriod.name
        })
        
        // Set up observers for sales summary
        val salesSummaryText = view.findViewById<TextView>(R.id.text_sales_summary)
        viewModel.todaySalesSummary.observe(viewLifecycleOwner, Observer { summary ->
            salesSummaryText.text = summary
        })
        
        // Set up observers for waste summary
        val wasteSummaryText = view.findViewById<TextView>(R.id.text_waste_summary)
        viewModel.todayWasteSummary.observe(viewLifecycleOwner, Observer { summary ->
            wasteSummaryText.text = summary
        })
        
        // Set up observers for suggestions
        val suggestionsText = view.findViewById<TextView>(R.id.text_suggestions)
        viewModel.topSuggestions.observe(viewLifecycleOwner, Observer { suggestions ->
            if (suggestions.isNotEmpty()) {
                suggestionsText.text = suggestions.joinToString("\n")
            } else {
                suggestionsText.text = getString(R.string.no_data_available)
            }
        })
    }
}