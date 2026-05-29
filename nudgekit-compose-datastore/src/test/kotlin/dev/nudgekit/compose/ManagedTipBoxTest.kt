package dev.nudgekit.compose

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
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
 * Compose UI tests for [ManagedTipBox]. Confirms the anchor content always
 * renders and the managed tip appears when eligible.
 */
@RunWith(AndroidJUnit4::class)
class ManagedTipBoxTest {

    @get:Rule
    val composeRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var manager: DataStoreTipManager

    private val tip = Tip(
        id = "box_tip",
        title = "Box tip title",
        message = "Box tip message",
    )

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.root, "managed_box.preferences_pb") },
        )
        manager = DataStoreTipManager(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    private class CountingAnalytics : TipAnalytics {
        val shown = AtomicInteger(0)
        override fun onTipShown(tip: Tip) { shown.incrementAndGet() }
    }

    @Test
    fun `anchor content always renders`() {
        // Even with the tip dismissed (not shown), the wrapped content must render.
        runBlocking { manager.dismiss("box_tip") }

        composeRule.setContent {
            ManagedTipBox(tip = tip, manager = manager) {
                Text("Anchor content")
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
        composeRule.onNodeWithText("Box tip title").assertDoesNotExist()
    }

    @Test
    fun `shows the tip above content when eligible`() {
        val analytics = CountingAnalytics()

        composeRule.setContent {
            ManagedTipBox(tip = tip, manager = manager, analytics = analytics) {
                Text("Anchor content")
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { analytics.shown.get() >= 1 }
        composeRule.onNodeWithText("Box tip title").assertIsDisplayed()
        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
    }
}
