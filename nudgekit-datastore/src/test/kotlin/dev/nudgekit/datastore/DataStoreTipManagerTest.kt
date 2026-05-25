package dev.nudgekit.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import dev.nudgekit.core.Tip
import dev.nudgekit.core.TipDecision
import dev.nudgekit.core.TipHideReason
import dev.nudgekit.core.TipRule
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class DataStoreTipManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val fixedClock = 1_000_000_000L

    /**
     * Creates a fresh [DataStoreTipManager] backed by a temp file.
     * Uses [backgroundScope] so the DataStore lives for the duration of the test.
     */
    private fun TestScope.createManager(
        clock: () -> Long = { fixedClock },
    ): DataStoreTipManager {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = backgroundScope,
            produceFile = { File(tempFolder.root, "test.preferences_pb") },
        )
        return DataStoreTipManager(dataStore, clock = clock)
    }

    // ---------------------------------------------------------------
    // Default state
    // ---------------------------------------------------------------

    @Test
    fun `default TipState for unknown tip has clean defaults`() = runTest {
        val manager = createManager()
        val state = manager.getTipState("unknown_tip")

        assertThat(state.tipId).isEqualTo("unknown_tip")
        assertThat(state.isDismissed).isFalse()
        assertThat(state.displayCount).isEqualTo(0)
        assertThat(state.lastShownAtMillis).isNull()
    }

    @Test
    fun `default TipCounters has empty maps`() = runTest {
        val manager = createManager()
        val counters = manager.getCounters()

        assertThat(counters.eventCounts).isEmpty()
        assertThat(counters.screenVisitCounts).isEmpty()
    }

    // ---------------------------------------------------------------
    // trackEvent
    // ---------------------------------------------------------------

    @Test
    fun `trackEvent increments count by one`() = runTest {
        val manager = createManager()

        manager.trackEvent("click")

        val counters = manager.getCounters()
        assertThat(counters.eventCount("click")).isEqualTo(1)
    }

    @Test
    fun `trackEvent increments same event multiple times`() = runTest {
        val manager = createManager()

        manager.trackEvent("click")
        manager.trackEvent("click")
        manager.trackEvent("click")

        assertThat(manager.getCounters().eventCount("click")).isEqualTo(3)
    }

    @Test
    fun `trackEvent tracks different events independently`() = runTest {
        val manager = createManager()

        manager.trackEvent("click")
        manager.trackEvent("click")
        manager.trackEvent("scroll")

        val counters = manager.getCounters()
        assertThat(counters.eventCount("click")).isEqualTo(2)
        assertThat(counters.eventCount("scroll")).isEqualTo(1)
        assertThat(counters.eventCount("hover")).isEqualTo(0)
    }

    // ---------------------------------------------------------------
    // trackScreen
    // ---------------------------------------------------------------

    @Test
    fun `trackScreen increments count by one`() = runTest {
        val manager = createManager()

        manager.trackScreen("home")

        val counters = manager.getCounters()
        assertThat(counters.screenVisitCount("home")).isEqualTo(1)
    }

    @Test
    fun `trackScreen increments same screen multiple times`() = runTest {
        val manager = createManager()

        manager.trackScreen("home")
        manager.trackScreen("home")

        assertThat(manager.getCounters().screenVisitCount("home")).isEqualTo(2)
    }

    @Test
    fun `trackScreen tracks different screens independently`() = runTest {
        val manager = createManager()

        manager.trackScreen("home")
        manager.trackScreen("settings")
        manager.trackScreen("settings")

        val counters = manager.getCounters()
        assertThat(counters.screenVisitCount("home")).isEqualTo(1)
        assertThat(counters.screenVisitCount("settings")).isEqualTo(2)
    }

    // ---------------------------------------------------------------
    // dismiss
    // ---------------------------------------------------------------

    @Test
    fun `dismiss marks tip as dismissed`() = runTest {
        val manager = createManager()

        manager.dismiss("tip1")

        val state = manager.getTipState("tip1")
        assertThat(state.isDismissed).isTrue()
    }

    @Test
    fun `dismiss does not affect other tips`() = runTest {
        val manager = createManager()

        manager.dismiss("tip1")

        assertThat(manager.getTipState("tip1").isDismissed).isTrue()
        assertThat(manager.getTipState("tip2").isDismissed).isFalse()
    }

    // ---------------------------------------------------------------
    // markShown
    // ---------------------------------------------------------------

    @Test
    fun `markShown increments display count`() = runTest {
        val manager = createManager()

        manager.markShown("tip1")

        assertThat(manager.getTipState("tip1").displayCount).isEqualTo(1)
    }

    @Test
    fun `markShown increments display count multiple times`() = runTest {
        val manager = createManager()

        manager.markShown("tip1")
        manager.markShown("tip1")
        manager.markShown("tip1")

        assertThat(manager.getTipState("tip1").displayCount).isEqualTo(3)
    }

    @Test
    fun `markShown sets lastShownAtMillis from clock`() = runTest {
        val manager = createManager(clock = { 42_000L })

        manager.markShown("tip1")

        assertThat(manager.getTipState("tip1").lastShownAtMillis).isEqualTo(42_000L)
    }

    @Test
    fun `markShown updates lastShownAtMillis on each call`() = runTest {
        var time = 1_000L
        val manager = createManager(clock = { time })

        manager.markShown("tip1")
        assertThat(manager.getTipState("tip1").lastShownAtMillis).isEqualTo(1_000L)

        time = 5_000L
        manager.markShown("tip1")
        assertThat(manager.getTipState("tip1").lastShownAtMillis).isEqualTo(5_000L)
        assertThat(manager.getTipState("tip1").displayCount).isEqualTo(2)
    }

    // ---------------------------------------------------------------
    // reset (single tip)
    // ---------------------------------------------------------------

    @Test
    fun `reset clears only the specified tip state`() = runTest {
        val manager = createManager()

        manager.dismiss("tip1")
        manager.markShown("tip1")
        manager.dismiss("tip2")
        manager.markShown("tip2")

        manager.reset("tip1")

        // tip1 should be clean
        val state1 = manager.getTipState("tip1")
        assertThat(state1.isDismissed).isFalse()
        assertThat(state1.displayCount).isEqualTo(0)
        assertThat(state1.lastShownAtMillis).isNull()

        // tip2 should be untouched
        val state2 = manager.getTipState("tip2")
        assertThat(state2.isDismissed).isTrue()
        assertThat(state2.displayCount).isEqualTo(1)
    }

    @Test
    fun `reset does not clear event or screen counters`() = runTest {
        val manager = createManager()

        manager.trackEvent("click")
        manager.trackScreen("home")
        manager.dismiss("tip1")

        manager.reset("tip1")

        val counters = manager.getCounters()
        assertThat(counters.eventCount("click")).isEqualTo(1)
        assertThat(counters.screenVisitCount("home")).isEqualTo(1)
    }

    // ---------------------------------------------------------------
    // resetAll
    // ---------------------------------------------------------------

    @Test
    fun `resetAll clears all tips and counters`() = runTest {
        val manager = createManager()

        manager.dismiss("tip1")
        manager.markShown("tip1")
        manager.dismiss("tip2")
        manager.trackEvent("click")
        manager.trackScreen("home")

        manager.resetAll()

        assertThat(manager.getTipState("tip1").isDismissed).isFalse()
        assertThat(manager.getTipState("tip1").displayCount).isEqualTo(0)
        assertThat(manager.getTipState("tip2").isDismissed).isFalse()
        assertThat(manager.getCounters().eventCounts).isEmpty()
        assertThat(manager.getCounters().screenVisitCounts).isEmpty()
    }

    // ---------------------------------------------------------------
    // evaluate / shouldShow
    // ---------------------------------------------------------------

    @Test
    fun `evaluate returns Show for fresh tip with NotDismissed rule`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "welcome",
            title = "Hello",
            message = "Welcome!",
            rules = listOf(TipRule.NotDismissed),
        )

        val decision = manager.evaluate(tip)
        assertThat(decision).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `evaluate returns Hide after dismiss`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "welcome",
            title = "Hello",
            message = "Welcome!",
            rules = listOf(TipRule.NotDismissed),
        )

        manager.dismiss("welcome")

        val decision = manager.evaluate(tip)
        assertThat(decision).isEqualTo(TipDecision.Hide(TipHideReason.Dismissed))
    }

    @Test
    fun `evaluate uses persisted counters for AfterEvent rule`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "feature",
            title = "Feature",
            message = "Try this!",
            rules = listOf(TipRule.AfterEvent("item_viewed", 3)),
        )

        // Not enough events yet
        manager.trackEvent("item_viewed")
        manager.trackEvent("item_viewed")
        assertThat(manager.evaluate(tip)).isInstanceOf(TipDecision.Hide::class.java)

        // Now enough
        manager.trackEvent("item_viewed")
        assertThat(manager.evaluate(tip)).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `evaluate uses persisted counters for AfterScreenVisits rule`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "settings_tip",
            title = "Settings",
            message = "Check settings!",
            rules = listOf(TipRule.AfterScreenVisits("settings", 2)),
        )

        manager.trackScreen("settings")
        assertThat(manager.evaluate(tip)).isInstanceOf(TipDecision.Hide::class.java)

        manager.trackScreen("settings")
        assertThat(manager.evaluate(tip)).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `shouldShow returns boolean correctly`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "once_tip",
            title = "Once",
            message = "Show once!",
            rules = listOf(TipRule.Once),
        )

        assertThat(manager.shouldShow(tip)).isTrue()

        manager.markShown("once_tip")

        assertThat(manager.shouldShow(tip)).isFalse()
    }

    @Test
    fun `evaluate respects MaxDisplayCount with persisted state`() = runTest {
        val manager = createManager()
        val tip = Tip(
            id = "limited",
            title = "Limited",
            message = "Show 3 times max.",
            rules = listOf(TipRule.MaxDisplayCount(3)),
        )

        manager.markShown("limited")
        manager.markShown("limited")
        assertThat(manager.shouldShow(tip)).isTrue()

        manager.markShown("limited")
        assertThat(manager.shouldShow(tip)).isFalse()
    }

    @Test
    fun `evaluate respects MinIntervalHours with persisted timestamp`() = runTest {
        val oneHourMs = 3_600_000L
        var now = 10_000_000L
        val manager = createManager(clock = { now })
        val tip = Tip(
            id = "interval_tip",
            title = "Interval",
            message = "Wait between shows.",
            rules = listOf(TipRule.MinIntervalHours(2)),
        )

        // First show — never shown, so passes
        assertThat(manager.shouldShow(tip, nowMillis = now)).isTrue()
        manager.markShown("interval_tip")

        // 1 hour later — too soon
        now += oneHourMs
        assertThat(manager.shouldShow(tip, nowMillis = now)).isFalse()

        // 2+ hours later — enough time
        now += oneHourMs + 1
        assertThat(manager.shouldShow(tip, nowMillis = now)).isTrue()
    }

    // ---------------------------------------------------------------
    // Flow observation
    // ---------------------------------------------------------------

    @Test
    fun `observeTipState emits initial default then updates`() = runTest {
        val manager = createManager()

        manager.observeTipState("tip1").test {
            val initial = awaitItem()
            assertThat(initial.isDismissed).isFalse()
            assertThat(initial.displayCount).isEqualTo(0)

            manager.dismiss("tip1")

            val updated = awaitItem()
            assertThat(updated.isDismissed).isTrue()
            assertThat(updated.displayCount).isEqualTo(0)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `observeCounters emits initial empty then updates`() = runTest {
        val manager = createManager()

        manager.observeCounters().test {
            val initial = awaitItem()
            assertThat(initial.eventCounts).isEmpty()
            assertThat(initial.screenVisitCounts).isEmpty()

            manager.trackEvent("click")

            val updated = awaitItem()
            assertThat(updated.eventCount("click")).isEqualTo(1)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------------------------------------------------------------
    // Input validation
    // ---------------------------------------------------------------

    @Test(expected = IllegalArgumentException::class)
    fun `trackEvent with blank name throws`() = runTest {
        createManager().trackEvent("  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `trackEvent with empty name throws`() = runTest {
        createManager().trackEvent("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `trackScreen with blank name throws`() = runTest {
        createManager().trackScreen("  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dismiss with blank tipId throws`() = runTest {
        createManager().dismiss("  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `markShown with blank tipId throws`() = runTest {
        createManager().markShown("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `reset with blank tipId throws`() = runTest {
        createManager().reset("  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `getTipState with blank tipId throws`() = runTest {
        createManager().getTipState("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `observeTipState with blank tipId throws`() = runTest {
        createManager().observeTipState("  ")
    }

    // ---------------------------------------------------------------
    // Edge cases
    // ---------------------------------------------------------------

    @Test
    fun `event names with dots are tracked correctly`() = runTest {
        val manager = createManager()

        manager.trackEvent("user.profile.click")
        manager.trackEvent("user.profile.click")

        assertThat(manager.getCounters().eventCount("user.profile.click")).isEqualTo(2)
    }

    @Test
    fun `screen names with dots are tracked correctly`() = runTest {
        val manager = createManager()

        manager.trackScreen("app.settings.privacy")

        assertThat(manager.getCounters().screenVisitCount("app.settings.privacy")).isEqualTo(1)
    }

    @Test
    fun `mixed operations produce correct state`() = runTest {
        val manager = createManager(clock = { 99_000L })

        manager.trackEvent("view")
        manager.trackEvent("view")
        manager.trackScreen("home")
        manager.dismiss("tip_a")
        manager.markShown("tip_b")
        manager.markShown("tip_b")

        val stateA = manager.getTipState("tip_a")
        assertThat(stateA.isDismissed).isTrue()
        assertThat(stateA.displayCount).isEqualTo(0)

        val stateB = manager.getTipState("tip_b")
        assertThat(stateB.isDismissed).isFalse()
        assertThat(stateB.displayCount).isEqualTo(2)
        assertThat(stateB.lastShownAtMillis).isEqualTo(99_000L)

        val counters = manager.getCounters()
        assertThat(counters.eventCount("view")).isEqualTo(2)
        assertThat(counters.screenVisitCount("home")).isEqualTo(1)
    }
}
