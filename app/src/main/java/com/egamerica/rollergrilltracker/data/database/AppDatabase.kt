package com.egamerica.rollergrilltracker.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.egamerica.rollergrilltracker.data.dao.*
import com.egamerica.rollergrilltracker.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(
    entities = [
        Product::class,
        SalesEntry::class,
        SalesDetail::class,
        WasteEntry::class,
        WasteDetail::class,
        TimePeriod::class,
        Suggestion::class,
        Setting::class,
        SlotAssignment::class,
        InventoryCount::class,
        OrderSettings::class,
        OrderSuggestion::class,
        GrillConfig::class,
        StoreHours::class,
        ProductHoldTime::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun salesEntryDao(): SalesEntryDao
    abstract fun salesDetailDao(): SalesDetailDao
    abstract fun wasteEntryDao(): WasteEntryDao
    abstract fun wasteDetailDao(): WasteDetailDao
    abstract fun timePeriodDao(): TimePeriodDao
    abstract fun suggestionDao(): SuggestionDao
    abstract fun settingDao(): SettingDao
    abstract fun slotDao(): SlotDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun orderSettingsDao(): OrderSettingsDao
    abstract fun orderSuggestionDao(): OrderSuggestionDao
    abstract fun grillConfigDao(): GrillConfigDao
    abstract fun storeHoursDao(): StoreHoursDao
    abstract fun productHoldTimeDao(): ProductHoldTimeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "roller_grill_database"
                )
                .addCallback(DatabaseCallback(scope))
                .addMigrations(Migration_1_2())
                .build()
                INSTANCE = instance
                instance
            }
        }
        
        // Make INSTANCE accessible for DatabaseCallback
        internal fun getInstance(): AppDatabase? {
            return INSTANCE
        }
    }
}

// Database callback to prepopulate data
class DatabaseCallback(private val scope: CoroutineScope) : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        AppDatabase.getInstance()?.let { database ->
            scope.launch(Dispatchers.IO) {
                // Prepopulate time periods
                val timePeriodDao = database.timePeriodDao()
                timePeriodDao.insertAll(
                    listOf(
                        TimePeriod(1, "Morning", "06:00", "10:00", 1, isActive = true, is24HourOnly = false),
                        TimePeriod(2, "Midday", "10:00", "14:00", 2, isActive = true, is24HourOnly = false),
                        TimePeriod(3, "Afternoon", "14:00", "18:00", 3, isActive = true, is24HourOnly = false),
                        TimePeriod(4, "Evening", "18:00", "22:00", 4, isActive = true, is24HourOnly = false)
                    )
                )

                // Prepopulate products with the updated list and corrected UPC
                val productDao = database.productDao()
                productDao.insertAll(
                    listOf(
                        Product(name = "SEC Tornado", barcode = "042704020021", category = "Tornado"),
                        Product(name = "French Toast Tornado", barcode = "04270403404", category = "Tornado"),
                        Product(name = "Chicken and Waffle RollerBite", barcode = "042704002270", category = "RollerBite"),
                        Product(name = "Breakfast Sausage", barcode = "042704036176", category = "Sausage"),
                        Product(name = "Hot Dog", barcode = "042704034042", category = "Hot Dog"),
                        Product(name = "Pork/Veggie Egg Roll", barcode = "042704020540", category = "Egg Roll"),
                        Product(name = "Buffalo Chicken RollerBite", barcode = "042704036923", category = "RollerBite"),
                        Product(name = "Monterey Chicken RollerBite", barcode = "042704036237", category = "RollerBite"),
                        Product(name = "Ranchero Tornado", barcode = "042704019988", category = "Tornado"),
                        Product(name = "Pepperjack Tornado", barcode = "042704019995", category = "Tornado"),
                        Product(name = "Chicken Fajita Tornado", barcode = "042704005356", category = "Tornado"),
                        Product(name = "Cheddar Beer Brat", barcode = "042704917031", category = "Brat"),
                        Product(name = "Cheeseburger Tornado", barcode = "042704082135", category = "Tornado"),
                        Product(name = "Cheesy Pepperoni Tornado", barcode = "042704004632", category = "Tornado"),
                        Product(name = "Southwest Chicken Tornado", barcode = "071007867378", category = "Tornado"),
                        Product(name = "Pork Tamales", barcode = "042704004625", category = "Tamale")
                    )
                )
                
                // Initialize default slots
                val slotDao = database.slotDao()
                slotDao.insertAll(
                    listOf(
                        SlotAssignment(grillNumber = 1, slotNumber = 1, productId = null, maxCapacity = 8),
                        SlotAssignment(grillNumber = 1, slotNumber = 2, productId = null, maxCapacity = 8),
                        SlotAssignment(grillNumber = 1, slotNumber = 3, productId = null, maxCapacity = 8),
                        SlotAssignment(grillNumber = 1, slotNumber = 4, productId = null, maxCapacity = 8),
                        SlotAssignment(grillNumber = 1, slotNumber = 5, productId = null, maxCapacity = 16) // Tamale cooker
                    )
                )
                
                // Initialize default order settings
                val orderSettingsDao = database.orderSettingsDao()
                orderSettingsDao.insertOrUpdateSettings(
                    OrderSettings(
                        id = 1,
                        orderFrequency = 2,
                        orderDays = "1,4", // Monday and Thursday
                        leadTimeDays = 1,
                        updatedAt = LocalDateTime.now()
                    )
                )
                
                // Initialize default grill config
                val grillConfigDao = database.grillConfigDao()
                grillConfigDao.insert(
                    GrillConfig(
                        grillNumber = 1,
                        grillName = "Main Grill",
                        numberOfSlots = 5,
                        isActive = true
                    )
                )
                
                // Initialize default store hours
                val storeHoursDao = database.storeHoursDao()
                for (dayOfWeek in 1..7) {
                    storeHoursDao.insert(
                        StoreHours(
                            dayOfWeek = dayOfWeek,
                            openTime = "06:00",
                            closeTime = "22:00",
                            is24Hours = false
                        )
                    )
                }
            }
        }
    }
}