package com.example.lifeassets

import android.app.Application
import androidx.room.Room
import com.example.lifeassets.data.AppDatabase
import com.example.lifeassets.data.AppSettingsRepository
import com.example.lifeassets.data.AssetRepositoryImpl
import com.example.lifeassets.data.ExpenseRepositoryImpl
import com.example.lifeassets.data.JsonBackupService

class LifeAssetsApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "life_assets.db"
        ).build()
        container = AppContainer(
            assetRepository = AssetRepositoryImpl(database.assetDao()),
            expenseRepository = ExpenseRepositoryImpl(database.expenseDao()),
            backupService = JsonBackupService(database),
            settingsRepository = AppSettingsRepository(this)
        )
    }
}

data class AppContainer(
    val assetRepository: com.example.lifeassets.data.AssetRepository,
    val expenseRepository: com.example.lifeassets.data.ExpenseRepository,
    val backupService: com.example.lifeassets.data.BackupService,
    val settingsRepository: AppSettingsRepository
)
