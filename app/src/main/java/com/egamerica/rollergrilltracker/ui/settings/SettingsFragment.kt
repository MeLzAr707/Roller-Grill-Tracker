package com.yourcompany.rollergrilltracker.ui.settings

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.yourcompany.rollergrilltracker.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()
    
    private lateinit var storeNameEditText: EditText
    private lateinit var managerNameEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var darkModeSwitch: Switch
    private lateinit var showSuggestionsSwitch: Switch
    private lateinit var wasteAlertsSwitch: Switch
    private lateinit var autoBackupSwitch: Switch
    private lateinit var saveSettingsButton: Button
    private lateinit var backupButton: Button
    private lateinit var restoreButton: Button
    private lateinit var exportButton: Button
    private lateinit var clearDataButton: Button
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize views
        storeNameEditText = view.findViewById(R.id.edit_store_name)
        managerNameEditText = view.findViewById(R.id.edit_manager_name)
        contactEditText = view.findViewById(R.id.edit_contact)
        darkModeSwitch = view.findViewById(R.id.switch_dark_mode)
        showSuggestionsSwitch = view.findViewById(R.id.switch_show_suggestions)
        wasteAlertsSwitch = view.findViewById(R.id.switch_waste_alerts)
        autoBackupSwitch = view.findViewById(R.id.switch_auto_backup)
        saveSettingsButton = view.findViewById(R.id.button_save_settings)
        backupButton = view.findViewById(R.id.button_backup)
        restoreButton = view.findViewById(R.id.button_restore)
        exportButton = view.findViewById(R.id.button_export)
        clearDataButton = view.findViewById(R.id.button_clear_data)
        progressBar = view.findViewById(R.id.progress_bar)
        
        // Set up click listeners
        saveSettingsButton.setOnClickListener {
            saveSettings()
        }
        
        backupButton.setOnClickListener {
            viewModel.backupData()
        }
        
        restoreButton.setOnClickListener {
            confirmRestore()
        }
        
        exportButton.setOnClickListener {
            viewModel.exportReports()
        }
        
        clearDataButton.setOnClickListener {
            confirmClearData()
        }
        
        // Observe view model data
        observeViewModel()
    }
    
    private fun observeViewModel() {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
        
        // Observe settings data
        viewModel.storeName.observe(viewLifecycleOwner, Observer { name ->
            if (storeNameEditText.text.toString() != name) {
                storeNameEditText.setText(name)
            }
        })
        
        viewModel.managerName.observe(viewLifecycleOwner, Observer { name ->
            if (managerNameEditText.text.toString() != name) {
                managerNameEditText.setText(name)
            }
        })
        
        viewModel.contactInfo.observe(viewLifecycleOwner, Observer { contact ->
            if (contactEditText.text.toString() != contact) {
                contactEditText.setText(contact)
            }
        })
        
        viewModel.darkMode.observe(viewLifecycleOwner, Observer { enabled ->
            darkModeSwitch.isChecked = enabled
        })
        
        viewModel.showSuggestions.observe(viewLifecycleOwner, Observer { enabled ->
            showSuggestionsSwitch.isChecked = enabled
        })
        
        viewModel.enableWasteAlerts.observe(viewLifecycleOwner, Observer { enabled ->
            wasteAlertsSwitch.isChecked = enabled
        })
        
        viewModel.autoBackup.observe(viewLifecycleOwner, Observer { enabled ->
            autoBackupSwitch.isChecked = enabled
        })
        
        // Observe save status
        viewModel.saveStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is SettingsViewModel.SaveStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.settings_saved),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is SettingsViewModel.SaveStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_saving_data),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
        
        // Observe backup status
        viewModel.backupStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                is SettingsViewModel.BackupStatus.SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_backup_created),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is SettingsViewModel.BackupStatus.ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_backup_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SettingsViewModel.BackupStatus.RESTORE_SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_data_restored),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is SettingsViewModel.BackupStatus.RESTORE_ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_restore_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SettingsViewModel.BackupStatus.EXPORT_SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_export),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is SettingsViewModel.BackupStatus.EXPORT_ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_export_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is SettingsViewModel.BackupStatus.CLEAR_SUCCESS -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.success_data_cleared),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
                is SettingsViewModel.BackupStatus.CLEAR_ERROR -> {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.error_clear_failed),
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            }
        })
    }
    
    private fun saveSettings() {
        // Update view model with current values
        viewModel.setStoreName(storeNameEditText.text.toString())
        viewModel.setManagerName(managerNameEditText.text.toString())
        viewModel.setContactInfo(contactEditText.text.toString())
        viewModel.setDarkMode(darkModeSwitch.isChecked)
        viewModel.setShowSuggestions(showSuggestionsSwitch.isChecked)
        viewModel.setEnableWasteAlerts(wasteAlertsSwitch.isChecked)
        viewModel.setAutoBackup(autoBackupSwitch.isChecked)
        
        // Save settings
        viewModel.saveSettings()
    }
    
    private fun confirmRestore() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.confirm_restore)
            .setPositiveButton(R.string.restore_data) { _, _ ->
                viewModel.restoreData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
    
    private fun confirmClearData() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm)
            .setMessage(R.string.confirm_clear_data)
            .setPositiveButton(R.string.clear_all_data) { _, _ ->
                viewModel.clearAllData()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}