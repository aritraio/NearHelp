package com.example.nearhelp.ui.home

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.CardSurfaceVariant
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedDark
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted
import com.example.nearhelp.ui.auth.AuthViewModel

@Composable
fun HomeScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToProfile: () -> Unit = {},
  onNavigateToMap: () -> Unit = {},
  onNavigateToTracking: () -> Unit = {},
  onNavigateToAssistant: () -> Unit = {},
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier,
) {
  val userName = viewModel.getStoredUserName() ?: "Emergency Responder"
  val userEmail = viewModel.getStoredUserEmail() ?: ""
  val isAnonymous = viewModel.isAnonymous()

  val infiniteTransition = rememberInfiniteTransition(label = "HomeSosPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "HomeSosScale",
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
      .verticalScroll(rememberScrollState())
      .padding(20.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .background(EmergencyRed, CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "Logo",
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text(
            text = "NearHelp AI",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextHighContrast,
          )
          Text(
            text = "Emergency Response Ready",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = SafeGreen,
          )
        }
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateToMap) {
          Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "Community Geo-Map",
            tint = AiCyan,
          )
        }
        if (!isAnonymous) {
          IconButton(onClick = onNavigateToProfile) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Profile & Medical ID",
              tint = AiCyan,
            )
          }
        }
        IconButton(
          onClick = {
            viewModel.logout()
            onNavigateToLogin()
          }
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
            contentDescription = "Sign Out",
            tint = TextMediumContrast,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(20.dp))

    // User Profile Status Card (Clickable to view Profile & Medical ID)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(enabled = !isAnonymous) { onNavigateToProfile() },
      colors = CardDefaults.cardColors(containerColor = CardSurface),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, if (!isAnonymous) AiCyan.copy(alpha = 0.4f) else SurfaceBorder),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (isAnonymous) Color(0xFF3B1212) else Color(0xFF1E3A5F)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = if (isAnonymous) Icons.Default.Warning else Icons.Default.Person,
            contentDescription = "User",
            tint = if (isAnonymous) EmergencyRed else AiCyan,
            modifier = Modifier.size(24.dp),
          )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = if (isAnonymous) "Anonymous Victim Session" else userName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = TextHighContrast,
            )
            if (!isAnonymous) {
              Spacer(modifier = Modifier.width(6.dp))
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Verified",
                tint = SafeGreen,
                modifier = Modifier.size(16.dp),
              )
            }
          }

          Text(
            text = if (isAnonymous) "Temporary 1-Tap SOS Session · No PII Stored" else userEmail,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            color = TextMediumContrast,
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(32.dp))

    // Central Pulsing SOS Button
    Text(
      text = "TAP FOR IMMEDIATE EMERGENCY DISPATCH",
      style = MaterialTheme.typography.labelMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
      ),
      color = TextMuted,
    )

    Spacer(modifier = Modifier.height(16.dp))

    Box(
      contentAlignment = Alignment.Center,
      modifier = Modifier.size(220.dp),
    ) {
      // Outer Glow Pulse
      Box(
        modifier = Modifier
          .size(200.dp)
          .scale(pulseScale)
          .background(EmergencyRedGlow, CircleShape)
      )

      // SOS Outer Ring
      Box(
        modifier = Modifier
          .size(170.dp)
          .background(EmergencyRed.copy(alpha = 0.3f), CircleShape)
          .border(2.dp, EmergencyRed, CircleShape)
      )

      // Center Big Red Button
      Box(
        modifier = Modifier
          .size(140.dp)
          .clip(CircleShape)
          .background(
            brush = Brush.radialGradient(
              colors = listOf(
                EmergencyRed,
                EmergencyRedDark,
              )
            )
          )
          .border(3.dp, Color.White, CircleShape)
          .clickable { /* SOS Trigger Handler */ },
        contentAlignment = Alignment.Center,
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "SOS",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Black,
              fontSize = 38.sp,
              letterSpacing = 2.sp,
            ),
            color = Color.White,
          )
          Text(
            text = "AI TRIAGE",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
            ),
            color = Color.White.copy(alpha = 0.9f),
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(28.dp))

    // Live Geolocation Status
    Row(
      modifier = Modifier
        .clip(RoundedCornerShape(20.dp))
        .background(CardSurfaceVariant)
        .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = Icons.Default.LocationOn,
        contentDescription = "GPS",
        tint = AiCyan,
        modifier = Modifier.size(16.dp),
      )
      Spacer(modifier = Modifier.width(8.dp))
      Text(
        text = "GPS High-Precision Telemetry Active",
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        color = TextHighContrast,
      )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Community Geo-Map Live Card
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateToMap() },
      colors = CardDefaults.cardColors(containerColor = CardSurface),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, AiCyan.copy(alpha = 0.5f)),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(Color(0xFF0E2536), CircleShape)
              .border(1.dp, AiCyan, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Map,
              contentDescription = "Map",
              tint = AiCyan,
              modifier = Modifier.size(22.dp),
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Community Geo-Map",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighContrast,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .background(SafeGreen, CircleShape)
              )
            }
            Text(
              text = "Live PostGIS Waves • Responders • Hospitals • AEDs",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = TextMediumContrast,
            )
          }
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "View Map",
          tint = AiCyan,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Active Live Tracking Stream Card (Module 8)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateToTracking() },
      colors = CardDefaults.cardColors(containerColor = Color(0xFF0C0D10)),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SafeGreen.copy(alpha = 0.6f)),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(SafeGreen.copy(alpha = 0.15f), CircleShape)
              .border(1.5.dp, SafeGreen, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Navigation,
              contentDescription = "Navigation",
              tint = SafeGreen,
              modifier = Modifier.size(22.dp),
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Live Rescue Navigation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighContrast,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(SafeGreen.copy(alpha = 0.2f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "LIVE WS",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                  ),
                  color = SafeGreen
                )
              }
            }
            Text(
              text = "Turn-by-Turn • ETA Stream • CPR Metronome",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = TextMediumContrast,
            )
          }
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Open Tracking",
          tint = SafeGreen,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // AI Crisis Assistant Card (Module 10)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable { onNavigateToAssistant() },
      colors = CardDefaults.cardColors(containerColor = Color(0xFF140D12)),
      shape = RoundedCornerShape(16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4081).copy(alpha = 0.6f)),
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
              .background(Color(0xFFFF4081).copy(alpha = 0.15f), CircleShape)
              .border(1.5.dp, Color(0xFFFF4081), CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = "AI Crisis Assistant",
              tint = Color(0xFFFF4081),
              modifier = Modifier.size(22.dp),
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "AI Crisis Assistant",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextHighContrast,
              )
              Spacer(modifier = Modifier.width(6.dp))
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color(0xFFFF4081).copy(alpha = 0.2f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "110 BPM CPR",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                  ),
                  color = Color(0xFFFF4081)
                )
              }
            }
            Text(
              text = "AHA Protocol • Q&A Assistant • Sec 134A Shield",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
              color = TextMediumContrast,
            )
          }
        }

        Icon(
          imageVector = Icons.Default.ChevronRight,
          contentDescription = "Open Assistant",
          tint = Color(0xFFFF4081),
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Switch account / Sign out button
    OutlinedButton(
      onClick = {
        viewModel.logout()
        onNavigateToLogin()
      },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(12.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
    ) {
      Text(
        text = if (isAnonymous) "Exit Anonymous Mode & Sign In" else "Sign Out",
        color = TextMediumContrast,
      )
    }
  }
}
