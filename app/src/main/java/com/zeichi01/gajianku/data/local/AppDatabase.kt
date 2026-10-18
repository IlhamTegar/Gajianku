package com.zeichi01.gajianku.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zeichi01.gajianku.data.local.dao.AllocationDao
import com.zeichi01.gajianku.data.local.dao.CategoryDao
import com.zeichi01.gajianku.data.local.dao.DeficitDao
import com.zeichi01.gajianku.data.local.dao.EmergencyFundDao
import com.zeichi01.gajianku.data.local.dao.ExpenseDao
import com.zeichi01.gajianku.data.local.dao.NotificationDao
import com.zeichi01.gajianku.data.local.dao.PresetDao
import com.zeichi01.gajianku.data.local.dao.SavingsTargetDao
import com.zeichi01.gajianku.data.local.dao.UserDao
import com.zeichi01.gajianku.data.local.entity.AllocationItem
import com.zeichi01.gajianku.data.local.entity.AppNotification
import com.zeichi01.gajianku.data.local.entity.Category
import com.zeichi01.gajianku.data.local.entity.DeficitHistory
import com.zeichi01.gajianku.data.local.entity.EmergencyFund
import com.zeichi01.gajianku.data.local.entity.EmergencyFundHistory
import com.zeichi01.gajianku.data.local.entity.Expense
import com.zeichi01.gajianku.data.local.entity.GlobalDeficit
import com.zeichi01.gajianku.data.local.entity.IncomingNotification
import com.zeichi01.gajianku.data.local.entity.Preset
import com.zeichi01.gajianku.data.local.entity.PresetItem
import com.zeichi01.gajianku.data.local.entity.SalaryAllocation
import com.zeichi01.gajianku.data.local.entity.SavingsTarget
import com.zeichi01.gajianku.data.local.entity.User

@Database(
    entities = [
        User::class,
        Category::class,
        Preset::class,
        PresetItem::class,
        SavingsTarget::class,
        SalaryAllocation::class,
        AllocationItem::class,
        GlobalDeficit::class,
        DeficitHistory::class,
        IncomingNotification::class,
        AppNotification::class,
        EmergencyFund::class,
        EmergencyFundHistory::class,
        Expense::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun presetDao(): PresetDao
    abstract fun savingsTargetDao(): SavingsTargetDao
    abstract fun allocationDao(): AllocationDao
    abstract fun deficitDao(): DeficitDao
    abstract fun notificationDao(): NotificationDao
    abstract fun emergencyFundDao(): EmergencyFundDao
    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gajianku.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}