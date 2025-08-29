package com.yourcompany.rollergrilltracker.ui.waste

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.yourcompany.rollergrilltracker.R
import com.yourcompany.rollergrilltracker.data.entities.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasteManualEntryFragment : Fragment() {

    private val viewModel: WasteManualEntryViewModel by viewModels()
    private val parentViewModel: WasteDisplayViewModel by viewModels({ requireParentFragment() })
    
    private lateinit var productSpinner: Spinner
    private lateinit var quantityEditText: EditText
    private lateinit var incrementButton: Button
    private lateinit var decrementButton: Button
    private lateinit var reasonEditText: EditText
    private lateinit var addButton: Button
    private lateinit var addAnotherButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_waste_manual_entry, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        productSpinner = view.findViewById(R.id.spinner_product)
        quantityEditText = view.findViewById(R.id.edit_quantity)
        incrementButton = view.findViewById(R.id.button_increment)
        decrementButton = view.findViewById(R.id.button_decrement)
        reasonEditText = view.findViewById(R.id.edit_reason)
        addButton = view.findViewById(R.id.button_add)
        addAnotherButton = view.findViewById(R.id.button_add_another)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up click listeners
        incrementButton.setOnClickListener {
            viewModel.incrementQuantity()
        }
        
        decrementButton.setOnClickListener {
            viewModel.decrementQuantity()
        }
        
        addButton.setOnClickListener {
            addWasteItem(false)
        }
        
        addAnotherButton.setOnClickListener {
            addWasteItem(true)
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe products
        viewModel.products.observe(viewLifecycleOwner, Observer { products ->
            setupProductSpinner(products)
        })
        
        // Observe selected product
        viewModel.selectedProduct.observe(viewLifecycleOwner, Observer { product ->
            val position = (productSpinner.adapter as? ArrayAdapter<Product>)?.getPosition(product) ?: 0
            productSpinner.setSelection(position)
        })
        
        // Observe quantity
        viewModel.quantity.observe(viewLifecycleOwner, Observer { quantity ->
            quantityEditText.setText(quantity.toString())
        })
        
        // Observe waste item
        viewModel.wasteItem.observe(viewLifecycleOwner, Observer { wasteItem ->
            wasteItem?.let {
                // Add the waste item to the parent view model
                parentViewModel.addWasteItem(
                    Product(
                        id = it.productId,
                        name = it.productName,
                        barcode = it.barcode,
                        category = "",
                        isActive = true,
                        inStock = true
                    ),
                    it.quantity,
                    it.reason
                )
                
                // Reset the waste item
                viewModel.resetWasteItem()
                
                // Navigate back or clear fields
                if (findNavController().currentDestination?.id == R.id.navigation_waste_manual) {
                    findNavController().navigateUp()
                }
            }
        })
    }
    
    private fun setupProductSpinner(products: List<Product>) {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            products
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        productSpinner.adapter = adapter
        
        productSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedProduct = products[position]
                viewModel.setSelectedProduct(selectedProduct)
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }
    
    private fun addWasteItem(addAnother: Boolean) {
        // Update quantity from edit text
        val quantityText = quantityEditText.text.toString()
        val quantity = quantityText.toIntOrNull() ?: 1
        viewModel.setQuantity(quantity)
        
        // Update reason from edit text
        val reason = reasonEditText.text.toString()
        viewModel.setReason(reason)
        
        // Create waste item
        viewModel.createWasteItem()
        
        if (addAnother) {
            // Clear fields for another entry
            reasonEditText.text.clear()
            viewModel.setQuantity(1)
        }
    }
}