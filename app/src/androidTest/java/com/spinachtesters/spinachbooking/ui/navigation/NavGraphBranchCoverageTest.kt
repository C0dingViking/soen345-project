package com.spinachtesters.spinachbooking.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavGraphBranchCoverageTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        composeRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavGraph(navController = navController)
        }
    }

    @Test
    fun navigateToAddEvent_reachesAddEventDestination() {
        composeRule.runOnUiThread {
            navController.navigate(Screen.AddEvent.route)
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(
                Screen.AddEvent.route,
                navController.currentBackStackEntry?.destination?.route
            )
        }
    }

    @Test
    fun navigateToModifyEvent_withEventId_setsBackStackArgument() {
        val eventId = "event-123"

        composeRule.runOnUiThread {
            navController.navigate(Screen.ModifyEvent.createRoute(eventId))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(
                Screen.ModifyEvent.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals(
                eventId,
                navController.currentBackStackEntry?.arguments?.getString("eventId")
            )
        }
    }

    @Test
    fun navigateToModifyEvent_withBlankEventId_stillReachesModifyDestination() {
        composeRule.runOnUiThread {
            navController.navigate(Screen.ModifyEvent.createRoute(""))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(
                Screen.ModifyEvent.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals("", navController.currentBackStackEntry?.arguments?.getString("eventId"))
        }
    }

    @Test
    fun navigateToEventDetail_withEventId_setsBackStackArgument() {
        val eventId = "event-456"

        composeRule.runOnUiThread {
            navController.navigate(Screen.EventDetail.createRoute(eventId))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(
                Screen.EventDetail.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals(
                eventId,
                navController.currentBackStackEntry?.arguments?.getString("eventId")
            )
        }
    }

    @Test
    fun navigateToEventDetail_withBlankEventId_stillReachesEventDetailDestination() {
        composeRule.runOnUiThread {
            navController.navigate(Screen.EventDetail.createRoute(""))
        }
        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assertEquals(
                Screen.EventDetail.route,
                navController.currentBackStackEntry?.destination?.route
            )
            assertEquals("", navController.currentBackStackEntry?.arguments?.getString("eventId"))
        }
    }
}

