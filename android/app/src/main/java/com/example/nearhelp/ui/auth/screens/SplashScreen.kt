package com.example.nearhelp.ui.auth.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedDark
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted
import com.example.nearhelp.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateToLogin: () -> Unit,
  onNavigateToHome: () -> Unit,
  viewModel: AuthViewModel,
  modifier: Modifier = Modifier,
) {
  val logoScale = remember { Animatable(0.6f) }
  val logoAlpha = remember { Animatable(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "SplashRadarPulse")
  val pulseRingScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.45f,
    animationSpec = infiniteRepeatable(
      animation = tween(1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "SplashPulseRing",
  )

  LaunchedEffect(Unit) {
    logoAlpha.animateTo(1f, animationSpec = tween(700))
    logoScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))

    delay(1400)
    if (viewModel.checkExistingSession()) {
      onNavigateToHome()
    } else {
      onNavigateToLogin()
    }
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            DarkBackground,
            Color(0xFF1A1A1A),
            DarkBackground,
          )
        )
      ),
    contentAlignment = Alignment.Center,
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(logoScale.value)
        .alpha(logoAlpha.value)
        .padding(24.dp),
    ) {
      // Animated Pulsing Shield Logo
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp),
      ) {
        // Outer pulsing ring
        Box(
          modifier = Modifier
            .size(130.dp)
            .scale(pulseRingScale)
            .background(EmergencyRedGlow, CircleShape)
        )

        // Mid glow ring
        Box(
          modifier = Modifier
            .size(110.dp)
            .background(EmergencyRed.copy(alpha = 0.25f), CircleShape)
            .border(1.5.dp, AiCyan.copy(alpha = 0.4f), CircleShape)
        )

        // Center Shield Circle
        Box(
          modifier = Modifier
            .size(90.dp)
            .background(
              brush = Brush.radialGradient(
                colors = listOf(EmergencyRed, EmergencyRedDark)
              ),
              shape = CircleShape,
            )
            .border(2.5.dp, Color.White.copy(alpha = 0.8f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "NearHelp Logo",
            tint = Color.White,
            modifier = Modifier.size(52.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(28.dp))

      // App Title
      Text(
        text = "NearHelp AI",
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Black,
          fontSize = 34.sp,
          letterSpacing = 1.sp,
        ),
        color = TextHighContrast,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Tagline
      Text(
        text = "Instant Response · AI Triage · Community Lifesaver",
        style = MaterialTheme.typography.bodyMedium.copy(
          fontWeight = FontWeight.Medium,
          fontSize = 13.sp,
          letterSpacing = 0.5.sp,
        ),
        color = TextMediumContrast,
        textAlign = TextAlign.Center,
      )

      Spacer(modifier = Modifier.height(32.dp))

      // Protection Badge
      Row(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(Color(0xFF222222))
          .border(1.dp, SafeGreen.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Default.LocalHospital,
          contentDescription = "Good Samaritan",
          tint = SafeGreen,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "Good Samaritan Law Protected",
          style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
          ),
          color = SafeGreen,
        )
      }
    }

    // Bottom Version Footer
    Text(
      text = "NearHelp AI v1.0 · Phase 1 MVP",
      style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
      color = TextMuted,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 32.dp),
    )
  }
}
