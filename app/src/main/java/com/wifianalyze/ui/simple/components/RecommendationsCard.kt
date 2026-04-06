package com.wifianalyze.ui.simple.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.SettingsInputAntenna
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.wifianalyze.domain.model.Recommendation

@Composable
fun RecommendationsCard(
    recommendations: List<Recommendation>,
    modifier: Modifier = Modifier
) {
    if (recommendations.isEmpty()) return

    var expanded by rememberSaveable { mutableStateOf(true) }
    val highCount = recommendations.count { it.priority == Recommendation.Priority.HIGH }
    val medCount = recommendations.count { it.priority == Recommendation.Priority.MEDIUM }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = if (highCount > 0)
                Recommendation.Priority.HIGH.color.copy(alpha = 0.08f)
            else
                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Build,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Suggestions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = buildSummary(highCount, medCount, recommendations.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expandable recommendations list
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    recommendations.forEachIndexed { index, rec ->
                        RecommendationRow(recommendation = rec)
                        if (index < recommendations.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 10.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecommendationRow(
    recommendation: Recommendation,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = categoryIcon(recommendation.category),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp),
            tint = recommendation.priority.color
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = recommendation.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                PriorityBadge(priority = recommendation.priority)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = recommendation.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PriorityBadge(priority: Recommendation.Priority) {
    val label = when (priority) {
        Recommendation.Priority.HIGH -> "Important"
        Recommendation.Priority.MEDIUM -> "Suggested"
        Recommendation.Priority.LOW -> "Info"
    }

    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = priority.color,
        fontWeight = FontWeight.Bold
    )
}

private fun categoryIcon(category: Recommendation.Category): ImageVector {
    return when (category) {
        Recommendation.Category.CHANNEL -> Icons.Rounded.SettingsInputAntenna
        Recommendation.Category.SECURITY -> Icons.Rounded.Lock
        Recommendation.Category.BAND -> Icons.Rounded.Wifi
        Recommendation.Category.PLACEMENT -> Icons.Rounded.Router
        Recommendation.Category.GENERAL -> Icons.Rounded.CheckCircle
    }
}

private fun buildSummary(high: Int, med: Int, total: Int): String {
    val parts = mutableListOf<String>()
    if (high > 0) parts.add("$high important")
    if (med > 0) parts.add("$med suggested")
    val low = total - high - med
    if (low > 0) parts.add("$low info")
    return parts.joinToString(" · ")
}
