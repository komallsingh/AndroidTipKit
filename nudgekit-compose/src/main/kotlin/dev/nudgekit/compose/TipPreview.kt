package dev.nudgekit.compose

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.nudgekit.core.Tip

// ─── Sample tips used across previews ──────────────────────────────

private val previewTipBasic = Tip(
    id = "preview_basic",
    title = "Welcome to NudgeKit",
    message = "Contextual tips help users discover features at the right moment.",
)

private val previewTipWithAction = Tip(
    id = "preview_action",
    title = "Use Filters",
    message = "Narrow down results by applying filters to find exactly what you need.",
    actionLabel = "Try Filters",
)

// ─── InlineTip previews ────────────────────────────────────────────

@Preview(showBackground = true, name = "InlineTip – basic")
@Composable
private fun InlineTipBasicPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            InlineTip(
                tip = previewTipBasic,
                modifier = Modifier.fillMaxWidth(),
                onDismiss = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "InlineTip – with action")
@Composable
private fun InlineTipWithActionPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            InlineTip(
                tip = previewTipWithAction,
                modifier = Modifier.fillMaxWidth(),
                onDismiss = {},
                onActionClick = {},
            )
        }
    }
}

@Preview(
    showBackground = true,
    name = "InlineTip – dark mode",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun InlineTipDarkPreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            InlineTip(
                tip = previewTipWithAction,
                modifier = Modifier.fillMaxWidth(),
                onDismiss = {},
                onActionClick = {},
            )
        }
    }
}

@Preview(showBackground = true, name = "InlineTip – no dismiss button")
@Composable
private fun InlineTipNoDismissPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            InlineTip(
                tip = previewTipBasic,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// ─── TipBox previews ───────────────────────────────────────────────

@Preview(showBackground = true, name = "TipBox – bottom")
@Composable
private fun TipBoxBottomPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            var visible by remember { mutableStateOf(true) }
            TipBox(
                tip = previewTipWithAction,
                visible = visible,
                position = TipPosition.Bottom,
                onDismiss = { visible = false },
                onActionClick = {},
            ) {
                Button(onClick = {}) {
                    Text("Notification Settings")
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "TipBox – top")
@Composable
private fun TipBoxTopPreview() {
    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface(modifier = Modifier.padding(16.dp)) {
            TipBox(
                tip = previewTipBasic,
                visible = true,
                position = TipPosition.Top,
                onDismiss = {},
            ) {
                Button(onClick = {}) {
                    Text("Settings")
                }
            }
        }
    }
}
