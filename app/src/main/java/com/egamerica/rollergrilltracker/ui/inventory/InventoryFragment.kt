package com.egamerica.rollergrilltracker.ui.inventory

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.egamerica.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class InventoryFragment : Fragment() {

    private val viewModel: InventoryViewModel by viewModels()
    
    private lateinit var dateButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar
    
    private val inventoryAdapter = InventoryAdapter(
        onStartingCountChanged = { productId, count ->
            viewModel.updateStartingCount(productId, count)
        },
        onDeliveryCountChanged = { productId, count ->
            viewModel.updateDeliveryCount(productId, count)
        },
        onEndingCountChanged = { productId, count ->
            viewModel.updateEndingCount(productId, count)
        }
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        dateButton = view.findViewById(R.id.button_date)
        recyclerView = view.findViewById(R.id.recycler_inventory)
        emptyView = view.findViewById(R.id.text_empty)
        saveButton = view.findViewById(R.id.button_save)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = inventoryAdapter
        
        // Set up click listeners
        dateButton.setOnClickListener {
            showDatePicker()
        }
        
        saveButton.setOnClickListener {
            viewModel.saveInventory()
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
        
        // Observe inventory items
        viewModel.inventoryItems.observe(viewLifecycleOwner, Observer { items ->
            inventoryAdapter.submitList(items)
            
            if (items.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is InventoryViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_inventory_saved),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is InventoryViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
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
    
    class InventoryAdapter(
        private val onStartingCountChanged: (Int, Int) -> Unit,
        private val onDeliveryCountChanged: (Int, Int) -> Unit,
        private val onEndingCountChanged: (Int, Int) -> Unit
    ) : RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {
        
        private var items: List<InventoryViewModel.InventoryItem> = emptyList()
        
        fun submitList(newItems: List<InventoryViewModel.InventoryItem>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_inventory, parent, false)
            return InventoryViewHolder(
                view,
                onStartingCountChanged,
                onDeliveryCountChanged,
                onEndingCountChanged
            )
        }
        
        override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class InventoryViewHolder(
            itemView: View,
            private val onStartingCountChanged: (Int, Int) -> Unit,
            private val onDeliveryCountChanged: (Int, Int) -> Unit,
            private val onEndingCountChanged: (Int, Int) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val startingCountEditText: EditText = itemView.findViewById(R.id.edit_starting_count)
            private val deliveryCountEditText: EditText = itemView.findViewById(R.id.edit_delivery_count)
            private val endingCountEditText: EditText = itemView.findViewById(R.id.edit_ending_count)
            private val usedTextView: TextView = itemView.findViewById(R.id.text_used)
            
            private var currentProductId: Int = 0
            
            init {
                startingCountEditText.addTextChangedListener(createTextWatcher { count ->
                    onStartingCountChanged(currentProductId, count)
                })
                
                deliveryCountEditText.addTextChangedListener(createTextWatcher { count ->
                    onDeliveryCountChanged(currentProductId, count)
                })
                
                endingCountEditText.addTextChangedListener(createTextWatcher { count ->
                    onEndingCountChanged(currentProductId, count)
                })
            }
            
            fun bind(item: InventoryViewModel.InventoryItem) {
                currentProductId = item.product.id
                
                productNameTextView.text = item.product.name
                
                // Set values without triggering text watchers
                startingCountEditText.removeTextChangedListener(startingCountEditText.tag as? TextWatcher)
                deliveryCountEditText.removeTextChangedListener(deliveryCountEditText.tag as? TextWatcher)
                endingCountEditText.removeTextChangedListener(endingCountEditText.tag as? TextWatcher)
                
                startingCountEditText.setText(item.startingCount.toString())
                deliveryCountEditText.setText(item.deliveryCount.toString())
                endingCountEditText.setText(item.endingCount.toString())
                usedTextView.text = item.used.toString()
                
                startingCountEditText.addTextChangedListener(startingCountEditText.tag as? TextWatcher)
                deliveryCountEditText.addTextChangedListener(deliveryCountEditText.tag as? TextWatcher)
                endingCountEditText.addTextChangedListener(endingCountEditText.tag as? TextWatcher)
            }
            
            private fun createTextWatcher(onValueChanged: (Int) -> Unit): TextWatcher {
                val textWatcher = object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                        // Do nothing
                    }
                    
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        // Do nothing
                    }
                    
                    override fun afterTextChanged(s: Editable?) {
                        val value = s.toString().toIntOrNull() ?: 0
                        onValueChanged(value)
                    }
                }
                
                // Store the text watcher as a tag on the EditText
                val editText = when (onValueChanged) {
                    { count -> onStartingCountChanged(currentProductId, count) } -> startingCountEditText
                    { count -> onDeliveryCountChanged(currentProductId, count) } -> deliveryCountEditText
                    else -> endingCountEditText
                }
                
                editText.tag = textWatcher
                
                return textWatcher
            }
        }
    }
}