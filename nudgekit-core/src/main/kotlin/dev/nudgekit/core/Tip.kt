package dev.nudgekit.core

/**
 * A contextual tip that can be shown to the user when all [rules] are satisfied.
 *
 * Tips are immutable value objects. Define them as top-level `val` constants
 * or inside an `object` holder and reuse the same instance throughout the app.
 */
data class Tip(
    val id: String,
    val title: String,
    val message: String,
    val actionLabel: String? = null,
    val priority: Int = 0,
    val rules: List<TipRule> = listOf(TipRule.NotDismissed),
) {
    init {
        require(id.isNotBlank()) { "Tip id must not be blank" }
        require(title.isNotBlank()) { "Tip title must not be blank" }
        require(message.isNotBlank()) { "Tip message must not be blank" }
    }
}
