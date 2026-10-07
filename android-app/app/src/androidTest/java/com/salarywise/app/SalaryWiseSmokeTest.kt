package com.salarywise.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class SalaryWiseSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchAndCreateProfileDoesNotCrash() {
        composeRule.onNodeWithText("Create My Financial Plan", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeRule.waitForIdle()

        // Wait for the onboarding transaction/navigation instead of using a fixed delay.
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("SalaryWise", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("SalaryWise", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
