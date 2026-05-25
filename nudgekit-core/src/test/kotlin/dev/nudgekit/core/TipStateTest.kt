package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TipStateTest {

    @Test
    fun `default state has sensible defaults`() {
        val state = TipState(tipId = "tip1")
        assertThat(state.isDismissed).isFalse()
        assertThat(state.displayCount).isEqualTo(0)
        assertThat(state.lastShownAtMillis).isNull()
    }

    @Test
    fun `state with all fields set`() {
        val state = TipState(
            tipId = "tip1",
            isDismissed = true,
            displayCount = 5,
            lastShownAtMillis = 1_000_000L,
        )
        assertThat(state.isDismissed).isTrue()
        assertThat(state.displayCount).isEqualTo(5)
        assertThat(state.lastShownAtMillis).isEqualTo(1_000_000L)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank tipId throws`() {
        TipState(tipId = "  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty tipId throws`() {
        TipState(tipId = "")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `negative displayCount throws`() {
        TipState(tipId = "tip1", displayCount = -1)
    }

    @Test
    fun `zero displayCount is valid`() {
        val state = TipState(tipId = "tip1", displayCount = 0)
        assertThat(state.displayCount).isEqualTo(0)
    }

    @Test
    fun `copy preserves immutability`() {
        val original = TipState(tipId = "tip1")
        val updated = original.copy(isDismissed = true, displayCount = 1)
        assertThat(original.isDismissed).isFalse()
        assertThat(original.displayCount).isEqualTo(0)
        assertThat(updated.isDismissed).isTrue()
        assertThat(updated.displayCount).isEqualTo(1)
    }
}
