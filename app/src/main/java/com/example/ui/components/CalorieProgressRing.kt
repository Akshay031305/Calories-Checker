package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExceededRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalorieProgressRing(
    consumedCalories: Double,
    targetCalories: Int,
    modifier: Modifier = Modifier,
    size: Dp = 190.dp,
    strokeWidth: Dp = 14.dp
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    val consumedInt = consumedCalories.toInt()
    val remaining = targetCalories - consumedInt
    val isExceeded = remaining < 0

    val progress = if (targetCalories > 0) {
        (consumedCalories / targetCalories).toFloat().coerceIn(0f, 1.25f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceAtMost(1f),
        animationSpec = tween(durationMillis = 800),
        label = "calorie_ring_progress"
    )

    val ringColor = if (isExceeded) ExceededRed else EmeraldAccent
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .testTag("calorie_progress_ring")
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )

            // Active Progress
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                style = stroke
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${numberFormat.format(consumedInt)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.testTag("calorie_consumed_text")
            )
            Text(
                text = "of ${numberFormat.format(targetCalories)} kcal",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (isExceeded) {
                Text(
                    text = "${numberFormat.format(-remaining)} kcal over",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = ExceededRed,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.testTag("calorie_over_tag")
                )
            } else {
                Text(
                    text = "${numberFormat.format(remaining)} kcal left",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = EmeraldAccent,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.testTag("calorie_remaining_tag")
                )
            }
        }
    }
}
