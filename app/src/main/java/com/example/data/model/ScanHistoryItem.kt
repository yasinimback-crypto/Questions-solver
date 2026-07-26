package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "scan_history")
@Serializable
data class ScanHistoryItem(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val imageBase64: String = "",
  val extractedQuestion: String,
  val quickAnswer: String,
  val stepByStepSolution: String,
  val subject: String,
  val keyFormulas: String = "",
  val timestamp: Long = System.currentTimeMillis()
)
