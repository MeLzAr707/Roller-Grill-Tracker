package com.egamerica.rollergrilltracker.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.egamerica.rollergrilltracker.data.dao.ProductDao
import com.egamerica.rollergrilltracker.data.dao.SalesDetailDao
import com.egamerica.rollergrilltracker.data.dao.SalesEntryDao
import com.egamerica.rollergrilltracker.data.dao.TimePeriodDao
import com.egamerica.rollergrilltracker.data.dao.WasteDetailDao
import com.egamerica.rollergrilltracker.data.dao.WasteEntryDao
import org.junit.After
import org.junit.Before
import java.io.IOException

/**
 * Base class for database tests that provides a test instance of the AppDatabase.
 */
abstract class TestDatabase {
    protected lateinit var db: AppDatabase
    protected lateinit var productDao: ProductDao
    protected lateinit var salesEntryDao: SalesEntryDao
    protected lateinit var salesDetailDao: SalesDetailDao
    protected lateinit var wasteEntryDao: WasteEntryDao
    protected lateinit var wasteDetailDao: WasteDetailDao
    protected lateinit var timePeriodDao: TimePeriodDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        productDao = db.productDao()
        salesEntryDao = db.salesEntryDao()
        salesDetailDao = db.salesDetailDao()
        wasteEntryDao = db.wasteEntryDao()
        wasteDetailDao = db.wasteDetailDao()
        timePeriodDao = db.timePeriodDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}