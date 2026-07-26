package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScanHistoryItem
import com.example.ui.components.MathFormulaViewer
import com.example.ui.theme.*
import com.example.ui.viewmodel.SolutionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
  state: SolutionState,
  croppedBitmap: Bitmap?,
  activeTab: Int,
  isSpeaking: Boolean,
  isBangla: Boolean,
  onTabSelected: (Int) -> Unit,
  onSpeak: (String) -> Unit,
  onCopy: (String) -> Unit,
  onShare: (ScanHistoryItem) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          title = {
            Text(
              text = if (isBangla) "এআই সমাধান" else "AI Solution",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Slate800
            )
          },
          navigationIcon = {
            IconButton(onClick = onBack) {
              Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Slate800)
            }
          },
          actions = {
            if (state is SolutionState.Success) {
              val item = state.item
              // Speak Button with active indication
              IconButton(
                onClick = {
                  val textToSpeak = if (activeTab == 0) item.quickAnswer else item.stepByStepSolution
                  onSpeak(textToSpeak)
                }
              ) {
                Icon(
                  imageVector = if (isSpeaking) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                  contentDescription = "Speak",
                  tint = if (isSpeaking) AccentRose else RoyalBlue
                )
              }

              // Copy Button
              IconButton(
                onClick = {
                  val textToCopy = if (activeTab == 0) item.quickAnswer else item.stepByStepSolution
                  onCopy(textToCopy)
                }
              ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = RoyalBlue)
              }

              // Share Button
              IconButton(onClick = { onShare(item) }) {
                Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = RoyalBlue)
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )
        HorizontalDivider(color = Slate100, thickness = 1.dp)
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (state) {
        is SolutionState.Loading -> {
          LoadingSolutionState(isBangla = isBangla)
        }
        is SolutionState.Error -> {
          ErrorSolutionState(
            message = state.message,
            isBangla = isBangla,
            onRetry = onBack
          )
        }
        is SolutionState.Success -> {
          val item = state.item
          SuccessSolutionContent(
            item = item,
            croppedBitmap = croppedBitmap,
            activeTab = activeTab,
            isBangla = isBangla,
            onTabSelected = onTabSelected
          )
        }
        SolutionState.Idle -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No question selected")
          }
        }
      }
    }
  }
}

@Composable
private fun LoadingSolutionState(isBangla: Boolean) {
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alpha by infiniteTransition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "alpha"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(100.dp)
        .clip(CircleShape)
        .background(RoyalBlue.copy(alpha = 0.15f)),
      contentAlignment = Alignment.Center
    ) {
      CircularProgressIndicator(
        modifier = Modifier.size(70.dp),
        color = RoyalBlue,
        strokeWidth = 5.dp
      )
      Icon(
        imageVector = Icons.Default.AutoAwesome,
        contentDescription = null,
        tint = RoyalBlue.copy(alpha = alpha),
        modifier = Modifier.size(36.dp)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
      text = if (isBangla) "এআই টিউটর সমাধান তৈরি করছে..." else "AI Tutor is deriving your solution...",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = if (isBangla) "ধাপে ধাপে ব্যাখ্যা ও সঠিক সূত্র প্রস্তুত করা হচ্ছে। অনুগ্রহ করে অপেক্ষা করুন।" else "Analyzing equations, checking logic, and preparing LaTeX formatting.",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
      textAlign = TextAlign.Center
    )
  }
}

@Composable
private fun ErrorSolutionState(
  message: String,
  isBangla: Boolean,
  onRetry: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = Icons.Default.ErrorOutline,
      contentDescription = "Error",
      tint = MaterialTheme.colorScheme.error,
      modifier = Modifier.size(64.dp)
    )
    Spacer(modifier = Modifier.height(16.dp))
    Text(
      text = if (isBangla) "সমাধানে সমস্যা হয়েছে" else "Analysis Failed",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.error
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = message,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
      textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(24.dp))
    Button(
      onClick = onRetry,
      colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
    ) {
      Text(text = if (isBangla) "ফিরে যান ও চেষ্টা করুন" else "Go Back & Retry", fontWeight = FontWeight.Bold)
    }
  }
}

@Composable
private fun SuccessSolutionContent(
  item: ScanHistoryItem,
  croppedBitmap: Bitmap?,
  activeTab: Int,
  isBangla: Boolean,
  onTabSelected: (Int) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(bottom = 32.dp)
  ) {
    // Extracted Question Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DeepIndigo),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = SkyBlue.copy(alpha = 0.2f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = item.subject,
                color = SkyBlue,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
              )
            }

            Text(
              text = if (isBangla) "প্রশ্ন বিশ্লেষণ" else "Extracted Question",
              color = Color.White.copy(alpha = 0.7f),
              style = MaterialTheme.typography.labelMedium
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (croppedBitmap != null) {
              Image(
                bitmap = croppedBitmap.asImageBitmap(),
                contentDescription = "Cropped Question",
                modifier = Modifier
                  .size(80.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(14.dp))
            }

            Text(
              text = item.extractedQuestion,
              color = Color.White,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              lineHeight = 22.sp,
              modifier = Modifier.weight(1f)
            )
          }
        }
      }
    }

    // Tabs for Quick Answer vs Step-by-Step
    item {
      TabRow(
        selectedTabIndex = activeTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = RoyalBlue,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
      ) {
        Tab(
          selected = activeTab == 0,
          onClick = { onTabSelected(0) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isBangla) "সংক্ষিপ্ত উত্তর" else "Quick Answer",
                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        )
        Tab(
          selected = activeTab == 1,
          onClick = { onTabSelected(1) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = Icons.Default.FormatListNumbered, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (isBangla) "ধাপে ধাপে সমাধান" else "Step-by-Step",
                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium
              )
            }
          }
        )
      }
    }

    // Tab Content
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        if (activeTab == 0) {
          // Quick Answer Tab
          Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(20.dp),
              colors = CardDefaults.cardColors(containerColor = AccentEmerald.copy(alpha = 0.1f)),
              border = androidx.compose.foundation.BorderStroke(1.5.dp, AccentEmerald.copy(alpha = 0.4f))
            ) {
              Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentEmerald)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = if (isBangla) "চূড়ান্ত উত্তর ও সারাংশ" else "Final Answer & Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = AccentEmerald
                  )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = item.quickAnswer,
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface,
                  lineHeight = 24.sp
                )
              }
            }

            if (item.keyFormulas.isNotBlank()) {
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
              ) {
                Column(modifier = Modifier.padding(20.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Functions, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = if (isBangla) "প্রয়োজনীয় সূত্র ও নিয়ম" else "Key Formulas & Concepts",
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = RoyalBlue
                    )
                  }
                  Spacer(modifier = Modifier.height(10.dp))
                  MathFormulaViewer(text = item.keyFormulas)
                }
              }
            }
          }
        } else {
          // Step-by-Step Solution Tab
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
          ) {
            Column(modifier = Modifier.padding(20.dp)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.MenuBook, contentDescription = null, tint = RoyalBlue)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = if (isBangla) "ধাপে ধাপে ব্যাখ্যা ও সমাধান" else "Detailed Step-by-Step Explanation",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue
                )
              }
              Spacer(modifier = Modifier.height(16.dp))
              MathFormulaViewer(text = item.stepByStepSolution)
            }
          }
        }
      }
    }
  }
}
