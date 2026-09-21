package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CalorieBurnBlue
import com.example.ui.theme.CalorieBurnBlueContainer
import com.example.ui.theme.CalorieIntakeOrange
import com.example.ui.theme.CalorieIntakeOrangeContainer
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.MacroCarbs
import com.example.ui.theme.MacroFat
import com.example.ui.theme.MacroProtein
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CalorieSummaryCards(
    consumedCalories: Double,
    burnedCalories: Double,
    netCalories: Double,
    modifier: Modifier = Modifier
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Consumed Card
        CalorieStatCard(
            title = "Consumed",
            value = "${numberFormat.format(consumedCalories.toInt())}",
            unit = "kcal",
            icon = Icons.Default.Restaurant,
            color = CalorieIntakeOrange,
            containerColor = CalorieIntakeOrangeContainer.copy(alpha = 0.35f),
            modifier = Modifier
                .weight(1f)
                .testTag("stat_consumed")
        )

        // Burned Card
        CalorieStatCard(
            title = "Burned",
            value = "${numberFormat.format(burnedCalories.toInt())}",
            unit = "kcal",
            icon = Icons.Default.FitnessCenter,
            color = CalorieBurnBlue,
            containerColor = CalorieBurnBlueContainer.copy(alpha = 0.35f),
            modifier = Modifier
                .weight(1f)
                .testTag("stat_burned")
        )

        // Net Card
        CalorieStatCard(
            title = "Net",
            value = "${numberFormat.format(netCalories.toInt())}",
            unit = "kcal",
            icon = Icons.Default.ElectricBolt,
            color = EmeraldAccent,
            containerColor = EmeraldContainer.copy(alpha = 0.35f),
            modifier = Modifier
                .weight(1f)
                .testTag("stat_net")
        )
    }
}

@Composable
private fun CalorieStatCard(
    title: String,
    value: String,
    unit: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(containerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun MacroNutrientRow(
    proteinG: Double,
    carbsG: Double,
    fatG: Double,
    dailyCalorieTarget: Int,
    modifier: Modifier = Modifier
) {
    // Standard recommended macro distributions:
    // Protein: ~25% of calories (4 kcal/g)
    // Carbs: ~50% of calories (4 kcal/g)
    // Fat: ~25% of calories (9 kcal/g)
    val targetProtein = ((dailyCalorieTarget * 0.25) / 4.0).toInt().coerceAtLeast(50)
    val targetCarbs = ((dailyCalorieTarget * 0.50) / 4.0).toInt().coerceAtLeast(150)
    val targetFat = ((dailyCalorieTarget * 0.25) / 9.0).toInt().coerceAtLeast(40)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MacroCard(
            label = "Protein",
            current = proteinG,
            target = targetProtein,
            color = MacroProtein,
            modifier = Modifier.weight(1f).testTag("macro_protein")
        )
        MacroCard(
            label = "Carbs",
            current = carbsG,
            target = targetCarbs,
            color = MacroCarbs,
            modifier = Modifier.weight(1f).testTag("macro_carbs")
        )
        MacroCard(
            label = "Fat",
            current = fatG,
            target = targetFat,
            color = MacroFat,
            modifier = Modifier.weight(1f).testTag("macro_fat")
        )
    }
}

@Composable
private fun MacroCard(
    label: String,
    current: Double,
    target: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val progress = (current / target).toFloat().coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "macro_progress"
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${String.format(Locale.US, "%.1f", current)}g",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            )

            Text(
                text = "Target: ${target}g",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
