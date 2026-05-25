package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TipCountersTest {

    @Test
    fun `empty counters return zero for any key`() {
        val counters = TipCounters()
        assertThat(counters.eventCount("unknown")).isEqualTo(0)
        assertThat(counters.screenVisitCount("unknown")).isEqualTo(0)
    }

    @Test
    fun `eventCount returns tracked value`() {
        val counters = TipCounters(eventCounts = mapOf("click" to 3, "scroll" to 7))
        assertThat(counters.eventCount("click")).isEqualTo(3)
        assertThat(counters.eventCount("scroll")).isEqualTo(7)
    }

    @Test
    fun `eventCount returns zero for missing key`() {
        val counters = TipCounters(eventCounts = mapOf("click" to 3))
        assertThat(counters.eventCount("hover")).isEqualTo(0)
    }

    @Test
    fun `screenVisitCount returns tracked value`() {
        val counters = TipCounters(screenVisitCounts = mapOf("home" to 5, "settings" to 2))
        assertThat(counters.screenVisitCount("home")).isEqualTo(5)
        assertThat(counters.screenVisitCount("settings")).isEqualTo(2)
    }

    @Test
    fun `screenVisitCount returns zero for missing key`() {
        val counters = TipCounters(screenVisitCounts = mapOf("home" to 5))
        assertThat(counters.screenVisitCount("profile")).isEqualTo(0)
    }

    @Test
    fun `default counters have empty maps`() {
        val counters = TipCounters()
        assertThat(counters.eventCounts).isEmpty()
        assertThat(counters.screenVisitCounts).isEmpty()
    }
}
