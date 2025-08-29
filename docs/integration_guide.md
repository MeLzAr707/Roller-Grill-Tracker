# Roller Grill Sales & Waste Tracker - Integration Guide

This guide provides instructions for importing and setting up the complete Roller Grill Sales & Waste Tracker application with all enhancements in Android Studio.

## Importing the Project

1. **Extract the ZIP file**
   - Extract the `roller_grill_complete_app.zip` file to your desired location

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the extracted `roller_grill_complete` folder and select it
   - Click "OK" to open the project

3. **Sync Gradle Files**
   - Wait for Android Studio to index the project
   - If prompted, click "Sync Now" to sync the Gradle files
   - If sync fails, try clicking the "Sync Project with Gradle Files" button in the toolbar

## Project Structure

The project follows a standard Android MVVM architecture:

- **app/src/main/java/com/yourcompany/rollergrilltracker/**
  - **data/**: Data layer with entities, DAOs, repositories
  - **ui/**: UI layer with fragments and ViewModels
  - **util/**: Utility classes

- **app/src/main/res/**
  - **layout/**: XML layout files
  - **navigation/**: Navigation graphs
  - **values/**: Resources (strings, colors, etc.)

## Key Components

### Database

The application uses Room for database operations. The database is set up in:
- `data/database/AppDatabase.kt`

A migration script is provided to update from version 1 to version 2:
- `data/database/Migration_1_2.kt`

### Repositories

Repositories provide a clean API for accessing data:
- `data/repositories/`

### ViewModels

ViewModels handle UI-related data and business logic:
- `ui/dashboard/DashboardViewModel.kt`
- `ui/sales/SalesEntryViewModel.kt`
- And others in their respective packages

### UI Components

UI components are organized by feature:
- `ui/dashboard/`: Dashboard screens
- `ui/sales/`: Sales tracking screens
- `ui/waste/`: Waste tracking screens
- `ui/settings/`: Settings screens
- And others for their respective features

## Enhancement Features

### Multiple Roller Grill Support

- Configuration: Settings > Grill Configuration
- Key files:
  - `data/entities/GrillConfig.kt`
  - `ui/settings/GrillConfigViewModel.kt`
  - `ui/settings/GrillConfigFragment.kt`

### Customizable Store Hours

- Configuration: Settings > Store Hours
- Key files:
  - `data/entities/StoreHours.kt`
  - `ui/settings/StoreHoursViewModel.kt`
  - `ui/settings/StoreHoursSettingsFragment.kt`

### Extended Time Periods for 24-Hour Stores

- Automatically enabled when 24-hour operation is configured
- Key files:
  - `data/entities/TimePeriod_updated.kt`
  - `data/repositories/TimePeriodRepository_updated.kt`

### Product Hold Time Tracking

- Access: Dashboard > Hold Times
- Key files:
  - `data/entities/ProductHoldTime.kt`
  - `ui/holdtime/ProductHoldTimeViewModel.kt`
  - `ui/holdtime/ProductHoldTimeFragment.kt`

## Running the Application

1. **Configure an Emulator or Connect a Device**
   - Set up an Android Virtual Device (AVD) or connect a physical device

2. **Build and Run**
   - Click the "Run" button in Android Studio
   - Select your target device
   - Wait for the app to build and install

3. **Initial Setup**
   - On first run, the app will initialize the database with default data
   - Default grill configuration: 1 grill with 4 slots
   - Default store hours: 6am-10pm for all days
   - Default time periods: Morning, Midday, Afternoon, Evening, plus Late Night and Early Morning for 24-hour operation

## Troubleshooting

### Build Issues

- **Gradle Sync Failed**: Try invalidating caches and restarting (File > Invalidate Caches / Restart)
- **Missing Dependencies**: Check that all dependencies in build.gradle are resolved

### Runtime Issues

- **Database Errors**: Check Logcat for specific error messages. The database migration should handle updating from version 1 to version 2.
- **UI Issues**: Ensure all layout files are properly included in the project

## Additional Resources

- **User Guide**: See `docs/user_guide_enhancements.md` for detailed usage instructions
- **Developer Guide**: See `docs/developer_guide_enhancements.md` for technical implementation details
- **Enhancement Summary**: See `docs/roller_grill_enhancements_summary.md` for an overview of the enhancements