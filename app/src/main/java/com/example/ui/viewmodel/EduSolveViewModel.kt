package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.ScanHistoryItem
import com.example.data.repository.GeminiRepository
import com.example.data.repository.HistoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Locale

class EduSolveViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

  private val historyRepo: HistoryRepository
  private val geminiRepo = GeminiRepository()

  init {
    val dao = AppDatabase.getDatabase(application).scanHistoryDao()
    historyRepo = HistoryRepository(dao)
  }

  // --- UI State ---
  private val _isBangla = MutableStateFlow(false)
  val isBangla: StateFlow<Boolean> = _isBangla.asStateFlow()

  private val _selectedSubject = MutableStateFlow("All Subjects")
  val selectedSubject: StateFlow<String> = _selectedSubject.asStateFlow()

  private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
  val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

  private val _croppedBitmap = MutableStateFlow<Bitmap?>(null)
  val croppedBitmap: StateFlow<Bitmap?> = _croppedBitmap.asStateFlow()

  private val _solutionState = MutableStateFlow<SolutionState>(SolutionState.Idle)
  val solutionState: StateFlow<SolutionState> = _solutionState.asStateFlow()

  private val _activeTab = MutableStateFlow(0) // 0: Quick Answer, 1: Step-by-Step
  val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

  private val _isSpeaking = MutableStateFlow(false)
  val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

  // --- Scan History ---
  val historyList: StateFlow<List<ScanHistoryItem>> = _selectedSubject
    .flatMapLatest { subject ->
      if (subject == "All Subjects") {
        historyRepo.allHistory
      } else {
        historyRepo.getHistoryBySubject(subject)
      }
    }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )

  // --- TextToSpeech ---
  private var tts: TextToSpeech? = TextToSpeech(application, this)

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val res = tts?.setLanguage(Locale.ENGLISH)
      if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
        // Fallback or log
      }
      tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
          _isSpeaking.value = true
        }

        override fun onDone(utteranceId: String?) {
          _isSpeaking.value = false
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
          _isSpeaking.value = false
        }
      })
    }
  }

  fun toggleLanguage() {
    _isBangla.value = !_isBangla.value
    // Update TTS language if possible
    if (_isBangla.value) {
      val bnLocale = Locale("bn", "BD")
      val res = tts?.setLanguage(bnLocale)
      if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale("bn", "IN"))
      }
    } else {
      tts?.setLanguage(Locale.ENGLISH)
    }
  }

  fun selectSubject(subject: String) {
    _selectedSubject.value = subject
  }

  fun setActiveTab(tabIndex: Int) {
    _activeTab.value = tabIndex
  }

  fun setImageForCropping(bitmap: Bitmap) {
    _currentBitmap.value = bitmap
    _solutionState.value = SolutionState.Idle
  }

  fun setCroppedImageAndAnalyze(bitmap: Bitmap) {
    _croppedBitmap.value = bitmap
    analyzeImage(bitmap)
  }

  fun analyzeImage(bitmap: Bitmap) {
    viewModelScope.launch {
      _solutionState.value = SolutionState.Loading
      try {
        val resultJson = geminiRepo.analyzeHomeworkImage(
          bitmap = bitmap,
          selectedSubject = _selectedSubject.value,
          isBangla = _isBangla.value
        )

        // Convert cropped bitmap to base64 for saving in Room DB
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val base64Img = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

        val historyItem = ScanHistoryItem(
          imageBase64 = base64Img,
          extractedQuestion = resultJson.extractedQuestion,
          quickAnswer = resultJson.quickAnswer,
          stepByStepSolution = resultJson.stepByStepSolution,
          subject = resultJson.subjectDetected.ifBlank { _selectedSubject.value },
          keyFormulas = resultJson.keyFormulas,
          timestamp = System.currentTimeMillis()
        )

        // Save to Room DB
        val insertedId = historyRepo.insert(historyItem)
        val savedItem = historyItem.copy(id = insertedId.toInt())

        _solutionState.value = SolutionState.Success(savedItem)
      } catch (e: Exception) {
        _solutionState.value = SolutionState.Error(e.message ?: "Failed to analyze homework image.")
      }
    }
  }

  fun selectHistoryItem(item: ScanHistoryItem) {
    // If we have base64 image, decode it for display
    if (item.imageBase64.isNotEmpty()) {
      try {
        val decodedBytes = Base64.decode(item.imageBase64, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        _croppedBitmap.value = bitmap
      } catch (e: Exception) {
        _croppedBitmap.value = null
      }
    } else {
      _croppedBitmap.value = null
    }
    _solutionState.value = SolutionState.Success(item)
  }

  fun speakText(context: Context, text: String) {
    if (_isSpeaking.value) {
      tts?.stop()
      _isSpeaking.value = false
    } else {
      if (text.isNotBlank()) {
        val res = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_${System.currentTimeMillis()}")
        if (res != TextToSpeech.SUCCESS) {
          Toast.makeText(context, "Text to Speech not available for this text.", Toast.LENGTH_SHORT).show()
        }
      }
    }
  }

  fun stopSpeech() {
    if (_isSpeaking.value) {
      tts?.stop()
      _isSpeaking.value = false
    }
  }

  fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("EduSolve AI Solution", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, if (_isBangla.value) "ক্লিপবোর্ডে কপি করা হয়েছে!" else "Copied to clipboard!", Toast.LENGTH_SHORT).show()
  }

  fun shareSolution(context: Context, item: ScanHistoryItem) {
    val shareText = buildString {
      appendLine("📚 EduSolve AI - Photo Homework Helper")
      appendLine("---------------------------------------")
      appendLine("❓ Question: ${item.extractedQuestion}")
      appendLine("")
      appendLine("💡 Quick Answer: ${item.quickAnswer}")
      appendLine("")
      appendLine("📝 Step-by-Step Solution:")
      appendLine(item.stepByStepSolution)
      if (item.keyFormulas.isNotBlank()) {
        appendLine("")
        appendLine("📐 Key Formulas: ${item.keyFormulas}")
      }
      appendLine("---------------------------------------")
      appendLine("Solved with EduSolve AI")
    }

    val sendIntent = Intent().apply {
      action = Intent.ACTION_SEND
      putExtra(Intent.EXTRA_TEXT, shareText)
      type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, if (_isBangla.value) "সমাধান শেয়ার করুন" else "Share Solution via")
    context.startActivity(shareIntent)
  }

  fun deleteHistoryItem(id: Int) {
    viewModelScope.launch {
      historyRepo.deleteById(id)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      historyRepo.clearAll()
    }
  }

  fun resetState() {
    stopSpeech()
    _solutionState.value = SolutionState.Idle
    _currentBitmap.value = null
    _croppedBitmap.value = null
  }

  override fun onCleared() {
    super.onCleared()
    tts?.stop()
    tts?.shutdown()
  }
}
