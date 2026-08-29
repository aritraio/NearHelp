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
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
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
