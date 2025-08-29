# Roller Grill Sales & Waste Tracker

## Overview

The Roller Grill Sales & Waste Tracker is a comprehensive Android application designed for convenience stores to efficiently track and manage roller grill product sales and waste. This application helps store operators optimize their roller grill operations, reduce waste, and maximize profitability through data-driven insights.

## Key Features

### Core Features
- **Sales Tracking**: Record sales by time period throughout the day
- **Waste Tracking**: Track waste with barcode scanning or manual entry
- **Inventory Management**: Monitor starting, delivery, and ending counts
- **Smart Suggestions**: Get AI-powered recommendations for optimal product quantities
- **Slot Management**: Organize products across your roller grill slots
- **Order Management**: Set up automatic order suggestions based on sales data
- **Comprehensive Reports**: Analyze sales, waste, and inventory data

### Enhanced Features
- **Multiple Roller Grill Support**: Configure and manage multiple roller grills
- **Customizable Store Hours**: Set custom operating hours for each day of the week
- **Extended Time Periods for 24-Hour Stores**: Additional time periods for 24-hour operation
- **Product Hold Time Tracking**: Track 4-hour hold times for food safety compliance

## Technical Details

### Architecture
The application follows the MVVM (Model-View-ViewModel) architecture pattern:
- **Model**: Room database entities and repositories
- **View**: Fragments and layouts
- **ViewModel**: ViewModels that connect the UI with the data layer

### Libraries and Technologies
- **Room**: For database operations
- **Hilt**: For dependency injection
- **LiveData**: For observable data patterns
- **Coroutines**: For asynchronous operations
- **Navigation Component**: For fragment navigation
- **Material Design Components**: For UI elements

## Getting Started

### Prerequisites
- Android Studio Arctic Fox (2020.3.1) or newer
- Android SDK 31 or higher
- Kotlin 1.6.0 or higher

### Installation
1. Clone this repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run the application on an emulator or physical device

## Documentation

Comprehensive documentation is available in the `docs` directory:
- **User Guide**: Instructions for using the application
- **Developer Guide**: Technical documentation for developers
- **Enhancement Summary**: Overview of enhanced features

## License

This project is licensed under the MIT License - see the LICENSE file for details.