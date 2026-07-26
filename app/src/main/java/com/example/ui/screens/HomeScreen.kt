package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ScanHistoryItem
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  isBangla: Boolean,
  selectedSubject: String,
  historyList: List<ScanHistoryItem>,
  onToggleLanguage: () -> Unit,
  onSubjectSelected: (String) -> Unit,
  onCameraClick: () -> Unit,
  onGalleryClick: () -> Unit,
  onHistoryItemClick: (ScanHistoryItem) -> Unit,
  onDeleteHistoryItem: (Int) -> Unit,
  onClearAllHistory: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showClearConfirmDialog by remember { mutableStateOf(false) }

  if (showClearConfirmDialog) {
    AlertDialog(
      onDismissRequest = { showClearConfirmDialog = false },
      title = {
        Text(text = if (isBangla) "সব ইতিহাস মুছবেন?" else "Clear All History?")
      },
      text = {
        Text(text = if (isBangla) "আপনার সংরক্ষিত সমস্ত স্ক্যান মুছে যাবে। এটি আর ফিরিয়ে আনা যাবে না।" else "All your saved homework solutions will be permanently deleted.")
      },
      confirmButton = {
        TextButton(
          onClick = {
            onClearAllHistory()
            showClearConfirmDialog = false
          },
          colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
          Text(text = if (isBangla) "মুছে ফেলুন" else "Clear All", fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearConfirmDialog = false }) {
          Text(text = if (isBangla) "বাতিল" else "Cancel")
        }
      }
    )
  }

  Scaffold(
    topBar = {
      Column {
        TopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(RoyalBlue),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "E",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 18.sp
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = "EduSolve ",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = Slate800,
                  fontSize = 20.sp
                )
                Text(
                  text = "AI",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = RoyalBlue,
                  fontSize = 20.sp
                )
              }
            }
          },
          actions = {
            // Language Toggle Button
            Surface(
              onClick = onToggleLanguage,
              shape = RoundedCornerShape(50),
              color = Slate100,
              modifier = Modifier.padding(end = 8.dp)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = if (isBangla) "BN | EN" else "EN | BN",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = Slate500,
                  letterSpacing = 1.sp
                )
              }
            }

            if (historyList.isNotEmpty()) {
              IconButton(onClick = { showClearConfirmDialog = true }) {
                Icon(
                  imageVector = Icons.Default.DeleteSweep,
                  contentDescription = "Clear History",
                  tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              }
            }
          },
          colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
          )
        )
        HorizontalDivider(color = Slate100, thickness = 1.dp)
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentPadding = PaddingValues(bottom = 32.dp)
    ) {
      // Hero Card Section
      item {
        HeroCard(
          isBangla = isBangla,
          onCameraClick = onCameraClick,
          onGalleryClick = onGalleryClick
        )
      }

      // Subject Filter Grid
      item {
        SubjectGrid(
          selectedSubject = selectedSubject,
          onSubjectSelected = onSubjectSelected,
          isBangla = isBangla
        )
      }

      // History Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (isBangla) "সাম্প্রতিক স্ক্যানসমূহ (${historyList.size})" else "RECENT SCANS (${historyList.size})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = Slate400,
            letterSpacing = 1.2.sp
          )
          Text(
            text = if (isBangla) "ইতিহাস দেখুন" else "See History",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Slate400
          )
        }
      }

      // History List or Empty State
      if (historyList.isEmpty()) {
        item {
          EmptyHistoryState(isBangla = isBangla, onScanClick = onCameraClick)
        }
      } else {
        items(historyList, key = { it.id }) { item ->
          HistoryCard(
            item = item,
            isBangla = isBangla,
            onItemClick = { onHistoryItemClick(item) },
            onDeleteClick = { onDeleteHistoryItem(item.id) }
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyHistoryState(
  isBangla: Boolean,
  onScanClick: () -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 32.dp, vertical = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Box(
      modifier = Modifier
        .size(160.dp)
        .clip(RoundedCornerShape(24.dp))
        .background(RoyalBlue.copy(alpha = 0.08f)),
      contentAlignment = Alignment.Center
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_empty_history),
        contentDescription = "Empty History",
        modifier = Modifier
          .size(140.dp)
          .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Crop
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = if (isBangla) "এখনো কোনো প্রশ্ন সমাধান করা হয়নি!" else "No homework scanned yet!",
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onBackground,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = if (isBangla) "যেকোনো কঠিন গণিত, বিজ্ঞান বা ইংরেজি প্রশ্নের ছবি তুলুন। এআই নিমেষেই সমাধান করে দেবে!" else "Take a photo of any Math, Science, or English problem. Our AI will explain it step-by-step!",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
      textAlign = TextAlign.Center,
      lineHeight = 20.sp
    )

    Spacer(modifier = Modifier.height(20.dp))

    Button(
      onClick = onScanClick,
      colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
      shape = RoundedCornerShape(14.dp)
    ) {
      Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = if (isBangla) "প্রথম প্রশ্ন স্ক্যান করুন" else "Scan Your First Question",
        fontWeight = FontWeight.Bold
      )
    }
  }
}
