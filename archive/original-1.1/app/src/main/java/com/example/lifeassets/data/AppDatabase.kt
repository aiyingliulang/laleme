package com.example.lifeassets.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AssetEntity::class, ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
    abstract fun expenseDao(): ExpenseDao
}
