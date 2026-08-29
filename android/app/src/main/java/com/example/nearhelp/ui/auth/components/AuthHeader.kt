package com.example.nearhelp.ui.auth.components

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedDark
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast

@Composable
fun AuthHeader(
  title: String,
  subtitle: String,
  modifier: Modifier = Modifier,
  showLogo: Boolean = true,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.25f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "PulseScale",
  )

  Column(
    modifier = modifier,
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    if (showLogo) {
      Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(96.dp),
      ) {
        // Outer Radar Glow Pulse
        Box(
          modifier = Modifier
            .size(90.dp)
            .scale(pulseScale)
            .background(EmergencyRedGlow, CircleShape)
        )

        // Main Shield Icon Circle
        Box(
          modifier = Modifier
            .size(72.dp)
            .background(
              brush = Brush.radialGradient(
                colors = listOf(EmergencyRed, EmergencyRedDark)
              ),
              shape = CircleShape,
            )
            .border(2.dp, AiCyan.copy(alpha = 0.6f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = "NearHelp Emergency Shield Logo",
            tint = Color.White,
            modifier = Modifier.size(40.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }

    Text(
      text = title,
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Black,
        letterSpacing = 0.5.sp,
      ),
      color = TextHighContrast,
      textAlign = TextAlign.Center,
    )

    Spacer(modifier = Modifier.height(6.dp))

    Text(
      text = subtitle,
      style = MaterialTheme.typography.bodyMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
      ),
      color = TextMediumContrast,
      textAlign = TextAlign.Center,
    )
  }
}
