package com.example.lifeassets.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lifeassets.AppContainer
import com.example.lifeassets.data.AssetEntity
import com.example.lifeassets.data.AssetFilter
import com.example.lifeassets.data.AssetRepository
import com.example.lifeassets.data.AssetStatus
import com.example.lifeassets.data.ExpenseEntity
import com.example.lifeassets.data.ExpenseFilter
import com.example.lifeassets.data.ExpenseRepository
import com.example.lifeassets.data.ExpenseType
import com.example.lifeassets.data.ImportMode
import com.example.lifeassets.data.PeriodUnit
import com.example.lifeassets.domain.nextDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class MainUiState(
    val allAssets: List<AssetEntity> = emptyList(),
    val visibleAssets: List<AssetEntity> = emptyList(),
    val allExpenses: List<ExpenseEntity> = emptyList(),
    val visibleExpenses: List<ExpenseEntity> = emptyList(),
    val assetQuery: String = "",
    val assetCategory: String? = null,
    val assetStatus: AssetStatus? = null,
    val expenseQuery: String = "",
    val expenseCategory: String? = null,
    val expenseType: ExpenseType? = null
)

class MainViewModel(
    private val assetRepository: AssetRepository,
    private val expenseRepository: ExpenseRepository,
    private val backupService: com.example.lifeassets.data.BackupService
) : ViewModel() {
    private val assetQuery = MutableStateFlow("")
    private val assetCategory = MutableStateFlow<String?>(null)
    private val assetStatus = MutableStateFlow<AssetStatus?>(null)
    private val expenseQuery = MutableStateFlow("")
    private val expenseCategory = MutableStateFlow<String?>(null)
    private val expenseType = MutableStateFlow<ExpenseType?>(null)

    private val allAssets = assetRepository.observeAssets(AssetFilter()).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    private val allExpenses = expenseRepository.observeExpenses(ExpenseFilter()).stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )

    val state: StateFlow<MainUiState> = combine(
        allAssets,
        allExpenses,
        assetQuery,
        assetCategory,
        assetStatus,
        expenseQuery,
        expenseCategory,
        expenseType
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val assets = values[0] as List<AssetEntity>
        @Suppress("UNCHECKED_CAST")
        val expenses = values[1] as List<ExpenseEntity>
        val aQuery = values[2] as String
        val aCategory = values[3] as String?
        val aStatus = values[4] as AssetStatus?
        val eQuery = values[5] as String
        val eCategory = values[6] as String?
        val eType = values[7] as ExpenseType?
        MainUiState(
            allAssets = assets,
            visibleAssets = assets.filtered(aQuery, aCategory, aStatus),
            allExpenses = expenses,
            visibleExpenses = expenses.filtered(eQuery, eCategory, eType),
            assetQuery = aQuery,
            assetCategory = aCategory,
            assetStatus = aStatus,
            expenseQuery = eQuery,
            expenseCategory = eCategory,
            expenseType = eType
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun setAssetQuery(value: String) { assetQuery.value = value }
    fun setAssetCategory(value: String?) { assetCategory.value = value }
    fun setAssetStatus(value: AssetStatus?) { assetStatus.value = value }
    fun setExpenseQuery(value: String) { expenseQuery.value = value }
    fun setExpenseCategory(value: String?) { expenseCategory.value = value }
    fun setExpenseType(value: ExpenseType?) { expenseType.value = value }

    fun saveAsset(asset: AssetEntity) = viewModelScope.launch { assetRepository.save(asset) }
    fun saveExpense(expense: ExpenseEntity) = viewModelScope.launch { expenseRepository.save(expense) }

    fun deleteAsset(id: String) = viewModelScope.launch { assetRepository.delete(id) }
    fun deleteExpense(id: String) = viewModelScope.launch { expenseRepository.delete(id) }

    fun markPaid(expense: ExpenseEntity) = viewModelScope.launch {
        val unit = runCatching { PeriodUnit.valueOf(expense.periodUnit) }.getOrDefault(PeriodUnit.MONTH)
        val next = runCatching { LocalDate.parse(expense.nextDueDate) }.getOrNull()
            ?.let { nextDate(it, unit.name) }
            ?.toString()
            ?: expense.nextDueDate
        val nextPaidPeriods = (expense.paidPeriods + 1).coerceAtMost(
            if (expense.totalPeriods > 0) expense.totalPeriods else Int.MAX_VALUE
        )
        expenseRepository.update(
            expense.copy(
                paidPeriods = nextPaidPeriods,
                nextDueDate = next,
                status = if (expense.totalPeriods > 0 && nextPaidPeriods >= expense.totalPeriods) {
                    com.example.lifeassets.data.ExpenseStatus.COMPLETED.name
                } else expense.status,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun clearAll() = viewModelScope.launch {
        assetRepository.deleteAll()
        expenseRepository.deleteAll()
    }

    suspend fun exportJson(): ByteArray = backupService.exportJson()
    suspend fun exportCsv(): ByteArray = backupService.exportCsv()
    suspend fun importJson(data: ByteArray, mode: ImportMode) = backupService.importJson(data, mode)

    fun asset(id: String?): AssetEntity? = id?.let { value -> allAssets.value.firstOrNull { it.id == value } }
    fun expense(id: String?): ExpenseEntity? = id?.let { value -> allExpenses.value.firstOrNull { it.id == value } }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(
                    container.assetRepository,
                    container.expenseRepository,
                    container.backupService
                ) as T
            }
    }
}

private fun List<AssetEntity>.filtered(
    query: String,
    category: String?,
    status: AssetStatus?
): List<AssetEntity> = filter { item ->
    val matchesQuery = query.isBlank() || listOf(item.name, item.category, item.note)
        .any { it.contains(query.trim(), ignoreCase = true) }
    val matchesCategory = category == null || item.category == category
    val matchesStatus = status == null || item.status == status.name
    matchesQuery && matchesCategory && matchesStatus
}

private fun List<ExpenseEntity>.filtered(
    query: String,
    category: String?,
    type: ExpenseType?
): List<ExpenseEntity> = filter { item ->
    val matchesQuery = query.isBlank() || listOf(item.name, item.category, item.note)
        .any { it.contains(query.trim(), ignoreCase = true) }
    val matchesCategory = category == null || item.category == category
    val matchesType = type == null || item.type == type.name
    matchesQuery && matchesCategory && matchesType
}
