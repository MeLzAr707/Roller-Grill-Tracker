package com.egamerica.rollergrilltracker.ui.sales

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.egamerica.rollergrilltracker.R
import com.egamerica.rollergrilltracker.data.entities.Product
import com.egamerica.rollergrilltracker.data.entities.TimePeriod
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class SalesEntryFragment : Fragment() {

    private val viewModel: SalesEntryViewModel by viewModels()
    private lateinit var productContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var dateTextView: TextView
    private lateinit var timePeriodSpinner: Spinner
    private lateinit var saveButton: Button
    private lateinit var copyButton: Button
    
    private val productViews = mutableMapOf<Int, View>()
    private val quantityEditTexts = mutableMapOf<Int, android.widget.EditText>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sales_entry, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        productContainer = view.findViewById(R.id.product_container)
        progressBar = view.findViewById(R.id.progress_bar)
        dateTextView = view.findViewById(R.id.text_date)
        timePeriodSpinner = view.findViewById(R.id.spinner_time_period)
        saveButton = view.findViewById(R.id.button_save)
        copyButton = view.findViewById(R.id.button_copy_previous)
        
        // Set up date picker
        dateTextView.setOnClickListener {
            showDatePicker()
        }
        
        // Set up copy button
        copyButton.setOnClickListener {
            viewModel.copyFromPreviousPeriod()
        }
        
        // Set up save button
        saveButton.setOnClickListener {
            saveEntries()
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
            dateTextView.text = formatDisplayDate(date)
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
        
        // Observe products
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            setupProductViews(products)
        })
        
        // Observe product quantities
        viewModel.productQuantities.observe(viewLifecycleOwner, Observer { quantities ->
            updateQuantityViews(quantities)
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is SalesEntryViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_sales_saved),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SalesEntryViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
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
    
    private fun setupProductViews(products: List<Product>) {
        productContainer.removeAllViews()
        productViews.clear()
        quantityEditTexts.clear()
        
        val inflater = LayoutInflater.from(requireContext())
        
        for (product in products) {
            val productView = inflater.inflate(R.layout.item_sales_entry_product, productContainer, false)
            
            val nameTextView = productView.findViewById<TextView>(R.id.text_product_name)
            val quantityEditText = productView.findViewById<android.widget.EditText>(R.id.edit_quantity)
            
            nameTextView.text = product.name
            
            // Set up quantity change listener
            quantityEditText.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val quantity = quantityEditText.text.toString().toIntOrNull() ?: 0
                    viewModel.updateProductQuantity(product.id, quantity)
                }
            }
            
            productViews[product.id] = productView
            quantityEditTexts[product.id] = quantityEditText
            
            productContainer.addView(productView)
        }
    }
    
    private fun updateQuantityViews(quantities: Map<Int, Int>) {
        for ((productId, quantity) in quantities) {
            quantityEditTexts[productId]?.setText(quantity.toString())
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val currentDate = viewModel.selectedDate.value?.let {
            dateFormat.parse(it)
        } ?: Date()
        
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
    
    private fun saveEntries() {
        // Update all quantities from edit texts
        for ((productId, editText) in quantityEditTexts) {
            val quantity = editText.text.toString().toIntOrNull() ?: 0
            viewModel.updateProductQuantity(productId, quantity)
        }
        
        // Save the sales entry
        viewModel.saveSalesEntry()
    }
    
    private fun formatDisplayDate(dateString: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outputFormat = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US)
        
        return try {
            val date = inputFormat.parse(dateString)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            dateString
        }
    }
}