package com.example.nearhelp.ui.auth.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedDark
import com.example.nearhelp.theme.EmergencyRedGlow
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast

/**
 * High-visibility, pulsing 1-Tap Anonymous Emergency Bypass Button
 * adhering to the Zero-Barrier SOS design principle in UI_GUIDANCE.md.
 */
@Composable
fun EmergencyButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "EmergencyPulse")
  val borderAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse,
    ),
    label = "BorderAlpha",
  )

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .background(
          brush = Brush.horizontalGradient(
            colors = listOf(
              EmergencyRedDark,
              EmergencyRed,
              EmergencyRedDark,
            )
          )
        )
        .border(
          width = 2.dp,
          color = EmergencyRed.copy(alpha = borderAlpha),
          shape = RoundedCornerShape(16.dp),
        )
        .clickable(enabled = !isLoading, onClick = onClick)
        .padding(vertical = 16.dp, horizontal = 20.dp),
      contentAlignment = Alignment.Center,
    ) {
      if (isLoading) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp,
          )
          Spacer(modifier = Modifier.width(12.dp))
          Text(
            text = "Initiating Emergency Mode...",
            color = TextHighContrast,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
          )
        }
      } else {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Instant SOS Bypass",
            tint = Color.White,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "1-TAP EMERGENCY SOS",
              style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
              ),
              color = Color.White,
            )
            Text(
              text = "Skip login · Instant anonymous triage",
              style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
              ),
              color = Color.White.copy(alpha = 0.9f),
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(6.dp))

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = "Disclaimer",
        tint = TextMediumContrast,
        modifier = Modifier.size(12.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "No account required for active emergencies",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = TextMediumContrast,
      )
    }
  }
}
