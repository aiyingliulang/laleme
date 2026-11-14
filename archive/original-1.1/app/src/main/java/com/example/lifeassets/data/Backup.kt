package com.example.lifeassets.data

import androidx.room.withTransaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

enum class ImportMode { MERGE, REPLACE }

interface BackupService {
    suspend fun exportJson(): ByteArray
    suspend fun exportCsv(): ByteArray
    suspend fun importJson(data: ByteArray, mode: ImportMode)
}

class JsonBackupService(private val database: AppDatabase) : BackupService {
    override suspend fun exportJson(): ByteArray = withContext(Dispatchers.IO) {
        val root = JSONObject()
            .put("schemaVersion", 1)
            .put("exportedAt", System.currentTimeMillis())

        val assets = JSONArray()
        database.assetDao().getAll().forEach { asset ->
            assets.put(JSONObject().apply {
                put("id", asset.id)
                put("name", asset.name)
                put("category", asset.category)
                put("priceCents", asset.priceCents)
                put("purchaseDate", asset.purchaseDate)
                put("targetDate", asset.targetDate ?: JSONObject.NULL)
                put("status", asset.status)
                put("imagePath", asset.imagePath ?: JSONObject.NULL)
                put("note", asset.note)
                put("createdAt", asset.createdAt)
                put("updatedAt", asset.updatedAt)
            })
        }
        root.put("assets", assets)

        val expenses = JSONArray()
        database.expenseDao().getAll().forEach { expense ->
            expenses.put(JSONObject().apply {
                put("id", expense.id)
                put("name", expense.name)
                put("category", expense.category)
                put("type", expense.type)
                put("totalAmountCents", expense.totalAmountCents)
                put("periodAmountCents", expense.periodAmountCents)
                put("periodUnit", expense.periodUnit)
                put("totalPeriods", expense.totalPeriods)
                put("paidPeriods", expense.paidPeriods)
                put("nextDueDate", expense.nextDueDate)
                put("autoRenew", expense.autoRenew)
                put("status", expense.status)
                put("imagePath", expense.imagePath ?: JSONObject.NULL)
                put("note", expense.note)
                put("createdAt", expense.createdAt)
                put("updatedAt", expense.updatedAt)
            })
        }
        root.put("expenses", expenses)
        root.toString(2).toByteArray(Charsets.UTF_8)
    }

    override suspend fun exportCsv(): ByteArray = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        builder.appendLine("kind,id,name,category,type,amountCents,periodAmountCents,periodUnit,purchaseDate,targetDate,nextDueDate,totalPeriods,paidPeriods,status,note")
        database.assetDao().getAll().forEach { asset ->
            builder.appendCsvRow(
                listOf("asset", asset.id, asset.name, asset.category, "", asset.priceCents,
                    "", "", asset.purchaseDate, asset.targetDate ?: "", "", "", "",
                    asset.status, asset.note)
            )
        }
        database.expenseDao().getAll().forEach { expense ->
            builder.appendCsvRow(
                listOf("expense", expense.id, expense.name, expense.category, expense.type,
                    expense.totalAmountCents, expense.periodAmountCents, expense.periodUnit,
                    "", "", expense.nextDueDate, expense.totalPeriods, expense.paidPeriods,
                    expense.status, expense.note)
            )
        }
        builder.toString().toByteArray(Charsets.UTF_8)
    }

    override suspend fun importJson(data: ByteArray, mode: ImportMode) {
        withContext(Dispatchers.IO) {
            val root = JSONObject(String(data, Charsets.UTF_8))
            require(root.optInt("schemaVersion", 0) == 1) { "不支持的备份版本" }
            val assets = root.optJSONArray("assets") ?: JSONArray()
            val expenses = root.optJSONArray("expenses") ?: JSONArray()
            database.withTransaction {
                if (mode == ImportMode.REPLACE) {
                    database.assetDao().deleteAll()
                    database.expenseDao().deleteAll()
                }
                database.assetDao().upsertAll((0 until assets.length()).map { assets.toAsset(it) })
                database.expenseDao().upsertAll((0 until expenses.length()).map { expenses.toExpense(it) })
            }
        }
    }

    private fun JSONArray.toAsset(index: Int): AssetEntity {
        val value = getJSONObject(index)
        return AssetEntity(
            id = value.getString("id"),
            name = value.getString("name"),
            category = value.optString("category", "其他"),
            priceCents = value.optLong("priceCents", 0),
            purchaseDate = value.optString("purchaseDate"),
            targetDate = value.takeNullableString("targetDate"),
            status = value.optString("status", AssetStatus.ACTIVE.name),
            imagePath = value.takeNullableString("imagePath"),
            note = value.optString("note"),
            createdAt = value.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = value.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun JSONArray.toExpense(index: Int): ExpenseEntity {
        val value = getJSONObject(index)
        return ExpenseEntity(
            id = value.getString("id"),
            name = value.getString("name"),
            category = value.optString("category", "其他"),
            type = value.optString("type", ExpenseType.SUBSCRIPTION.name),
            totalAmountCents = value.optLong("totalAmountCents", 0),
            periodAmountCents = value.optLong("periodAmountCents", 0),
            periodUnit = value.optString("periodUnit", PeriodUnit.MONTH.name),
            totalPeriods = value.optInt("totalPeriods", 0),
            paidPeriods = value.optInt("paidPeriods", 0),
            nextDueDate = value.optString("nextDueDate"),
            autoRenew = value.optBoolean("autoRenew", true),
            status = value.optString("status", ExpenseStatus.ACTIVE.name),
            imagePath = value.takeNullableString("imagePath"),
            note = value.optString("note"),
            createdAt = value.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = value.optLong("updatedAt", System.currentTimeMillis())
        )
    }
}

private fun JSONObject.takeNullableString(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }

private fun StringBuilder.appendCsvRow(values: List<Any?>) {
    appendLine(values.joinToString(",") { value ->
        val text = value?.toString() ?: ""
        if (text.any { it == ',' || it == '"' || it == '\n' }) {
            "\"${text.replace("\"", "\"\"")}\""
        } else {
            text
        }
    })
}
