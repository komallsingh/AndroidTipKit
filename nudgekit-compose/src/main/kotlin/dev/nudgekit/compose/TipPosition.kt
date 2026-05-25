package dev.nudgekit.compose

/**
 * Controls where the tip is placed relative to the anchor content in [TipBox].
 */
enum class TipPosition {
    /** Tip appears above the anchor content. */
    Top,

    /** Tip appears below the anchor content. */
    Bottom,

    /** Tip appears to the start (left in LTR) of the anchor content. */
    Start,

    /** Tip appears to the end (right in LTR) of the anchor content. */
    End,
}
