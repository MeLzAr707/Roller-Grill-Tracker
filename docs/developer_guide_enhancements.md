# Roller Grill Sales & Waste Tracker - Enhancement Implementation Guide

## Table of Contents
1. [Architecture Overview](#architecture-overview)
2. [Database Schema Changes](#database-schema-changes)
3. [Repository Layer Changes](#repository-layer-changes)
4. [ViewModel Layer Changes](#viewmodel-layer-changes)
5. [UI Implementation](#ui-implementation)
6. [Migration Strategy](#migration-strategy)
7. [Testing Considerations](#testing-considerations)

## Architecture Overview

The enhancements maintain the existing MVVM architecture while extending it to support new features:

1. **Multiple Roller Grill Support**: Added entities, DAOs, repositories, and UI components to manage multiple grills
2. **Customizable Store Hours**: Added entities, DAOs, repositories, and UI components to manage store hours
3. **Extended Time Periods**: Enhanced existing time period components to support 24-hour operation
4. **Product Hold Time Tracking**: Added new entities, DAOs, repositories, and UI components for tracking product hold times

## Database Schema Changes

### New Entities

1. **GrillConfig**
   - Represents a roller grill unit configuration
   - Contains grill number, name, number of slots, and active status
   - Primary entity for multiple grill support

2. **StoreHours**
   - Represents store operating hours for each day of the week
   - Contains day of week, open time, close time, and 24-hour flag
   - Primary entity for customizable store hours

3. **ProductHoldTime**
   - Represents a product's hold time tracking information
   - Contains product ID, slot assignment ID, start time, expiration time, and status
   - Primary entity for hold time tracking

### Updated Entities

1. **SlotAssignment**
   - Added `grillNumber` field to associate slots with specific grills
   - Added `productAddedAt` field to track when products were added to slots

2. **TimePeriod**
   - Added `isActive` field to enable/disable time periods
   - Added `is24HourOnly` field to mark periods only for 24-hour stores

### Database Relationships

```
GrillConfig (1) --< SlotAssignment (*)
SlotAssignment (1) --< ProductHoldTime (*)
Product (1) --< ProductHoldTime (*)
```

## Repository Layer Changes

### New Repositories

1. **GrillConfigRepository**
   - Manages grill configurations
   - Provides methods for adding, updating, and deleting grills
   - Handles slot creation and management for grills

2. **StoreHoursRepository**
   - Manages store hours for each day of the week
   - Provides methods for updating store hours and 24-hour status
   - Initializes default store hours

3. **ProductHoldTimeRepository**
   - Manages product hold time tracking
   - Provides methods for starting, monitoring, and ending hold times
   - Handles expired product detection and management

### Updated Repositories

1. **TimePeriodRepository**
   - Added methods to support 24-hour operation
   - Added methods to filter time periods based on store hours
   - Added initialization of extended time periods

## ViewModel Layer Changes

### New ViewModels

1. **GrillConfigViewModel**
   - Manages UI state for grill configuration
   - Handles grill CRUD operations
   - Manages slot configuration for grills

2. **StoreHoursViewModel**
   - Manages UI state for store hours settings
   - Handles store hours updates
   - Manages time period configuration

3. **ProductHoldTimeViewModel**
   - Manages UI state for hold time tracking
   - Handles hold time operations
   - Provides real-time updates of remaining time

### Updated ViewModels

1. **DashboardViewModel**
   - Added support for displaying expired hold time counts
   - Added support for showing store hours information
   - Added periodic checking for expired products

## UI Implementation

### New Fragments

1. **GrillConfigFragment**
   - UI for managing grill configurations
   - Allows adding, editing, and deleting grills
   - Provides slot configuration interface

2. **StoreHoursSettingsFragment**
   - UI for managing store hours
   - Allows setting open/close times for each day
   - Provides 24-hour operation toggle

3. **ProductHoldTimeFragment**
   - UI for tracking product hold times
   - Shows hold time status for all products
   - Provides interface for handling expired products

4. **GrillHoldTimePageFragment**
   - UI for showing hold times for a specific grill
   - Used within a ViewPager in ProductHoldTimeFragment
   - Shows slots with their associated products and hold times

### New Adapters

1. **GrillConfigAdapter**
   - RecyclerView adapter for grill configuration list

2. **SlotConfigAdapter**
   - RecyclerView adapter for slot configuration list

3. **StoreHoursAdapter**
   - RecyclerView adapter for store hours list

4. **TimePeriodAdapter**
   - RecyclerView adapter for time period configuration

5. **HoldTimeSlotAdapter**
   - RecyclerView adapter for hold time slots

6. **ExpiredProductAdapter**
   - RecyclerView adapter for expired products list

7. **GrillHoldTimePageAdapter**
   - ViewPager2 adapter for grill hold time pages

### Updated Layouts

1. **Dashboard**
   - Added hold time button with notification badge
   - Added store hours display

2. **Settings**
   - Added options for grill configuration and store hours

## Migration Strategy

### Database Migration

1. **Migration_1_2**
   - Creates new tables for GrillConfig, StoreHours, and ProductHoldTime
   - Updates existing tables (SlotAssignment, TimePeriod)
   - Initializes default data for new entities

### Data Migration

1. **Existing Data**
   - All existing slots are associated with Grill #1
   - Default store hours are set to 6am-10pm for all days
   - Default time periods are preserved and extended periods are added

### Initialization

1. **DatabaseInitializer**
   - Handles initialization of default values for new entities
   - Checks if initialization is needed before performing it
   - Provides methods for initializing each entity type

## Testing Considerations

### Unit Tests

1. **Repository Tests**
   - Test new repositories (GrillConfigRepository, StoreHoursRepository, ProductHoldTimeRepository)
   - Test updated repositories with new functionality

2. **ViewModel Tests**
   - Test new ViewModels (GrillConfigViewModel, StoreHoursViewModel, ProductHoldTimeViewModel)
   - Test updated ViewModels with new functionality

### Integration Tests

1. **Database Tests**
   - Test migration from version 1 to version 2
   - Test relationships between new and existing entities
   - Test complex queries involving multiple entities

2. **Repository-DAO Tests**
   - Test integration between repositories and DAOs
   - Test transaction handling for complex operations

### UI Tests

1. **Fragment Tests**
   - Test new fragments (GrillConfigFragment, StoreHoursSettingsFragment, ProductHoldTimeFragment)
   - Test navigation between fragments

2. **Adapter Tests**
   - Test data binding in adapters
   - Test user interactions with adapter items

### End-to-End Tests

1. **Feature Tests**
   - Test complete workflows for each new feature
   - Test integration between features

2. **Migration Tests**
   - Test upgrading from previous version to new version
   - Verify data integrity after migration