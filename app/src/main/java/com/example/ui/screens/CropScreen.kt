package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

enum class AspectRatioPreset(val labelEn: String, val labelBn: String, val ratio: Float?) {
  FREE("Free", "ফ্রি", null),
  SQUARE("1:1", "১:১", 1f),
  STANDARD("4:3", "৪:৩", 4f / 3f),
  WIDE("16:9", "১৬:৯", 16f / 9f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropScreen(
  bitmap: Bitmap?,
  isBangla: Boolean,
  onBack: () -> Unit,
  onAnalyze: (Bitmap) -> Unit,
  modifier: Modifier = Modifier
) {
  var currentBitmap by remember(bitmap) { mutableStateOf(bitmap) }
  var canvasSize by remember { mutableStateOf(IntSize.Zero) }

  // Normalized crop rectangle: left, top, right, bottom (from 0f to 1f)
  var cropLeft by remember { mutableStateOf(0.1f) }
  var cropTop by remember { mutableStateOf(0.1f) }
  var cropRight by remember { mutableStateOf(0.9f) }
  var cropBottom by remember { mutableStateOf(0.9f) }

  var selectedPreset by remember { mutableStateOf(AspectRatioPreset.FREE) }

  // Handle aspect ratio preset changes
  fun applyAspectRatio(preset: AspectRatioPreset) {
    selectedPreset = preset
    val r = preset.ratio
    if (r != null) {
      val width = cropRight - cropLeft
      val height = width / r
      if (cropTop + height <= 1f) {
        cropBottom = cropTop + height
      } else {
        val newHeight = 1f - cropTop
        val newWidth = newHeight * r
        if (cropLeft + newWidth <= 1f) {
          cropRight = cropLeft + newWidth
          cropBottom = 1f
        }
      }
    }
  }

  fun resetCrop() {
    cropLeft = 0.05f
    cropTop = 0.05f
    cropRight = 0.95f
    cropBottom = 0.95f
    selectedPreset = AspectRatioPreset.FREE
  }

  fun rotateBitmap() {
    currentBitmap?.let { bmp ->
      val matrix = Matrix().apply { postRotate(90f) }
      val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
      currentBitmap = rotated
      resetCrop()
    }
  }

  fun performCropAndAnalyze() {
    val bmp = currentBitmap ?: return
    val x = (cropLeft * bmp.width).roundToInt().coerceIn(0, bmp.width - 1)
    val y = (cropTop * bmp.height).roundToInt().coerceIn(0, bmp.height - 1)
    val w = ((cropRight - cropLeft) * bmp.width).roundToInt().coerceIn(1, bmp.width - x)
    val h = ((cropBottom - cropTop) * bmp.height).roundToInt().coerceIn(1, bmp.height - y)

    val cropped = Bitmap.createBitmap(bmp, x, y, w, h)
    onAnalyze(cropped)
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = if (isBangla) "প্রশ্নটি ক্রপ করুন" else "Crop Question Area",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { rotateBitmap() }) {
            Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate", tint = RoyalBlue)
          }
          IconButton(onClick = { resetCrop() }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = RoyalBlue)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(16.dp)
      ) {
        // Aspect Ratio Presets Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          AspectRatioPreset.values().forEach { preset ->
            val isSelected = selectedPreset == preset
            FilterChip(
              selected = isSelected,
              onClick = { applyAspectRatio(preset) },
              label = {
                Text(
                  text = if (isBangla) preset.labelBn else preset.labelEn,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = RoyalBlue,
                selectedLabelColor = Color.White
              )
            )
          }
        }

        // Analyze Button
        Button(
          onClick = { performCropAndAnalyze() },
          modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
          colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
          shape = RoundedCornerShape(16.dp),
          elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
        ) {
          Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = if (isBangla) "✨ এআই দিয়ে সমাধান করুন" else "✨ Analyze with AI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    },
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(Color(0xFF0F172A))
        .onGloballyPositioned { coordinates ->
          canvasSize = coordinates.size
        },
      contentAlignment = Alignment.Center
    ) {
      if (currentBitmap != null) {
        val imageBitmap = remember(currentBitmap) { currentBitmap!!.asImageBitmap() }

        Canvas(
          modifier = Modifier
            .fillMaxSize()
            .pointerInput(canvasSize, selectedPreset) {
              detectDragGestures { change, dragAmount ->
                change.consume()
                val width = canvasSize.width.toFloat()
                val height = canvasSize.height.toFloat()
                if (width <= 0f || height <= 0f) return@detectDragGestures

                val dx = dragAmount.x / width
                val dy = dragAmount.y / height

                val touchX = change.position.x / width
                val touchY = change.position.y / height

                // Check proximity to corners (threshold 0.15f)
                val isTopLeft = touchX < cropLeft + 0.15f && touchY < cropTop + 0.15f
                val isTopRight = touchX > cropRight - 0.15f && touchY < cropTop + 0.15f
                val isBottomLeft = touchX < cropLeft + 0.15f && touchY > cropBottom - 0.15f
                val isBottomRight = touchX > cropRight - 0.15f && touchY > cropBottom - 0.15f

                if (isTopLeft) {
                  cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - 0.1f)
                  cropTop = (cropTop + dy).coerceIn(0f, cropBottom - 0.1f)
                } else if (isTopRight) {
                  cropRight = (cropRight + dx).coerceIn(cropLeft + 0.1f, 1f)
                  cropTop = (cropTop + dy).coerceIn(0f, cropBottom - 0.1f)
                } else if (isBottomLeft) {
                  cropLeft = (cropLeft + dx).coerceIn(0f, cropRight - 0.1f)
                  cropBottom = (cropBottom + dy).coerceIn(cropTop + 0.1f, 1f)
                } else if (isBottomRight) {
                  cropRight = (cropRight + dx).coerceIn(cropLeft + 0.1f, 1f)
                  cropBottom = (cropBottom + dy).coerceIn(cropTop + 0.1f, 1f)
                } else {
                  // Pan entire crop box
                  val boxWidth = cropRight - cropLeft
                  val boxHeight = cropBottom - cropTop
                  if (cropLeft + dx >= 0f && cropRight + dx <= 1f) {
                    cropLeft += dx
                    cropRight += dx
                  }
                  if (cropTop + dy >= 0f && cropBottom + dy <= 1f) {
                    cropTop += dy
                    cropBottom += dy
                  }
                }
              }
            }
        ) {
          val canvasW = size.width
          val canvasH = size.height

          // Calculate scaled image bounds
          val imgW = imageBitmap.width.toFloat()
          val imgH = imageBitmap.height.toFloat()
          val scale = min(canvasW / imgW, canvasH / imgH)
          val drawW = imgW * scale
          val drawH = imgH * scale
          val offsetX = (canvasW - drawW) / 2f
          val offsetY = (canvasH - drawH) / 2f

          // Draw image
          drawImage(
            image = imageBitmap,
            dstOffset = androidx.compose.ui.unit.IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
            dstSize = IntSize(drawW.roundToInt(), drawH.roundToInt())
          )

          // Calculate crop box coordinates in canvas pixel space
          val boxLeft = offsetX + cropLeft * drawW
          val boxTop = offsetY + cropTop * drawH
          val boxRight = offsetX + cropRight * drawW
          val boxBottom = offsetY + cropBottom * drawH

          // Draw dark overlay outside crop box
          drawRect(color = Color.Black.copy(alpha = 0.65f), topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(canvasW, boxTop))
          drawRect(color = Color.Black.copy(alpha = 0.65f), topLeft = Offset(0f, boxBottom), size = androidx.compose.ui.geometry.Size(canvasW, canvasH - boxBottom))
          drawRect(color = Color.Black.copy(alpha = 0.65f), topLeft = Offset(0f, boxTop), size = androidx.compose.ui.geometry.Size(boxLeft, boxBottom - boxTop))
          drawRect(color = Color.Black.copy(alpha = 0.65f), topLeft = Offset(boxRight, boxTop), size = androidx.compose.ui.geometry.Size(canvasW - boxRight, boxBottom - boxTop))

          // Draw crop box border
          drawRect(
            color = Color.White,
            topLeft = Offset(boxLeft, boxTop),
            size = androidx.compose.ui.geometry.Size(boxRight - boxLeft, boxBottom - boxTop),
            style = Stroke(width = 2.5f.dp.toPx())
          )

          // Draw 3x3 Grid Lines inside crop box
          val thirdW = (boxRight - boxLeft) / 3f
          val thirdH = (boxBottom - boxTop) / 3f
          drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(boxLeft + thirdW, boxTop), end = Offset(boxLeft + thirdW, boxBottom), strokeWidth = 1f.dp.toPx())
          drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(boxLeft + 2 * thirdW, boxTop), end = Offset(boxLeft + 2 * thirdW, boxBottom), strokeWidth = 1f.dp.toPx())
          drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(boxLeft, boxTop + thirdH), end = Offset(boxRight, boxTop + thirdH), strokeWidth = 1f.dp.toPx())
          drawLine(color = Color.White.copy(alpha = 0.35f), start = Offset(boxLeft, boxTop + 2 * thirdH), end = Offset(boxRight, boxTop + 2 * thirdH), strokeWidth = 1f.dp.toPx())

