package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.RoyalBlue

@Composable
fun MathFormulaViewer(
  text: String,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Split by double line breaks or block formulas
    val paragraphs = text.split("\n\n")
    for (paragraph in paragraphs) {
      if (paragraph.trim().startsWith("$$") || paragraph.trim().startsWith("\\[") || paragraph.contains("=")) {
        // Render as highlighted formula block
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(
            containerColor = RoyalBlue.copy(alpha = 0.08f)
          ),
          border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f))
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            val cleanFormula = paragraph
              .replace("$$", "")
              .replace("\\[", "")
              .replace("\\]", "")
              .trim()
            Text(
              text = cleanFormula,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
              ),
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      } else {
        // Render standard paragraph with inline formula bolding
        val styledText = paragraph
          .replace("$", " ")
          .trim()
        if (styledText.isNotEmpty()) {
          Text(
            text = styledText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 24.sp
          )
        }
      }
    }
  }
}
