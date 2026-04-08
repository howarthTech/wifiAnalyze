package com.wifianalyze.ui.onboarding

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val bullets: List<String>
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Rounded.Wifi,
        title = "WiFi Analyze",
        subtitle = "Your personal home WiFi intelligence tool",
        bullets = emptyList()
    ),
    OnboardingPage(
        icon = Icons.Rounded.Home,
        title = "Room Coverage",
        subtitle = "Understand your WiFi in every corner of your home",
        bullets = listOf(
            "Real-time signal quality with plain-English ratings",
            "Walk room-to-room and log signal readings",
            "Router placement recommendations",
            "IoT device readiness check"
        )
    ),
    OnboardingPage(
        icon = Icons.Rounded.Analytics,
        title = "Advanced Analytics",
        subtitle = "Dig deeper into your network performance",
        bullets = listOf(
            "Live signal history charts with stability score",
            "Download & upload speed testing",
            "Ping latency to gateway and internet",
            "Channel congestion maps and recommendations",
            "Network score graded A–F"
        )
    ),
    OnboardingPage(
        icon = Icons.Rounded.Lock,
        title = "Privacy First",
        subtitle = "Everything stays on your device — nothing is ever uploaded or shared",
        bullets = listOf(
            "Location access — required by Android to scan nearby WiFi networks",
            "Notifications — optional, for weak-signal and recovery alerts"
        )
    )
)

@Composable
fun OnboardingScreen(
    onComplete: (permissionsGranted: Boolean) -> Unit
) {
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        onComplete(perms.values.all { it })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Pager fills remaining space
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pages.size) { index ->
                val isSelected = pagerState.currentPage == index
                val color by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    animationSpec = tween(200),
                    label = "dot"
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (isSelected) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
            }
        }

        // Action buttons — all at the bottom, thumb-friendly
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isLastPage) {
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.NEARBY_WIFI_DEVICES,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Grant Permissions & Get Started",
                        style = MaterialTheme.typography.titleMedium)
                }
                OutlinedButton(
                    onClick = { onComplete(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("I'll do this later")
                }
            } else {
                Button(
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (pagerState.currentPage == pages.lastIndex - 1) "Almost done →" else "Next →",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                OutlinedButton(
                    onClick = { onComplete(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text("Skip")
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (page.bullets.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                page.bullets.forEach { bullet ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = bullet,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
