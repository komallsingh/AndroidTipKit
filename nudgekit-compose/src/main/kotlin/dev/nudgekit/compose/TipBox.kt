package dev.nudgekit.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.nudgekit.core.Tip

/**
 * An anchored tip wrapper that displays the [content] and optionally shows
 * an [InlineTip] adjacent to it based on [position].
 *
 * This is a **pure UI component** — visibility is controlled by the [visible]
 * flag. For a state-aware version that integrates with [DataStoreTipManager],
 * use [ManagedTipBox].
 *
 * For [TipPosition.Top] and [TipPosition.Bottom], a vertical [Column] layout
 * is used and the tip animates in/out. For [TipPosition.Start] and
 * [TipPosition.End], a horizontal [Row] layout is used with the tip
 * constrained to a maximum width of 240 dp.
 *
 * @param tip           The tip to display.
 * @param visible       Whether the tip is currently shown.
 * @param modifier      Modifier applied to the outer layout.
 * @param position      Where the tip appears relative to [content].
 * @param colors        Color scheme; defaults to [NudgeTipDefaults.colors].
 * @param onDismiss     Called when the user taps the dismiss button.
 * @param onActionClick Called when the user taps the action button.
 * @param content       The anchor composable that the tip is attached to.
 */
@Composable
fun TipBox(
    tip: Tip,
    visible: Boolean,
    modifier: Modifier = Modifier,
    position: TipPosition = TipPosition.Bottom,
    colors: NudgeTipColors = NudgeTipDefaults.colors(),
    onDismiss: (() -> Unit)? = null,
    onActionClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    when (position) {
        TipPosition.Top -> {
            Column(modifier = modifier) {
                AnimatedVisibility(
                    visible = visible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    InlineTip(
                        tip = tip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = NudgeTipDefaults.AnchorSpacing),
                        colors = colors,
                        onDismiss = onDismiss,
                        onActionClick = onActionClick,
                    )
                }
                content()
            }
        }

        TipPosition.Bottom -> {
            Column(modifier = modifier) {
                content()
                AnimatedVisibility(
                    visible = visible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    InlineTip(
                        tip = tip,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = NudgeTipDefaults.AnchorSpacing),
                        colors = colors,
                        onDismiss = onDismiss,
                        onActionClick = onActionClick,
                    )
                }
            }
        }

        TipPosition.Start -> {
            Row(modifier = modifier, verticalAlignment = Alignment.Top) {
                AnimatedVisibility(visible = visible) {
                    InlineTip(
                        tip = tip,
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .padding(end = NudgeTipDefaults.AnchorSpacing),
                        colors = colors,
                        onDismiss = onDismiss,
                        onActionClick = onActionClick,
                    )
                }
                Box(modifier = Modifier.weight(1f)) { content() }
            }
        }

        TipPosition.End -> {
            Row(modifier = modifier, verticalAlignment = Alignment.Top) {
                Box(modifier = Modifier.weight(1f)) { content() }
                AnimatedVisibility(visible = visible) {
                    InlineTip(
                        tip = tip,
                        modifier = Modifier
                            .widthIn(max = 240.dp)
                            .padding(start = NudgeTipDefaults.AnchorSpacing),
                        colors = colors,
                        onDismiss = onDismiss,
                        onActionClick = onActionClick,
                    )
                }
            }
        }
    }
}
