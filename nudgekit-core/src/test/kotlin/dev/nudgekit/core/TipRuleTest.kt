package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TipRuleTest {

    // --- MaxDisplayCount validation ---

    @Test
    fun `MaxDisplayCount with positive count is valid`() {
        val rule = TipRule.MaxDisplayCount(3)
        assertThat(rule.count).isEqualTo(3)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MaxDisplayCount with zero count throws`() {
        TipRule.MaxDisplayCount(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MaxDisplayCount with negative count throws`() {
        TipRule.MaxDisplayCount(-1)
    }

    // --- AfterEvent validation ---

    @Test
    fun `AfterEvent with valid params is valid`() {
        val rule = TipRule.AfterEvent("click", 5)
        assertThat(rule.eventName).isEqualTo("click")
        assertThat(rule.count).isEqualTo(5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterEvent with blank eventName throws`() {
        TipRule.AfterEvent("  ", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterEvent with empty eventName throws`() {
        TipRule.AfterEvent("", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterEvent with zero count throws`() {
        TipRule.AfterEvent("click", 0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterEvent with negative count throws`() {
        TipRule.AfterEvent("click", -1)
    }

    // --- AfterScreenVisits validation ---

    @Test
    fun `AfterScreenVisits with valid params is valid`() {
        val rule = TipRule.AfterScreenVisits("home", 2)
        assertThat(rule.screenName).isEqualTo("home")
        assertThat(rule.count).isEqualTo(2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterScreenVisits with blank screenName throws`() {
        TipRule.AfterScreenVisits("  ", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterScreenVisits with empty screenName throws`() {
        TipRule.AfterScreenVisits("", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `AfterScreenVisits with zero count throws`() {
        TipRule.AfterScreenVisits("home", 0)
    }

    // --- MinIntervalHours validation ---

    @Test
    fun `MinIntervalHours with positive hours is valid`() {
        val rule = TipRule.MinIntervalHours(24)
        assertThat(rule.hours).isEqualTo(24)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MinIntervalHours with zero hours throws`() {
        TipRule.MinIntervalHours(0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `MinIntervalHours with negative hours throws`() {
        TipRule.MinIntervalHours(-1)
    }

    // --- Singleton rules ---

    @Test
    fun `NotDismissed is a singleton`() {
        assertThat(TipRule.NotDismissed).isSameInstanceAs(TipRule.NotDismissed)
    }

    @Test
    fun `Once is a singleton`() {
        assertThat(TipRule.Once).isSameInstanceAs(TipRule.Once)
    }

    // --- Custom ---

    @Test
    fun `Custom rule holds predicate`() {
        val rule = TipRule.Custom { true }
        assertThat(rule.predicate).isNotNull()
    }
}
