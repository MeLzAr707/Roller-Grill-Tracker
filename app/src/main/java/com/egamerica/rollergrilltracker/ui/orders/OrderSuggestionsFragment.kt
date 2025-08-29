package com.yourcompany.rollergrilltracker.ui.orders

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OrderSuggestionsFragment : Fragment() {

    private val viewModel: OrderSuggestionsViewModel by viewModels()
    
    private lateinit var dateButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var totalCasesTextView: TextView
    private lateinit var totalUnitsTextView: TextView
    private lateinit var totalProductsTextView: TextView
    private lateinit var exportButton: Button
    private lateinit var refreshButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    
    private val orderSuggestionAdapter = OrderSuggestionAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_suggestions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        dateButton = view.findViewById(R.id.button_date)
        recyclerView = view.findViewById(R.id.recycler_suggestions)
        emptyView = view.findViewById(R.id.text_empty)
        totalCasesTextView = view.findViewById(R.id.text_total_cases)
        totalUnitsTextView = view.findViewById(R.id.text_total_units)
        totalProductsTextView = view.findViewById(R.id.text_total_products)
        exportButton = view.findViewById(R.id.button_export)
        refreshButton = view.findViewById(R.id.fab_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = orderSuggestionAdapter
        
        // Set up click listeners
        dateButton.setOnClickListener {
            showDatePicker()
        }
        
        exportButton.setOnClickListener {
            viewModel.exportOrder()
        }
        
        refreshButton.setOnClickListener {
            viewModel.refreshOrderSuggestions()
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe selected date
        viewModel.selectedDate.observe(viewLifecycleOwner, Observer { date ->
            dateButton.text = formatDisplayDate(date)
        })
        
        // Observe order suggestions
        viewModel.orderSuggestions.observe(viewLifecycleOwner, Observer { suggestions ->
            orderSuggestionAdapter.submitList(suggestions)
            
            if (suggestions.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        })
        
        // Observe order summary
        viewModel.orderSummary.observe(viewLifecycleOwner, Observer { summary ->
            totalCasesTextView.text = summary.totalCases.toString()
            totalUnitsTextView.text = summary.totalUnits.toString()
            totalProductsTextView.text = summary.totalProducts.toString()
        })
        
        // Observe export status
        viewModel.exportStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is OrderSuggestionsViewModel.ExportStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_order_exported),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is OrderSuggestionsViewModel.ExportStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_export_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val currentDate = viewModel.selectedDate.value ?: Date()
        
        calendar.time = currentDate
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                viewModel.setSelectedDate(calendar.time)
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    private fun formatDisplayDate(date: Date): String {
        val outputFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
        return outputFormat.format(date)
    }
    
    class OrderSuggestionAdapter : 
            RecyclerView.Adapter<OrderSuggestionAdapter.OrderSuggestionViewHolder>() {
        
        private var items: List<OrderSuggestionsViewModel.OrderSuggestionItem> = emptyList()
        
        fun submitList(newItems: List<OrderSuggestionsViewModel.OrderSuggestionItem>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderSuggestionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_order_suggestion, parent, false)
            return OrderSuggestionViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: OrderSuggestionViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class OrderSuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val unitsPerCaseTextView: TextView = itemView.findViewById(R.id.text_units_per_case)
            private val suggestedCasesTextView: TextView = itemView.findViewById(R.id.text_suggested_cases)
            private val suggestedUnitsTextView: TextView = itemView.findViewById(R.id.text_suggested_units)
            private val totalUnitsTextView: TextView = itemView.findViewById(R.id.text_total_units)
            
            fun bind(item: OrderSuggestionsViewModel.OrderSuggestionItem) {
                productNameTextView.text = item.product.name
                unitsPerCaseTextView.text = item.unitsPerCase.toString()
                suggestedCasesTextView.text = item.suggestedCases.toString()
                suggestedUnitsTextView.text = item.suggestedUnits.toString()
                totalUnitsTextView.text = item.totalUnits.toString()
            }
        }
    }
}