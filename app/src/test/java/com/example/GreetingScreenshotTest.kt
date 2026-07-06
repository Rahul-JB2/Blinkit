package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.screens.EmptyProductsView
import com.example.ui.screens.HomeBottomNav
import com.example.ui.theme.MyApplicationTheme
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
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun bottom_nav_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        HomeBottomNav(
          selectedTab   = 0,
          cartCount     = 3,
          onTabSelected = {}
        )
      }
    }
    composeTestRule.onRoot().captureRoboImage(
      filePath = "src/test/screenshots/bottom_nav.png"
    )
  }

  @Test
  fun empty_state_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        EmptyProductsView(query = "organic milk")
      }
    }
    composeTestRule.onRoot().captureRoboImage(
      filePath = "src/test/screenshots/empty_state.png"
    )
  }
}
