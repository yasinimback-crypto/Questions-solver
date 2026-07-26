package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@Composable
fun HeroCard(
  isBangla: Boolean,
  onCameraClick: () -> Unit,
  onGalleryClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    shape = RoundedCornerShape(24.dp),
    colors = CardDefaults.cardColors(containerColor = Slate800),
    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(230.dp)
    ) {
      // Background Image with Gradient Overlay
      Image(
        painter = painterResource(id = R.drawable.img_hero_banner),
        contentDescription = "EduSolve Hero Banner",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Slate800.copy(alpha = 0.70f),
                Slate800.copy(alpha = 0.96f)
              )
            )
          )
      )

      // Decorative Geometric Elements (Bottom Right Glow & Top Right Dot Grid)
      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
          .offset(x = 30.dp, y = 30.dp)
          .size(150.dp)
          .background(
            Brush.radialGradient(
              colors = listOf(RoyalBlue.copy(alpha = 0.25f), Color.Transparent)
            ),
            shape = CircleShape
          )
      )

      // Top Right 3x3 Dot Grid
      Column(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(top = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        repeat(3) {
          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(3) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
              )
            }
          }
        }
      }

      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
      ) {
        // Title and Subtitle
        Column {
          Surface(
            color = RoyalBlue.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
            modifier = Modifier.padding(bottom = 10.dp)
          ) {
            Text(
              text = if (isBangla) "✨ এআই হোমওয়ার্ক সলভার" else "✨ AI HOMEWORK SOLVER",
              color = Color(0xFF60A5FA),
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
          }

          Text(
            text = if (isBangla) "প্রশ্ন স্ক্যান করে সমাধান পান" else "Stuck on a question?",
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 28.sp
          )

          Text(
            text = if (isBangla) "যেকোনো প্রশ্নের ছবি তুলুন আর নিমেষেই পান ধাপে ধাপে সমাধান।" else "Snap a photo and get expert step-by-step solutions instantly.",
            color = Slate300,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
          )
        }

        // Action Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Button(
            onClick = onCameraClick,
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = RoyalBlue,
              contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Default.CameraAlt,
              contentDescription = "Camera",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isBangla) "ক্যামেরা" else "Camera",
              fontWeight = FontWeight.Bold
            )
          }

          OutlinedButton(
            onClick = onGalleryClick,
            modifier = Modifier
              .weight(1f)
              .height(50.dp),
            colors = ButtonDefaults.outlinedButtonColors(
              containerColor = Color.White.copy(alpha = 0.12f),
              contentColor = Color.White
            ),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f)),
            shape = RoundedCornerShape(16.dp)
          ) {
            Icon(
              imageVector = Icons.Default.PhotoLibrary,
              contentDescription = "Gallery",
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (isBangla) "গ্যালারি" else "Gallery",
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }
}
