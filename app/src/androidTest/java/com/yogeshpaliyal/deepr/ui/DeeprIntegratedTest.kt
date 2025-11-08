package com.yogeshpaliyal.deepr.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yogeshpaliyal.deepr.MainActivity
import com.yogeshpaliyal.deepr.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integrated UI tests for Deepr app covering:
 * - Add new link
 * - Edit link
 * - Delete link
 * - Search functionality
 * - Filter by tag
 * - Add/remove favorites
 */
@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class DeeprIntegratedTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun getString(resId: Int): String = composeTestRule.activity.getString(resId)

    @Test
    fun testAddNewLink() {
        // Wait for the app to load
        composeTestRule.waitForIdle()

        // Click the FAB to add a new link
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.add_link))
            .performClick()

        // Wait for the bottom sheet to appear
        composeTestRule.waitForIdle()

        // Enter a deeplink
        composeTestRule
            .onNodeWithText(getString(R.string.enter_deeplink_command))
            .performTextInput("myapp://test")

        // Enter a name
        composeTestRule
            .onNodeWithText(getString(R.string.enter_link_name))
            .performTextInput("Test Link")

        // Click save button
        composeTestRule
            .onNodeWithText(getString(R.string.save))
            .performScrollTo()
            .performClick()

        // Wait for the link to be saved
        composeTestRule.waitForIdle()

        // Verify the link appears in the list
        composeTestRule
            .onNodeWithText("Test Link")
            .assertIsDisplayed()
    }

    @Test
    fun testEditLink() {
        // First add a link
        testAddNewLink()

        // Wait for the list to update
        composeTestRule.waitForIdle()

        // Find the link and swipe right to edit
        composeTestRule
            .onNodeWithText("Test Link")
            .performTouchInput {
                swipeRight()
            }

        // Wait for the edit dialog to appear
        composeTestRule.waitForIdle()

        // Verify we're in edit mode by checking for the Edit title
        composeTestRule
            .onNodeWithText(getString(R.string.edit))
            .assertIsDisplayed()

        // Clear and update the name
        val nameField = composeTestRule.onNodeWithText(getString(R.string.enter_link_name))
        nameField.performTextClearance()
        nameField.performTextInput("Updated Test Link")

        // Save changes
        composeTestRule
            .onNodeWithText(getString(R.string.save))
            .performScrollTo()
            .performClick()

        // Wait for changes to be saved
        composeTestRule.waitForIdle()

        // Verify the updated link appears
        composeTestRule
            .onNodeWithText("Updated Test Link")
            .assertIsDisplayed()
    }

    @Test
    fun testDeleteLink() {
        // First add a link
        testAddNewLink()

        // Wait for the list to update
        composeTestRule.waitForIdle()

        // Swipe left to delete
        composeTestRule
            .onNodeWithText("Test Link")
            .performTouchInput {
                swipeLeft()
            }

        // Wait for the delete action
        composeTestRule.waitForIdle()

        // Confirm deletion by clicking the Delete button in the dialog
        composeTestRule
            .onNodeWithText(getString(R.string.delete))
            .performClick()

        // Wait for deletion to complete
        composeTestRule.waitForIdle()

        // Verify the link is no longer displayed
        composeTestRule
            .onNodeWithText("Test Link")
            .assertDoesNotExist()
    }

    @Test
    fun testSearchFunctionality() {
        // Add a couple of links
        addLinkWithDetails("myapp://test1", "First Test Link")
        addLinkWithDetails("myapp://test2", "Second Test Link")

        composeTestRule.waitForIdle()

        // Click on the search bar to expand it
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.search))
            .performClick()

        composeTestRule.waitForIdle()

        // Enter search query
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.search))
            .performTextInput("First")

        composeTestRule.waitForIdle()

        // Verify only the matching link is displayed
        composeTestRule
            .onNodeWithText("First Test Link")
            .assertIsDisplayed()
    }

    @Test
    fun testAddRemoveFavorite() {
        // Add a link
        testAddNewLink()

        composeTestRule.waitForIdle()

        // Find and click the favorite button
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.add_to_favourites))
            .performClick()

        composeTestRule.waitForIdle()

        // Verify the favorite icon changed
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.remove_from_favourites))
            .assertIsDisplayed()

        // Click again to remove from favorites
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.remove_from_favourites))
            .performClick()

        composeTestRule.waitForIdle()

        // Verify it's back to unfavorited state
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.add_to_favourites))
            .assertIsDisplayed()
    }

    @Test
    fun testFilterByTag() {
        // Add a link with a tag
        addLinkWithTag("myapp://tagged", "Tagged Link", "TestTag")

        composeTestRule.waitForIdle()

        // Verify the link is displayed
        composeTestRule
            .onNodeWithText("Tagged Link")
            .assertIsDisplayed()

        // Click on the tag to filter
        composeTestRule
            .onNodeWithText("TestTag")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify only the tagged link is displayed
        composeTestRule
            .onNodeWithText("Tagged Link")
            .assertIsDisplayed()
    }

    // Helper method to add a link with specific details
    private fun addLinkWithDetails(
        link: String,
        name: String,
    ) {
        // Click the FAB
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.add_link))
            .performClick()

        composeTestRule.waitForIdle()

        // Enter deeplink
        composeTestRule
            .onNodeWithText(getString(R.string.enter_deeplink_command))
            .performTextInput(link)

        // Enter name
        composeTestRule
            .onNodeWithText(getString(R.string.enter_link_name))
            .performTextInput(name)

        // Save
        composeTestRule
            .onNodeWithText(getString(R.string.save))
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
    }

    // Helper method to add a link with a tag
    private fun addLinkWithTag(
        link: String,
        name: String,
        tag: String,
    ) {
        // Click the FAB
        composeTestRule
            .onNodeWithContentDescription(getString(R.string.add_link))
            .performClick()

        composeTestRule.waitForIdle()

        // Enter deeplink
        composeTestRule
            .onNodeWithText(getString(R.string.enter_deeplink_command))
            .performTextInput(link)

        // Enter name
        composeTestRule
            .onNodeWithText(getString(R.string.enter_link_name))
            .performTextInput(name)

        // Add tag
        composeTestRule
            .onNodeWithText(getString(R.string.new_tag))
            .performTextInput(tag)

        composeTestRule
            .onNodeWithText(getString(R.string.create_tag))
            .performClick()

        composeTestRule.waitForIdle()

        // Save
        composeTestRule
            .onNodeWithText(getString(R.string.save))
            .performScrollTo()
            .performClick()

        composeTestRule.waitForIdle()
    }
}
