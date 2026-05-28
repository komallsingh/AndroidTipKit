package dev.nudgekit.core

/**
 * SDK-agnostic hook for observing tip lifecycle events.
 *
 * NudgeKit bundles **no** analytics dependency. Implement this interface to
 * forward tip events to whatever pipeline you already use — Firebase,
 * Mixpanel, Amplitude, a plain logger, an in-memory list, etc. NudgeKit
 * never talks to the network and never depends on a vendor SDK.
 *
 * Every method has a default no-op body, so an implementation only needs to
 * override the events it cares about.
 *
 * Managed Compose components (`ManagedInlineTip`, `ManagedTipBox` in
 * `nudgekit-compose-datastore`) invoke these callbacks on the main thread
 * during composition / event handling. Keep them fast and non-blocking —
 * offload heavy work (disk, network) to your own dispatcher.
 */
interface TipAnalytics {

    /**
     * Called once each time [tip] becomes visible.
     *
     * Aligned with the managed component's `markShown` call, so it fires
     * once per actual appearance — not on every recomposition.
     */
    fun onTipShown(tip: Tip) {}

    /** Called when the user dismisses [tip]. */
    fun onTipDismissed(tip: Tip) {}

    /** Called when the user taps [tip]'s action button. */
    fun onTipActionClicked(tip: Tip) {}
}

/**
 * A [TipAnalytics] that ignores every event.
 *
 * Used as the default in managed components so analytics is strictly
 * opt-in — wiring an implementation is never required.
 */
object NoOpTipAnalytics : TipAnalytics
