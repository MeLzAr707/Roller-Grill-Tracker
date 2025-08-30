package com.egamerica.rollergrilltracker.di

import android.content.Context
import com.egamerica.rollergrilltracker.data.dao.*
import com.egamerica.rollergrilltracker.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCoroutineScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        coroutineScope: CoroutineScope
    ): AppDatabase {
        return AppDatabase.getDatabase(context, coroutineScope)
    }

    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }

    @Provides
    fun provideSalesEntryDao(database: AppDatabase): SalesEntryDao {
        return database.salesEntryDao()
    }

    @Provides
    fun provideSalesDetailDao(database: AppDatabase): SalesDetailDao {
        return database.salesDetailDao()
    }

    @Provides
    fun provideWasteEntryDao(database: AppDatabase): WasteEntryDao {
        return database.wasteEntryDao()
    }

    @Provides
    fun provideWasteDetailDao(database: AppDatabase): WasteDetailDao {
        return database.wasteDetailDao()
    }

    @Provides
    fun provideTimePeriodDao(database: AppDatabase): TimePeriodDao {
        return database.timePeriodDao()
    }

    @Provides
    fun provideSuggestionDao(database: AppDatabase): SuggestionDao {
        return database.suggestionDao()
    }

    @Provides
    fun provideSettingDao(database: AppDatabase): SettingDao {
        return database.settingDao()
    }

    @Provides
    fun provideSlotDao(database: AppDatabase): SlotDao {
        return database.slotDao()
    }

    @Provides
    fun provideInventoryDao(database: AppDatabase): InventoryDao {
        return database.inventoryDao()
    }

    @Provides
    fun provideOrderSettingsDao(database: AppDatabase): OrderSettingsDao {
        return database.orderSettingsDao()
    }

    @Provides
    fun provideOrderSuggestionDao(database: AppDatabase): OrderSuggestionDao {
        return database.orderSuggestionDao()
    }

    @Provides
    fun provideGrillConfigDao(database: AppDatabase): GrillConfigDao {
        return database.grillConfigDao()
    }

    @Provides
    fun provideStoreHoursDao(database: AppDatabase): StoreHoursDao {
        return database.storeHoursDao()
    }

    @Provides
    fun provideProductHoldTimeDao(database: AppDatabase): ProductHoldTimeDao {
        return database.productHoldTimeDao()
    }
}