package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

data class SubjectItem(
  val nameEn: String,
  val nameBn: String,
  val icon: ImageVector,
  val accentColor: Color
)

val subjectList = listOf(
  SubjectItem("All Subjects", "সব বিষয়", Icons.Default.Apps, RoyalBlue),
  SubjectItem("Mathematics", "গণিত", Icons.Default.Calculate, AccentEmerald),
  SubjectItem("Science", "বিজ্ঞান", Icons.Default.Science, AccentPurple),
  SubjectItem("English", "ইংরেজি", Icons.Default.Translate, AccentAmber),
  SubjectItem("Bangla", "বাংলা", Icons.Default.MenuBook, AccentRose),
  SubjectItem("Islamic Studies", "ইসলাম শিক্ষা", Icons.Default.Mosque, SkyBlue)
)

@Composable
fun SubjectGrid(
  selectedSubject: String,
  onSubjectSelected: (String) -> Unit,
  isBangla: Boolean,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = if (isBangla) "বিষয়সমূহ" else "BY SUBJECT",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Slate400,
        letterSpacing = 1.2.sp
      )
      Text(
        text = if (isBangla) "সব বিষয়" else "VIEW ALL",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = RoyalBlue
      )
    }

    LazyRow(
      contentPadding = PaddingValues(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.padding(bottom = 12.dp)
    ) {
      items(subjectList) { item ->
        val isSelected = selectedSubject == item.nameEn || (selectedSubject == "All Subjects" && item.nameEn == "All Subjects")
        SubjectCard(
          item = item,
          isSelected = isSelected,
          isBangla = isBangla,
          onClick = { onSubjectSelected(item.nameEn) }
        )
      }
    }
  }
}

@Composable
private fun SubjectCard(
  item: SubjectItem,
  isSelected: Boolean,
  isBangla: Boolean,
  onClick: () -> Unit
) {
  val containerColor = if (isSelected) RoyalBlue else Color.White
  val contentColor = if (isSelected) Color.White else Slate800
  val borderColor = if (isSelected) RoyalBlue else Slate100

  Card(
    modifier = Modifier
      .width(115.dp)
      .height(105.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 1.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isSelected) Color.White.copy(alpha = 0.2f)
            else item.accentColor.copy(alpha = 0.15f)
          ),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = item.icon,
          contentDescription = item.nameEn,
          tint = if (isSelected) Color.White else item.accentColor,
          modifier = Modifier.size(24.dp)
        )
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (isBangla) item.nameBn else item.nameEn,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
        color = contentColor,
        maxLines = 1
      )
    }
  }
}
