package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TipAnalyticsTest {

    private val tip = Tip(
        id = "analytics_tip",
        title = "Title",
        message = "Message",
        actionLabel = "Act",
    )

    // ---------------------------------------------------------------
    // NoOpTipAnalytics
    // ---------------------------------------------------------------

    @Test
    fun `NoOpTipAnalytics is a TipAnalytics`() {
        assertThat(NoOpTipAnalytics).isInstanceOf(TipAnalytics::class.java)
    }

    @Test
    fun `NoOpTipAnalytics ignores all events without throwing`() {
        // Should be safe to call every callback; reaching the assertion = pass.
        NoOpTipAnalytics.onTipShown(tip)
        NoOpTipAnalytics.onTipDismissed(tip)
        NoOpTipAnalytics.onTipActionClicked(tip)

        assertThat(NoOpTipAnalytics).isNotNull()
    }

    // ---------------------------------------------------------------
    // Default no-op interface bodies
    // ---------------------------------------------------------------

    @Test
    fun `interface provides default no-op bodies so partial overrides compile`() {
        // Only overrides onTipShown; the other two rely on the default bodies.
        var shown = 0
        val partial = object : TipAnalytics {
            override fun onTipShown(tip: Tip) {
                shown++
            }
        }

        partial.onTipShown(tip)
        partial.onTipDismissed(tip)      // default no-op
        partial.onTipActionClicked(tip)  // default no-op

        assertThat(shown).isEqualTo(1)
    }

    // ---------------------------------------------------------------
    // Recording fake (also doubles as a copy-paste example for consumers)
    // ---------------------------------------------------------------

    @Test
    fun `recording fake captures each event type`() {
        val analytics = RecordingTipAnalytics()

        analytics.onTipShown(tip)
        analytics.onTipDismissed(tip)
        analytics.onTipActionClicked(tip)

        assertThat(analytics.events).containsExactly(
            "shown:analytics_tip",
            "dismissed:analytics_tip",
            "action:analytics_tip",
        ).inOrder()
    }

    @Test
    fun `recording fake preserves order across repeated events`() {
        val analytics = RecordingTipAnalytics()

        analytics.onTipShown(tip)
        analytics.onTipShown(tip)
        analytics.onTipDismissed(tip)

        assertThat(analytics.events).containsExactly(
            "shown:analytics_tip",
            "shown:analytics_tip",
            "dismissed:analytics_tip",
        ).inOrder()
    }

    /**
     * Simple in-memory [TipAnalytics] used by the tests above. A consumer can
     * copy this pattern to forward events to a real analytics SDK.
     */
    private class RecordingTipAnalytics : TipAnalytics {
        val events = mutableListOf<String>()

        override fun onTipShown(tip: Tip) {
            events += "shown:${tip.id}"
        }

        override fun onTipDismissed(tip: Tip) {
            events += "dismissed:${tip.id}"
        }

        override fun onTipActionClicked(tip: Tip) {
            events += "action:${tip.id}"
        }
    }
}