          // Draw corner handles (thick blue lines)
          val handleLen = 24f.dp.toPx()
          val handleStroke = 5f.dp.toPx()
          val handleColor = Color(0xFF3B82F6)

          // Top Left
          drawLine(color = handleColor, start = Offset(boxLeft, boxTop), end = Offset(boxLeft + handleLen, boxTop), strokeWidth = handleStroke)
          drawLine(color = handleColor, start = Offset(boxLeft, boxTop), end = Offset(boxLeft, boxTop + handleLen), strokeWidth = handleStroke)

          // Top Right
          drawLine(color = handleColor, start = Offset(boxRight - handleLen, boxTop), end = Offset(boxRight, boxTop), strokeWidth = handleStroke)
          drawLine(color = handleColor, start = Offset(boxRight, boxTop), end = Offset(boxRight, boxTop + handleLen), strokeWidth = handleStroke)

          // Bottom Left
          drawLine(color = handleColor, start = Offset(boxLeft, boxBottom), end = Offset(boxLeft + handleLen, boxBottom), strokeWidth = handleStroke)
          drawLine(color = handleColor, start = Offset(boxLeft, boxBottom - handleLen), end = Offset(boxLeft, boxBottom), strokeWidth = handleStroke)

          // Bottom Right
          drawLine(color = handleColor, start = Offset(boxRight - handleLen, boxBottom), end = Offset(boxRight, boxBottom), strokeWidth = handleStroke)
          drawLine(color = handleColor, start = Offset(boxRight, boxBottom - handleLen), end = Offset(boxRight, boxBottom), strokeWidth = handleStroke)
        }
      } else {
        Text(text = "No image loaded", color = Color.White)
      }
    }
  }
}
