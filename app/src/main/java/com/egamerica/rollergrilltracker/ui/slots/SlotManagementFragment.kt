package com.egamerica.rollergrilltracker.ui.slots

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.egamerica.rollergrilltracker.R
import com.egamerica.rollergrilltracker.data.entities.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SlotManagementFragment : Fragment() {

    private val viewModel: SlotManagementViewModel by viewModels()
    
    private lateinit var slotSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var addProductButton: Button
    private lateinit var clearSlotButton: Button
    private lateinit var saveButton: Button
    private lateinit var progressBar: ProgressBar
    
    private val slotProductAdapter = SlotProductAdapter(
        onMoveUp = { product ->
            viewModel.moveProductUp(product)
        },
        onMoveDown = { product ->
            viewModel.moveProductDown(product)
        },
        onRemove = { product ->
            viewModel.removeProductFromSlot(product)
        }
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_slot_management, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        slotSpinner = view.findViewById(R.id.spinner_slot)
        recyclerView = view.findViewById(R.id.recycler_products)
        emptyView = view.findViewById(R.id.text_empty)
        addProductButton = view.findViewById(R.id.button_add_product)
        clearSlotButton = view.findViewById(R.id.button_clear_slot)
        saveButton = view.findViewById(R.id.button_save)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = slotProductAdapter
        
        // Set up slot spinner
        setupSlotSpinner()
        
        // Set up click listeners
        addProductButton.setOnClickListener {
            showProductSelectionDialog()
        }
        
        clearSlotButton.setOnClickListener {
            confirmClearSlot()
        }
        
        saveButton.setOnClickListener {
            // No need to save explicitly as we're saving changes immediately
            Snackbar.make(
                requireView(),
                getString(R.string.success_slot_configuration_saved),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun setupSlotSpinner() {
        val slotNames = listOf(
            getString(R.string.slot_1),
            getString(R.string.slot_2),
            getString(R.string.slot_3),
            getString(R.string.slot_4),
            getString(R.string.tamale_cooker)
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            slotNames
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        slotSpinner.adapter = adapter
        
        slotSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Slot numbers are 1-based
                viewModel.setCurrentSlot(position + 1)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe current slot
        viewModel.currentSlot.observe(viewLifecycleOwner, Observer { slotNumber ->
            // Slot numbers are 1-based, spinner positions are 0-based
            slotSpinner.setSelection(slotNumber - 1)
        })
        
        // Observe current slot products
        viewModel.currentSlotProducts.observe(viewLifecycleOwner, Observer { products ->
            slotProductAdapter.submitList(products)
            
            if (products.isEmpty()) {
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
                is SlotManagementViewModel.SaveStatus.SUCCESS -> {
                    // Success is handled silently
                }
                is SlotManagementViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SlotManagementViewModel.SaveStatus.ERROR_SLOT_FULL -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_slot_full),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SlotManagementViewModel.SaveStatus.ERROR_PRODUCT_ALREADY_ASSIGNED -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_product_already_assigned),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
    
    private fun showProductSelectionDialog() {
        val products = viewModel.availableProducts.value ?: emptyList()
        
        if (products.isEmpty()) {
            Snackbar.make(
                requireView(),
                getString(R.string.no_products_available),
                Snackbar.LENGTH_LONG
            ).show()
            return
        }
        
        val productNames = products.map { it.name }.toTypedArray()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.select_product)
            .setItems(productNames) { _, which ->
                val selectedProduct = products[which]
                viewModel.addProductToSlot(selectedProduct)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun confirmClearSlot() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.confirm_clear_slot)
            .setPositiveButton(R.string.clear_all) { _, _ ->
                viewModel.clearSlot()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    class SlotProductAdapter(
        private val onMoveUp: (Product) -> Unit,
        private val onMoveDown: (Product) -> Unit,
        private val onRemove: (Product) -> Unit
    ) : RecyclerView.Adapter<SlotProductAdapter.SlotProductViewHolder>() {
        
        private var items: List<Product> = emptyList()
        
        fun submitList(newItems: List<Product>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_slot_product, parent, false)
            return SlotProductViewHolder(view, onMoveUp, onMoveDown, onRemove)
        }
        
        override fun onBindViewHolder(holder: SlotProductViewHolder, position: Int) {
            holder.bind(items[position], position, items.size)
        }
        
        override fun getItemCount(): Int = items.size
        
        class SlotProductViewHolder(
            itemView: View,
            private val onMoveUp: (Product) -> Unit,
            private val onMoveDown: (Product) -> Unit,
            private val onRemove: (Product) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val positionTextView: TextView = itemView.findViewById(R.id.text_position)
            private val moveUpButton: View = itemView.findViewById(R.id.button_move_up)
            private val moveDownButton: View = itemView.findViewById(R.id.button_move_down)
            private val removeButton: View = itemView.findViewById(R.id.button_remove)
            
            fun bind(product: Product, position: Int, totalItems: Int) {
                productNameTextView.text = product.name
                positionTextView.text = (position + 1).toString()
                
                // Enable/disable move buttons based on position
                moveUpButton.isEnabled = position > 0
                moveDownButton.isEnabled = position < totalItems - 1
                
                moveUpButton.setOnClickListener {
                    onMoveUp(product)
                }
                
                moveDownButton.setOnClickListener {
                    onMoveDown(product)
                }
                
                removeButton.setOnClickListener {
                    onRemove(product)
                }
            }
        }
    }
}