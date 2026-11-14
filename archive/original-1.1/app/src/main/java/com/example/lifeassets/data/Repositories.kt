package com.example.lifeassets.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class AssetFilter(
    val query: String = "",
    val category: String? = null,
    val status: AssetStatus? = null
)

data class ExpenseFilter(
    val query: String = "",
    val category: String? = null,
    val type: ExpenseType? = null
)

interface AssetRepository {
    fun observeAssets(filter: AssetFilter): Flow<List<AssetEntity>>
    suspend fun save(asset: AssetEntity)
    suspend fun update(asset: AssetEntity)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}

interface ExpenseRepository {
    fun observeExpenses(filter: ExpenseFilter): Flow<List<ExpenseEntity>>
    suspend fun save(expense: ExpenseEntity)
    suspend fun update(expense: ExpenseEntity)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}

class AssetRepositoryImpl(private val dao: AssetDao) : AssetRepository {
    override fun observeAssets(filter: AssetFilter): Flow<List<AssetEntity>> =
        dao.observeAll().map { items ->
            items.filter { item ->
                val query = filter.query.trim()
                val matchesQuery = query.isBlank() || listOf(item.name, item.category, item.note)
                    .any { it.contains(query, ignoreCase = true) }
                val matchesCategory = filter.category == null || item.category == filter.category
                val matchesStatus = filter.status == null || item.status == filter.status.name
                matchesQuery && matchesCategory && matchesStatus
            }
        }

    override suspend fun save(asset: AssetEntity) = dao.upsert(asset)
    override suspend fun update(asset: AssetEntity) = dao.upsert(asset)
    override suspend fun delete(id: String) {
        dao.getAll().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }
    override suspend fun deleteAll() = dao.deleteAll()
}

class ExpenseRepositoryImpl(private val dao: ExpenseDao) : ExpenseRepository {
    override fun observeExpenses(filter: ExpenseFilter): Flow<List<ExpenseEntity>> =
        dao.observeAll().map { items ->
            items.filter { item ->
                val query = filter.query.trim()
                val matchesQuery = query.isBlank() || listOf(item.name, item.category, item.note)
                    .any { it.contains(query, ignoreCase = true) }
                val matchesCategory = filter.category == null || item.category == filter.category
                val matchesType = filter.type == null || item.type == filter.type.name
                matchesQuery && matchesCategory && matchesType
            }
        }

    override suspend fun save(expense: ExpenseEntity) = dao.upsert(expense)
    override suspend fun update(expense: ExpenseEntity) = dao.upsert(expense)
    override suspend fun delete(id: String) {
        dao.getAll().firstOrNull { it.id == id }?.let { dao.delete(it) }
    }
    override suspend fun deleteAll() = dao.deleteAll()
}

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class AppSettingsRepository(private val context: Context) {
    private val currencyKey = stringPreferencesKey("currency")

    val currency: Flow<String> = context.settingsDataStore.data.map { it[currencyKey] ?: "CNY" }

    suspend fun setCurrency(currency: String) {
        context.settingsDataStore.edit { it[currencyKey] = currency }
    }
}
