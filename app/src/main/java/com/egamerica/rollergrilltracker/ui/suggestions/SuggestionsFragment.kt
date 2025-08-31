package com.egamerica.rollergrilltracker.ui.suggestions

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.egamerica.rollergrilltracker.R
import com.egamerica.rollergrilltracker.data.entities.TimePeriod
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SuggestionsFragment : Fragment() {

    private val viewModel: SuggestionsViewModel by viewModels()
    
    private lateinit var dateButton: Button
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var refreshButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    
    private val suggestionAdapter = SuggestionAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_suggestions, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        dateButton = view.findViewById(R.id.button_date)
        timePeriodSpinner = view.findViewById(R.id.spinner_time_period)
        recyclerView = view.findViewById(R.id.recycler_suggestions)
        emptyView = view.findViewById(R.id.text_empty)
        refreshButton = view.findViewById(R.id.fab_refresh)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = suggestionAdapter
        
        // Set up click listeners
        dateButton.setOnClickListener {
            showDatePicker()
        }
        
        refreshButton.setOnClickListener {
            viewModel.refreshSuggestions()
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
        
        // Observe time periods
        viewModel.timePeriods.observe(viewLifecycleOwner, Observer { periods ->
            setupTimePeriodSpinner(periods)
        })
        
        // Observe selected time period
        viewModel.selectedTimePeriod.observe(viewLifecycleOwner, Observer { period ->
            val position = (timePeriodSpinner.adapter as? ArrayAdapter<TimePeriod>)?.getPosition(period) ?: 0
            timePeriodSpinner.setSelection(position)
        })
        
        // Observe suggestions
        viewModel.suggestions.observe(viewLifecycleOwner, Observer { suggestions ->
            suggestionAdapter.submitList(suggestions)
            
            if (suggestions.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        })
    }
    
    private fun setupTimePeriodSpinner(periods: List<TimePeriod>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            periods
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        timePeriodSpinner.adapter = adapter
        
        timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedPeriod = periods[position]
                viewModel.setSelectedTimePeriod(selectedPeriod)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
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
    
    class SuggestionAdapter : 
            RecyclerView.Adapter<SuggestionAdapter.SuggestionViewHolder>() {
        
        private var items: List<com.egamerica.rollergrilltracker.data.dao.SuggestionWithProduct> = emptyList()
        
        fun submitList(newItems: List<com.egamerica.rollergrilltracker.data.dao.SuggestionWithProduct>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_suggestion, parent, false)
            return SuggestionViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val quantityTextView: TextView = itemView.findViewById(R.id.text_quantity)
            private val confidenceTextView: TextView = itemView.findViewById(R.id.text_confidence)
            
            fun bind(item: com.egamerica.rollergrilltracker.data.dao.SuggestionWithProduct) {
                productNameTextView.text = item.name
                quantityTextView.text = item.suggestedQuantity.toString()
                
                val confidencePercent = String.format("%.0f", item.confidenceScore)
                confidenceTextView.text = "$confidencePercent%"
            }
        }
    }
}