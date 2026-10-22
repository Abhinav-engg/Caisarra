package com.abhinav.caisarra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.presentation.nearby.components.PlayerCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog

@RunWith(AndroidJUnit4::class)
class PlayerCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun playerCard_challengeButton_triggersCallback() {
        var challengeClicked = false

        val testPlayer = NearbyPlayer(
            userId = 42,
            username = "Magnus",
            rating = 2850,
            distanceKm = 3,
            active = true
        )

        composeTestRule.setContent {
            PlayerCard(
                player = testPlayer,
                onChallenge = { challengeClicked = true },
                onAddFriend = {},
                onChat = {}
            )
        }

        composeTestRule.onRoot().printToLog("PLAYER_CARD_TREE")
        composeTestRule.onNodeWithText("Challenge").performClick()
        assertTrue("onChallenge callback should be called", challengeClicked)
    }

    @Test
    fun playerCard_addFriendButton_triggersCallback() {
        var addFriendClicked = false

        val testPlayer = NearbyPlayer(
            userId = 42,
            username = "Magnus",
            rating = 2850,
            distanceKm = 3,
            active = true
        )

        composeTestRule.setContent {
            PlayerCard(
                player = testPlayer,
                onChallenge = {},
                onAddFriend = { addFriendClicked = true },
                onChat = {}
            )
        }

        composeTestRule.onRoot().printToLog("PLAYER_CARD_TREE")
        composeTestRule.onNodeWithContentDescription("Add friend").performClick()
        assertTrue("onAddFriend callback should be called", addFriendClicked)
    }

    @Test
    fun playerCard_singleClick_preventsRapidDoubleClicks() {
        var clickCount = 0

        val testPlayer = NearbyPlayer(
            userId = 99,
            username = "Hikaru",
            rating = 2820,
            distanceKm = 1,
            active = true
        )

        composeTestRule.setContent {
            PlayerCard(
                player = testPlayer,
                onChallenge = { clickCount++ },
                onAddFriend = {},
                onChat = {}
            )
        }

        val challengeNode = composeTestRule.onNodeWithText("Challenge")
        challengeNode.performClick()
        challengeNode.performClick()

        assertEquals("Rapid clicks should only execute once within guard window", 1, clickCount)
    }
}
