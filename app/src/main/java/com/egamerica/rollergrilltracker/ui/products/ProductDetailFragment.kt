package com.egamerica.rollergrilltracker.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.egamerica.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private val viewModel: ProductDetailViewModel by viewModels()
    
    private lateinit var nameEditText: EditText
    private lateinit var barcodeEditText: EditText
    private lateinit var categoryEditText: EditText
    private lateinit var activeSwitch: Switch
    private lateinit var stockSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var deleteButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_detail, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        nameEditText = view.findViewById(R.id.edit_product_name)
        barcodeEditText = view.findViewById(R.id.edit_barcode)
        categoryEditText = view.findViewById(R.id.edit_category)
        activeSwitch = view.findViewById(R.id.switch_active)
        stockSwitch = view.findViewById(R.id.switch_stock)
        saveButton = view.findViewById(R.id.button_save)
        deleteButton = view.findViewById(R.id.button_delete)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up click listeners
        saveButton.setOnClickListener {
            saveProduct()
        }
        
        deleteButton.setOnClickListener {
            confirmDelete()
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe product data
        viewModel.productName.observe(viewLifecycleOwner, Observer { name ->
            if (nameEditText.text.toString() != name) {
                nameEditText.setText(name)
            }
        })
        
        viewModel.barcode.observe(viewLifecycleOwner, Observer { barcode ->
            if (barcodeEditText.text.toString() != barcode) {
                barcodeEditText.setText(barcode)
            }
        })
        
        viewModel.category.observe(viewLifecycleOwner, Observer { category ->
            if (categoryEditText.text.toString() != category) {
                categoryEditText.setText(category)
            }
        })
        
        viewModel.isActive.observe(viewLifecycleOwner, Observer { isActive ->
            activeSwitch.isChecked = isActive
        })
        
        viewModel.inStock.observe(viewLifecycleOwner, Observer { inStock ->
            stockSwitch.isChecked = inStock
        })
        
        // Observe is new product
        viewModel.isNewProduct.observe(viewLifecycleOwner, Observer { isNew ->
            deleteButton.visibility = if (isNew) View.GONE else View.VISIBLE
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is ProductDetailViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_product_saved),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
                is ProductDetailViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is ProductDetailViewModel.SaveStatus.ERROR_EMPTY_NAME -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_empty_product_name),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is ProductDetailViewModel.SaveStatus.DELETED -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_product_deleted),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    findNavController().navigateUp()
                }
            }
        })
    }
    
    private fun saveProduct() {
        // Update view model with current values
        viewModel.setProductName(nameEditText.text.toString())
        viewModel.setBarcode(barcodeEditText.text.toString())
        viewModel.setCategory(categoryEditText.text.toString())
        viewModel.setIsActive(activeSwitch.isChecked)
        viewModel.setInStock(stockSwitch.isChecked)
        
        // Save product
        viewModel.saveProduct()
    }
    
    private fun confirmDelete() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.confirm_delete_product)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteProduct()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}