package dev.nudgekit.compose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import dev.nudgekit.core.Tip
import dev.nudgekit.core.TipAnalytics
import dev.nudgekit.datastore.DataStoreTipManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

/**
 * Compose UI tests for [ManagedInlineTip], backed by a real (temp-file)
 * DataStore. Determinism comes from [androidx.compose.ui.test.junit4.ComposeContentTestRule.waitUntil]
 * polling on observable state — no sleeps or fixed timeouts.
 */
@RunWith(AndroidJUnit4::class)
class ManagedInlineTipTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var manager: DataStoreTipManager

    private val tip = Tip(
        id = "welcome",
        title = "Welcome tip",
        message = "Hello there",
        actionLabel = "Got it",
    )

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.root, "managed.preferences_pb") },
        )
        manager = DataStoreTipManager(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    private class RecordingAnalytics : TipAnalytics {
        val shown = AtomicInteger(0)
        val dismissed = AtomicInteger(0)
        val action = AtomicInteger(0)
        override fun onTipShown(tip: Tip) { shown.incrementAndGet() }
        override fun onTipDismissed(tip: Tip) { dismissed.incrementAndGet() }
        override fun onTipActionClicked(tip: Tip) { action.incrementAndGet() }
    }

    @Test
    fun `renders when eligible and marks shown exactly once`() {
        val analytics = RecordingAnalytics()
        composeRule.setContent {
            ManagedInlineTip(tip = tip, manager = manager, analytics = analytics)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.shown.get() >= 1 }
        composeRule.onNodeWithText("Welcome tip").assertIsDisplayed()
        composeRule.onNodeWithText("Hello there").assertIsDisplayed()

        composeRule.waitForIdle()
        // onTipShown fires once per appearance, not on every recomposition.
        assertThat(analytics.shown.get()).isEqualTo(1)
        // markShown persisted exactly one display.
        assertThat(runBlocking { manager.getTipState("welcome").displayCount }).isEqualTo(1)
    }

    @Test
    fun `dismiss hides the tip, fires onTipDismissed, and persists`() {
        val analytics = RecordingAnalytics()
        composeRule.setContent {
            ManagedInlineTip(tip = tip, manager = manager, analytics = analytics)
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.shown.get() >= 1 }

        composeRule.onNodeWithContentDescription("Dismiss tip").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.dismissed.get() >= 1 }
        composeRule.onNodeWithText("Welcome tip").assertDoesNotExist()
        assertThat(analytics.dismissed.get()).isEqualTo(1)
        // Dismissal is persisted (best-effort write completes).
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { manager.getTipState("welcome").isDismissed }
        }
    }

    @Test
    fun `action button fires onTipActionClicked`() {
        val analytics = RecordingAnalytics()
        composeRule.setContent {
            ManagedInlineTip(
                tip = tip,
                manager = manager,
                analytics = analytics,
                onActionClick = { /* host action */ },
            )
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.shown.get() >= 1 }

        composeRule.onNodeWithText("Got it").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.action.get() >= 1 }
        assertThat(analytics.action.get()).isEqualTo(1)
    }

    @Test
    fun `already-dismissed tip never shows`() {
        runBlocking { manager.dismiss("welcome") }
        val analytics = RecordingAnalytics()

        composeRule.setContent {
            ManagedInlineTip(tip = tip, manager = manager, analytics = analytics)
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Welcome tip").assertDoesNotExist()
        assertThat(analytics.shown.get()).isEqualTo(0)
    }
}
