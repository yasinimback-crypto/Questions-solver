package com.example.data.repository

import com.example.data.db.ScanHistoryDao
import com.example.data.model.ScanHistoryItem
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val scanHistoryDao: ScanHistoryDao) {
  val allHistory: Flow<List<ScanHistoryItem>> = scanHistoryDao.getAllHistory()

  fun getHistoryBySubject(subject: String): Flow<List<ScanHistoryItem>> {
    return scanHistoryDao.getHistoryBySubject(subject)
  }

  suspend fun insert(item: ScanHistoryItem): Long {
    return scanHistoryDao.insertItem(item)
  }

  suspend fun deleteById(id: Int) {
    scanHistoryDao.deleteItemById(id)
  }

  suspend fun clearAll() {
    scanHistoryDao.clearAll()
  }
}
