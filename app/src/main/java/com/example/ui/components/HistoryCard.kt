package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ScanHistoryItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryCard(
  item: ScanHistoryItem,
  isBangla: Boolean,
  onItemClick: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val subjectColor = when (item.subject.lowercase()) {
    "mathematics", "গণিত" -> AccentEmerald
    "science", "বিজ্ঞান" -> AccentPurple
    "english", "ইংরেজি" -> AccentAmber
    "bangla", "বাংলা" -> AccentRose
    "islamic studies", "ইসলাম শিক্ষা" -> SkyBlue
    else -> RoyalBlue
  }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(20.dp))
      .clickable(onClick = onItemClick),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Subject Badge Icon
      Box(
        modifier = Modifier
          .size(48.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(subjectColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.School,
          contentDescription = item.subject,
          tint = subjectColor,
          modifier = Modifier.size(24.dp)
        )
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Content Info
      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            color = subjectColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = item.subject,
              color = subjectColor,
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          Text(
            text = formatTimestamp(item.timestamp, isBangla),
            style = MaterialTheme.typography.labelMedium,
            color = Slate400,
            fontWeight = FontWeight.Medium
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
          text = item.extractedQuestion.ifBlank { if (isBangla) "স্ক্যান করা প্রশ্ন" else "Scanned Question" },
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = Slate800,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = item.quickAnswer.ifBlank { item.stepByStepSolution },
          style = MaterialTheme.typography.bodyMedium,
          color = Slate500,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      // Delete Button
      IconButton(
        onClick = onDeleteClick,
        modifier = Modifier.size(36.dp)
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

private fun formatTimestamp(timestamp: Long, isBangla: Boolean): String {
  val diff = System.currentTimeMillis() - timestamp
  val seconds = diff / 1000
  val minutes = seconds / 60
  val hours = minutes / 60
  val days = hours / 24

  return when {
    minutes < 1 -> if (isBangla) "এইমাত্র" else "Just now"
    minutes < 60 -> if (isBangla) "${minutes} মিনিট আগে" else "${minutes}m ago"
    hours < 24 -> if (isBangla) "${hours} ঘণ্টা আগে" else "${hours}h ago"
    days == 1L -> if (isBangla) "গতকাল" else "Yesterday"
    else -> {
      val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
      sdf.format(Date(timestamp))
    }
  }
}
