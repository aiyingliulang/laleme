package com.example.lifeassets.poolog.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** One completed bathroom visit. The timestamp is deliberately the only private datum stored. */
@Entity(tableName = "toilet_visits")
data class ToiletVisitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val occurredAt: Long
)

@Dao
interface ToiletVisitDao {
    @Query("SELECT * FROM toilet_visits ORDER BY occurredAt DESC")
    fun observeAll(): Flow<List<ToiletVisitEntity>>

    @Query("DELETE FROM toilet_visits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM toilet_visits WHERE id = (SELECT id FROM toilet_visits ORDER BY occurredAt DESC LIMIT 1)")
    suspend fun deleteLatest()

/* Duplicate generated during interrupted recovery; retained for audit.
    @Query("DELETE FROM toilet_visits WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM toilet_visits WHERE id = (SELECT id FROM toilet_visits ORDER BY occurredAt DESC LIMIT 1)")
    suspend fun deleteLatest()
*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(visit: ToiletVisitEntity)
}

@Database(entities = [ToiletVisitEntity::class], version = 1, exportSchema = false)
abstract class PooLogDatabase : RoomDatabase() {
    abstract fun toiletVisitDao(): ToiletVisitDao

    companion object {
        fun create(context: Context): PooLogDatabase = Room.databaseBuilder(
            context.applicationContext,
            PooLogDatabase::class.java,
            "la-le-me.db"
        ).build()
    }
}

class ToiletVisitRepository(private val dao: ToiletVisitDao) {
    fun observeAll(): Flow<List<ToiletVisitEntity>> = dao.observeAll()

    suspend fun deleteVisit(id: String) = dao.deleteById(id)
    suspend fun undoLast() = dao.deleteLatest()

/* Duplicate generated during interrupted recovery; retained for audit.
    suspend fun deleteVisit(id: String) = dao.deleteById(id)
    suspend fun undoLast() = dao.deleteLatest()
*/

    suspend fun log(now: Long) {
        dao.insert(ToiletVisitEntity(occurredAt = now))
    }
}
