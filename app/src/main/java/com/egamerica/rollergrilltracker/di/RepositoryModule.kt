package com.egamerica.rollergrilltracker.di

import com.egamerica.rollergrilltracker.data.dao.*
import com.egamerica.rollergrilltracker.data.repositories.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideProductRepository(productDao: ProductDao): ProductRepository {
        return ProductRepository(productDao)
    }

    @Provides
    @Singleton
    fun provideSalesRepository(
        salesEntryDao: SalesEntryDao,
        salesDetailDao: SalesDetailDao
    ): SalesRepository {
        return SalesRepository(salesEntryDao, salesDetailDao)
    }

    @Provides
    @Singleton
    fun provideWasteRepository(
        wasteEntryDao: WasteEntryDao,
        wasteDetailDao: WasteDetailDao
    ): WasteRepository {
        return WasteRepository(wasteEntryDao, wasteDetailDao)
    }

    @Provides
    @Singleton
    fun provideTimePeriodRepository(timePeriodDao: TimePeriodDao): TimePeriodRepository {
        return TimePeriodRepository(timePeriodDao)
    }

    @Provides
    @Singleton
    fun provideSuggestionRepository(suggestionDao: SuggestionDao): SuggestionRepository {
        return SuggestionRepository(suggestionDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(settingDao: SettingDao): SettingsRepository {
        return SettingsRepository(settingDao)
    }

    @Provides
    @Singleton
    fun provideSlotRepository(slotDao: SlotDao): SlotRepository {
        return SlotRepository(slotDao)
    }

    @Provides
    @Singleton
    fun provideInventoryRepository(inventoryDao: InventoryDao): InventoryRepository {
        return InventoryRepository(inventoryDao)
    }

    @Provides
    @Singleton
    fun provideOrderSettingsRepository(orderSettingsDao: OrderSettingsDao): OrderSettingsRepository {
        return OrderSettingsRepository(orderSettingsDao)
    }

    @Provides
    @Singleton
    fun provideOrderSuggestionRepository(orderSuggestionDao: OrderSuggestionDao): OrderSuggestionRepository {
        return OrderSuggestionRepository(orderSuggestionDao)
    }

    @Provides
    @Singleton
    fun provideGrillConfigRepository(
        grillConfigDao: GrillConfigDao,
        slotDao: SlotDao
    ): GrillConfigRepository {
        return GrillConfigRepository(grillConfigDao, slotDao)
    }

    @Provides
    @Singleton
    fun provideStoreHoursRepository(storeHoursDao: StoreHoursDao): StoreHoursRepository {
        return StoreHoursRepository(storeHoursDao)
    }

    @Provides
    @Singleton
    fun provideProductHoldTimeRepository(
        productHoldTimeDao: ProductHoldTimeDao,
        slotDao: SlotDao
    ): ProductHoldTimeRepository {
        return ProductHoldTimeRepository(productHoldTimeDao, slotDao)
    }
}