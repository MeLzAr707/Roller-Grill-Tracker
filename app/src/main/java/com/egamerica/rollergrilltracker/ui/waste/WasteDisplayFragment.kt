package com.yourcompany.rollergrilltracker.ui.waste

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WasteDisplayFragment : Fragment() {

    private val viewModel: WasteDisplayViewModel by viewModels()
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var displayBarcodeButton: Button
    private lateinit var manualEntryButton: Button
    private lateinit var clearAllButton: Button
    private lateinit var saveButton: Button
    
    private val wasteAdapter = WasteItemAdapter { position ->
        viewModel.removeWasteItem(position)
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_waste_display, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_waste_items)
        progressBar = view.findViewById(R.id.progress_bar)
        emptyView = view.findViewById(R.id.text_empty)
        displayBarcodeButton = view.findViewById(R.id.button_display_barcode)
        manualEntryButton = view.findViewById(R.id.button_manual_entry)
        clearAllButton = view.findViewById(R.id.button_clear_all)
        saveButton = view.findViewById(R.id.button_save)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = wasteAdapter
        
        // Set up click listeners
        displayBarcodeButton.setOnClickListener {
            viewModel.selectedProduct.value?.let { product ->
                showBarcodeDialog(product.barcode, product.name)
            }
        }
        
        manualEntryButton.setOnClickListener {
            findNavController().navigate(R.id.action_waste_to_manual_entry)
        }
        
        clearAllButton.setOnClickListener {
            viewModel.clearAllWasteItems()
        }
        
        saveButton.setOnClickListener {
            viewModel.saveWasteEntry()
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe waste items
        viewModel.wasteItems.observe(viewLifecycleOwner, Observer { items ->
            wasteAdapter.submitList(items)
            
            if (items.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
                clearAllButton.visibility = View.GONE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
                clearAllButton.visibility = View.VISIBLE
            }
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is WasteDisplayViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_waste_saved),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is WasteDisplayViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is WasteDisplayViewModel.SaveStatus.EMPTY -> {
                    Snackbar.make(
                        requireView(),
                        "Please add at least one waste item",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
    
    private fun showBarcodeDialog(barcode: String, productName: String) {
        val dialog = BarcodeDisplayDialogFragment.newInstance(barcode, productName)
        dialog.show(childFragmentManager, "barcode_dialog")
    }
    
    class WasteItemAdapter(private val onDeleteClick: (Int) -> Unit) : 
            RecyclerView.Adapter<WasteItemAdapter.WasteItemViewHolder>() {
        
        private var items: List<WasteDisplayViewModel.WasteItem> = emptyList()
        
        fun submitList(newItems: List<WasteDisplayViewModel.WasteItem>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WasteItemViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_waste, parent, false)
            return WasteItemViewHolder(view, onDeleteClick)
        }
        
        override fun onBindViewHolder(holder: WasteItemViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class WasteItemViewHolder(itemView: View, private val onDeleteClick: (Int) -> Unit) : 
                RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val quantityTextView: TextView = itemView.findViewById(R.id.text_quantity)
            private val reasonTextView: TextView = itemView.findViewById(R.id.text_reason)
            private val deleteButton: ImageView = itemView.findViewById(R.id.button_delete)
            
            init {
                deleteButton.setOnClickListener {
                    onDeleteClick(adapterPosition)
                }
            }
            
            fun bind(item: WasteDisplayViewModel.WasteItem) {
                productNameTextView.text = item.productName
                quantityTextView.text = item.quantity.toString()
                reasonTextView.text = item.reason
            }
        }
    }
}