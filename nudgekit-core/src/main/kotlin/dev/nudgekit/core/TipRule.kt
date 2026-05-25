package dev.nudgekit.core

/**
 * A rule that controls whether a [Tip] is eligible to be shown.
 *
 * Rules are evaluated in declaration order by [TipEvaluator]. The first
 * rule that fails short-circuits evaluation and produces a [TipDecision.Hide].
 */
sealed interface TipRule {

    /** Passes when the tip has not been dismissed. */
    data object NotDismissed : TipRule

    /** Passes only if the tip has never been shown (displayCount == 0). */
    data object Once : TipRule

    /** Passes while displayCount is below [count]. */
    data class MaxDisplayCount(val count: Int) : TipRule {
        init {
            require(count > 0) { "MaxDisplayCount count must be positive, was $count" }
        }
    }

    /** Passes once the named event has been tracked at least [count] times. */
    data class AfterEvent(val eventName: String, val count: Int) : TipRule {
        init {
            require(eventName.isNotBlank()) { "AfterEvent eventName must not be blank" }
            require(count > 0) { "AfterEvent count must be positive, was $count" }
        }
    }

    /** Passes once the named screen has been visited at least [count] times. */
    data class AfterScreenVisits(val screenName: String, val count: Int) : TipRule {
        init {
            require(screenName.isNotBlank()) { "AfterScreenVisits screenName must not be blank" }
            require(count > 0) { "AfterScreenVisits count must be positive, was $count" }
        }
    }

    /** Passes when at least [hours] hours have elapsed since the tip was last shown. */
    data class MinIntervalHours(val hours: Int) : TipRule {
        init {
            require(hours > 0) { "MinIntervalHours hours must be positive, was $hours" }
        }
    }

    /**
     * Passes when [predicate] returns `true`.
     *
     * Use this for app-specific eligibility logic that doesn't fit the built-in rules.
     * The predicate receives a [TipContext] as its receiver so it can inspect
     * the tip, its state, counters, and the current timestamp.
     *
     * Not a data class because lambda equality is undefined in Kotlin.
     */
    class Custom(val predicate: suspend TipContext.() -> Boolean) : TipRule
}
