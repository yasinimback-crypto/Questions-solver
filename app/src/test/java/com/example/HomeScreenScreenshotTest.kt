package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.EduSolveTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class HomeScreenScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screen_screenshot() {
    composeTestRule.setContent {
      EduSolveTheme {
        HomeScreen(
          isBangla = false,
          selectedSubject = "All Subjects",
          historyList = emptyList(),
          onToggleLanguage = {},
          onSubjectSelected = {},
          onCameraClick = {},
          onGalleryClick = {},
          onHistoryItemClick = {},
          onDeleteHistoryItem = {},
          onClearAllHistory = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }
}
