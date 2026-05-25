package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TipEvaluatorTest {

    private val evaluator = TipEvaluator()

    private val baseTip = Tip(
        id = "test_tip",
        title = "Test",
        message = "Test message",
        rules = emptyList(),
    )

    private val freshState = TipState(tipId = "test_tip")
    private val emptyCounters = TipCounters()
    private val now = 1_000_000_000L

    // ---------------------------------------------------------------
    // No rules → always Show
    // ---------------------------------------------------------------

    @Test
    fun `tip with no rules always shows`() = runTest {
        val result = evaluator.evaluate(baseTip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    // ---------------------------------------------------------------
    // NotDismissed
    // ---------------------------------------------------------------

    @Test
    fun `NotDismissed passes when not dismissed`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.NotDismissed))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `NotDismissed fails when dismissed`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.NotDismissed))
        val state = freshState.copy(isDismissed = true)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.Dismissed))
    }

    // ---------------------------------------------------------------
    // Once
    // ---------------------------------------------------------------

    @Test
    fun `Once passes when never shown`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.Once))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `Once fails when shown once`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.Once))
        val state = freshState.copy(displayCount = 1)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.AlreadyShownOnce))
    }

    @Test
    fun `Once fails when shown multiple times`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.Once))
        val state = freshState.copy(displayCount = 5)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.AlreadyShownOnce))
    }

    // ---------------------------------------------------------------
    // MaxDisplayCount
    // ---------------------------------------------------------------

    @Test
    fun `MaxDisplayCount passes when under limit`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MaxDisplayCount(3)))
        val state = freshState.copy(displayCount = 2)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `MaxDisplayCount fails when at limit`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MaxDisplayCount(3)))
        val state = freshState.copy(displayCount = 3)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.MaxDisplayCountReached))
    }

    @Test
    fun `MaxDisplayCount fails when over limit`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MaxDisplayCount(3)))
        val state = freshState.copy(displayCount = 10)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.MaxDisplayCountReached))
    }

    @Test
    fun `MaxDisplayCount passes when zero displays`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MaxDisplayCount(1)))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    // ---------------------------------------------------------------
    // AfterEvent
    // ---------------------------------------------------------------

    @Test
    fun `AfterEvent passes when count reached`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterEvent("item_viewed", 3)))
        val counters = TipCounters(eventCounts = mapOf("item_viewed" to 3))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `AfterEvent passes when count exceeded`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterEvent("item_viewed", 3)))
        val counters = TipCounters(eventCounts = mapOf("item_viewed" to 10))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `AfterEvent fails when count not reached`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterEvent("item_viewed", 3)))
        val counters = TipCounters(eventCounts = mapOf("item_viewed" to 2))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.EventCountNotReached("item_viewed", 3, 2))
        )
    }

    @Test
    fun `AfterEvent fails when event never tracked`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterEvent("item_viewed", 1)))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.EventCountNotReached("item_viewed", 1, 0))
        )
    }

    // ---------------------------------------------------------------
    // AfterScreenVisits
    // ---------------------------------------------------------------

    @Test
    fun `AfterScreenVisits passes when count reached`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterScreenVisits("home", 2)))
        val counters = TipCounters(screenVisitCounts = mapOf("home" to 2))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `AfterScreenVisits passes when count exceeded`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterScreenVisits("home", 2)))
        val counters = TipCounters(screenVisitCounts = mapOf("home" to 5))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `AfterScreenVisits fails when count not reached`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterScreenVisits("home", 3)))
        val counters = TipCounters(screenVisitCounts = mapOf("home" to 1))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.ScreenVisitCountNotReached("home", 3, 1))
        )
    }

    @Test
    fun `AfterScreenVisits fails when screen never visited`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.AfterScreenVisits("settings", 1)))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.ScreenVisitCountNotReached("settings", 1, 0))
        )
    }

    // ---------------------------------------------------------------
    // MinIntervalHours
    // ---------------------------------------------------------------

    @Test
    fun `MinIntervalHours passes when never shown`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MinIntervalHours(24)))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `MinIntervalHours passes after enough time`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MinIntervalHours(1)))
        val oneHourAgo = now - 3_600_001L // 1 hour + 1 ms ago
        val state = freshState.copy(lastShownAtMillis = oneHourAgo)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `MinIntervalHours passes at exact boundary`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MinIntervalHours(1)))
        val exactlyOneHourAgo = now - 3_600_000L
        val state = freshState.copy(lastShownAtMillis = exactlyOneHourAgo)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `MinIntervalHours fails before enough time`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MinIntervalHours(2)))
        val oneHourAgo = now - 3_600_000L
        val state = freshState.copy(lastShownAtMillis = oneHourAgo)
        val result = evaluator.evaluate(tip, state, emptyCounters, now)

        val decision = result as TipDecision.Hide
        val reason = decision.reason as TipHideReason.MinIntervalNotReached
        assertThat(reason.requiredHours).isEqualTo(2)
        assertThat(reason.elapsedMillis).isEqualTo(3_600_000L)
    }

    @Test
    fun `MinIntervalHours fails when just shown`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.MinIntervalHours(24)))
        val state = freshState.copy(lastShownAtMillis = now - 1000L) // 1 second ago
        val result = evaluator.evaluate(tip, state, emptyCounters, now)

        val decision = result as TipDecision.Hide
        assertThat(decision.reason).isInstanceOf(TipHideReason.MinIntervalNotReached::class.java)
    }

    // ---------------------------------------------------------------
    // Custom
    // ---------------------------------------------------------------

    @Test
    fun `Custom rule passes when predicate returns true`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.Custom { true }))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `Custom rule fails when predicate returns false`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.Custom { false }))
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(TipDecision.Hide(TipHideReason.CustomRuleFailed))
    }

    @Test
    fun `Custom rule receives correct context`() = runTest {
        var capturedContext: TipContext? = null
        val tip = baseTip.copy(rules = listOf(TipRule.Custom {
            capturedContext = this
            true
        }))
        val counters = TipCounters(eventCounts = mapOf("x" to 1))
        evaluator.evaluate(tip, freshState, counters, now)

        assertThat(capturedContext).isNotNull()
        assertThat(capturedContext?.tip).isEqualTo(tip)
        assertThat(capturedContext?.state).isEqualTo(freshState)
        assertThat(capturedContext?.counters).isEqualTo(counters)
        assertThat(capturedContext?.nowMillis).isEqualTo(now)
    }

    // ---------------------------------------------------------------
    // Rule ordering and short-circuit
    // ---------------------------------------------------------------

    @Test
    fun `evaluator returns first failed rule`() = runTest {
        val tip = baseTip.copy(
            rules = listOf(
                TipRule.NotDismissed,
                TipRule.AfterEvent("click", 5),
                TipRule.Once,
            )
        )
        // NotDismissed passes (not dismissed), AfterEvent fails (0 < 5)
        val result = evaluator.evaluate(tip, freshState, emptyCounters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.EventCountNotReached("click", 5, 0))
        )
    }

    @Test
    fun `evaluator skips remaining rules after first failure`() = runTest {
        var customEvaluated = false
        val tip = baseTip.copy(
            rules = listOf(
                TipRule.NotDismissed,
                TipRule.Custom {
                    customEvaluated = true
                    true
                },
            )
        )
        val state = freshState.copy(isDismissed = true)
        evaluator.evaluate(tip, state, emptyCounters, now)
        assertThat(customEvaluated).isFalse()
    }

    @Test
    fun `all rules pass returns Show`() = runTest {
        val tip = baseTip.copy(
            rules = listOf(
                TipRule.NotDismissed,
                TipRule.MaxDisplayCount(5),
                TipRule.AfterEvent("click", 2),
                TipRule.AfterScreenVisits("home", 1),
            )
        )
        val state = freshState.copy(displayCount = 2)
        val counters = TipCounters(
            eventCounts = mapOf("click" to 3),
            screenVisitCounts = mapOf("home" to 1),
        )
        val result = evaluator.evaluate(tip, state, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    // ---------------------------------------------------------------
    // shouldShow convenience
    // ---------------------------------------------------------------

    @Test
    fun `shouldShow returns true when all rules pass`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.NotDismissed))
        val result = evaluator.shouldShow(tip, freshState, emptyCounters, now)
        assertThat(result).isTrue()
    }

    @Test
    fun `shouldShow returns false when a rule fails`() = runTest {
        val tip = baseTip.copy(rules = listOf(TipRule.NotDismissed))
        val state = freshState.copy(isDismissed = true)
        val result = evaluator.shouldShow(tip, state, emptyCounters, now)
        assertThat(result).isFalse()
    }

    // ---------------------------------------------------------------
    // Complex multi-rule scenarios
    // ---------------------------------------------------------------

    @Test
    fun `realistic tip with multiple rules all passing`() = runTest {
        val tip = baseTip.copy(
            rules = listOf(
                TipRule.NotDismissed,
                TipRule.MaxDisplayCount(3),
                TipRule.AfterEvent("item_viewed", 3),
                TipRule.MinIntervalHours(1),
                TipRule.Custom { counters.eventCount("item_viewed") > 2 },
            )
        )
        val state = freshState.copy(
            displayCount = 1,
            lastShownAtMillis = now - 7_200_000L, // 2 hours ago
        )
        val counters = TipCounters(eventCounts = mapOf("item_viewed" to 5))
        val result = evaluator.evaluate(tip, state, counters, now)
        assertThat(result).isEqualTo(TipDecision.Show)
    }

    @Test
    fun `realistic tip fails on third rule`() = runTest {
        val tip = baseTip.copy(
            rules = listOf(
                TipRule.NotDismissed,
                TipRule.MaxDisplayCount(10),
                TipRule.AfterScreenVisits("settings", 3),
            )
        )
        val counters = TipCounters(screenVisitCounts = mapOf("settings" to 2))
        val result = evaluator.evaluate(tip, freshState, counters, now)
        assertThat(result).isEqualTo(
            TipDecision.Hide(TipHideReason.ScreenVisitCountNotReached("settings", 3, 2))
        )
    }
}
