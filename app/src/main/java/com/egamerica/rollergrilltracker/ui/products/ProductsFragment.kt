package com.egamerica.rollergrilltracker.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.egamerica.rollergrilltracker.R
import com.egamerica.rollergrilltracker.data.entities.Product
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductsFragment : Fragment() {

    private val viewModel: ProductsViewModel by viewModels()
    
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var categoryChipGroup: ChipGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var addProductButton: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    
    private val productAdapter = ProductAdapter(
        onItemClick = { product ->
            navigateToProductDetail(product)
        },
        onActiveToggle = { product ->
            viewModel.toggleProductActive(product)
        },
        onStockToggle = { product ->
            viewModel.toggleProductStock(product)
        }
    )
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        searchEditText = view.findViewById(R.id.edit_search)
        searchButton = view.findViewById(R.id.button_search)
        categoryChipGroup = view.findViewById(R.id.chip_group_categories)
        recyclerView = view.findViewById(R.id.recycler_products)
        emptyView = view.findViewById(R.id.text_empty)
        addProductButton = view.findViewById(R.id.fab_add_product)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = productAdapter
        
        // Set up click listeners
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            viewModel.setSearchQuery(query)
        }
        
        addProductButton.setOnClickListener {
            navigateToProductDetail(null)
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe categories
        viewModel.categories.observe(viewLifecycleOwner, Observer { categories ->
            setupCategoryChips(categories)
        })
        
        // Observe selected category
        viewModel.selectedCategory.observe(viewLifecycleOwner, Observer { category ->
            updateSelectedCategoryChip(category)
        })
        
        // Observe filtered products
        viewModel.filteredProducts.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            
            if (products.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        })
    }
    
    private fun setupCategoryChips(categories: List<String>) {
        categoryChipGroup.removeAllViews()
        
        for (category in categories) {
            val chip = layoutInflater.inflate(
                R.layout.item_category_chip, categoryChipGroup, false
            ) as Chip
            
            chip.text = category
            chip.isCheckable = true
            chip.setOnClickListener {
                viewModel.setSelectedCategory(category)
            }
            
            categoryChipGroup.addView(chip)
        }
    }
    
    private fun updateSelectedCategoryChip(selectedCategory: String) {
        for (i in 0 until categoryChipGroup.childCount) {
            val chip = categoryChipGroup.getChildAt(i) as Chip
            chip.isChecked = chip.text == selectedCategory
        }
    }
    
    private fun navigateToProductDetail(product: Product?) {
        val action = if (product != null) {
            ProductsFragmentDirections.actionProductsToProductDetail(product.id)
        } else {
            ProductsFragmentDirections.actionProductsToProductDetail(0) // 0 means new product
        }
        findNavController().navigate(action)
    }
    
    class ProductAdapter(
        private val onItemClick: (Product) -> Unit,
        private val onActiveToggle: (Product) -> Unit,
        private val onStockToggle: (Product) -> Unit
    ) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {
        
        private var items: List<Product> = emptyList()
        
        fun submitList(newItems: List<Product>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
            return ProductViewHolder(view, onItemClick, onActiveToggle, onStockToggle)
        }
        
        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class ProductViewHolder(
            itemView: View,
            private val onItemClick: (Product) -> Unit,
            private val onActiveToggle: (Product) -> Unit,
            private val onStockToggle: (Product) -> Unit
        ) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val barcodeTextView: TextView = itemView.findViewById(R.id.text_barcode)
            private val categoryTextView: TextView = itemView.findViewById(R.id.text_category)
            private val activeButton: Button = itemView.findViewById(R.id.button_active)
            private val stockButton: Button = itemView.findViewById(R.id.button_stock)
            
            fun bind(product: Product) {
                productNameTextView.text = product.name
                barcodeTextView.text = product.barcode
                categoryTextView.text = product.category
                
                activeButton.text = if (product.isActive) "Active" else "Inactive"
                activeButton.setBackgroundResource(
                    if (product.isActive) R.color.colorPrimary else R.color.colorInactive
                )
                
                stockButton.text = if (product.inStock) "In Stock" else "Out of Stock"
                stockButton.setBackgroundResource(
                    if (product.inStock) R.color.colorAccent else R.color.colorInactive
                )
                
                itemView.setOnClickListener {
                    onItemClick(product)
                }
                
                activeButton.setOnClickListener {
                    onActiveToggle(product)
                }
                
                stockButton.setOnClickListener {
                    onStockToggle(product)
                }
            }
        }
    }
}