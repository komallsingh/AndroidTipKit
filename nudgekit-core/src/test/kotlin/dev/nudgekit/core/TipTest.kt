package dev.nudgekit.core

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TipTest {

    @Test
    fun `valid tip is created successfully`() {
        val tip = Tip(
            id = "welcome",
            title = "Welcome",
            message = "Thanks for joining!",
        )
        assertThat(tip.id).isEqualTo("welcome")
        assertThat(tip.title).isEqualTo("Welcome")
        assertThat(tip.message).isEqualTo("Thanks for joining!")
        assertThat(tip.actionLabel).isNull()
        assertThat(tip.priority).isEqualTo(0)
        assertThat(tip.rules).containsExactly(TipRule.NotDismissed)
    }

    @Test
    fun `tip with all fields set`() {
        val rules = listOf(TipRule.NotDismissed, TipRule.Once)
        val tip = Tip(
            id = "feature",
            title = "Try this",
            message = "A new feature is available.",
            actionLabel = "Learn more",
            priority = 5,
            rules = rules,
        )
        assertThat(tip.actionLabel).isEqualTo("Learn more")
        assertThat(tip.priority).isEqualTo(5)
        assertThat(tip.rules).isEqualTo(rules)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank id throws`() {
        Tip(id = "  ", title = "Title", message = "Message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty id throws`() {
        Tip(id = "", title = "Title", message = "Message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank title throws`() {
        Tip(id = "id", title = "  ", message = "Message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty title throws`() {
        Tip(id = "id", title = "", message = "Message")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `blank message throws`() {
        Tip(id = "id", title = "Title", message = "  ")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `empty message throws`() {
        Tip(id = "id", title = "Title", message = "")
    }

    @Test
    fun `tip with empty rules list is allowed`() {
        val tip = Tip(id = "id", title = "T", message = "M", rules = emptyList())
        assertThat(tip.rules).isEmpty()
    }
}
