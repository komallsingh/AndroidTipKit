package dev.nudgekit.core

/**
 * Central interface for tip lifecycle operations.
 *
 * Implementations connect to a persistence layer (DataStore, in-memory, etc.)
 * to track events, screen visits, and per-tip state. The [TipEvaluator] uses
 * the data managed by a [TipManager] to decide tip eligibility.
 *
 * A concrete implementation backed by DataStore will be provided in the
 * `nudgekit-datastore` module.
 */
interface TipManager {

    /** Record an occurrence of a named event (e.g. "item_viewed"). */
    suspend fun trackEvent(name: String)

    /** Record a visit to a named screen (e.g. "home"). */
    suspend fun trackScreen(screenName: String)

    /** Mark a tip as dismissed by the user. */
    suspend fun dismiss(tipId: String)

    /** Increment the display count and update the last-shown timestamp. */
    suspend fun markShown(tipId: String)

    /** Reset state for a single tip (un-dismiss, zero counters). */
    suspend fun reset(tipId: String)

    /** Reset all tip state and counters. */
    suspend fun resetAll()
}
