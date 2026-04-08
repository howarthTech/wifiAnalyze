package com.wifianalyze.ui.advanced.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wifianalyze.ui.advanced.NetworkScore

@Composable
fun NetworkScoreCard(
    score: NetworkScore,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = score.gradeColor.copy(alpha = 0.10f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Network Score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large grade letter
                Text(
                    text = score.grade,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = score.gradeColor,
                    lineHeight = 72.sp
                )

                Spacer(modifier = Modifier.width(20.dp))

                Column {
                    Text(
                        text = "${score.score} / ${score.maxScore}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = score.gradeColor
                    )
                    Text(
                        text = "points",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (score.maxScore < 100) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Run speed & latency tests\nfor a full 100-pt score",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            Spacer(modifier = Modifier.height(10.dp))

            // Breakdown
            ScoreRow("Signal",    score.signalPoints,    40, always = true)
            ScoreRow("Stability", score.stabilityPoints, 20)
            ScoreRow("Speed",     score.speedPoints,     20)
            ScoreRow("Latency",   score.latencyPoints,   20)
        }
    }
}

@Composable
private fun ScoreRow(label: String, points: Int?, maxPoints: Int, always: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (points != null || always) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (points != null) {
            val pct = points.toFloat() / maxPoints
            val color = when {
                pct >= 0.85f -> Color(0xFF4CAF50)
                pct >= 0.65f -> Color(0xFF8BC34A)
                pct >= 0.45f -> Color(0xFFFFC107)
                pct >= 0.25f -> Color(0xFFFF9800)
                else         -> Color(0xFFF44336)
            }
            Text(
                text = "$points / $maxPoints",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color
            )
        } else {
            Text(
                text = "— / $maxPoints",
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
