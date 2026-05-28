package dev.nudgekit.compose

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.nudgekit.core.Tip
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Compose UI tests for the pure-UI [TipBox] wrapper.
 *
 * Verifies that:
 *  - the anchor content is always rendered,
 *  - the tip itself is shown only when `visible = true`,
 *  - the layout works for both `Top` and `Bottom` positions.
 */
@RunWith(AndroidJUnit4::class)
class TipBoxTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val tip = Tip(
        id = "box_tip",
        title = "Box title",
        message = "Box message",
    )

    @Test
    fun `renders wrapped content`() {
        composeRule.setContent {
            TipBox(tip = tip, visible = false) {
                Text("Anchor content")
            }
        }

        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
    }

    @Test
    fun `shows tip title and message when visible`() {
        composeRule.setContent {
            TipBox(tip = tip, visible = true) {
                Text("Anchor content")
            }
        }

        composeRule.onNodeWithText("Box title").assertIsDisplayed()
        composeRule.onNodeWithText("Box message").assertIsDisplayed()
        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
    }

    @Test
    fun `does not show tip when not visible`() {
        composeRule.setContent {
            TipBox(tip = tip, visible = false) {
                Text("Anchor content")
            }
        }

        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
        composeRule.onAllNodesWithText("Box title").assertCountEquals(0)
        composeRule.onAllNodesWithText("Box message").assertCountEquals(0)
    }

    @Test
    fun `shows tip in Top position`() {
        composeRule.setContent {
            TipBox(
                tip = tip,
                visible = true,
                position = TipPosition.Top,
            ) {
                Text("Anchor content")
            }
        }

        composeRule.onNodeWithText("Box title").assertIsDisplayed()
        composeRule.onNodeWithText("Anchor content").assertIsDisplayed()
    }
}

/*
 * TODO(v0.2): add tests for ManagedInlineTip and ManagedTipBox.
 *
 * Those require constructing a DataStoreTipManager backed by
 * PreferenceDataStoreFactory + TemporaryFolder (mirroring the
 * :nudgekit-datastore test harness) and coordinating DataStore writes
 * with the Compose mainClock via `composeRule.waitUntil { … }`.
 *
 * Deferred for the v0.1.x alpha to keep the initial Compose test setup
 * minimal and deterministic. Managed components are exercised end-to-end
 * by the sample app in the meantime.
 */
