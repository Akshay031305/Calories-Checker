package com.example.ui.components

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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.HealthEntryEntity
import com.example.ui.theme.CalorieBurnBlue
import com.example.ui.theme.CalorieBurnBlueContainer
import com.example.ui.theme.CalorieIntakeOrange
import com.example.ui.theme.CalorieIntakeOrangeContainer
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HealthEntryCard(
    entry: HealthEntryEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBurn = entry.type == "burn"
    val accentColor = if (isBurn) CalorieBurnBlue else CalorieIntakeOrange
    val containerColor = if (isBurn) CalorieBurnBlueContainer.copy(alpha = 0.35f) else CalorieIntakeOrangeContainer.copy(alpha = 0.35f)

    val iconText = getEmojiForEntry(entry.name, isBurn)
    val timeFormatted = formatTimeFromTimestamp(entry.timestamp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("entry_card_${entry.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(containerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iconText,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isBurn) "• Activity" else "• Intake",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = accentColor,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = entry.name,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Calorie badge & Macros
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isBurn) "-${entry.calories.toInt()} kcal burned" else "${entry.calories.toInt()} kcal",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            fontSize = 12.sp
                        )
                    )

                    if (!isBurn && (entry.proteinG > 0 || entry.carbsG > 0 || entry.fatG > 0)) {
                        Text(
                            text = "P: ${String.format(Locale.US, "%.1f", entry.proteinG)}g  C: ${String.format(Locale.US, "%.1f", entry.carbsG)}g  F: ${String.format(Locale.US, "%.1f", entry.fatG)}g",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        )
                    }
                }
            }

            // Actions
            IconButton(
                onClick = onEditClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("edit_entry_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit entry",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("delete_entry_${entry.id}")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Delete entry",
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun getEmojiForEntry(name: String, isBurn: Boolean): String {
    val lower = name.lowercase()
    if (isBurn) {
        return when {
            lower.contains("run") || lower.contains("jog") -> "🏃"
            lower.contains("walk") -> "🚶"
            lower.contains("cycl") || lower.contains("bike") -> "🚴"
            lower.contains("swim") -> "🏊"
            lower.contains("gym") || lower.contains("weight") || lower.contains("push") || lower.contains("lift") -> "🏋️"
            lower.contains("yoga") || lower.contains("stretch") -> "🧘"
            lower.contains("badminton") || lower.contains("tennis") -> "🏸"
            lower.contains("cricket") -> "🏏"
            lower.contains("football") || lower.contains("soccer") -> "⚽"
            lower.contains("dance") -> "💃"
            else -> "⚡"
        }
    } else {
        return when {
            lower.contains("egg") || lower.contains("omelet") -> "🍳"
            lower.contains("milk") || lower.contains("shake") -> "🥛"
            lower.contains("idli") || lower.contains("dosa") || lower.contains("vada") || lower.contains("sambar") -> "🍚"
            lower.contains("rice") || lower.contains("biryani") || lower.contains("pulao") -> "🍛"
            lower.contains("chapati") || lower.contains("roti") || lower.contains("paratha") || lower.contains("bread") -> "🫓"
            lower.contains("dal") || lower.contains("curry") || lower.contains("soup") -> "🍲"
            lower.contains("chicken") || lower.contains("meat") || lower.contains("fish") || lower.contains("mutton") -> "🍗"
            lower.contains("banana") || lower.contains("apple") || lower.contains("fruit") || lower.contains("orange") -> "🍌"
            lower.contains("salad") || lower.contains("veg") || lower.contains("paneer") -> "🥗"
            lower.contains("tea") || lower.contains("chai") || lower.contains("coffee") -> "☕"
            lower.contains("water") -> "💧"
            lower.contains("sweet") || lower.contains("cake") || lower.contains("chocolate") -> "🍫"
            else -> "🍽️"
        }
    }
}

private fun formatTimeFromTimestamp(timestamp: String): String {
    return try {
        val dt = LocalDateTime.parse(timestamp)
        dt.format(DateTimeFormatter.ofPattern("hh:mm a", Locale.getDefault()))
    } catch (e: Exception) {
        if (timestamp.contains("T")) {
            val parts = timestamp.split("T")
            if (parts.size > 1 && parts[1].length >= 5) {
                parts[1].substring(0, 5)
            } else timestamp
        } else timestamp
    }
}
