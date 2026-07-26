package com.example

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.CropRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.ResultRoute
import com.example.ui.screens.CropScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.theme.EduSolveTheme
import com.example.ui.viewmodel.EduSolveViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      EduSolveTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          EduSolveApp()
        }
      }
    }
  }
}

@Composable
fun EduSolveApp(viewModel: EduSolveViewModel = viewModel()) {
  val context = LocalContext.current
  val navController = rememberNavController()

  val isBangla by viewModel.isBangla.collectAsStateWithLifecycle()
  val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
  val historyList by viewModel.historyList.collectAsStateWithLifecycle()
  val currentBitmap by viewModel.currentBitmap.collectAsStateWithLifecycle()
  val croppedBitmap by viewModel.croppedBitmap.collectAsStateWithLifecycle()
  val solutionState by viewModel.solutionState.collectAsStateWithLifecycle()
  val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
  val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()

  // Camera Launcher (TakePicturePreview returns a thumbnail Bitmap)
  val takePictureLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      viewModel.setImageForCropping(bitmap)
      navController.navigate(CropRoute)
    } else {
      Toast.makeText(context, if (isBangla) "কোনো ছবি তোলা হয়নি" else "No photo captured", Toast.LENGTH_SHORT).show()
    }
  }

  // Gallery Picker Launcher (GetContent returns a Uri)
  val pickGalleryLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
          val source = ImageDecoder.createSource(context.contentResolver, uri)
          ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
          }
        } else {
          @Suppress("DEPRECATION")
          MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
        if (bitmap != null) {
          viewModel.setImageForCropping(bitmap)
          navController.navigate(CropRoute)
        }
      } catch (e: Exception) {
        Toast.makeText(context, "Error loading image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
      }
    }
  }

  NavHost(
    navController = navController,
    startDestination = HomeRoute
  ) {
    composable<HomeRoute> {
      HomeScreen(
        isBangla = isBangla,
        selectedSubject = selectedSubject,
        historyList = historyList,
        onToggleLanguage = { viewModel.toggleLanguage() },
        onSubjectSelected = { subject -> viewModel.selectSubject(subject) },
        onCameraClick = { takePictureLauncher.launch(null) },
        onGalleryClick = { pickGalleryLauncher.launch("image/*") },
        onHistoryItemClick = { item ->
          viewModel.selectHistoryItem(item)
          navController.navigate(ResultRoute)
        },
        onDeleteHistoryItem = { id -> viewModel.deleteHistoryItem(id) },
        onClearAllHistory = { viewModel.clearAllHistory() }
      )
    }

    composable<CropRoute> {
      CropScreen(
        bitmap = currentBitmap,
        isBangla = isBangla,
        onBack = { navController.popBackStack() },
        onAnalyze = { cropped ->
          viewModel.setCroppedImageAndAnalyze(cropped)
          navController.navigate(ResultRoute) {
            popUpTo(HomeRoute) { inclusive = false }
          }
        }
      )
    }

    composable<ResultRoute> {
      ResultScreen(
        state = solutionState,
        croppedBitmap = croppedBitmap,
        activeTab = activeTab,
        isSpeaking = isSpeaking,
        isBangla = isBangla,
        onTabSelected = { tab -> viewModel.setActiveTab(tab) },
        onSpeak = { text -> viewModel.speakText(context, text) },
        onCopy = { text -> viewModel.copyToClipboard(context, text) },
        onShare = { item -> viewModel.shareSolution(context, item) },
        onBack = {
          viewModel.stopSpeech()
          navController.popBackStack()
        }
      )
    }
  }
}
