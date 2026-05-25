package dev.nudgekit.core

/**
 * Global counters for tracked events and screen visits.
 *
 * Used by [TipRule.AfterEvent] and [TipRule.AfterScreenVisits] during
 * rule evaluation. Missing keys are treated as zero.
 */
data class TipCounters(
    val eventCounts: Map<String, Int> = emptyMap(),
    val screenVisitCounts: Map<String, Int> = emptyMap(),
) {
    /** Returns the tracked count for [name], or 0 if not tracked. */
    fun eventCount(name: String): Int = eventCounts.getOrDefault(name, 0)

    /** Returns the tracked visit count for [name], or 0 if not tracked. */
    fun screenVisitCount(name: String): Int = screenVisitCounts.getOrDefault(name, 0)
}
