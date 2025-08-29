package com.yourcompany.rollergrilltracker.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class OrderSettingsFragment : Fragment() {

    private val viewModel: OrderSettingsViewModel by viewModels()
    
    private lateinit var frequencySpinner: Spinner
    private lateinit var sundayCheckBox: CheckBox
    private lateinit var mondayCheckBox: CheckBox
    private lateinit var tuesdayCheckBox: CheckBox
    private lateinit var wednesdayCheckBox: CheckBox
    private lateinit var thursdayCheckBox: CheckBox
    private lateinit var fridayCheckBox: CheckBox
    private lateinit var saturdayCheckBox: CheckBox
    private lateinit var leadTimeSpinner: Spinner
    private lateinit var nextOrderDatesTextView: TextView
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_order_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        frequencySpinner = view.findViewById(R.id.spinner_frequency)
        sundayCheckBox = view.findViewById(R.id.checkbox_sunday)
        mondayCheckBox = view.findViewById(R.id.checkbox_monday)
        tuesdayCheckBox = view.findViewById(R.id.checkbox_tuesday)
        wednesdayCheckBox = view.findViewById(R.id.checkbox_wednesday)
        thursdayCheckBox = view.findViewById(R.id.checkbox_thursday)
        fridayCheckBox = view.findViewById(R.id.checkbox_friday)
        saturdayCheckBox = view.findViewById(R.id.checkbox_saturday)
        leadTimeSpinner = view.findViewById(R.id.spinner_lead_time)
        nextOrderDatesTextView = view.findViewById(R.id.text_next_order_dates)
        saveButton = view.findViewById(R.id.button_save)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up spinners
        setupFrequencySpinner()
        setupLeadTimeSpinner()
        
        // Set up click listeners
        saveButton.setOnClickListener {
            viewModel.saveOrderSettings()
        }
        
        setupDayCheckBoxes()
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun setupFrequencySpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.order_frequency_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        frequencySpinner.adapter = adapter
        
        frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Convert position to frequency value (1, 2, 3, 5, 7)
                val frequency = when (position) {
                    0 -> 1 // Weekly
                    1 -> 2 // Twice a week
                    2 -> 3 // Three times a week
                    3 -> 5 // Every weekday
                    4 -> 7 // Daily
                    else -> 2 // Default to twice a week
                }
                viewModel.setOrderFrequency(frequency)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun setupLeadTimeSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.lead_time_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        leadTimeSpinner.adapter = adapter
        
        leadTimeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Lead time is the position (0, 1, 2, 3)
                viewModel.setLeadTime(position)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun setupDayCheckBoxes() {
        sundayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.SUNDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.SUNDAY)
            }
        }
        
        mondayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.MONDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.MONDAY)
            }
        }
        
        tuesdayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.TUESDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.TUESDAY)
            }
        }
        
        wednesdayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.WEDNESDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.WEDNESDAY)
            }
        }
        
        thursdayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.THURSDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.THURSDAY)
            }
        }
        
        fridayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.FRIDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.FRIDAY)
            }
        }
        
        saturdayCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewModel.toggleOrderDay(Calendar.SATURDAY)
            } else {
                viewModel.toggleOrderDay(Calendar.SATURDAY)
            }
        }
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe order frequency
        viewModel.orderFrequency.observe(viewLifecycleOwner, Observer { frequency ->
            // Convert frequency to spinner position
            val position = when (frequency) {
                1 -> 0 // Weekly
                2 -> 1 // Twice a week
                3 -> 2 // Three times a week
                5 -> 3 // Every weekday
                7 -> 4 // Daily
                else -> 1 // Default to twice a week
            }
            frequencySpinner.setSelection(position)
        })
        
        // Observe order days
        viewModel.orderDays.observe(viewLifecycleOwner, Observer { days ->
            // Update checkboxes without triggering listeners
            sundayCheckBox.setOnCheckedChangeListener(null)
            mondayCheckBox.setOnCheckedChangeListener(null)
            tuesdayCheckBox.setOnCheckedChangeListener(null)
            wednesdayCheckBox.setOnCheckedChangeListener(null)
            thursdayCheckBox.setOnCheckedChangeListener(null)
            fridayCheckBox.setOnCheckedChangeListener(null)
            saturdayCheckBox.setOnCheckedChangeListener(null)
            
            sundayCheckBox.isChecked = days.contains(Calendar.SUNDAY)
            mondayCheckBox.isChecked = days.contains(Calendar.MONDAY)
            tuesdayCheckBox.isChecked = days.contains(Calendar.TUESDAY)
            wednesdayCheckBox.isChecked = days.contains(Calendar.WEDNESDAY)
            thursdayCheckBox.isChecked = days.contains(Calendar.THURSDAY)
            fridayCheckBox.isChecked = days.contains(Calendar.FRIDAY)
            saturdayCheckBox.isChecked = days.contains(Calendar.SATURDAY)
            
            // Restore listeners
            setupDayCheckBoxes()
        })
        
        // Observe lead time
        viewModel.leadTime.observe(viewLifecycleOwner, Observer { leadTime ->
            leadTimeSpinner.setSelection(leadTime)
        })
        
        // Observe next order dates
        viewModel.nextOrderDates.observe(viewLifecycleOwner, Observer { dates ->
            if (dates.isEmpty()) {
                nextOrderDatesTextView.text = getString(R.string.no_order_days_selected)
            } else {
                val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
                val formattedDates = dates.joinToString("\n") { dateFormat.format(it) }
                nextOrderDatesTextView.text = formattedDates
            }
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is OrderSettingsViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_order_settings_saved),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is OrderSettingsViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is OrderSettingsViewModel.SaveStatus.ERROR_NO_DAYS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_no_order_days),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
}