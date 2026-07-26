package com.example.ui.viewmodel

import com.example.data.model.ScanHistoryItem

sealed interface SolutionState {
  object Idle : SolutionState
  object Loading : SolutionState
  data class Success(val item: ScanHistoryItem) : SolutionState
  data class Error(val message: String) : SolutionState
}
