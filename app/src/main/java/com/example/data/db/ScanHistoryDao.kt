package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanHistoryDao {
  @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
  fun getAllHistory(): Flow<List<ScanHistoryItem>>

  @Query("SELECT * FROM scan_history WHERE subject = :subject ORDER BY timestamp DESC")
  fun getHistoryBySubject(subject: String): Flow<List<ScanHistoryItem>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ScanHistoryItem): Long

  @Query("DELETE FROM scan_history WHERE id = :id")
  suspend fun deleteItemById(id: Int)

  @Query("DELETE FROM scan_history")
  suspend fun clearAll()
}
