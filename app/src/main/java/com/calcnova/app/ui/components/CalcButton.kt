package com.calcnova.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight

enum class CalcButtonStyle { NUMBER, OPERATOR, FUNCTION, ACCENT, DANGER }

/**
 * A calculator key that scales down slightly when pressed (a small,
 * cheap animation that makes the whole keypad feel far more responsive)
 * and optionally fires a light haptic tick.
 */
@Composable
fun CalcButton(
    label: String,
    modifier: Modifier = Modifier,
    style: CalcButtonStyle = CalcButtonStyle.NUMBER,
    hapticEnabled: Boolean = true,
    aspectRatio: Float = 1.3f,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 700f),
        label = "buttonScale"
    )
    val haptic = LocalHapticFeedback.current

    val colors = when (style) {
        CalcButtonStyle.NUMBER -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        CalcButtonStyle.OPERATOR -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        CalcButtonStyle.FUNCTION -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
        CalcButtonStyle.ACCENT -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
        CalcButtonStyle.DANGER -> ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    }

    FilledTonalButton(
        onClick = {
            if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        interactionSource = interactionSource,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}
