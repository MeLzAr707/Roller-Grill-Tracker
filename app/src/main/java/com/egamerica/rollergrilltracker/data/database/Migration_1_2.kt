package com.egamerica.rollergrilltracker.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import java.time.LocalDateTime

/**
 * Migration from database version 1 to version 2
 * 
 * This migration adds support for:
 * 1. Multiple roller grills
 * 2. Customizable store hours
 * 3. Extended time periods for 24-hour stores
 * 4. Product hold time tracking
 */
class Migration_1_2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create new tables
        createGrillConfigTable(database)
        createStoreHoursTable(database)
        createProductHoldTimeTable(database)
        
        // 2. Update existing tables
        updateSlotAssignmentsTable(database)
        updateTimePeriodsTable(database)
        
        // 3. Initialize default data
        insertDefaultGrillConfig(database)
        insertDefaultStoreHours(database)
        insertExtendedTimePeriods(database)
    }
    
    private fun createGrillConfigTable(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS grill_config (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                grillNumber INTEGER NOT NULL,
                grillName TEXT NOT NULL,
                numberOfSlots INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt TEXT NOT NULL,
                updatedAt TEXT NOT NULL
            )
            """
        )
        
        // Create index on grillNumber for faster lookups
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_grill_config_grillNumber ON grill_config(grillNumber)")
    }
    
    private fun createStoreHoursTable(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS store_hours (
                dayOfWeek INTEGER PRIMARY KEY NOT NULL,
                openTime TEXT NOT NULL,
                closeTime TEXT NOT NULL,
                is24Hours INTEGER NOT NULL DEFAULT 0,
                updatedAt TEXT NOT NULL
            )
            """
        )
    }
    
    private fun createProductHoldTimeTable(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS product_hold_times (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                productId INTEGER NOT NULL,
                slotAssignmentId INTEGER NOT NULL,
                grillNumber INTEGER NOT NULL,
                slotNumber INTEGER NOT NULL,
                startTime TEXT NOT NULL,
                expirationTime TEXT NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                wasDiscarded INTEGER NOT NULL DEFAULT 0,
                discardedAt TEXT,
                discardReason TEXT,
                FOREIGN KEY (productId) REFERENCES products(id) ON DELETE CASCADE,
                FOREIGN KEY (slotAssignmentId) REFERENCES slot_assignments(id) ON DELETE CASCADE
            )
            """
        )
        
        // Create indices for faster lookups
        database.execSQL("CREATE INDEX IF NOT EXISTS index_product_hold_times_productId ON product_hold_times(productId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_product_hold_times_slotAssignmentId ON product_hold_times(slotAssignmentId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_product_hold_times_grillNumber_slotNumber ON product_hold_times(grillNumber, slotNumber)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_product_hold_times_isActive ON product_hold_times(isActive)")
    }
    
    private fun updateSlotAssignmentsTable(database: SupportSQLiteDatabase) {
        // Create a temporary table with the new schema
        database.execSQL(
            """
            CREATE TABLE slot_assignments_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                grillNumber INTEGER NOT NULL,
                slotNumber INTEGER NOT NULL,
                productId INTEGER,
                maxCapacity INTEGER NOT NULL,
                updatedAt TEXT NOT NULL,
                productAddedAt TEXT,
                FOREIGN KEY (productId) REFERENCES products(id) ON DELETE SET NULL
            )
            """
        )
        
        // Copy data from old table to new table, setting grillNumber to 1 for all existing slots
        database.execSQL(
            """
            INSERT INTO slot_assignments_temp (id, grillNumber, slotNumber, productId, maxCapacity, updatedAt, productAddedAt)
            SELECT id, 1, slotNumber, productId, maxCapacity, updatedAt, NULL FROM slot_assignments
            """
        )
        
        // Drop the old table
        database.execSQL("DROP TABLE slot_assignments")
        
        // Rename the temporary table to the original name
        database.execSQL("ALTER TABLE slot_assignments_temp RENAME TO slot_assignments")
        
        // Create a unique index on grillNumber and slotNumber
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_slot_assignments_grillNumber_slotNumber ON slot_assignments(grillNumber, slotNumber)")
    }
    
    private fun updateTimePeriodsTable(database: SupportSQLiteDatabase) {
        // Create a temporary table with the new schema
        database.execSQL(
            """
            CREATE TABLE time_periods_temp (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                startTime TEXT NOT NULL,
                endTime TEXT NOT NULL,
                displayOrder INTEGER NOT NULL,
                isActive INTEGER NOT NULL DEFAULT 1,
                is24HourOnly INTEGER NOT NULL DEFAULT 0
            )
            """
        )
        
        // Copy data from old table to new table, setting isActive to 1 and is24HourOnly to 0
        database.execSQL(
            """
            INSERT INTO time_periods_temp (id, name, startTime, endTime, displayOrder, isActive, is24HourOnly)
            SELECT id, name, startTime, endTime, displayOrder, 1, 0 FROM time_periods
            """
        )
        
        // Drop the old table
        database.execSQL("DROP TABLE time_periods")
        
        // Rename the temporary table to the original name
        database.execSQL("ALTER TABLE time_periods_temp RENAME TO time_periods")
    }
    
    private fun insertDefaultGrillConfig(database: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()
        
        // Insert default grill config if none exists
        database.execSQL(
            """
            INSERT INTO grill_config (grillNumber, grillName, numberOfSlots, isActive, createdAt, updatedAt)
            VALUES (1, 'Main Grill', 4, 1, '$now', '$now')
            """
        )
    }
    
    private fun insertDefaultStoreHours(database: SupportSQLiteDatabase) {
        val now = LocalDateTime.now().toString()
        
        // Insert default store hours for each day of the week
        for (dayOfWeek in 1..7) {
            database.execSQL(
                """
                INSERT INTO store_hours (dayOfWeek, openTime, closeTime, is24Hours, updatedAt)
                VALUES ($dayOfWeek, '06:00', '22:00', 0, '$now')
                """
            )
        }
    }
    
    private fun insertExtendedTimePeriods(database: SupportSQLiteDatabase) {
        // Insert extended time periods for 24-hour stores
        database.execSQL(
            """
            INSERT INTO time_periods (name, startTime, endTime, displayOrder, isActive, is24HourOnly)
            VALUES ('Late Night (10pm-2am)', '22:00', '02:00', 5, 1, 1)
            """
        )
        
        database.execSQL(
            """
            INSERT INTO time_periods (name, startTime, endTime, displayOrder, isActive, is24HourOnly)
            VALUES ('Early Morning (2am-6am)', '02:00', '06:00', 6, 1, 1)
            """
        )
    }
}