package com.yourcompany.rollergrilltracker.ui.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private val viewModel: ReportsViewModel by viewModels()
    
    private lateinit var startDateButton: Button
    private lateinit var endDateButton: Button
    private lateinit var applyButton: Button
    private lateinit var reportTypeRadioGroup: RadioGroup
    private lateinit var salesRadio: RadioButton
    private lateinit var wasteRadio: RadioButton
    private lateinit var salesVsWasteRadio: RadioButton
    private lateinit var customRadio: RadioButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: TextView
    private lateinit var salesVsWasteText: TextView
    private lateinit var exportPdfButton: Button
    private lateinit var exportCsvButton: Button
    private lateinit var progressBar: ProgressBar
    
    private val productAdapter = ProductPerformanceAdapter()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reports, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        startDateButton = view.findViewById(R.id.button_start_date)
        endDateButton = view.findViewById(R.id.button_end_date)
        applyButton = view.findViewById(R.id.button_apply)
        reportTypeRadioGroup = view.findViewById(R.id.radio_group_report_type)
        salesRadio = view.findViewById(R.id.radio_sales)
        wasteRadio = view.findViewById(R.id.radio_waste)
        salesVsWasteRadio = view.findViewById(R.id.radio_sales_vs_waste)
        customRadio = view.findViewById(R.id.radio_custom)
        recyclerView = view.findViewById(R.id.recycler_products)
        emptyView = view.findViewById(R.id.text_empty)
        salesVsWasteText = view.findViewById(R.id.text_sales_vs_waste)
        exportPdfButton = view.findViewById(R.id.button_export_pdf)
        exportCsvButton = view.findViewById(R.id.button_export_csv)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up recycler view
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = productAdapter
        
        // Set up click listeners
        startDateButton.setOnClickListener {
            showDatePicker(true)
        }
        
        endDateButton.setOnClickListener {
            showDatePicker(false)
        }
        
        applyButton.setOnClickListener {
            applyFilters()
        }
        
        reportTypeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_sales -> viewModel.setReportType(ReportsViewModel.ReportType.SALES)
                R.id.radio_waste -> viewModel.setReportType(ReportsViewModel.ReportType.WASTE)
                R.id.radio_sales_vs_waste -> viewModel.setReportType(ReportsViewModel.ReportType.SALES_VS_WASTE)
                R.id.radio_custom -> viewModel.setReportType(ReportsViewModel.ReportType.CUSTOM)
            }
        }
        
        exportPdfButton.setOnClickListener {
            exportReport(true)
        }
        
        exportCsvButton.setOnClickListener {
            exportReport(false)
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe date range
        viewModel.startDate.observe(viewLifecycleOwner, Observer { date ->
            startDateButton.text = formatDisplayDate(date)
        })
        
        viewModel.endDate.observe(viewLifecycleOwner, Observer { date ->
            endDateButton.text = formatDisplayDate(date)
        })
        
        // Observe report type
        viewModel.reportType.observe(viewLifecycleOwner, Observer { type ->
            when (type) {
                ReportsViewModel.ReportType.SALES -> {
                    salesRadio.isChecked = true
                    salesVsWasteText.visibility = View.GONE
                }
                ReportsViewModel.ReportType.WASTE -> {
                    wasteRadio.isChecked = true
                    salesVsWasteText.visibility = View.GONE
                }
                ReportsViewModel.ReportType.SALES_VS_WASTE -> {
                    salesVsWasteRadio.isChecked = true
                    salesVsWasteText.visibility = View.VISIBLE
                }
                ReportsViewModel.ReportType.CUSTOM -> {
                    customRadio.isChecked = true
                    salesVsWasteText.visibility = View.VISIBLE
                }
            }
        })
        
        // Observe top products
        viewModel.topProducts.observe(viewLifecycleOwner, Observer { products ->
            productAdapter.submitList(products)
            
            if (products.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyView.visibility = View.VISIBLE
            } else {
                recyclerView.visibility = View.VISIBLE
                emptyView.visibility = View.GONE
            }
        })
        
        // Observe sales vs waste data
        viewModel.salesVsWaste.observe(viewLifecycleOwner, Observer { data ->
            val salesPercentage = String.format("%.1f", data.salesPercentage)
            val wastePercentage = String.format("%.1f", data.wastePercentage)
            
            salesVsWasteText.text = getString(
                R.string.sales_vs_waste_format,
                data.totalSales,
                salesPercentage,
                data.totalWaste,
                wastePercentage
            )
        })
    }
    
    private fun showDatePicker(isStartDate: Boolean) {
        val calendar = Calendar.getInstance()
        val currentDate = if (isStartDate) {
            viewModel.startDate.value
        } else {
            viewModel.endDate.value
        } ?: Date()
        
        calendar.time = currentDate
        
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                calendar.set(selectedYear, selectedMonth, selectedDay)
                if (isStartDate) {
                    viewModel.setStartDate(calendar.time)
                } else {
                    viewModel.setEndDate(calendar.time)
                }
            },
            year,
            month,
            day
        )
        
        datePickerDialog.show()
    }
    
    private fun applyFilters() {
        // The view model automatically updates when dates or report type changes
        // This button is mainly for UX purposes
    }
    
    private fun exportReport(isPdf: Boolean) {
        // Export functionality would be implemented here
        // For now, just show a toast
        val format = if (isPdf) "PDF" else "CSV"
        val message = getString(R.string.export_format_message, format)
        // Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun formatDisplayDate(date: Date): String {
        val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
        return outputFormat.format(date)
    }
    
    class ProductPerformanceAdapter : 
            RecyclerView.Adapter<ProductPerformanceAdapter.ProductViewHolder>() {
        
        private var items: List<ReportsViewModel.ProductPerformance> = emptyList()
        
        fun submitList(newItems: List<ReportsViewModel.ProductPerformance>) {
            items = newItems
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product_performance, parent, false)
            return ProductViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            holder.bind(items[position])
        }
        
        override fun getItemCount(): Int = items.size
        
        class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            
            private val productNameTextView: TextView = itemView.findViewById(R.id.text_product_name)
            private val salesTextView: TextView = itemView.findViewById(R.id.text_sales)
            private val wasteTextView: TextView = itemView.findViewById(R.id.text_waste)
            
            fun bind(item: ReportsViewModel.ProductPerformance) {
                productNameTextView.text = item.productName
                salesTextView.text = item.salesQuantity.toString()
                wasteTextView.text = item.wasteQuantity.toString()
            }
        }
    }
}