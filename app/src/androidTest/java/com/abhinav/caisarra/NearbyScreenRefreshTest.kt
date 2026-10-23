package com.abhinav.caisarra

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.abhinav.caisarra.data.location.LocationProvider
import com.abhinav.caisarra.data.remote.api.NearbyService
import com.abhinav.caisarra.data.remote.dto.LocationRequest
import com.abhinav.caisarra.data.remote.dto.LocationResponse
import com.abhinav.caisarra.data.remote.dto.NearbyPlayer
import com.abhinav.caisarra.data.remote.dto.NearbyPlayersResponse
import com.abhinav.caisarra.data.remote.dto.VisibilityRequest
import com.abhinav.caisarra.data.remote.dto.VisibilityResponse
import com.abhinav.caisarra.data.repository.NearbyRepository
import com.abhinav.caisarra.presentation.nearby.NearbyViewModel
import com.abhinav.caisarra.presentation.screens.NearbyScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class NearbyScreenRefreshTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private class FakeNearbyService : NearbyService {
        var getPlayersCallCount = 0

        override suspend fun saveLocation(body: LocationRequest): Response<LocationResponse> =
            Response.success(LocationResponse(message = "ok", nearbyEnabled = true))

        override suspend fun setVisibility(body: VisibilityRequest): Response<VisibilityResponse> =
            Response.success(VisibilityResponse(nearbyEnabled = body.enabled))

        override suspend fun getPlayers(): Response<NearbyPlayersResponse> {
            getPlayersCallCount++
            return Response.success(
                NearbyPlayersResponse(
                    radiusKm = 50,
                    players = listOf(
                        NearbyPlayer(
                            userId = 1,
                            username = "TestPlayer",
                            rating = 1500,
                            distanceKm = 5,
                            active = true
                        )
                    )
                )
            )
        }
    }

    @Test
    fun refreshButton_invokesOnRefresh() {
        var refreshClicked = false

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = FakeNearbyService()
        val repository = NearbyRepository(service)
        val viewModel = NearbyViewModel(repository, LocationProvider(context))

        composeTestRule.setContent {
            NearbyScreen(
                viewModel = viewModel,
                onRefresh = { refreshClicked = true }
            )
        }

        composeTestRule.onNodeWithContentDescription("Refresh").performClick()
        assertTrue("Refresh button click should invoke onRefresh callback", refreshClicked)
    }

    @Test
    fun viewModelRefresh_fetchesUpdatedPlayers() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val service = FakeNearbyService()
        val repository = NearbyRepository(service)
        val viewModel = NearbyViewModel(repository, LocationProvider(context))

        // Initial fetch from init block
        composeTestRule.waitForIdle()
        val initialCalls = service.getPlayersCallCount

        viewModel.refresh()
        composeTestRule.waitForIdle()

        assertEquals("refresh() should trigger getPlayers call", initialCalls + 1, service.getPlayersCallCount)
        assertEquals(1, viewModel.state.value.players.size)
        assertEquals("TestPlayer", viewModel.state.value.players[0].username)
    }
}
