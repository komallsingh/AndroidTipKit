package dev.nudgekit.core

/**
 * Snapshot of everything the rule engine needs to evaluate a single tip.
 *
 * Passed as the receiver to [TipRule.Custom] predicates and used
 * internally by [TipEvaluator].
 */
data class TipContext(
    val tip: Tip,
    val state: TipState,
    val counters: TipCounters,
    val nowMillis: Long,
)
